package functions;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class LinkedListTabulatedFunction implements TabulatedFunction, Serializable {
    private static class FunctionNode implements Serializable {
        FunctionPoint point;
        FunctionNode prev;
        FunctionNode next;
        
        private static final long serialVersionUID = 1L;
        
        FunctionNode(FunctionPoint point) {
            this.point = point;
        }
    }

    private FunctionNode head;
    private FunctionNode lastAccessedNode;
    private int lastAccessedIndex;
    private int pointsCount;
    
    private static final double EPSILON = 1e-10;
    
    private static final long serialVersionUID = 1L;
    
    // Фабрика для LinkedListTabulatedFunction
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
    
    private boolean equals(double a, double b) {
        return Math.abs(a - b) < EPSILON;
    }

    private boolean lessOrEqual(double a, double b) {
        return a < b || equals(a, b);
    }

    private void initializeList() {
        head = new FunctionNode(null);
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
            current = head.next;
            for (int i = 0; i < index; i++) {
                current = current.next;
            }
        }
    
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
            currentNode = head;
        } else {
            currentNode = getNodeByIndex(index);
        }

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
        return head.next.point.getX();
    }

    public double getRightDomainBorder() {
        return head.prev.point.getX();
    }

    public double getFunctionValue(double x) {
        if (x < getLeftDomainBorder() || x > getRightDomainBorder()) {
            return Double.NaN;
        }

        FunctionNode current = head.next;
        while (current != head && current.next != head) {
            if (lessOrEqual(current.point.getX(), x) && lessOrEqual(x, current.next.point.getX())) {
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
        FunctionNode current = head.next;
        while (current != head) {
            if (equals(current.point.getX(), point.getX())) {
                throw new InappropriateFunctionPointException("Точка с X=" + point.getX() + " уже существует");
            }

            current = current.next;
        }

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

    // Итератор
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
                    throw new NoSuchElementException("Нет следующего элемента");
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
            
            if (obj instanceof LinkedListTabulatedFunction) {
                LinkedListTabulatedFunction otherList = (LinkedListTabulatedFunction) obj;
                
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
            
            cloned.head = new FunctionNode(null);
            cloned.head.prev = cloned.head;
            cloned.head.next = cloned.head;
            cloned.pointsCount = 0;
            cloned.lastAccessedNode = cloned.head;
            cloned.lastAccessedIndex = -1;
            
            FunctionNode current = this.head.next;
            while (current != this.head) {
                FunctionNode newNode = new FunctionNode(current.point.clone());
                
                newNode.prev = cloned.head.prev;
                newNode.next = cloned.head;
                cloned.head.prev.next = newNode;
                cloned.head.prev = newNode;
                
                cloned.pointsCount++;
                current = current.next;
            }
            
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Клонирование не поддерживается", e);
        }
    }
}