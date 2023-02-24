package earth;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class Metadata {

    private LocalDate date;

    private List<ImageMetadata> images;

    public Metadata(LocalDate date, List<ImageMetadata> imageMetadataList) {
        this.date = date;
        images = imageMetadataList.stream().sorted(Comparator.comparing(ImageMetadata::date)).toList();
    }
    public Optional<ImageMetadata> getImageAfter(OffsetDateTime dateTime) {
        for (ImageMetadata image : images) {
            if (image.date().isAfter(dateTime)) {
                return Optional.of(image);
            }
        }
        return Optional.empty();
    }

    public Optional<ImageMetadata> getImageBefore(OffsetDateTime dateTime) {
        Optional<ImageMetadata> lastImage = Optional.empty();
        for (ImageMetadata image : images) {
            if (image.date().isAfter(dateTime)) {
                return lastImage;
            }
            lastImage = Optional.of(image);
        }
        return lastImage;
    }

    public boolean isEmpty() {
        return images.isEmpty();
    }

    public LocalDate getDate() {
        return date;
    }

    public List<ImageMetadata> getMetadata() {
        return images;
    }
}
