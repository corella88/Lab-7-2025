import functions.*;
import functions.basic.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== ЛАБОРАТОРНАЯ РАБОТА №7 ===");
        
        System.out.println("\n--- ЗАДАНИЕ 1: Итератор ---");
        testIterator();
        
        System.out.println("\n--- ЗАДАНИЕ 2: Фабричный метод ---");
        testFactoryMethod();
        
        System.out.println("\n--- ЗАДАНИЕ 3: Рефлексия ---");
        testReflection();
    }
    
    private static void testIterator() {
        FunctionPoint[] points = {
            new FunctionPoint(0, 0),
            new FunctionPoint(1, 1),
            new FunctionPoint(2, 4),
            new FunctionPoint(3, 9)
        };
        
        System.out.println("ArrayTabulatedFunction:");
        TabulatedFunction arrayFunc = new ArrayTabulatedFunction(points);
        for (FunctionPoint p : arrayFunc) {
            System.out.println(p);
        }
        
        System.out.println("\nLinkedListTabulatedFunction:");
        TabulatedFunction linkedFunc = new LinkedListTabulatedFunction(points);
        for (FunctionPoint p : linkedFunc) {
            System.out.println(p);
        }
    }
    
    private static void testFactoryMethod() {
        Function f = new Cos();
        TabulatedFunction tf;
        
        tf = TabulatedFunctions.tabulate(f, 0, Math.PI, 11);
        System.out.println("Фабрика по умолчанию: " + tf.getClass());
        
        TabulatedFunctions.setTabulatedFunctionFactory(
            new LinkedListTabulatedFunction.LinkedListTabulatedFunctionFactory());
        tf = TabulatedFunctions.tabulate(f, 0, Math.PI, 11);
        System.out.println("LinkedList фабрика: " + tf.getClass());
        
        TabulatedFunctions.setTabulatedFunctionFactory(
            new ArrayTabulatedFunction.ArrayTabulatedFunctionFactory());
        tf = TabulatedFunctions.tabulate(f, 0, Math.PI, 11);
        System.out.println("Array фабрика: " + tf.getClass());
    }
    
    private static void testReflection() {
        TabulatedFunction f;
        
        System.out.println("1. Создание ArrayTabulatedFunction через рефлексию:");
        f = TabulatedFunctions.createTabulatedFunction(
            ArrayTabulatedFunction.class, 0, 10, 3);
        System.out.println("Класс: " + f.getClass());
        System.out.println("Функция: " + f);
        
        System.out.println("\n2. Создание ArrayTabulatedFunction с массивом значений:");
        f = TabulatedFunctions.createTabulatedFunction(
            ArrayTabulatedFunction.class, 0, 10, new double[] {0, 10, 20});
        System.out.println("Класс: " + f.getClass());
        System.out.println("Функция: " + f);
        
        System.out.println("\n3. Создание LinkedListTabulatedFunction через рефлексию:");
        f = TabulatedFunctions.createTabulatedFunction(
            LinkedListTabulatedFunction.class, 
            new FunctionPoint[] {
                new FunctionPoint(0, 0),
                new FunctionPoint(5, 25),
                new FunctionPoint(10, 100)
            }
        );
        System.out.println("Класс: " + f.getClass());
        System.out.println("Функция: " + f);
        
        System.out.println("\n4. Табулирование через рефлексию:");
        f = TabulatedFunctions.tabulate(
            LinkedListTabulatedFunction.class, new Sin(), 0, Math.PI, 11);
        System.out.println("Класс: " + f.getClass());
        System.out.println("Количество точек: " + f.getPointsCount());
        
        System.out.println("Первые 5 точек:");
        int count = 0;
        for (FunctionPoint p : f) {
            if (count++ >= 5) break;
            System.out.println(p);
        }
    }
}