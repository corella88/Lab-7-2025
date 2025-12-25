package functions;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

public class FunctionPoint implements Serializable, Externalizable, Cloneable {
    private double x;
    private double y;
    
    private static final double EPSILON = 1e-10;
    
    private static final long serialVersionUID = 1L;

    public FunctionPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public FunctionPoint(FunctionPoint point) {
        this.x = point.x;
        this.y = point.y;
    }

    public FunctionPoint() {
        this(0.0, 0.0);
    }
    
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeDouble(x);
        out.writeDouble(y);
    }
    
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        x = in.readDouble();
        y = in.readDouble();
    }
    
    @Override
    public String toString() {
        return "(" + x + "; " + y + ")";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        FunctionPoint other = (FunctionPoint) obj;
        
        return Math.abs(x - other.x) < EPSILON && 
               Math.abs(y - other.y) < EPSILON;
    }
    
    @Override
    public int hashCode() {
        long xBits = Double.doubleToLongBits(x);
        long yBits = Double.doubleToLongBits(y);
        
        int xHash = (int)(xBits ^ (xBits >>> 32));
        int yHash = (int)(yBits ^ (yBits >>> 32));
        
        return xHash ^ yHash;
    }
    
    @Override
    public FunctionPoint clone() {
        try {
            return (FunctionPoint) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Клонирование не поддерживается", e);
        }
    }
}