package earth;

import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.*;

public class EarthTextureCache {

    private static final int THREADS = 3;

    private static final int MAX_TEXTURES = 10;

    private final Sphere sphere;

    private final EarthImageLoader loader;

    private final BufferedImage chequerGrid;

    private final ExecutorService executor = Executors.newFixedThreadPool(THREADS);

    private final ConcurrentMap<LocalDate, Metadata> metadataMap = new ConcurrentHashMap<>();

    private final TreeMap<OffsetDateTime, ImageMetadata> imageMetadataMap = new TreeMap<>();

    private final ConcurrentMap<LocalDate, CompletableFuture<Void>> metadataQueue = new ConcurrentHashMap<>();

    private final ConcurrentMap<OffsetDateTime, EarthTexture> earthTextureCache = new ConcurrentHashMap<>();

    private final ConcurrentMap<OffsetDateTime, CompletableFuture<Void>> earthTextureQueue = new ConcurrentHashMap<>();

    private final List<EarthTexture> earthTextureList = new ArrayList<>();

    private OffsetDateTime lastDateTime;

    private String status = "";

    public EarthTextureCache(Sphere sphere, EarthImageLoader loader) {
        this.sphere = sphere;
        this.loader = loader;
        this.chequerGrid = new ChequerGrid().getImage();
    }

    public void update(OffsetDateTime dateTime) {
        loadMetadata(dateTime);
        loadMetadata(dateTime.minusDays(1));
        loadMetadata(dateTime.plusDays(1));

        if (lastDateTime != null) {
            if (dateTime.isAfter(lastDateTime)) {
                // Time is moving forward
                loadTexturesBefore(dateTime, 1);
                loadTexturesAfter(dateTime, 4);
                deleteTexturesBefore(dateTime, 5);
            } else if (dateTime.isBefore(lastDateTime)) {
                // Time is moving backwards
                loadTexturesBefore(dateTime, 4);
                loadTexturesAfter(dateTime, 1);
                deleteTexturesAfter(dateTime, 5);
            }
        }

        lastDateTime = dateTime;
    }

    public void loadMetadata(OffsetDateTime dateTime) {
        LocalDate localDate = dateTime.toLocalDate();
        if (!metadataMap.containsKey(localDate)) {
            metadataQueue.computeIfAbsent(localDate, (date) -> CompletableFuture.supplyAsync(() -> {
                System.out.println("Loading metadata " + date);
                return loader.getMetadata(date);
                }, executor)
                    .thenAccept((metadata) -> {
                        synchronized (this) {
                            metadataMap.putIfAbsent(localDate, metadata);
                            for (ImageMetadata im : metadata.getMetadata()) {
                                imageMetadataMap.put(im.date(), im);
                            }
                            metadataQueue.remove(localDate);
                        }
                    }));
        }
    }

    private synchronized void loadTexturesBefore(OffsetDateTime dateTime, int numToLoad) {
        // Iterate over the map entries in reverse starting from datetime
        Collection<ImageMetadata> values = imageMetadataMap.descendingMap().tailMap(dateTime).values();
        Iterator<ImageMetadata> iterator = values.iterator();
        int i = 0;
        while (earthTextureCache.size() + earthTextureQueue.size() < MAX_TEXTURES && iterator.hasNext() && i < numToLoad) {
            loadTexture(iterator.next());
            i++;
        }
    }

    private synchronized void loadTexturesAfter(OffsetDateTime dateTime, int numToLoad) {
        // Iterate over the map entries starting at datetime
        Collection<ImageMetadata> values = imageMetadataMap.tailMap(dateTime).values();
        Iterator<ImageMetadata> iterator = values.iterator();
        int i = 0;
        while (earthTextureCache.size() + earthTextureQueue.size() < MAX_TEXTURES && iterator.hasNext() && i < numToLoad) {
            loadTexture(iterator.next());
            i++;
        }
    }

