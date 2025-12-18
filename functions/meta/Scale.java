package functions.meta;

import functions.Function;

public class Scale implements Function {
    private Function f;
    private double scaleX;
    private double scaleY;
    
    public Scale(Function f, double scaleX, double scaleY) {
        this.f = f;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }
    
    public double getLeftDomainBorder() {
        return f.getLeftDomainBorder() * scaleX;
    }
    
    public double getRightDomainBorder() {
        return f.getRightDomainBorder() * scaleX;
    }
    
    public double getFunctionValue(double x) {
        double originalX = x / scaleX;
        if (originalX < f.getLeftDomainBorder() || originalX > f.getRightDomainBorder()) {
            return Double.NaN;
        }
        return f.getFunctionValue(originalX) * scaleY;
    }
}
