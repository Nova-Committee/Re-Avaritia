package committee.nova.mods.avaritia.api.client.screen.component;

import com.mojang.blaze3d.platform.NativeImage;
import committee.nova.mods.avaritia.api.client.screen.coordinate.Coordinate;
import committee.nova.mods.avaritia.api.client.screen.coordinate.TexCoordinate;
import committee.nova.mods.avaritia.api.client.util.GuiUtils;
import committee.nova.mods.avaritia.api.client.util.TextureUtils;
import committee.nova.mods.avaritia.api.util.StringUtils;
import lombok.Data;
import lombok.experimental.Accessors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Consumer;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/12/25 20:27
 * @Description: 页面操作按钮 from <a href="https://github.com/TinyTsuki/SakuraSignIn_MC">...</a>
 */
@Data
@Accessors(chain = true)
@OnlyIn(Dist.CLIENT)
public class OperationButton {
    /**
     * 渲染辅助类：用于向自定义渲染函数传递上下文
     */
    public record RenderContext(GuiGraphics graphics, KeyEventManager keyManager, OperationButton button) {
    }

    /**
     * 按钮ID
     */
    private String id;

    /**
     * 自定义渲染函数
     */
    private Consumer<RenderContext> customRenderFunction;

    /**
     * 自定义弹出层渲染函数
     */
    private Runnable customPopupFunction = null;

    /**
     * 按钮材质资源
     */
    private ResourceLocation texture;
    /**
     * 按钮区域透明像素检测
     */
    private boolean transparentCheck;
    /**
     * 材质总宽度
     */
    private int textureWidth;
    /**
     * 材质总高度
     */
    private int textureHeight;
    /**
     * 基础坐标
     */
    private double baseX, baseY, scale = 1;
    /**
     * 按钮渲染坐标
     */
    private double x, y, width, height;
    /**
     * 操作标识
     */
    private int operation;
    /**
     * 按钮是否被按下, 是否悬浮
     */
    private boolean pressed, hovered;
    /**
     * 按钮材质UV: 默认
     */
    private double normalU, normalV, normalWidth, normalHeight;
    /**
     * 按钮材质UV: 悬浮
     */
    private double hoverU, hoverV, hoverWidth, hoverHeight;
    /**
     * 按钮材质UV: 点击
     */
    private double tapU, tapV, tapWidth, tapHeight;
    /**
     * 按钮背景颜色
     */
    private int normalBgColor, hoverBgColor, tapBgColor;
    /**
     * 按钮前景色
     */
    private int normalFgColor, hoverFgColor, tapFgColor;
    /**
     * 横向翻转或纵向翻转
     */
    private boolean flipHorizontal, flipVertical;
    /**
     * 旋转角度
     */
    private double rotatedAngle;
    /**
     * 抖动幅度
     */
    private double tremblingAmplitude;

    /**
     * 鼠标提示
     */
    private Text tooltip;
    /**
     * 提示文字是否仅按下按键时显示
     */
    private String keyNames;

    public OperationButton(int operation, Consumer<RenderContext> customRenderFunction) {
        this.operation = operation;
        this.customRenderFunction = customRenderFunction;
    }

    /**
     * @param operation 操作标识
     * @param resource  资源
     */
    public OperationButton(int operation, ResourceLocation resource) {
        this.operation = operation;
        this.texture = resource;
        this.transparentCheck = true;
    }

    /**
     * 设置按钮渲染坐标
     *
     * @param coordinate 渲染坐标
     */
    public OperationButton setCoordinate(Coordinate coordinate) {
        this.x = coordinate.getX();
        this.y = coordinate.getY();
        this.width = coordinate.getWidth();
        this.height = coordinate.getHeight();
        return this;
    }

    /**
     * 设置按钮默认材质UV
     *
     * @param normal 默认材质UV
     */
    public OperationButton setNormal(Coordinate normal) {
        this.normalU = normal.getU0();
        this.normalV = normal.getV0();
        this.normalWidth = normal.getUWidth();
        this.normalHeight = normal.getVHeight();
        return this;
    }

    /**
     * 设置按钮悬浮材质UV
     *
     * @param hover 悬浮材质UV
     */
    public OperationButton setHover(Coordinate hover) {
        this.hoverU = hover.getU0();
        this.hoverV = hover.getV0();
        this.hoverWidth = hover.getUWidth();
        this.hoverHeight = hover.getVHeight();
        return this;
    }

