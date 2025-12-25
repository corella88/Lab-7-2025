package threads;

import functions.Function;
import functions.basic.Log;

public class Generator extends Thread {
    private Task task;
    private java.util.concurrent.Semaphore writeSem;
    private java.util.concurrent.Semaphore readSem;
    
    public Generator(Task task, java.util.concurrent.Semaphore writeSem, java.util.concurrent.Semaphore readSem) {
        this.task = task;
        this.writeSem = writeSem;
        this.readSem = readSem;
    }
    
    @Override
    public void run() {
        try {
            while (task.hasNext() && !isInterrupted()) {
                writeSem.acquire();
                
                double base = 1.0 + Math.random() * 9.0;
                Function logFunc = new Log(base);
                
                double leftX = Math.random() * 100.0;
                double rightX = 100.0 + Math.random() * 100.0;
                double step = Math.random();
                
                task.setFunction(logFunc);
                task.setLeftX(leftX);
                task.setRightX(rightX);
                task.setStep(step);
                
                System.out.printf("Generator: Source %.2f %.2f %.4f\n", 
                    leftX, rightX, step);
                
                task.next();
                
                readSem.release(); 
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Generator прерван");
        }
        
        readSem.release();
    }
}