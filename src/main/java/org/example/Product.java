package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Product {
    private String name;
    private String price;
    private String id;
    private String shop;
    private String imageUrl;
    private String createdTimestamp;

    public Product(String name, String price, String id, String shop, String imageUrl) {
        this.name = name;
        this.price = price;
        this.id = id;
        this.shop = shop;
        this.imageUrl = imageUrl;
        this.createdTimestamp = LocalDateTime.now().toString();
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public String getId() {
        return id;
    }

    public String getShop() {
        return shop;
    }

    @Override
    public String toString() {
        return "Product{" +
                "name='" + name + '\'' +
                ", price='" + price + '\'' +
                ", id='" + id + '\'' +
                ", shop='" + shop + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", createdTimestamp='" + createdTimestamp + '\'' +
                '}';
    }




    public static void writeProductsToJson(List<Product> products, String filePath, String zipFilePath) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8)) {
            gson.toJson(products, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        updateZipFile(filePath, zipFilePath);


        File file = new File(filePath);
        File parentDirectory = new File(file.getParent());
        File[] jsonFiles = parentDirectory.listFiles((dir, name) -> name.endsWith(".json"));

        if (jsonFiles != null) {
            for (File jsonFile : jsonFiles) {
                if (!jsonFile.equals(file)) {
                    if (jsonFile.delete()) {
                        System.out.println("Deleted file: " + jsonFile.getName());
                    } else {
                        System.out.println("Failed to delete file: " + jsonFile.getName());
                    }
                }
            }
        }
    }

    private static void updateZipFile(String filePath, String zipFilePath) {
        try {
            File file = new File(filePath);
            File zipFile = new File(zipFilePath);

            if (!zipFile.exists()) {
                try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
                    addToZip(file, zos);
                }
            } else {
                File tempFile = File.createTempFile("temp", null);
                tempFile.deleteOnExit();

                try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
                     ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempFile))) {

                    addToZip(file, zos);

                    ZipEntry entry;
                    while ((entry = zis.getNextEntry()) != null) {
                        if (!entry.getName().equals(file.getName())) {
                            zos.putNextEntry(entry);

                            byte[] bytes = new byte[1024];
                            int length;
                            while ((length = zis.read(bytes)) >= 0) {
                                zos.write(bytes, 0, length);
                            }

                            zos.closeEntry();
                        }
                    }
                }
                if (zipFile.exists() && zipFile.delete()) {
                    tempFile.renameTo(zipFile);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addToZip(File file, ZipOutputStream zos) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        ZipEntry zipEntry = new ZipEntry(file.getName());
        zos.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zos.write(bytes, 0, length);
        }

        zos.closeEntry();
        fis.close();
    }


    public static List<String> readUrlsFromFile(String filePath) throws IOException {
        List<String> urls = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                urls.add(line.trim());
            }
        }

        return urls;
    }


    public static List<Product> findZipFiles() {
        String directoryPath = "prices/";
        File directory = new File(directoryPath);
        List<File> zipFiles = new ArrayList<>();

        if (directory.exists() && directory.isDirectory()) {
            File[] subdirectories = directory.listFiles();
            if (subdirectories != null) {
                for (File subdirectory : subdirectories) {
                    if (subdirectory.isDirectory()) {
                        File[] files = subdirectory.listFiles((dir, name) -> name.endsWith(".zip"));
                        if (files != null) {
                            zipFiles.addAll(Arrays.asList(files));
                        }
                    }
                }
            }
        }

        return createProductsFromJsonFiles(getFilesInsideZips(zipFiles));
    }

    public static List<File> getFilesInsideZips(List<File> zipFiles) {
        List<File> filesInsideZips = new ArrayList<>();

        for (File zipFile : zipFiles) {
            try (ZipFile zip = new ZipFile(zipFile)) {
                zip.stream().forEach(entry -> {
                    String entryName = entry.getName();
                    File outputFile = new File("tempPrices/" + entryName);
                    try {
                        InputStream inputStream = zip.getInputStream(entry);
                        OutputStream outputStream = new FileOutputStream(outputFile);
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = inputStream.read(buffer)) >= 0) {
                            outputStream.write(buffer, 0, length);
                        }
                        inputStream.close();
                        outputStream.close();
                        filesInsideZips.add(outputFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return filesInsideZips;
    }


    public static List<Product> createProductsFromJsonFiles(List<File> jsonFiles) {
        List<Product> products = new ArrayList<>();

        for (File jsonFile : jsonFiles) {
            try {
                List<Product> fileProducts = readProductsFromJson(jsonFile);
                products.addAll(fileProducts);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return products;
    }

    public static List<Product> readProductsFromJson(File jsonFile) throws IOException {
        List<Product> products = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(jsonFile), StandardCharsets.UTF_8))) {
            Gson gson = new Gson();
            Type productListType = new TypeToken<List<Product>>() {}.getType();
            products = gson.fromJson(reader, productListType);
        }

        return products;
    }



    public static void deleteFilesInDirectory() {
        File directory = new File("tempPrices/");

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        file.delete();
                    }
                }
            }
        }
    }


    public String getCreatedTimestamp() {
        return createdTimestamp;
    }
}
