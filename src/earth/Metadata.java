package earth;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class Metadata {

    private List<ImageMetadata> images;

    public Metadata(List<ImageMetadata> imageMetadataList) {
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
}
