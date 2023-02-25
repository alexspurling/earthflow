package earth;

import javax.imageio.ImageIO;
import javax.json.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;


public class EarthImageLoader {

    private static final String IMAGES_FOLDER = "images";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_FORMATTER2 = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private static final DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .append(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            .optionalStart()
            .appendOffset("+HH:mm", "Z")
            .optionalEnd()
            .toFormatter();
    private static final String DSCOVR_API = "https://epic.gsfc.nasa.gov/api/natural/date/";
    private static final String DSCOVR_ARCHIVE = "https://epic.gsfc.nasa.gov/archive/natural/";

    public EarthImageLoader() {

    }

    public BufferedImage loadImage(ImageMetadata im) {
        String pngFilename = im.image() + ".png";
        OffsetDateTime imageDate = im.date();
        File dateTimeDir = new File(IMAGES_FOLDER, DATE_FORMATTER.format(imageDate));
        File pngFile = new File(dateTimeDir, pngFilename);
        if (!pngFile.exists()) {
            try {
                downloadImage(pngFile, DSCOVR_ARCHIVE + DATE_FORMATTER2.format(imageDate) + "/png/" + pngFilename);
            } catch (Exception e) {
                throw new RuntimeException("Error downloading image: " + pngFilename, e);
            }
        }
        try {
            System.out.println("Loading png " + pngFile);
            return ImageIO.read(pngFile);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error loading image: " + pngFilename);
        }
    }

    private void downloadImage(File pngFile, String url) throws Exception {
        URL urlObj = new URL(url);
        System.out.println("Downloading " + url);

        HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
        InputStream inputStream = con.getInputStream();
        try (FileOutputStream outputStream = new FileOutputStream(pngFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            System.out.println("File downloaded successfully!");
        }
    }

    public Metadata getMetadata(LocalDate date) {
        String dateAsString = DATE_FORMATTER.format(date);
        File dateTimeDir = new File(IMAGES_FOLDER, dateAsString);
        File metadataFile = new File(dateTimeDir, "metadata.json");

        try {
            String metadataJson;
            if (!dateTimeDir.exists()) {
                String metadataUrl = DSCOVR_API + dateAsString;
                System.out.println("Downloading metadata from: " + metadataUrl);
                dateTimeDir.mkdir();
                metadataJson = downloadString(metadataUrl);
                writeString(metadataFile, metadataJson);
                return parseMetadata(date, metadataJson);
            } else {
                metadataJson = readString(metadataFile);
                Metadata metadata = parseMetadata(date, metadataJson);
                if (metadata.isEmpty()) {
                    metadataJson = downloadString(DSCOVR_API + dateAsString);
                    writeString(metadataFile, metadataJson);
                    return parseMetadata(date, metadataJson);
                }
                return metadata;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading metadata for :" + date);
            return null;
        }
    }

    private Metadata parseMetadata(LocalDate metadataDate, String metadataJson) {
        List<ImageMetadata> imageMetadataList = new ArrayList<>();
        try (JsonReader reader = Json.createReader(new StringReader(metadataJson))) {
            JsonValue jsonValue = reader.read();
            if (jsonValue.getValueType() == JsonValue.ValueType.ARRAY) {
                JsonArray jsonArray = (JsonArray) jsonValue;
                for (JsonValue value : jsonArray) {
                    if (value.getValueType() == JsonValue.ValueType.OBJECT) {
                        JsonObject jsonObject = (JsonObject) value;
                        String image = jsonObject.getString("image");
                        String date = jsonObject.getString("date");
                        OffsetDateTime dateTime = OffsetDateTime.parse(date + "Z", DATE_TIME_FORMATTER);
                        imageMetadataList.add(new ImageMetadata(image, dateTime));
                    }
                }
            }
        }
        System.out.println("Parsed metadata with " + imageMetadataList.size() + " images");
        return new Metadata(metadataDate, imageMetadataList);
    }

    private String readString(File fileToRead) throws IOException {
        StringBuilder fileContents = new StringBuilder();
        try (FileReader reader = new FileReader(fileToRead)) {
            int character;
            while ((character = reader.read()) != -1) {
                fileContents.append((char) character);
            }
            return fileContents.toString();
        }
    }

    private void writeString(File fileToWrite, String string) throws IOException {
        System.out.println("Writing " + fileToWrite);
        try (FileWriter writer = new FileWriter(fileToWrite)) {
            writer.write(string);
        }
    }

    private static String downloadString(String url) throws Exception {
        URL urlObj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");

        System.out.println("Downloading metadata " + url);
        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();
        } else {
            throw new Exception("Failed to download json file: " + responseCode);
        }
    }
}
