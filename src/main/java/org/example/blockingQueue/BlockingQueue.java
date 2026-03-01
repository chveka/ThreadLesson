package org.example.blockingQueue;

import java.util.ArrayDeque;
import java.util.Deque;

public class BlockingQueue<T> {
    private final Deque<T> queue;
    private final int maxSize;

    public BlockingQueue(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("The queue size must be positive");
        }
        this.queue = new ArrayDeque<>(maxSize);
        this.maxSize = maxSize;
    }

    public synchronized void enqueue(T item) throws InterruptedException {
        while (queue.size() == maxSize) {
            wait();
        }
        queue.addLast(item);
        notifyAll();
    }

    public synchronized T dequeue() throws InterruptedException {
        while (queue.isEmpty()) {
            wait();
        }
        T item = queue.removeFirst();
        notifyAll();
        return item;
    }

    public synchronized int size() {
        return queue.size();
    }
}