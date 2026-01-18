package org.example.cyclicBarrier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.example.StringTools.log;

class ComplexTaskExecutor {
    private final int threadCount;
    private final List<ComplexTask> tasks;
    private final AtomicInteger totalResult;
    private CyclicBarrier barrier;

    public ComplexTaskExecutor(int threadCount) {
        this.threadCount = threadCount;
        this.tasks = new ArrayList<>();
        this.totalResult = new AtomicInteger(0);
        for (int i = 0; i < threadCount; i++) {
            tasks.add(new ComplexTask(i + 1));
        }
    }

    public void executeTasks(int numberOfTasks) {
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfTasks);

        barrier = new CyclicBarrier(threadCount, () -> {
            combineResults();
            log(Thread.currentThread().getName() + ": All tasks are completed. Combining the results...");
        });

        for (int i = 0; i < threadCount; i++) {
            final int taskIndex = i % numberOfTasks;
            executorService.submit(() -> {
                try {
                    tasks.get(taskIndex).execute();
                    log(Thread.currentThread().getName() + " reached the barrier. Waiting for other threads...");
                    barrier.await();
                    log(Thread.currentThread().getName() + " continues execution after the barrier");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log(Thread.currentThread().getName() + " was interrupted");
                } catch (BrokenBarrierException e) {
                    log("The barrier was broken: " + e.getMessage());
                }
            });
        }
        executorService.shutdown();
        while (!executorService.isTerminated()) {
            // ждём завершениия потоков
        }
    }

    private void combineResults() {
        int sum = 0;
        for (ComplexTask task : tasks) {
            sum += task.getResult();
        }

        totalResult.set(sum);
        log("The overall result of all tasks: " + totalResult.get());
    }
}
