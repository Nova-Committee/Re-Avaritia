package committee.nova.mods.avaritia.api.client.screen.component;

import committee.nova.mods.avaritia.api.client.screen.coordinate.Coordinate;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * @author  cnlimiter
 * @implNote 支持纹理循环切换的按钮组件
 */
public class CyclingTextureButton extends OperationButton {

    private final ResourceLocation textureAtlas;
    private List<Coordinate> normalCoords;
    private List<Coordinate> hoverCoords;
    private List<Coordinate> tapCoords;
    private int currentTextureIndex = 0;

    /**
     * 构造函数
     *
     * @param operation 操作标识
     * @param textureAtlas 包含所有状态的纹理图集
     */
    public CyclingTextureButton(int operation, ResourceLocation textureAtlas) {
        super(operation, textureAtlas);
        this.textureAtlas = textureAtlas;
        this.normalCoords = List.of();
        this.hoverCoords = List.of();
        this.tapCoords = List.of();
    }

    /**
     * 设置纹理坐标列表
     *
     * @param normals 默认状态纹理坐标列表
     * @param hovers 悬停状态纹理坐标列表
     * @param taps 点击状态纹理坐标列表
     * @return 当前实例
     */
    public CyclingTextureButton setTextureCoordinates(
            List<Coordinate> normals,
            List<Coordinate> hovers,
            List<Coordinate> taps) {
        this.normalCoords = normals != null ? normals : List.of();
        this.hoverCoords = hovers != null ? hovers : List.of();
        this.tapCoords = taps != null ? taps : List.of();
        return this;
    }

    /**
     * 切换到下一个纹理坐标
     */
    public void cycleTexture() {
        currentTextureIndex = (currentTextureIndex + 1) % Math.max(Math.max(
                        normalCoords.size(),
                        hoverCoords.size()),
                tapCoords.size()
        );
    }

    /**
     * 获取当前纹理索引
     *
     * @return 当前纹理索引
     */
    public int getCurrentTextureIndex() {
        return currentTextureIndex;
    }

    /**
     * 设置当前纹理索引
     *
     * @param index 纹理索引
     */
    public void setCurrentTextureIndex(int index) {
        int maxSize = Math.max(Math.max(
                        normalCoords.size(),
                        hoverCoords.size()),
                tapCoords.size()
        );
        this.currentTextureIndex = index % maxSize;
    }

    /**
     * 获取当前应该使用的纹理坐标
     *
     * @return 当前纹理坐标
     */
    private Coordinate getCurrentCoordinate(List<Coordinate> coords) {
        if (coords.isEmpty() || currentTextureIndex >= coords.size()) {
            return null;
        }
        return coords.get(currentTextureIndex);
    }

    @Override
    public void render(GuiGraphics graphics, boolean renderPopup, KeyEventManager keyManager) {
        // 设置按钮使用的纹理图集
        this.setTexture(textureAtlas);

        // 根据当前状态和索引设置对应的纹理坐标
        if (isPressed() && isHovered()) {
            Coordinate tapCoord = getCurrentCoordinate(tapCoords);
            if (tapCoord != null) {
                setTap(tapCoord);
            }
        } else if (isHovered()) {
            Coordinate hoverCoord = getCurrentCoordinate(hoverCoords);
            if (hoverCoord != null) {
                setHover(hoverCoord);
            }
        } else {
            Coordinate normalCoord = getCurrentCoordinate(normalCoords);
            if (normalCoord != null) {
                setNormal(normalCoord);
            }
        }

        // 调用父类渲染方法
        super.render(graphics, renderPopup, keyManager);
    }
}
