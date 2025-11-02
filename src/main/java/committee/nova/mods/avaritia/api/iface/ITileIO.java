package committee.nova.mods.avaritia.api.iface;

import committee.nova.mods.avaritia.core.io.SideConfiguration;
import net.minecraft.core.Direction;
import net.minecraftforge.items.IItemHandler;

/**
 * 方块实体IO处理器通用接口
 * 提供统一的输入输出处理逻辑，支持相对方向转换
 * Description: 通用IO处理框架，支持方向转换和混合模式
 * @author cnlimiter
 * Date: 2025/11/02
 * Version: 1.0
 */
public interface ITileIO {
    /**
     * 获取IO配置
     */
    SideConfiguration getSideConfiguration();

    void setSideConfiguration(SideConfiguration sideConfiguration);

    void setIOChange();

    /**
     * 从外部处理器抽取物品
     */
    void extractFromHandler(IItemHandler externalHandler, Direction fromSide);

    /**
     * 向外部处理器插入物品
     */
    void insertToHandler(IItemHandler externalHandler, Direction toSide);
}

