package committee.nova.mods.avaritia.api.client.screen.coordinate;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

@Data
@Accessors(chain = true)
public class Coordinate implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public double x;
    public double y;
    public double width;
    public double height;

    public double u0;
    public double v0;
    public double uWidth;
    public double vHeight;

    // 手动添加getter方法以确保编译成功
    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public double getU0() { return u0; }
    public double getV0() { return v0; }
    public double getUWidth() { return uWidth; }
    public double getVHeight() { return vHeight; }

    // 手动添加setter方法以支持链式调用
    public Coordinate setX(double x) { this.x = x; return this; }
    public Coordinate setY(double y) { this.y = y; return this; }
    public Coordinate setWidth(double width) { this.width = width; return this; }
    public Coordinate setHeight(double height) { this.height = height; return this; }
    public Coordinate setU0(double u0) { this.u0 = u0; return this; }
    public Coordinate setV0(double v0) { this.v0 = v0; return this; }
    public Coordinate setUWidth(double uWidth) { this.uWidth = uWidth; return this; }
    public Coordinate setVHeight(double vHeight) { this.vHeight = vHeight; return this; }
}
