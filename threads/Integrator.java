package threads;

import functions.Functions;

public class Integrator extends Thread {
    private Task task;
    private Semaphore semaphore;
    
    public Integrator(Task task, Semaphore semaphore) {
        this.task = task;
        this.semaphore = semaphore;
    }
    
    @Override
    public void run() {
        try {
            while (task.hasNext() && !isInterrupted()) {
                semaphore.startRead();
                
                try {
                    if (task.getFunction() != null) {
                        double leftX = task.getLeftX();
                        double rightX = task.getRightX();
                        double step = task.getStep();
                        
                        try {
                            double result = Functions.integrate(task.getFunction(), leftX, rightX, step);
                            System.out.printf("Integrator: Result %.2f %.2f %.4f %.6f\n", 
                                leftX, rightX, step, result);
                        } catch (Exception e) {
                            System.out.printf("Integrator error: %s\n", e.getMessage());
                        }
                    }
                } finally {
                    semaphore.endRead();
                }
                
                // Короткая пауза, но с проверкой прерывания
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break; // Выходим из цикла при прерывании
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Восстанавливаем флаг прерывания
            System.out.println("Integrator был прерван");
        }
    }
}