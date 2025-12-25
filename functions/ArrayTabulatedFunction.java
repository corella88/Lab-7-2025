package functions;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ArrayTabulatedFunction implements TabulatedFunction, Serializable, Externalizable {
    private FunctionPoint[] points;
    private int pointsCount;

    private static final double EPSILON = 1e-10;
    
    private static final long serialVersionUID = 1L;

    // Фабрика для ArrayTabulatedFunction
    public static class ArrayTabulatedFunctionFactory implements TabulatedFunctionFactory {
        @Override
        public TabulatedFunction createTabulatedFunction(double leftX, double rightX, int pointsCount) {
            return new ArrayTabulatedFunction(leftX, rightX, pointsCount);
        }
        
        @Override
        public TabulatedFunction createTabulatedFunction(double leftX, double rightX, double[] values) {
            return new ArrayTabulatedFunction(leftX, rightX, values);
        }
        
        @Override
        public TabulatedFunction createTabulatedFunction(FunctionPoint[] points) {
            return new ArrayTabulatedFunction(points);
        }
    }

    public ArrayTabulatedFunction() {
        this.pointsCount = 0;
        this.points = new FunctionPoint[10];
    }

    private boolean equals(double a, double b) {
        return Math.abs(a - b) < EPSILON;
    }

    private boolean lessOrEqual(double a, double b) {
        return a < b || equals(a, b);
    }

    public ArrayTabulatedFunction(double leftX, double rightX, int pointsCount) {
        if (leftX >= rightX) {
            throw new IllegalArgumentException("Левая граница должна быть меньше правой");
        }

        if (pointsCount < 2) {
            throw new IllegalArgumentException("Количество точек должно быть не менее 2");
        }
        
        this.pointsCount = pointsCount;
        this.points = new FunctionPoint[pointsCount + 10];

        double step = (rightX - leftX) / (pointsCount - 1);

        for (int i = 0; i < pointsCount; i++) {
            double x = leftX + i * step;
            points[i] = new FunctionPoint(x, 0.0);
        }
    }

    public ArrayTabulatedFunction(double leftX, double rightX, double[] values) {
        if (leftX >= rightX) {
            throw new IllegalArgumentException("Левая граница должна быть меньше правой");
        }

        if (values.length < 2) {
            throw new IllegalArgumentException("Количество точек должно быть не менее 2");
        }
        
        this.pointsCount = values.length;
        this.points = new FunctionPoint[pointsCount + 10];

        double step = (rightX - leftX) / (pointsCount - 1);

        for (int i = 0; i < pointsCount; i++) {
            double x = leftX + i * step;
            points[i] = new FunctionPoint(x, values[i]);
        }
    }

    public ArrayTabulatedFunction(FunctionPoint[] points) {
        if (points.length < 2) {
            throw new IllegalArgumentException("Количество точек должно быть не менее 2");
        }
        
        for (int i = 1; i < points.length; i++) {
            if (points[i].getX() <= points[i - 1].getX()) {
                throw new IllegalArgumentException("Точки должны быть строго упорядочены по возрастанию X");
            }
        }
        
        this.pointsCount = points.length;
        this.points = new FunctionPoint[pointsCount + 10];
        
        for (int i = 0; i < pointsCount; i++) {
            this.points[i] = new FunctionPoint(points[i]);
        }
    }

    public double getLeftDomainBorder() {
        return points[0].getX();
    }

    public double getRightDomainBorder() {
        return points[pointsCount - 1].getX();
    }

    public double getFunctionValue(double x) {
        if (x < getLeftDomainBorder() || x > getRightDomainBorder()) {
            return Double.NaN;
        }

        for (int i = 0; i < pointsCount - 1; i++) {
            double x1 = points[i].getX();
            double x2 = points[i + 1].getX();

            if (lessOrEqual(x1, x) && lessOrEqual(x, x2)) {
                if (equals(x, x1)) {
                    return points[i].getY();
                }
                if (equals(x, x2)) {
                    return points[i + 1].getY();
                }

                double y1 = points[i].getY();
                double y2 = points[i + 1].getY();
                return y1 + (y2 - y1) * (x - x1) / (x2 - x1);
            }
        }

        return Double.NaN;
    }

    public int getPointsCount() {
        return pointsCount;
    }

    public FunctionPoint getPoint(int index) {
        if (index < 0 || index >= pointsCount) {
            throw new FunctionPointIndexOutOfBoundsException("Индекс " + index + " вне границ [0, " + (pointsCount - 1) + "]");
        }

        return new FunctionPoint(points[index]);
    }

    public void setPoint(int index, FunctionPoint point) throws InappropriateFunctionPointException {
        if (index < 0 || index >= pointsCount) {
            throw new FunctionPointIndexOutOfBoundsException("Индекс " + index + " вне границ [0, " + (pointsCount - 1) + "]");
        }

        if (index > 0 && lessOrEqual(point.getX(), points[index - 1].getX())) {
            throw new InappropriateFunctionPointException("X координата должна быть больше предыдущей точки");
        }

        if (index < pointsCount - 1 && lessOrEqual(points[index + 1].getX(), point.getX())) {
            throw new InappropriateFunctionPointException("X координата должна быть меньше следующей точки");
        }

        points[index] = new FunctionPoint(point);
    }

    public double getPointX(int index) {
        if (index < 0 || index >= pointsCount) {
            throw new FunctionPointIndexOutOfBoundsException("Индекс " + index + " вне границ [0, " + (pointsCount - 1) + "]");
        }

        return points[index].getX();
    }

    public void setPointX(int index, double x) throws InappropriateFunctionPointException {
        if (index < 0 || index >= pointsCount) {
            throw new FunctionPointIndexOutOfBoundsException("Индекс " + index + " вне границ [0, " + (pointsCount - 1) + "]");
        }

        if (index > 0 && lessOrEqual(x, points[index - 1].getX())) {
            throw new InappropriateFunctionPointException("X координата должна быть больше предыдущей точки");
        }

        if (index < pointsCount - 1 && lessOrEqual(points[index + 1].getX(), x)) {
            throw new InappropriateFunctionPointException("X координата должна быть меньше следующей точки");
        }

        double oldY = points[index].getY();
        points[index] = new FunctionPoint(x, oldY);
    }

    public double getPointY(int index) {
        if (index < 0 || index >= pointsCount) {
            throw new FunctionPointIndexOutOfBoundsException("Индекс " + index + " вне границ [0, " + (pointsCount - 1) + "]");
        }

        return points[index].getY();
    }

    public void setPointY(int index, double y) {
        if (index < 0 || index >= pointsCount) {
           throw new FunctionPointIndexOutOfBoundsException("Индекс " + index + " вне границ [0, " + (pointsCount - 1) + "]"); 
        }

        double oldX = points[index].getX();
        points[index] = new FunctionPoint(oldX, y);
    }

    public void deletePoint(int index) {
        if (pointsCount < 3) {
            throw new IllegalStateException("Нельзя удалить точку: минимальное количество точек - 3");
        }

        if (index < 0 || index >= pointsCount) {
            throw new FunctionPointIndexOutOfBoundsException("Индекс " + index + " вне границ [0, " + (pointsCount - 1) + "]");
        }

        for (int i = index; i < pointsCount - 1; i++) {
            points[i] = points[i + 1];
        }

        pointsCount--;
        points[pointsCount] = null;
    }

    public void addPoint(FunctionPoint point) throws InappropriateFunctionPointException {
        for (int i = 0; i < pointsCount; i++) {
            if (equals(points[i].getX(), point.getX())) {
                throw new InappropriateFunctionPointException("Точка с X=" + point.getX() + " уже существует");
            }
        }

        FunctionPoint newPoint = new FunctionPoint(point);

        int insertIndex = 0;
        while (insertIndex < pointsCount && lessOrEqual(points[insertIndex].getX(), newPoint.getX())) {
            insertIndex++;
        }

        if (pointsCount == points.length) {
            int newCapacity = points.length + 10;
            FunctionPoint[] newArray = new FunctionPoint[newCapacity];
            for (int i = 0; i < pointsCount; i++) {
                newArray[i] = points[i];
            }
            points = newArray;
        }
        
        if (pointsCount - insertIndex > 0) {
            for (int i = pointsCount; i > insertIndex; i--) {
                points[i] = points[i - 1];
            }
        }

        points[insertIndex] = newPoint;
        pointsCount++;
    }

    // Итератор
    @Override
    public Iterator<FunctionPoint> iterator() {
        return new Iterator<FunctionPoint>() {
            private int currentIndex = 0;
            
            @Override
            public boolean hasNext() {
                return currentIndex < pointsCount;
            }
            
            @Override
            public FunctionPoint next() {
                if (!hasNext()) {
                    throw new NoSuchElementException("Нет следующего элемента");
                }
                return new FunctionPoint(points[currentIndex++]);
            }
            
            @Override
            public void remove() {
                throw new UnsupportedOperationException("Удаление не поддерживается");
            }
        };
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(pointsCount);
        for (int i = 0; i < pointsCount; i++) {
            out.writeDouble(points[i].getX());
            out.writeDouble(points[i].getY());
        }
    }
    
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        pointsCount = in.readInt();
        points = new FunctionPoint[pointsCount + 10];
        for (int i = 0; i < pointsCount; i++) {
            double x = in.readDouble();
            double y = in.readDouble();
            points[i] = new FunctionPoint(x, y);
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        
        for (int i = 0; i < pointsCount; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(points[i].toString());
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
            
            if (obj instanceof ArrayTabulatedFunction) {
                ArrayTabulatedFunction otherArray = (ArrayTabulatedFunction) obj;
                
                for (int i = 0; i < pointsCount; i++) {
                    if (!points[i].equals(otherArray.points[i])) {
                        return false;
                    }
                }
            } else {
                for (int i = 0; i < pointsCount; i++) {
                    if (!this.getPoint(i).equals(other.getPoint(i))) {
                        return false;
                    }
                }
            }
            
            return true;
        }
        
        return false;
    }
    
    @Override
    public int hashCode() {
        int hash = pointsCount;
        
        for (int i = 0; i < pointsCount; i++) {
            hash ^= points[i].hashCode();
        }
        
        return hash;
    }
    
    @Override
    public ArrayTabulatedFunction clone() {
        try {
            ArrayTabulatedFunction cloned = (ArrayTabulatedFunction) super.clone();
            
            cloned.points = new FunctionPoint[this.points.length];
            for (int i = 0; i < this.pointsCount; i++) {
                if (this.points[i] != null) {
                    cloned.points[i] = this.points[i].clone();
                }
            }
            
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Клонирование не поддерживается", e);
        }
    }
}