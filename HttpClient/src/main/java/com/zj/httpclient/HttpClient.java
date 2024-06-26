package com.zj.httpclient;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.util.StatusPrinter;
import com.google.common.util.concurrent.RateLimiter;
import com.zj.httpclient.entity.Result;
import com.zj.httpclient.entity.Status;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.io.InputStreamResource;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 *
 */

public class HttpClient {

    private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);


    /**
     * send a request
     *
     * @param url
     * @return result of square root
     */
    public static String sendRequest(String url) {

        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpGet request = new HttpGet(url);

        try (CloseableHttpResponse response = httpClient.execute(request);) {
            if (response.getStatusLine().getStatusCode() == 200) {
                return EntityUtils.toString(response.getEntity());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return "";
    }

    /**
     * Request the result of an API request
     *
     * @return Result
     */
    public static Result calSquareRoot() {
        Random random = new Random();
        Integer inputNumber = random.nextInt(1000);
        String url = "http://localhost:8080/" + inputNumber;

        long startTime = System.currentTimeMillis();
        String squareRes = HttpClient.sendRequest(url);
        long delay = System.currentTimeMillis() - startTime;  // ms

        String status = (delay >= 100 &&
                delay <= 200 &&
                squareRes.equals(String.valueOf(Math.sqrt(inputNumber)))) ? "PASS" : "FAIL";

        return new Result(inputNumber, Double.valueOf(squareRes), delay, Status.valueOf(status));
    }

    /**
     * calculate the success rate
     *
     * @param total
     * @return
     */
    public static ArrayList<Result> getSquareRoots(Integer total) {

        ArrayList<Result> results = new ArrayList<>();

        for (int i = 1; i <= total; i++) {
            results.add(calSquareRoot());
        }

        return results;
    }


    /**
     * calculate the success rate of API under the different QPS
     * @param requestNum
     * @param QPS
     * @param threadNum
     * @return
     */
    public static Double StressTestFun(Integer requestNum, Integer QPS, Integer threadNum) {

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger totalCount = new AtomicInteger(0);

        RateLimiter limiter = RateLimiter.create(QPS);

        // warm-up
        for (int i = 0; i < 10; i++) {
            calSquareRoot();
        }

        ExecutorService executor = Executors.newFixedThreadPool(threadNum);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadNum; i++) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    while (totalCount.get() < requestNum) {
                        limiter.acquire();
                        Result result = calSquareRoot();
                        logger.info("result = " + result);
                        if (result.getStatus().equals(Status.PASS)) {
                            successCount.incrementAndGet();
                        }
                        totalCount.incrementAndGet();
                    }
                }
            });
        }

        executor.shutdown();

        // waiting for all the task to finish
        while (true) {
            if (executor.isTerminated()) {
                logger.info("共耗时:" + (System.currentTimeMillis() - startTime) / 1000.0 + "s");
                break;
            }
        }

        logger.info("actual QPS: " + totalCount.get() * 1000.0 / (System.currentTimeMillis() - startTime));
        logger.info("successCount: " + successCount.get());
        logger.info("totalCount: " + totalCount.get());
        logger.info("success rate: " + successCount.get() * 1.0 / totalCount.get() * 100 + "%");

        return successCount.get() * 1.0 / totalCount.get() * 100;
    }


    /**
     * get the max QPS when the availability > 99.95%
     * @param requestNum
     * @param minQPS
     * @param maxQPS
     * @return
     */
    public static Integer checkMaxQPS(Integer requestNum, Integer minQPS, Integer maxQPS) {

        int QPS_interval = 10;

        Map<Integer, Double> qpsMaps = new HashMap<>();

        for (int i = minQPS; i <= maxQPS; i += QPS_interval) {
            logger.info("Currnent QPS = " + i);

            try {
                double successRate = HttpClient.StressTestFun(requestNum, i, i / 5);
                boolean isAvail =  successRate > 99.95 ? true : false;
                if (isAvail) {
                    qpsMaps.put(i, successRate);
                }
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        if (qpsMaps.isEmpty()) {
            logger.info("There is currently no QPS that meets the requirements!");
            return null;
        }

        // if qpsMaps is not empty, we will get max qps
        for (Map.Entry<Integer, Double> entry : qpsMaps.entrySet()) {
            logger.info("QPS = " + entry.getKey() + ", successRate = " + entry.getValue());
        }
        Integer MAX_QPS = qpsMaps.keySet().stream().max(Comparator.comparing(x -> x)).orElse(null);
        logger.info(String.format("Max QPS is %s when the availability > 99.95%%", String.valueOf(MAX_QPS)));

        return MAX_QPS;

    }

}

