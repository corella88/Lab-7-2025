package functions.basic;

import functions.Function;

public class Log implements Function {
    private double base;
    
    public Log(double base) {
        if (base <= 0 || Math.abs(base - 1.0) < 1e-10) {
            throw new IllegalArgumentException("Основание логарифма должно быть > 0 и ≠ 1");
        }
        this.base = base;
    }
    
    public double getLeftDomainBorder() {
        return 0; // Логарифм определен для x > 0
    }
    
    public double getRightDomainBorder() {
        return Double.POSITIVE_INFINITY;
    }
    
    public double getFunctionValue(double x) {
        // Используем машинный эпсилон для сравнения
        if (x < -1e-10) { // x < 0
            return Double.NaN;
        }
        
        // x близко к 0 или 0
        if (Math.abs(x) < 1e-10) {
            return Double.NEGATIVE_INFINITY;
        }
        
        // x близко к 1
        if (Math.abs(x - 1.0) < 1e-10) {
            return 0.0;
        }
        
        // Обычный случай
        return Math.log(x) / Math.log(base);
    }
}