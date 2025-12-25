package threads;

import functions.Function;
import functions.Functions;

public class SimpleIntegrator implements Runnable {
    private Task task;
    
    public SimpleIntegrator(Task task) {
        this.task = task;
    }
    
    @Override
    public void run() {
        int processed = 0;
        
        while (processed < task.getTasksCount()) {
            synchronized (task) {
                while (task.getFunction() == null && processed < task.getTasksCount()) {
                    try {
                        task.wait(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                
                Function func = task.getFunction();
                if (func != null) {
                    double leftX = task.getLeftX();
                    double rightX = task.getRightX();
                    double step = task.getStep();
                    
                    try {
                        double result = Functions.integrate(func, leftX, rightX, step);
                        System.out.printf("Result %.2f %.2f %.4f %.6f\n", 
                            leftX, rightX, step, result);
                        processed++;
                    } catch (Exception e) {
                        System.out.printf("Error: %s\n", e.getMessage());
                    }
                    
                    task.setFunction(null);
                }
            }
        }
        
        System.out.printf("Интегратор обработал %d заданий\n", processed);
    }
}