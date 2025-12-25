package threads;

import functions.Function;
import functions.basic.Log;

public class SimpleGenerator implements Runnable {
    private Task task;
    
    public SimpleGenerator(Task task) {
        this.task = task;
    }
    
    @Override
    public void run() {
        while (task.hasNext()) {
            synchronized (task) {
                double base = 1.0 + Math.random() * 9.0;
                Function logFunc = new Log(base);
                
                double leftX = Math.random() * 100.0;
                double rightX = 100.0 + Math.random() * 100.0;
                double step = Math.random();
                
                task.setFunction(logFunc);
                task.setLeftX(leftX);
                task.setRightX(rightX);
                task.setStep(step);
                
                System.out.printf("Source %.2f %.2f %.4f\n", leftX, rightX, step);
                
                task.next();
                
                task.notify();
            }
            
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}