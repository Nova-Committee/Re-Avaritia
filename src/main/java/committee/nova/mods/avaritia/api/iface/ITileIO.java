package committee.nova.mods.avaritia.api.iface;

import committee.nova.mods.avaritia.core.io.SideConfiguration;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

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
    void extractFromHandler(ResourceHandler<ItemResource> externalHandler, Direction fromSide);

    /**
     * 向外部处理器插入物品
     */
    void insertToHandler(ResourceHandler<ItemResource> externalHandler, Direction toSide);

    /**
     * 自定义的面模式切换逻辑
     */
    void cycleSideModeForNeutronCollector(Direction direction);

    /**
     * 检查是否可以从指定面放置物品进来（用于被动输入）
     * @param direction 要检查的面方向
     * @param itemStack 要放置的物品（可以为 null，用于通配符检查）
     * @return 如果允许放置则返回 true
     */
    default boolean canPlaceItemFromSide(Direction direction, net.minecraft.world.item.ItemStack itemStack) {
        return true; // 默认允许
    }

    /**
     * 检查是否可以从指定面提取物品出去（用于被动输出）
     * @param direction 要检查的面方向
     * @param itemStack 要提取的物品（可以为 null，用于通配符检查）
     * @return 如果允许提取则返回 true
     */
    default boolean canTakeItemToSide(Direction direction, net.minecraft.world.item.ItemStack itemStack) {
        return true; // 默认允许
    }
}

