package functions.meta;

import functions.Function;

public class Sum implements Function {
    private Function f1;
    private Function f2;
    
    public Sum(Function f1, Function f2) {
        this.f1 = f1;
        this.f2 = f2;
    }
    
    public double getLeftDomainBorder() {
        // Пересечение областей определения
        return Math.max(f1.getLeftDomainBorder(), f2.getLeftDomainBorder());
    }
    
    public double getRightDomainBorder() {
        // Пересечение областей определения  
        return Math.min(f1.getRightDomainBorder(), f2.getRightDomainBorder());
    }
    
    public double getFunctionValue(double x) {
        // Проверяем, что x в области определения обеих функций
        if (x < getLeftDomainBorder() || x > getRightDomainBorder()) {
            return Double.NaN;
        }
        return f1.getFunctionValue(x) + f2.getFunctionValue(x);
    }
}