    private void loadTexture(ImageMetadata im) {
        if (!earthTextureCache.containsKey(im.date())) {
            earthTextureQueue.computeIfAbsent(im.date(), (date) -> CompletableFuture.supplyAsync(() -> {
                status = "Loading image " + im.date();
                System.out.println(status);
                return loader.loadImage(im);
            }, executor)
                    .thenAccept((image) -> {
                        EarthImage earthImage = new EarthImage(im, image);
                        EarthTexture earthTexture = new EarthTexture(sphere, earthImage);
                        synchronized (this) {
                            earthTextureCache.putIfAbsent(im.date(), earthTexture);
                            earthTextureList.add(earthTexture);
                            earthTextureList.sort(Comparator.comparing(o -> o.getEarthImage().metadata().date()));
                            System.out.println("Added texture for " + im.date() + ". Cache now has " + (earthTextureCache.size()) + " textures");
                            earthTextureQueue.remove(im.date());
                            status = "Loaded image " + earthTextureCache.size() + "/" + MAX_TEXTURES;
                            System.out.println(status);
                        }
                    }).exceptionally(ex -> {
                        earthTextureQueue.remove(im.date());
                        ex.printStackTrace();
                        System.err.println("Error loading image and texture");
                        return null;
                    }));
        }
    }

    private synchronized Optional<ImageMetadata> getImageMetadataBeforeDateTime(OffsetDateTime dateTime) {
        SortedMap<OffsetDateTime, ImageMetadata> headMap = imageMetadataMap.headMap(dateTime);
        if (headMap.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(headMap.get(headMap.lastKey()));
    }

    private synchronized Optional<ImageMetadata> getImageMetadataAfterDateTime(OffsetDateTime dateTime) {
        SortedMap<OffsetDateTime, ImageMetadata> tailMap = imageMetadataMap.tailMap(dateTime);
        if (tailMap.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(tailMap.get(tailMap.firstKey()));
    }

    public Texture getTextureBefore(OffsetDateTime dateTime) {
        Optional<ImageMetadata> image = getImageMetadataBeforeDateTime(dateTime);
        if (image.isEmpty()) {
            return new ChequerTexture(chequerGrid, dateTime);
        }
        EarthTexture texture = earthTextureCache.get(image.get().date());
        if (texture == null) {
            return new ChequerTexture(chequerGrid, image.get().date());
        }
        return texture;
    }

    public Texture getTextureAfter(OffsetDateTime dateTime) {
        Optional<ImageMetadata> image = getImageMetadataAfterDateTime(dateTime);
        if (image.isEmpty()) {
            return new ChequerTexture(chequerGrid, dateTime);
        }
        EarthTexture texture = earthTextureCache.get(image.get().date());
        if (texture == null) {
            return new ChequerTexture(chequerGrid, image.get().date());
        }
        return texture;
    }


    private synchronized void deleteTexturesBefore(OffsetDateTime dateTime, int skip) {
        Collection<ImageMetadata> values = imageMetadataMap.descendingMap().tailMap(dateTime).values();
        Iterator<ImageMetadata> iterator = values.iterator();
        // Don't delete the most recent image
        for (int i = 0; i < skip && iterator.hasNext(); i++) {
            iterator.next();
        }
        while (iterator.hasNext()) {
            OffsetDateTime dateToRemove = iterator.next().date();
            EarthTexture remove = earthTextureCache.remove(dateToRemove);
            if (remove != null) {
                System.out.println("Removed image: " + dateToRemove);
            }
        }
    }


    private synchronized void deleteTexturesAfter(OffsetDateTime dateTime, int skip) {
        Collection<ImageMetadata> values = imageMetadataMap.tailMap(dateTime).values();
        Iterator<ImageMetadata> iterator = values.iterator();
        // Don't delete the most recent image
        for (int i = 0; i < skip && iterator.hasNext(); i++) {
            iterator.next();
        }
        while (iterator.hasNext()) {
            EarthTexture remove = earthTextureCache.remove(iterator.next().date());
            if (remove != null) {
                System.out.println("Removed image");
            }
        }
    }

    public String getStatus() {
        return status;
    }
}