    /**
     * 设置按钮点击材质UV
     *
     * @param tap 点击材质UV
     */
    public OperationButton setTap(Coordinate tap) {
        this.tapU = tap.getU0();
        this.tapV = tap.getV0();
        this.tapWidth = tap.getUWidth();
        this.tapHeight = tap.getVHeight();
        return this;
    }

    /**
     * 获取按钮渲染绝对坐标X
     */
    public double getRealX() {
        return this.baseX + (this.x * this.scale);
    }

    /**
     * 获取按钮渲染绝对坐标Y
     */
    public double getRealY() {
        return this.baseY + (this.y * this.scale);
    }

    /**
     * 获取按钮渲染绝对坐标宽度
     */
    public double getRealWidth() {
        return this.width * this.scale;
    }

    /**
     * 获取按钮渲染绝对坐标高度
     */
    public double getRealHeight() {
        return this.height * this.scale;
    }

    /**
     * 获取经过旋转/翻转变换后鼠标的X绝对(吗?)坐标
     *
     * @param mouseX 鼠标X坐标
     * @param mouseY 鼠标Y坐标
     */
    public double getRealMouseX(double mouseX, double mouseY) {
        // 矩形范围定义
        double startX = getRealX(); // 起始 X
        double startY = getRealY(); // 起始 Y
        double width = getRealWidth();  // 范围宽度
        double height = getRealHeight(); // 范围高度

        double realX = mouseX;

        // 顺时针旋转
        if (this.getRotatedAngle() == 90) {
            realX = startX + (mouseY - startY);
        } else if (this.getRotatedAngle() == 180) {
            realX = startX + (width - (mouseX - startX));
        } else if (this.getRotatedAngle() == 270) {
            realX = startX + (height - (mouseY - startY));
        }

        // 水平翻转
        if (flipHorizontal) {
            realX = startX + (width - (realX - startX));
        }

        return realX;
    }

    /**
     * 获取经过旋转/翻转变换后鼠标的Y绝对(吗?)坐标
     *
     * @param mouseX 鼠标X坐标
     * @param mouseY 鼠标Y坐标
     */
    public double getRealMouseY(double mouseX, double mouseY) {
        // 矩形范围定义
        double startX = getRealX(); // 起始 X
        double startY = getRealY(); // 起始 Y
        double width = getRealWidth();  // 范围宽度
        double height = getRealHeight(); // 范围高度

        double realY = mouseY;

        // 顺时针旋转
        if (this.getRotatedAngle() == 90) {
            realY = startY + (width - (mouseX - startX));
        } else if (this.getRotatedAngle() == 180) {
            realY = startY + (height - (mouseY - startY));
        } else if (this.getRotatedAngle() == 270) {
            realY = startY + (mouseX - startX);
        }

        // 垂直翻转
        if (flipVertical) {
            realY = startY + (height - (realY - startY));
        }

        return realY;
    }

