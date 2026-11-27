package committee.nova.mods.avaritia.core.chest;

/** 未初始化或客户端方块实体使用的只读空通道。 */
public final class NullChestHandler extends ChestHandler {
    public static final NullChestHandler INSTANCE = new NullChestHandler();

    private NullChestHandler() {
    }

    @Override
    public boolean isRemoved() {
        return true;
    }
}
