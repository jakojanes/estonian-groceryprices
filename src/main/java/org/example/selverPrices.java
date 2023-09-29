package org.example;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class selverPrices {
    static WebDriver driver = createChromeDriver();


    public static void run() throws IOException {
        Logger.getLogger("org.openqa.selenium").setLevel(Level.SEVERE);
        String urlsFilePath = "productUrls/selver.txt";
        List<Product> products = new ArrayList();
        List<String> urls = Product.readUrlsFromFile(urlsFilePath);
        Iterator var3 = urls.iterator();

        String jsonFileName;
        while(var3.hasNext()) {
            jsonFileName = (String)var3.next();
            products.addAll(scrapeProducts(jsonFileName));
        }

        String timeSnapshot = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"));
        jsonFileName = "prices/prices-selver/" + timeSnapshot + "_selver.json";
        String zipFile = "prices/prices-selver/selver.zip";
        System.out.println("SELVER scrape done. Scraped items: " + products.size());
        System.out.println("SELVER WRITING TO JSON.");
        Product.writeProductsToJson(products, jsonFileName, zipFile);
        System.out.println("SELVER data written to JSON. " + jsonFileName);
        driver.quit();
    }

    public static WebDriver createChromeDriver() {
        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
        System.setProperty("webdriver.chrome.silentOutput", "true");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(15L, TimeUnit.SECONDS);
        return driver;
    }

    public static List<Product> scrapeProducts(String url) {
        List<Product> products = new ArrayList();

        try {
            System.out.println("SELVER SCRAPING: " + url);
            driver.get(url);
            Thread.sleep(5000L);
            Document doc = Jsoup.parse(driver.getPageSource());
            Elements productContainers = doc.select("div.ProductCard[data-v-691fd582][data-v-be812f2c]");
            Iterator var4 = productContainers.iterator();

            while(var4.hasNext()) {
                Element productContainer = (Element)var4.next();
                Element nameElement = productContainer.selectFirst("h3.ProductCard__title a");
                String name = nameElement.text();
                String id = nameElement.attr("href");
                Element priceElement = productContainer.selectFirst("div.ProductPrice.ProductPrice--special");
                if (priceElement == null) {
                    priceElement = productContainer.selectFirst("div.ProductPrice.ProductPrice");
                }

                String price = priceElement.ownText();
                price = price.replaceAll(",", ".");
                price = price.substring(0, price.length() - 1);
                String imageUrl = extractImageUrl(productContainer);
                Product product = new Product(name.strip(), price.strip(), id.strip(), "selver", imageUrl);
                products.add(product);
            }
        } catch (Exception var13) {
            var13.printStackTrace();
        }

        return products;
    }

    private static String extractImageUrl(Element productElement) {
        Element imgElement = productElement.selectFirst("img");
        String imageUrl = imgElement.attr("data-src");
        return imageUrl;
    }
}