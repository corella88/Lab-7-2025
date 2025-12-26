package functions;

import java.io.*;
import java.lang.reflect.Constructor;

public final class TabulatedFunctions {
    private static TabulatedFunctionFactory factory = new ArrayTabulatedFunction.ArrayTabulatedFunctionFactory();
    
    private TabulatedFunctions() {
        throw new AssertionError("Нельзя создавать объекты класса TabulatedFunctions");
    }
    
    public static void setTabulatedFunctionFactory(TabulatedFunctionFactory factory) {
        TabulatedFunctions.factory = factory;
    }
    
    // Методы с использованием фабрики
    public static TabulatedFunction createTabulatedFunction(double leftX, double rightX, int pointsCount) {
        return factory.createTabulatedFunction(leftX, rightX, pointsCount);
    }
    
    public static TabulatedFunction createTabulatedFunction(double leftX, double rightX, double[] values) {
        return factory.createTabulatedFunction(leftX, rightX, values);
    }
    
    public static TabulatedFunction createTabulatedFunction(FunctionPoint[] points) {
        return factory.createTabulatedFunction(points);
    }
    
    // Методы с использованием рефлексии
    public static TabulatedFunction createTabulatedFunction(Class<?> functionClass, 
            double leftX, double rightX, int pointsCount) {
        try {
            if (!TabulatedFunction.class.isAssignableFrom(functionClass)) {
                throw new IllegalArgumentException("Класс должен реализовывать TabulatedFunction");
            }
            
            Constructor<?> constructor = functionClass.getConstructor(
                double.class, double.class, int.class);
            return (TabulatedFunction) constructor.newInstance(leftX, rightX, pointsCount);
        } catch (Exception e) {
            throw new IllegalArgumentException("Ошибка при создании функции", e);
        }
    }
    
    public static TabulatedFunction createTabulatedFunction(Class<?> functionClass,
            double leftX, double rightX, double[] values) {
        try {
            if (!TabulatedFunction.class.isAssignableFrom(functionClass)) {
                throw new IllegalArgumentException("Класс должен реализовывать TabulatedFunction");
            }
            
            Constructor<?> constructor = functionClass.getConstructor(
                double.class, double.class, double[].class);
            return (TabulatedFunction) constructor.newInstance(leftX, rightX, values);
        } catch (Exception e) {
            throw new IllegalArgumentException("Ошибка при создании функции", e);
        }
    }
    
    public static TabulatedFunction createTabulatedFunction(Class<?> functionClass,
            FunctionPoint[] points) {
        try {
            if (!TabulatedFunction.class.isAssignableFrom(functionClass)) {
                throw new IllegalArgumentException("Класс должен реализовывать TabulatedFunction");
            }
            
            Constructor<?> constructor = functionClass.getConstructor(FunctionPoint[].class);
            return (TabulatedFunction) constructor.newInstance((Object)points);
        } catch (Exception e) {
            throw new IllegalArgumentException("Ошибка при создании функции", e);
        }
    }
    
    public static TabulatedFunction tabulate(Function function, double leftX, double rightX, int pointsCount) {
        if (leftX < function.getLeftDomainBorder()) {
            throw new IllegalArgumentException("Левая граница табулирования " + leftX + 
                " выходит за левую границу области определения " + function.getLeftDomainBorder());
        }
        
        if (rightX > function.getRightDomainBorder()) {
            throw new IllegalArgumentException("Правая граница табулирования " + rightX + 
                " выходит за правую границу области определения " + function.getRightDomainBorder());
        }
        
        if (pointsCount < 2) {
            throw new IllegalArgumentException("Количество точек должно быть не менее 2");
        }
        
        if (leftX >= rightX) {
            throw new IllegalArgumentException("Левая граница должна быть меньше правой");
        }
        
        double[] values = new double[pointsCount];
        double step = (rightX - leftX) / (pointsCount - 1);
        
        for (int i = 0; i < pointsCount; i++) {
            double x = leftX + i * step;
            
            if (i == pointsCount - 1) {
                x = rightX;
            }
            
            values[i] = function.getFunctionValue(x);
        }
        
        return createTabulatedFunction(leftX, rightX, values);
    }
    
