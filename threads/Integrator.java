package threads;

import functions.Functions;

public class Integrator extends Thread {
    private Task task;
    private java.util.concurrent.Semaphore writeSem;
    private java.util.concurrent.Semaphore readSem;
    
    public Integrator(Task task, java.util.concurrent.Semaphore writeSem, java.util.concurrent.Semaphore readSem) {
        this.task = task;
        this.writeSem = writeSem;
        this.readSem = readSem;
    }
    
    @Override
    public void run() {
        try {
            int processed = 0;
            int tasksCount = task.getTasksCount();
            
            while (processed < tasksCount && !isInterrupted()) {
                readSem.acquire();
                

                if (isInterrupted()) {
                    break;
                }
                
                if (task.getFunction() != null) {
                    double leftX = task.getLeftX();
                    double rightX = task.getRightX();
                    double step = task.getStep();
                    
                    try {
                        double result = Functions.integrate(task.getFunction(), leftX, rightX, step);
                        System.out.printf("Integrator: Result %.2f %.2f %.4f %.6f\n", 
                            leftX, rightX, step, result);
                        processed++;
                    } catch (Exception e) {
                        System.out.printf("Integrator error: %s\n", e.getMessage());
                        processed++;
                    }
                    
               
                    task.setFunction(null);
                }
                
                writeSem.release();
            }
            
            System.out.printf("Integrator обработал %d заданий\n", processed);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Integrator прерван");
            
            writeSem.release();
        }
    }
}