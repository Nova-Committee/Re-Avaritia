package committee.nova.mods.avaritia.client.screen.element;

import net.minecraft.client.renderer.Rect2i;

/** 添加一些自定义的，意图让外界获取的信息 */
public interface GuiElementAccess
{
    /**
     * 获取当前UI元素的所占的屏幕空间
     */
    Rect2i getElementArea();
}
