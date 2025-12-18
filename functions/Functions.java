package functions;

import functions.meta.*;

public final class Functions {
    // Приватный конструктор - нельзя создавать объекты
    private Functions() {
        throw new AssertionError("Нельзя создавать объекты класса Functions");
    }
    
    public static Function shift(Function f, double shiftX, double shiftY) {
        return new Shift(f, shiftX, shiftY);
    }
    
    public static Function scale(Function f, double scaleX, double scaleY) {
        return new Scale(f, scaleX, scaleY);
    }
    
    public static Function power(Function f, double power) {
        return new Power(f, power);
    }
    
    public static Function sum(Function f1, Function f2) {
        return new Sum(f1, f2);
    }
    
    public static Function mult(Function f1, Function f2) {
        return new Mult(f1, f2);
    }
    
    public static Function composition(Function f1, Function f2) {
        return new Composition(f1, f2);
    }

    public static double integrate(Function function, double a, double b, double step) 
            throws IllegalArgumentException {
        
        // Проверка границ интегрирования
        if (a < function.getLeftDomainBorder() || b > function.getRightDomainBorder()) {
            throw new IllegalArgumentException(
                String.format("Интервал интегрирования [%.2f, %.2f] выходит за область определения [%.2f, %.2f]",
                    a, b, function.getLeftDomainBorder(), function.getRightDomainBorder())
            );
        }
        
        if (a >= b) {
            throw new IllegalArgumentException("Левая граница должна быть меньше правой");
        }
        
        if (step <= 0) {
            throw new IllegalArgumentException("Шаг должен быть положительным");
        }
        
        double integral = 0.0;
        double currentX = a;
        
        // Метод трапеций
        while (currentX < b) {
            double nextX = Math.min(currentX + step, b);
            double f1 = function.getFunctionValue(currentX);
            double f2 = function.getFunctionValue(nextX);
            
            // Площадь трапеции
            double segmentArea = (f1 + f2) * (nextX - currentX) / 2.0;
            integral += segmentArea;
            
            currentX = nextX;
        }
        
        return integral;
    }
}
