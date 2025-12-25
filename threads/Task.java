package threads;

import functions.Function;

public class Task {
    private Function function;
    private double leftX;
    private double rightX;
    private double step;
    private int tasksCount;
    private int currentTask;
    private boolean dataReady = false;
    private boolean allDone = false;
    
    public Task(int tasksCount) {
        if (tasksCount < 1) {
            throw new IllegalArgumentException("Количество заданий должно быть положительным");
        }
        this.tasksCount = tasksCount;
        this.currentTask = 0;
        this.dataReady = false;
        this.allDone = false;
    }
    
    public synchronized void setFunction(Function function) {
        this.function = function;
    }
    
    public synchronized Function getFunction() {
        return function;
    }
    
    public synchronized void setLeftX(double leftX) {
        this.leftX = leftX;
    }
    
    public synchronized double getLeftX() {
        return leftX;
    }
    
    public synchronized void setRightX(double rightX) {
        this.rightX = rightX;
    }
    
    public synchronized double getRightX() {
        return rightX;
    }
    
    public synchronized void setStep(double step) {
        this.step = step;
    }
    
    public synchronized double getStep() {
        return step;
    }
    
    public synchronized int getTasksCount() {
        return tasksCount;
    }
    
    public synchronized boolean hasNext() {
        return currentTask < tasksCount;
    }
    
    public synchronized void next() {
        currentTask++;
        if (currentTask >= tasksCount) {
            allDone = true;
            notifyAll();
        }
    }
    
    public synchronized int getCurrentTask() {
        return currentTask;
    }
    
    public synchronized void markDataReady() {
        dataReady = true;
        notifyAll();
    }
    
    public synchronized void waitForData() throws InterruptedException {
        while (!dataReady && hasNext()) {
            wait();
        }
        dataReady = false;
    }
    
    public synchronized boolean isAllDone() {
        return allDone;
    }
}