package org.example.cyclicBarrier;

import static org.example.StringTools.log;

class ComplexTask {
    private final int taskId;
    private int result;

    public ComplexTask(int taskId) {
        this.taskId = taskId;
        this.result = 0;
    }

    public void execute() {
        log(Thread.currentThread().getName() + " execute task " + taskId);
        int localResult = 0;
        for (int i = 0; i < 1000; i++) {
            localResult += (int) (Math.random() * 100);
        }
        this.result = localResult;
        log(Thread.currentThread().getName() + " end task " + taskId + " with result: " + result);
    }

    public int getResult() {
        return result;
    }
}
