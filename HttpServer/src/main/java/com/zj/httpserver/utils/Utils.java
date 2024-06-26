package com.zj.httpserver.utils;

import java.util.Random;

public class Utils {

    public static void simulateTimeConsumingOperation() {
        Random random = new Random();

        double mean = 150.0;  // ( 100 + 200 ) / 2
        double std = 11.0;

        // generate random delay
        double randomDelay = mean + random.nextGaussian() * std;
        int time = Math.max(0, (int) randomDelay);

        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