    /**
     * 判断鼠标是否在按钮内
     */
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= this.getRealX() && mouseX <= this.getRealX() + this.getRealWidth() && mouseY >= this.getRealY() && mouseY <= this.getRealY() + this.getRealHeight();
    }

    /**
     * 检测点击是否有效（包含透明像素检测）
     */
    public boolean isMouseOverEx(double mouseX, double mouseY) {
        // 鼠标不在按钮范围内
        if (!isMouseOver(mouseX, mouseY)) {
            return false;
        } else if (texture == null) {
            return true;
        } else if (!transparentCheck) {
            return true;
        }
        // 映射到纹理的局部坐标
        int textureX = (int) (((this.getRealMouseX(mouseX, mouseY) - this.getRealX()) / (this.getRealWidth() / this.getHoverWidth())) + hoverU);
        int textureY = (int) (((this.getRealMouseY(mouseX, mouseY) - this.getRealY()) / (this.getRealHeight() / this.getHoverHeight())) + hoverV);
        // 检查透明像素
        NativeImage image = TextureUtils.getTextureImage(texture);
        if (image != null) {
            int pixel = image.getPixelRGBA(textureX, textureY);
            int alpha = (pixel >> 24) & 0xFF;
            return alpha > 0;
        }
        return true;
    }

    public double getU() {
        if (hovered && pressed) {
            return tapU;
        } else if (hovered) {
            return hoverU;
        } else {
            return normalU;
        }
    }

    public double getV() {
        if (hovered && pressed) {
            return tapV;
        } else if (hovered) {
            return hoverV;
        } else {
            return normalV;
        }
    }

    public double getUWidth() {
        if (hovered && pressed) {
            return tapWidth;
        } else if (hovered) {
            return hoverWidth;
        } else {
            return normalWidth;
        }
    }

    public double getVHeight() {
        if (hovered && pressed) {
            return tapHeight;
        } else if (hovered) {
            return hoverHeight;
        } else {
            return normalHeight;
        }
    }

    public int getBackgroundColor() {
        if (hovered && pressed) {
            return tapBgColor;
        } else if (hovered) {
            return hoverBgColor;
        } else {
            return normalBgColor;
        }
    }

    private int getForegroundColor() {
        if (hovered && pressed) {
            return tapFgColor;
        } else if (hovered) {
            return hoverFgColor;
        } else {
            return normalFgColor;
        }
    }

    public OperationButton setTooltip(String content) {
        this.tooltip = Text.literal(content);
        return this;
    }

    public OperationButton setTooltip(Text text) {
        this.tooltip = text;
        return this;
    }

    /**
     * 绘制按钮
     */
    public void render(GuiGraphics graphics, KeyEventManager keyManager) {
        this.render(graphics, false, keyManager);
    }

    /**
     * 绘制按钮
     *
     * @param renderPopup 是否绘制弹出层提示
     */
    public void render(GuiGraphics graphics, boolean renderPopup, KeyEventManager keyManager) {
        if (customRenderFunction != null) {
            // 使用自定义渲染逻辑
            customRenderFunction.accept(new RenderContext(graphics, keyManager, this));
        } else {
            TexCoordinate texCoordinate = new TexCoordinate().setTotalWidth(this.textureWidth).setTotalHeight(this.textureHeight);
            Coordinate coordinate = new Coordinate().setX(this.x).setY(this.y).setWidth(this.width).setHeight(this.height)
                    .setU0(getU()).setV0(getV()).setUWidth(getUWidth()).setVHeight(getVHeight());
            // 绘制背景颜色
            int bgColor = this.getBackgroundColor();
            if (bgColor != 0) {
                GuiUtils.fill(graphics, (int) (baseX + coordinate.getX() * scale), (int) (baseY + coordinate.getY() * scale), (int) (coordinate.getWidth() * scale), (int) (coordinate.getHeight() * scale), bgColor);
            }
            // 绘制纹理
            if (this.isHovered() && this.getTremblingAmplitude() > 0) {
                GuiUtils.renderTremblingTexture(graphics, this.texture, texCoordinate, coordinate, this.baseX, this.baseY, this.scale, true, this.getTremblingAmplitude());
            } else {
                GuiUtils.renderRotatedTexture(graphics, this.texture, texCoordinate, coordinate, this.baseX, this.baseY, this.scale, this.rotatedAngle, this.flipHorizontal, this.flipVertical);
            }
            // 绘制前景颜色
            int fgColor = this.getForegroundColor();
            if (fgColor != 0) {
                GuiUtils.fill(graphics, (int) (baseX + coordinate.getX() * scale), (int) (baseY + coordinate.getY() * scale), (int) (coordinate.getWidth() * scale), (int) (coordinate.getHeight() * scale), fgColor);
            }
        }
        if (renderPopup) {
            this.renderPopup(graphics, null, keyManager);
        }
    }

    /**
     * 绘制弹出层
     */
    public void renderPopup(GuiGraphics graphics, KeyEventManager keyManager) {
        this.renderPopup(graphics, null, keyManager);
    }

    /**
     * 绘制弹出层
     */
    public void renderPopup(GuiGraphics graphics, Font font, KeyEventManager keyManager) {
        // 绘制提示
        if (StringUtils.isNullOrEmptyEx(this.keyNames) || keyManager.isKeyPressed(this.keyNames)) {
            if (this.isHovered()) {
                if (customPopupFunction != null) {
                    customPopupFunction.run();
                } else if (tooltip != null && StringUtils.isNotNullOrEmpty(tooltip.getContent()) && Minecraft.getInstance().screen != null) {
                    if (font == null) font = Minecraft.getInstance().font;
                    GuiUtils.drawPopupMessage(tooltip.setGraphics(graphics).setFont(font), (int) keyManager.getMouseX(), (int) keyManager.getMouseY(), Minecraft.getInstance().screen.width, Minecraft.getInstance().screen.height);
                }
            }
        }
    }
}
