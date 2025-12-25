import functions.*;
import functions.basic.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Лабораторная работа №7 ===");
        
        // Тест 1: Итераторы
        System.out.println("1. Тестирование итераторов:");
        
        // Массивная функция
        TabulatedFunction arrayFunc = new ArrayTabulatedFunction(0, 10, 4);
        System.out.println("\nМассивная функция (4 точки от 0 до 10):");
        for (FunctionPoint p : arrayFunc) {
            System.out.println("  Точка: " + p);
        }
        
        // Списковая функция
        TabulatedFunction listFunc = new LinkedListTabulatedFunction(0, 10, 4);
        System.out.println("\nСписковая функция (4 точки от 0 до 10):");
        for (FunctionPoint p : listFunc) {
            System.out.println("  Точка: " + p);
        }
        
        // Тест 2: Фабричный метод
        System.out.println("\n\n2. Тестирование фабричного метода:");
        
        Function sin = new Sin();
        TabulatedFunction tf;
        
        System.out.println("\nИзначальная фабрика (ArrayTabulatedFunction):");
        tf = TabulatedFunctions.tabulate(sin, 0, Math.PI, 4);
        System.out.println("  Создана функция типа: " + tf.getClass().getSimpleName());
        
        System.out.println("\nМеняем фабрику на LinkedListTabulatedFunction:");
        TabulatedFunctions.setTabulatedFunctionFactory(
            new LinkedListTabulatedFunction.LinkedListTabulatedFunctionFactory());
        tf = TabulatedFunctions.tabulate(sin, 0, Math.PI, 4);
        System.out.println("  Теперь функция типа: " + tf.getClass().getSimpleName());
        
        System.out.println("\nВозвращаем фабрику обратно:");
        TabulatedFunctions.setTabulatedFunctionFactory(
            new ArrayTabulatedFunction.ArrayTabulatedFunctionFactory());
        tf = TabulatedFunctions.tabulate(sin, 0, Math.PI, 4);
        System.out.println("  Функция типа: " + tf.getClass().getSimpleName());
        
        // Тест 3: Рефлексия
        System.out.println("\n\n3. Тестирование рефлексии:");
        
        System.out.println("\na) Создание через рефлексию (ArrayTabulatedFunction):");
        TabulatedFunction f1 = TabulatedFunctions.createTabulatedFunction(
            ArrayTabulatedFunction.class, 0, 5, 3);
        System.out.println("  Создана: " + f1.getClass().getSimpleName());
        System.out.println("  Содержимое: " + f1);
        
        System.out.println("\nb) Создание через рефлексию (LinkedListTabulatedFunction):");
        TabulatedFunction f2 = TabulatedFunctions.createTabulatedFunction(
            LinkedListTabulatedFunction.class, 0, 5, new double[] {0, 2.5, 5});
        System.out.println("  Создана: " + f2.getClass().getSimpleName());
        System.out.println("  Содержимое: " + f2);
        
        System.out.println("\nc) Табулирование через рефлексию:");
        TabulatedFunction f3 = TabulatedFunctions.tabulate(
            LinkedListTabulatedFunction.class, new Cos(), 0, Math.PI, 3);
        System.out.println("  Тип: " + f3.getClass().getSimpleName());
        System.out.println("  Значения Cos от 0 до PI:");
        for (FunctionPoint p : f3) {
            System.out.println("    x = " + String.format("%.2f", p.getX()) + 
                             ", y = " + String.format("%.4f", p.getY()));
        }
    }
}