    // Версия с рефлексией
    public static TabulatedFunction tabulate(Class<?> functionClass, 
            Function function, double leftX, double rightX, int pointsCount) {
        double[] values = new double[pointsCount];
        double step = (rightX - leftX) / (pointsCount - 1);
        
        for (int i = 0; i < pointsCount; i++) {
            double x = leftX + i * step;
            
            if (i == pointsCount - 1) {
                x = rightX;
            }
            
            values[i] = function.getFunctionValue(x);
        }
        
        return createTabulatedFunction(functionClass, leftX, rightX, values);
    }

    public static void outputTabulatedFunction(TabulatedFunction function, OutputStream out) throws IOException {
        DataOutputStream dataOut = new DataOutputStream(out);
        
        dataOut.writeInt(function.getPointsCount());
        
        for (int i = 0; i < function.getPointsCount(); i++) {
            FunctionPoint point = function.getPoint(i);
            dataOut.writeDouble(point.getX());
            dataOut.writeDouble(point.getY());
        }
        
        dataOut.flush();
    }

    public static TabulatedFunction inputTabulatedFunction(InputStream in) throws IOException {
        DataInputStream dataIn = new DataInputStream(in);
        
        int pointsCount = dataIn.readInt();
        
        FunctionPoint[] points = new FunctionPoint[pointsCount];
        for (int i = 0; i < pointsCount; i++) {
            double x = dataIn.readDouble();
            double y = dataIn.readDouble();
            points[i] = new FunctionPoint(x, y);
        }
        
        return createTabulatedFunction(points);
    }

    public static void writeTabulatedFunction(TabulatedFunction function, Writer out) throws IOException {
        PrintWriter writer = new PrintWriter(out);
        
        writer.print(function.getPointsCount());
        writer.print(" ");
        
        for (int i = 0; i < function.getPointsCount(); i++) {
            FunctionPoint point = function.getPoint(i);
            double x = point.getX();
            double y = point.getY();
            
            if (Math.abs(y) < 1e-10) {
                y = 0.0;
            }
            
            writer.print(x);
            writer.print(" ");
            writer.print(y);
            if (i < function.getPointsCount() - 1) {
                writer.print(" ");
            }
        }
        
        writer.flush();
    }
    
    public static TabulatedFunction readTabulatedFunction(Reader in) throws IOException {
        StreamTokenizer tokenizer = new StreamTokenizer(in);
        
        tokenizer.nextToken();
        int pointsCount = (int) tokenizer.nval;
        
        FunctionPoint[] points = new FunctionPoint[pointsCount];
        for (int i = 0; i < pointsCount; i++) {
            tokenizer.nextToken();
            double x = tokenizer.nval;
            tokenizer.nextToken();
            double y = tokenizer.nval;
            
            if (Math.abs(y) < 1e-10) {
                y = 0.0;
            }
            
            points[i] = new FunctionPoint(x, y);
        }
        
        return createTabulatedFunction(points);
    }

    public static TabulatedFunction inputTabulatedFunction(InputStream in, Class<? extends TabulatedFunction> clazz) throws IOException {
        DataInputStream dataIn = new DataInputStream(in);
        
        int pointsCount = dataIn.readInt();
        
        FunctionPoint[] points = new FunctionPoint[pointsCount];
        for (int i = 0; i < pointsCount; i++) {
            double x = dataIn.readDouble();
            double y = dataIn.readDouble();
            points[i] = new FunctionPoint(x, y);
        }
        
        return createTabulatedFunction(clazz, points);
    }

    public static TabulatedFunction readTabulatedFunction(Reader in, Class<? extends TabulatedFunction> clazz) throws IOException {
        StreamTokenizer tokenizer = new StreamTokenizer(in);
        
        tokenizer.nextToken();
        int pointsCount = (int) tokenizer.nval;
        
        FunctionPoint[] points = new FunctionPoint[pointsCount];
        for (int i = 0; i < pointsCount; i++) {
            tokenizer.nextToken();
            double x = tokenizer.nval;
            tokenizer.nextToken();
            double y = tokenizer.nval;
            
            if (Math.abs(y) < 1e-10) {
                y = 0.0;
            }
            
            points[i] = new FunctionPoint(x, y);
        }
        
        return createTabulatedFunction(clazz, points);
    }
}