package functions;

import java.io.Serializable;
import java.util.Iterator;

public class LinkedListTabulatedFunction implements TabulatedFunction, Serializable {
    // Внутренний класс для узла списка
    private static class FunctionNode implements Serializable {
        FunctionPoint point;
        FunctionNode prev;
        FunctionNode next;
        
        private static final long serialVersionUID = 1L;
        
        // ОСНОВНОЙ конструктор
        FunctionNode(FunctionPoint point) {
            this.point = point;
        }
        
        // Копирующий конструктор для клонирования (не использовать с null!)
        FunctionNode(FunctionNode other, boolean forCloning) {
            this.point = other.point.clone();
            this.prev = null;
            this.next = null;
        }
    }

    private FunctionNode head; // Голова списка (не содержит данных)
    private FunctionNode lastAccessedNode; // Для оптимизацииg
    private int lastAccessedIndex;
    private int pointsCount;
    
    private static final double EPSILON = 1e-10;
    
    private static final long serialVersionUID = 1L;
    
    // Вспомогательные методы для сравнения
    private boolean equals(double a, double b) {
        return Math.abs(a - b) < EPSILON;
    }

    private boolean lessOrEqual(double a, double b) {
        return a < b || equals(a, b);
    }

    private void initializeList() {
        head = new FunctionNode(null);  // ← Здесь передается null, поэтому нужен основной конструктор
        head.prev = head;
        head.next = head;
        pointsCount = 0;
        lastAccessedNode = head;
        lastAccessedIndex = -1;
    }

    public LinkedListTabulatedFunction(double leftX, double rightX, int pointsCount) {
        if (leftX >= rightX) {
            throw new IllegalArgumentException("Левая граница должна быть меньше правой");
        }
        if (pointsCount < 2) {
            throw new IllegalArgumentException("Количество точек должно быть не менее 2");
        }

        initializeList();
        double step = (rightX - leftX) / (pointsCount - 1);
        for (int i = 0; i < pointsCount; i++) {
            double x = leftX + i * step;
            addNodeToTail().point = new FunctionPoint(x, 0);
        }
    }
    
    public LinkedListTabulatedFunction(double leftX, double rightX, double[] values) {
        if (leftX >= rightX) {
            throw new IllegalArgumentException("Левая граница должна быть меньше правой");
        }
        if (values.length < 2) {
            throw new IllegalArgumentException("Количество точек должно быть не менее 2");
        }
        
        initializeList();
        double step = (rightX - leftX) / (values.length - 1);
        for (int i = 0; i < values.length; i++) {
            double x = leftX + i * step;
            addNodeToTail().point = new FunctionPoint(x, values[i]);
        }
    }

    public LinkedListTabulatedFunction(FunctionPoint[] points) {
        if (points.length < 2) {
            throw new IllegalArgumentException("Количество точек должно быть не менее 2");
        }
        
        for (int i = 1; i < points.length; i++) {
            if (points[i].getX() <= points[i - 1].getX()) {
                throw new IllegalArgumentException("Точки должны быть строго упорядочены по возрастанию X");
            }
        }
        
        initializeList();
        for (FunctionPoint point : points) {
            addNodeToTail().point = new FunctionPoint(point);
        }
    }

    private FunctionNode getNodeByIndex(int index) {
        if (index < 0 || index >= pointsCount) {
            throw new FunctionPointIndexOutOfBoundsException("Индекс " + index + " вне границ [0, " + (pointsCount - 1) + "]");
        }

        // Оптимизация: начинаем с последнего доступного узла если это ближе
        FunctionNode current;
        if (lastAccessedIndex != -1 && Math.abs(index - lastAccessedIndex) < Math.min(index, pointsCount - index)) {
            current = lastAccessedNode;
            int currentIndex = lastAccessedIndex;
        
            if (index > currentIndex) {
                for (int i = currentIndex; i < index; i++) {
                    current = current.next;
                }
            } else {
                for (int i = currentIndex; i > index; i--) {
                    current = current.prev;
                }
            }
        } else {
            // Иначе начинаем с начала
            current = head.next;
            for (int i = 0; i < index; i++) {
                current = current.next;
            }
        }
    
        // Сохраняем для следующего вызова
        lastAccessedNode = current;
        lastAccessedIndex = index;

        return current;
    }

