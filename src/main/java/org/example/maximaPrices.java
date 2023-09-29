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


public class maximaPrices {


    public static void run() throws IOException {
        String urlsFilePath = "productUrls/maxima.txt";

        List<Product> products = new ArrayList<>();
        List<String> urls = Product.readUrlsFromFile(urlsFilePath);


        for (String url : urls) {
            products.addAll(scrapeProducts(url));
        }
        String timeSnapshot = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"));
        String jsonFileName = "prices/prices-maxima/" + timeSnapshot + "_maxima" + ".json";
        String zipFile = "prices/prices-maxima/maxima.zip";
        System.out.println("MAXIMA scrape done. Scraped items: " + products.size());
        System.out.println("MAXIMA WRITING TO JSON.");
        Product.writeProductsToJson(products, jsonFileName, zipFile);
        System.out.println("MAXIMA data written to JSON. " + jsonFileName);

    }


    public static List<Product> scrapeProducts(String url) throws IOException {
        System.out.println("MAXIMA SCRAPING: " + url);
        List<Product> products = new ArrayList<>();
        Document doc = Jsoup.parse(new URL(url).openStream(), "UTF-8", url);

        Elements productElements = doc.select("div.b-product--wrap.clearfix.b-product--js-hook");

        if (productElements.size() == 0) {
            return products;
        }


        for (Element productElement : productElements) {
            String productPrice = productElement.attr("data-b-units");
            String productTitle = productElement.attr("data-b-for-cart");
            String productId = productElement.attr("data-b-item-id");
            String imageUrl = extractImageUrl(productElement);


            String price = extractPrice(productPrice);
            String title = extractTitle(productTitle);

            Product product = new Product(title, price, productId, "maxima", imageUrl);
            products.add(product);
        }


        int currentPage = getPageNumber(url);
        int nextPage = currentPage + 1;
        String nextUrl = getNextPageUrl(url, nextPage);

        List<Product> nextPageProducts = scrapeProducts(nextUrl);
        products.addAll(nextPageProducts);


        return products;
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

    static String extractPrice(String productPrice) {
        int startIndex = productPrice.indexOf("\"price\":") + 8;
        int endIndex = productPrice.indexOf(",", startIndex);
        if (startIndex >= 0 && endIndex >= 0) {
            return productPrice.substring(startIndex, endIndex - 2);
        }
        return "";
    }

    static String extractTitle(String productTitle) {
        int startIndex = productTitle.indexOf("\"title\":\"") + 9;
        int endIndex = productTitle.indexOf("\",", startIndex);
        if (startIndex >= 0 && endIndex >= 0) {
            return productTitle.substring(startIndex, endIndex);
        }
        return "";
    }


    private static String extractImageUrl(Element productElement) {
        Element imgElement = productElement.selectFirst("img");
        return imgElement.attr("src");
    }
}
