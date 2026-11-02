package committee.nova.mods.avaritia.client.screen;

/**
 * 无尽箱（15×9 虚拟库存 + 玩家背包）界面的布局与材质坐标契约。
 * <p>26.3 Fabric：槽位坐标以 {@code InfinityChestMenu} 的 15×9 FakeSlot 网格为准。</p>
 */
final class InfinityChestScreenLayout {
    static final int SORT_BUTTON_WIDTH = 17;
    static final int SORT_BUTTON_HEIGHT = 18;
    static final int SORT_BUTTON_X = 249;
    static final int SORT_BUTTON_Y = 188;
    static final int SORT_TEXTURE_X = 303;
    static final int SORT_TEXTURE_Y = 0;
    static final int LOCK_BUTTON_X = 231;
    static final int LOCK_BUTTON_Y = 188;
    static final int LOCK_TEXTURE_Y = 36;
    static final int SEARCH_X = 8;
    static final int SEARCH_Y = 4;
    static final int SEARCH_WIDTH = 132;
    static final int SEARCH_HEIGHT = 12;
    static final int TITLE_X = 152;
    static final int TITLE_Y = 6;
    static final int SCROLLBAR_X = 282;
    static final int SCROLLBAR_Y = 16;
    static final int SCROLLBAR_WIDTH = 12;
    static final int SCROLLBAR_HEIGHT = 160;
    /** 纹理中存储区（含顶部搜索条）与玩家背包区的分界 y。 */
    static final int STORAGE_HEIGHT = 178;

    private InfinityChestScreenLayout() {
    }

    static int sortTextureX(int sortType) {
        return SORT_TEXTURE_X + sortType * SORT_BUTTON_WIDTH;
    }

    static int lockTextureX(boolean locked) {
        return SORT_TEXTURE_X + (locked ? 0 : SORT_BUTTON_WIDTH);
    }
}
