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

public class prismaPrices {

    public static void run() throws IOException {
        String urlsFilePath = "productUrls/prisma.txt";

        List<String> urls = Product.readUrlsFromFile(urlsFilePath);
        List<Product> products = new ArrayList<>();


        for (String url : urls) {
            products.addAll(scrapeProducts(url, 0));
        }

        String timeSnapshot = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"));
        String jsonFileName = "prices/prices-prisma/" + timeSnapshot + "_prisma" + ".json";
        String zipFile = "prices/prices-prisma/prisma.zip";
        System.out.println("PRISMA scrape done. Scraped items: " + products.size());
        System.out.println("PRISMA WRITING TO JSON.");
        Product.writeProductsToJson(products, jsonFileName, zipFile);
        System.out.println("PRISMA data written to JSON. " + jsonFileName);
    }


    private static List<Product> scrapeProducts(String url, int totalItemCount) throws IOException {
        System.out.println("PRISMA SCRAPING: " + url);
        List<Product> products = new ArrayList<>();
        Document doc = Jsoup.parse(new URL(url).openStream(), "UTF-8", url);


        Elements productContainers = doc.select("li.relative.item.effect.fade-shadow.js-shelf-item");

        if (productContainers.size() == 0) {
            return products;
        }

        for (Element productContainer : productContainers) {
            Element linkElement = productContainer.selectFirst("a.js-link-item");

            String href = linkElement.attr("href");
            String productId = href.substring(href.lastIndexOf('/') + 1);

            Element imageElement = productContainer.selectFirst("img.img-responsive");
            String imageUrl = imageElement.attr("src");

            if (imageUrl.equals("/images/entry_no_image_170.png"))
                imageUrl = null;

            Element nameElement = productContainer.selectFirst("div.name");
            String name = nameElement.text();

            Element priceElement = productContainer.selectFirst("div.js-info-price");
            String price = priceElement.selectFirst("span.whole-number").text() + "." + priceElement.selectFirst("span.decimal").text();

            Product product = new Product(name, price, productId, "prisma", imageUrl);
            products.add(product);

        }
        totalItemCount += products.size();

        int pageIndex = url.lastIndexOf("/") + 1;
        String pageNumberStr = url.substring(pageIndex);
        int pageNumber = Integer.parseInt(pageNumberStr);
        int nextPageNumber = pageNumber + 1;
        String nextUrl = url.substring(0, pageIndex) + nextPageNumber;


        if (totalItemCount == getCategoryItemCount(doc)) {
            return products;
        } else {
            List<Product> nextPageProducts = scrapeProducts(nextUrl, totalItemCount);
            products.addAll(nextPageProducts);
        }

        return products;
    }


    public static int getCategoryItemCount(Document doc) {
        Element categoryItems = doc.selectFirst("div.category-items.js-cat-items.clear.clearfix");
        String itemCountText = categoryItems.selectFirst("b").text();
        return Integer.parseInt(itemCountText);
    }


}
