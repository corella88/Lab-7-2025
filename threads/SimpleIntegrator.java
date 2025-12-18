package threads;

import functions.Function;  // ← ДОБАВЬ ЭТУ СТРОЧКУ!
import functions.Functions;

public class SimpleIntegrator implements Runnable {
    private Task task;
    
    public SimpleIntegrator(Task task) {
        this.task = task;
    }
    
    @Override
    public void run() {
        int processed = 0;
        
        while (task.hasNext()) {
            synchronized (task) {
                // Получаем данные на момент вызова
                Function func = task.getFunction();
                double leftX = task.getLeftX();
                double rightX = task.getRightX();
                double step = task.getStep();
                
                // Если функция не null - интегрируем
                if (func != null) {
                    try {
                        double result = Functions.integrate(func, leftX, rightX, step);
                        System.out.printf("Result %.2f %.2f %.4f %.6f\n", 
                            leftX, rightX, step, result);
                        processed++;
                    } catch (Exception e) {
                        System.out.printf("Error: %s\n", e.getMessage());
                    }
                }
            }
        }
        
        System.out.printf("Интегратор обработал %d заданий\n", processed);
    }
}