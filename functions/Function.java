package functions;

public interface Function {
    // Возвращает значение левой границы области определения функции
    double getLeftDomainBorder();
    
    // Возвращает значение правой границы области определения функции
    double getRightDomainBorder();
    
    // Возвращает значение функции в заданной точке
    double getFunctionValue(double x);
}