    private FunctionNode addNodeByIndex(int index) {
        if (index < 0 || index > pointsCount) {
            throw new FunctionPointIndexOutOfBoundsException("Индекс: " + index);
        }

        FunctionNode newNode = new FunctionNode(new FunctionPoint(0, 0));
        FunctionNode currentNode;

        if (index == pointsCount) {
            currentNode = head;  // вставка в конец
        } else {
            currentNode = getNodeByIndex(index);
        }

        // Вставляем newNode между currentNode.prev и currentNode
        newNode.prev = currentNode.prev;
        newNode.next = currentNode;
        currentNode.prev.next = newNode;
        currentNode.prev = newNode;
    
        pointsCount++;
        
        lastAccessedNode = newNode;
        lastAccessedIndex = index;

        return newNode;
    }

    private FunctionNode deleteNodeByIndex(int index) {
        if (pointsCount < 3) {
            throw new IllegalStateException("Нельзя удалить точку: минимальное количество точек - 3");
        }

        FunctionNode nodeToDelete = getNodeByIndex(index);
    
        // Исключаем nodeToDelete из списка
        nodeToDelete.prev.next = nodeToDelete.next;
        nodeToDelete.next.prev = nodeToDelete.prev;
    
        pointsCount--;

        if (lastAccessedIndex == index) {
            lastAccessedNode = head;
            lastAccessedIndex = -1;
        } else if (lastAccessedIndex > index) {
            lastAccessedIndex--;
        }

        return nodeToDelete;
    }

    public double getLeftDomainBorder() {
        return head.next.point.getX();  // первая реальная точка после головы
    }

    public double getRightDomainBorder() {
        return head.prev.point.getX();  // последняя реальная точка перед головой
    }

    public double getFunctionValue(double x) {
        if (x < getLeftDomainBorder() || x > getRightDomainBorder()) {
            return Double.NaN;
        }

        // Ищем отрезок, содержащий x
        FunctionNode current = head.next;
        while (current != head && current.next != head) {
            if (lessOrEqual(current.point.getX(), x) && lessOrEqual(x, current.next.point.getX())) {
                // Линейная интерполяция
                double k = (current.next.point.getY() - current.point.getY()) / (current.next.point.getX() - current.point.getX());
                return current.point.getY() + k * (x - current.point.getX());
            }

            current = current.next;
        }

        return Double.NaN;
    }

    public int getPointsCount() {
        return pointsCount;
    }

    public FunctionPoint getPoint(int index) {
        return new FunctionPoint(getNodeByIndex(index).point);
    }

    public void setPoint(int index, FunctionPoint point) throws InappropriateFunctionPointException {
        FunctionNode node = getNodeByIndex(index);

        // Проверка порядка точек
        if (index > 0 && lessOrEqual(point.getX(), node.prev.point.getX())) {
            throw new InappropriateFunctionPointException("X координата должна быть больше предыдущей точки");
        }
        if (index < pointsCount - 1 && lessOrEqual(node.next.point.getX(), point.getX())) {
            throw new InappropriateFunctionPointException("X координата должна быть меньше следующей точки");
        }

        node.point = new FunctionPoint(point);
    }

    public double getPointX(int index) {
        return getNodeByIndex(index).point.getX();
    }

    public void setPointX(int index, double x) throws InappropriateFunctionPointException {
        FunctionNode node = getNodeByIndex(index);

        // Проверка порядка точек
        if (index > 0 && lessOrEqual(x, node.prev.point.getX())) {
            throw new InappropriateFunctionPointException("X координата должна быть больше предыдущей точки");
        }

        if (index < pointsCount - 1 && lessOrEqual(node.next.point.getX(), x)) {
            throw new InappropriateFunctionPointException("X координата должна быть меньше следующей точки");
        }

        double oldY = node.point.getY();
        node.point = new FunctionPoint(x, oldY);
    }

    public double getPointY(int index) {
        return getNodeByIndex(index).point.getY();
    }

    public void setPointY(int index, double y) {
        FunctionNode node = getNodeByIndex(index);
        double oldX = node.point.getX();
        node.point = new FunctionPoint(oldX, y);
    }

    public void deletePoint(int index) {
        deleteNodeByIndex(index);
    }

    public void addPoint(FunctionPoint point) throws InappropriateFunctionPointException {
        // Проверка на дублирование X
        FunctionNode current = head.next;
        while (current != head) {
            if (equals(current.point.getX(), point.getX())) {
                throw new InappropriateFunctionPointException("Точка с X=" + point.getX() + " уже существует");
            }

            current = current.next;
        }

        // Поиск позиции для вставки
        int insertIndex = 0;
        current = head.next;
        while (current != head && current.point.getX() < point.getX()) {
            current = current.next;
            insertIndex++;
        }

        FunctionNode newNode = addNodeByIndex(insertIndex);
        newNode.point = new FunctionPoint(point);
    }

