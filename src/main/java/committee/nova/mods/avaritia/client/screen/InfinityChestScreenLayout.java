package committee.nova.mods.avaritia.client.screen;

/** 1.20.1 无尽箱界面的布局与材质坐标契约。 */
final class InfinityChestScreenLayout {
    static final int SORT_BUTTON_WIDTH = 17;
    static final int SORT_BUTTON_HEIGHT = 18;
    static final int SORT_BUTTON_X = 249;
    static final int SORT_BUTTON_Y = 151;
    static final int SORT_TEXTURE_X = 303;
    static final int SORT_TEXTURE_Y = 0;
    static final int LOCK_BUTTON_X = 231;
    static final int LOCK_BUTTON_Y = 151;
    static final int LOCK_TEXTURE_Y = 36;

    private InfinityChestScreenLayout() {
    }

    static int sortTextureX(int sortType) {
        return SORT_TEXTURE_X + sortType * SORT_BUTTON_WIDTH;
    }

    static int lockTextureX(boolean locked) {
        return SORT_TEXTURE_X + (locked ? 0 : SORT_BUTTON_WIDTH);
    }
}
