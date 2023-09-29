package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class rimiPrices {


    public static void run() throws IOException {
        String urlsFilePath = "productUrls/rimi.txt";

        List<String> urls = Product.readUrlsFromFile(urlsFilePath);
        List<Product> products = new ArrayList<>();


        for (String url : urls) {
            products.addAll(scrapeProducts(url));
        }

        String timeSnapshot = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"));
        String jsonFileName = "prices/prices-rimi/" + timeSnapshot + "_rimi" + ".json";
        String zipFile = "prices/prices-rimi/rimi.zip";
        System.out.println("RIMI scrape done. Scraped items: " + products.size());
        System.out.println("RIMI WRITING TO JSON.");
        Product.writeProductsToJson(products, jsonFileName, zipFile);
        System.out.println("RIMI data written to JSON. " + jsonFileName);
    }

    private static List<Product> scrapeProducts(String url) throws IOException {
        System.out.println("RIMI SCRAPING: " + url);
        List<Product> products = new ArrayList<>();
        Document doc = Jsoup.parse(new URL(url).openStream(), "UTF-8", url);


        Elements productContainers = doc.select(".js-product-container");

        if (productContainers.size() == 0) {
            return products;
        }

        for (Element productContainer : productContainers) {
            String bannerTitle = productContainer.attr("data-gtms-banner-title");
            String productId = productContainer.attr("data-gtms-product-id");
            String price = extractPriceFromGtmProductData(productContainer.attr("data-gtm-eec-product"));
            String imageUrl = extractImageUrl(productContainer);


            Product product = new Product(bannerTitle, price, productId, "rimi", imageUrl);
            products.add(product);
        }


        int currentPage = getPageNumber(url);
        int nextPage = currentPage + 1;
        String nextUrl = getNextPageUrl(url, nextPage);

        List<Product> nextPageProducts = scrapeProducts(nextUrl);
        products.addAll(nextPageProducts);

        return products;
    }

    private static String extractPriceFromGtmProductData(String gtmProductData) {
        int startIndex = gtmProductData.indexOf("\"price\":") + 8;
        int endIndex = gtmProductData.indexOf(",", startIndex);

        if (startIndex < 0 || endIndex < 0) {
            return "";
        }

        return gtmProductData.substring(startIndex, endIndex);
    }


    private static int getPageNumber(String url) {
        int startIndex = url.indexOf("page=") + 5;
        int endIndex = url.indexOf("&", startIndex);
        if (endIndex == -1) {
            endIndex = url.length();
        }
        String pageNumberStr = url.substring(startIndex, endIndex);
        return Integer.parseInt(pageNumberStr);
    }

    private static String getNextPageUrl(String url, int nextPage) {
        return url.replaceAll("page=\\d+", "page=" + nextPage);
    }


    private static String extractImageUrl(Element productElement) {
        Element imgElement = productElement.selectFirst("img");
        return imgElement.attr("src");
    }

}