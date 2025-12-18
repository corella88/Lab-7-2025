package threads;

import functions.Function;
import functions.basic.Log;

public class Generator extends Thread {
    private Task task;
    private Semaphore semaphore;
    
    public Generator(Task task, Semaphore semaphore) {
        this.task = task;
        this.semaphore = semaphore;
    }
    
    @Override
    public void run() {
        try {
            while (task.hasNext() && !isInterrupted()) {
                semaphore.startWrite();
                
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
                
                semaphore.endWrite();
                
                Thread.sleep(1);
            }
        } catch (InterruptedException e) {
            return;
        }
    }
}