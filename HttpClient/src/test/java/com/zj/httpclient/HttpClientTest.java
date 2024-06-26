package com.zj.httpclient;

import com.zj.httpclient.entity.Result;
import com.zj.httpclient.entity.Status;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


public class HttpClientTest {

    @Test
    public void FunctionTest() {

        int total = 10000;

        ArrayList<Result> squareRoots = HttpClient.getSquareRoots(total);
        long successCount = squareRoots.stream()
                .filter(result -> result.getStatus() == Status.PASS)
                .count();

        double successRate = successCount * 1.0 / total;

        // write data to file

        String filePath = "result_totalCount_" + total + ".txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Result result : squareRoots) {
                writer.write(result.toString() + "\n");
            }
            writer.write("successCount = " + successCount + "\n");
            writer.write("totalCount = " + total + "\n");
            writer.write("successRate = " + successRate);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("The success Rate is: " + successRate);

    }

    @Test
    public void testQPS() {
        int requestNum = 10000;
        int QPS = 550;
        int threadNum = QPS / 5;

        HttpClient.StressTestFun(requestNum, QPS, threadNum);

    }

    @Test
    public void checkMaxQPS() {
        int requestNum = 10000;
        int minQPS = 10;
        int maxQPS = 150;

        HttpClient.checkMaxQPS(requestNum, minQPS, maxQPS);
    }

}
