package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class coopPrices {


    public static void run() throws InterruptedException, IOException {
        WebDriver driver = createChromeDriver();
        String urlsFilePath = "productUrls/coop.txt";

        List<String> urls = Product.readUrlsFromFile(urlsFilePath);
        List<Product> products = new ArrayList<>();


        for (String url : urls) {
            products.addAll(scrapeProducts(driver, url));
        }

        String timeSnapshot = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"));
        String jsonFileName = "prices/prices-coop/" + timeSnapshot + "_coop" + ".json";
        String zipFile = "prices/prices-coop/coop.zip";
        System.out.println("COOP scrape done. Scraped items: " + products.size());
        System.out.println("COOP WRITING TO JSON.");
        Product.writeProductsToJson(products, jsonFileName, zipFile);
        System.out.println("COOP data written to JSON. " + jsonFileName);

        driver.quit();
    }

    public static List<Product> scrapeProducts(WebDriver driver, String url) throws InterruptedException {
        List<Product> products = new ArrayList<>();
        try {
            System.out.println("COOP SCRAPING: " + url);

            driver.get(url);

            WebDriverWait wait = new WebDriverWait(driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("app-product-card.item")));


            long startTime = System.currentTimeMillis();
            long timeout = 60 * 1000;

            while (System.currentTimeMillis() - startTime < timeout) {
                Document doc = Jsoup.parse(driver.getPageSource());

                Elements productContainers;
                try {
                    productContainers = doc.select("app-product-card.item");
                } catch (Exception e) {
                    return products;
                }

                if (productContainers.isEmpty()) {
                    return products;
                }

                for (Element productContainer : productContainers) {
                    String id = productContainer.selectFirst("a.product-content").attr("href").replaceAll("\\D+", "");
                    String name = productContainer.selectFirst("p.product-name").text();
                    String price = productContainer.selectFirst("div.integer").text() + "." + productContainer.selectFirst("div.decimal").text();
                    price = price.substring(0, price.length() - 1).strip();
                    String imageSrc = productContainer.selectFirst("div.product-img-wp img").attr("src");

                    Product product = new Product(name, price, id, "coop", imageSrc);
                    products.add(product);
                }

                int currentPage = getPageNumber(url);
                int nextPage = currentPage + 1;
                String nextUrl = getNextPageUrl(url, nextPage);

                List<Product> nextPageProducts = scrapeProducts(driver, nextUrl);
                products.addAll(nextPageProducts);


                return products;


            }


            System.out.println("COOP SCRAPING TIMEOUT: " + url);
            return products;

        } catch (Exception ignored) {
            return products;
        }
    }


    public static WebDriver createChromeDriver() {
        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
        System.setProperty("webdriver.chrome.silentOutput", "true");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
        return driver;
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


}