    private FunctionNode addNodeToTail() {
        return addNodeByIndex(pointsCount);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        
        FunctionNode current = head.next;
        int count = 0;
        while (current != head) {
            if (count > 0) {
                sb.append(", ");
            }
            sb.append(current.point.toString());
            current = current.next;
            count++;
        }
        
        sb.append("}");
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        
        if (obj instanceof TabulatedFunction) {
            TabulatedFunction other = (TabulatedFunction) obj;
            
            if (this.getPointsCount() != other.getPointsCount()) {
                return false;
            }
            
            // Оптимизация для сравнения с другим LinkedListTabulatedFunction
            if (obj instanceof LinkedListTabulatedFunction) {
                LinkedListTabulatedFunction otherList = (LinkedListTabulatedFunction) obj;
                
                // Быстрое сравнение списков
                FunctionNode thisCurrent = this.head.next;
                FunctionNode otherCurrent = otherList.head.next;
                
                while (thisCurrent != this.head && otherCurrent != otherList.head) {
                    if (!thisCurrent.point.equals(otherCurrent.point)) {
                        return false;
                    }
                    thisCurrent = thisCurrent.next;
                    otherCurrent = otherCurrent.next;
                }
                
                return true;
            } else {
                // Общий случай для любого TabulatedFunction
                for (int i = 0; i < pointsCount; i++) {
                    if (!this.getPoint(i).equals(other.getPoint(i))) {
                        return false;
                    }
                }
                
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public int hashCode() {
        int hash = pointsCount;
        
        FunctionNode current = head.next;
        while (current != head) {
            hash ^= current.point.hashCode();
            current = current.next;
        }
        
        return hash;
    }
    

    @Override
    public LinkedListTabulatedFunction clone() {
        try {
            LinkedListTabulatedFunction cloned = (LinkedListTabulatedFunction) super.clone();
            
            // Создаем новую голову
            cloned.head = new FunctionNode(null);
            cloned.head.prev = cloned.head;
            cloned.head.next = cloned.head;
            cloned.pointsCount = 0;
            cloned.lastAccessedNode = cloned.head;
            cloned.lastAccessedIndex = -1;
            
            // Копируем все точки из текущего списка
            FunctionNode current = this.head.next;
            while (current != this.head) {
                // Создаем новый узел с копией точки
                FunctionNode newNode = new FunctionNode(current.point.clone());
                
                // Добавляем в конец нового списка
                newNode.prev = cloned.head.prev;
                newNode.next = cloned.head;
                cloned.head.prev.next = newNode;
                cloned.head.prev = newNode;
                
                cloned.pointsCount++;
                current = current.next;
            }
            
            return cloned;
        } catch (CloneNotSupportedException e) {
            // Это не должно происходить, так как класс реализует Cloneable
            throw new AssertionError("Клонирование не поддерживается", e);
        }
    }

    @Override
    public Iterator<FunctionPoint> iterator() {
        return new Iterator<FunctionPoint>() {
            private FunctionNode currentNode = head.next;
            
            @Override
            public boolean hasNext() {
                return currentNode != head;
            }
            
            @Override
            public FunctionPoint next() {
                if (!hasNext()) {
                    throw new java.util.NoSuchElementException("Нет следующего элемента");
                }
                FunctionPoint point = new FunctionPoint(currentNode.point);
                currentNode = currentNode.next;
                return point;
            }
            
            @Override
            public void remove() {
                throw new UnsupportedOperationException("Удаление не поддерживается");
            }
        };
    }

    public static class LinkedListTabulatedFunctionFactory implements TabulatedFunctionFactory {
        @Override
        public TabulatedFunction createTabulatedFunction(double leftX, double rightX, int pointsCount) {
            return new LinkedListTabulatedFunction(leftX, rightX, pointsCount);
        }
        
        @Override
        public TabulatedFunction createTabulatedFunction(double leftX, double rightX, double[] values) {
            return new LinkedListTabulatedFunction(leftX, rightX, values);
        }
        
        @Override
        public TabulatedFunction createTabulatedFunction(FunctionPoint[] points) {
            return new LinkedListTabulatedFunction(points);
        }
    }
}