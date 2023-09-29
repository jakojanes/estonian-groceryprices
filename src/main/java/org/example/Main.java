package org.example;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) throws IOException {

        runScrape();

    }


    public static void runScrape() {

        int availableProcessors = Runtime.getRuntime().availableProcessors();
        int maxThreads = availableProcessors - 1;

        ExecutorService executorService = Executors.newFixedThreadPool(maxThreads);

        executorService.submit(() -> {
            try {
                rimiPrices.run();
            } catch (IOException ignored) {

            }
        });

        executorService.submit(() -> {
            try {
                selverPrices.run();
            } catch (IOException ignored) {

            }
        });

        executorService.submit(() -> {
            try {
                maximaPrices.run();
            } catch (IOException ignored) {

            }
        });


        executorService.submit(() -> {
            try {
                coopPrices.run();
            } catch (IOException | InterruptedException ignored) {

            }
        });


        executorService.submit(() -> {
            try {
                prismaPrices.run();
            } catch (IOException ignored) {
            }
        });


        executorService.shutdown();

    }


}
