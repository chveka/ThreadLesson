package org.example.blockingQueue;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.example.StringTools.log;

public class BlockingQueueExample {
    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<Integer> queue = new BlockingQueue<>(100);
        AtomicInteger addedTask = new AtomicInteger(0);
        AtomicInteger executedTask = new AtomicInteger(0);
        AtomicLong totalWaitTime = new AtomicLong(0);

        int producersAddedTasksCount = 7;
        int producersExecutedTasksCount = 8;
        int itemsPerProducer = 100;

        Thread[] producersAddedTasks = new Thread[producersAddedTasksCount];
        Thread[] producersExecutedTasks = new Thread[producersExecutedTasksCount];

        for (int i = 0; i < producersAddedTasksCount; i++) {
            final int addedTaskId = i;
            producersAddedTasks[i] = new Thread(() -> {
                for (int j = 0; j < itemsPerProducer; j++) {
                    try {
                        long startTime = System.nanoTime();
                        queue.enqueue(addedTaskId * 1000 + j);
                        long endTime = System.nanoTime();

                        addedTask.incrementAndGet();
                        totalWaitTime.addAndGet(endTime - startTime);

                        Thread.sleep((long) (Math.random() * 10));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                log("Add task " + addedTaskId + " ended");
            });
        }

        for (int i = 0; i < producersExecutedTasksCount; i++) {
            final int executedTaskId = i;
            producersExecutedTasks[i] = new Thread(() -> {
                while (executedTask.get() < producersAddedTasksCount * itemsPerProducer) {
                    try {
                        long startTime = System.nanoTime();
                        Integer item = queue.dequeue();
                        long endTime = System.nanoTime();

                        if (item != null) {
                            executedTask.incrementAndGet();
                            totalWaitTime.addAndGet(endTime - startTime);
                        }

                        Thread.sleep((long) (Math.random() * 15));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                log("Execute task " + executedTaskId + " ended");
            });
        }

        log("Start test with " + producersAddedTasksCount + " addedTask " + producersExecutedTasksCount + " executedTask");

        long startTime = System.currentTimeMillis();

        for (Thread producerExecutedTask : producersExecutedTasks) {
            producerExecutedTask.start();
        }

        for (Thread producerAddedTask : producersAddedTasks) {
            producerAddedTask.start();
        }

        for (Thread producerAddedTask : producersAddedTasks) {
            producerAddedTask.join();
        }

        for (Thread producerExecutedTask : producersExecutedTasks) {
            producerExecutedTask.join();
        }

        long endTime = System.currentTimeMillis();

        log("\n=== Results ===");
        log("Add task: " + addedTask.get());
        log("Execute task: " + executedTask.get());
        log("Total time: " + (endTime - startTime) + " ms");
    }
}