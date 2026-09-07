package committee.nova.mods.avaritia.client.screen;

/** 1.20.1 超立方体主界面的布局与材质坐标契约。 */
final class TesseractScreenLayout {
    static final int WIDTH = 218;
    static final int HEIGHT = 265;

    static final int SEARCH_X = 104;
    static final int SEARCH_Y = 4;
    static final int SEARCH_WIDTH = 90;
    static final int SEARCH_HEIGHT = 12;

    static final int CONTROL_X = 198;
    static final int CONTROL_SIZE = 16;
    static final int CRAFTING_TOGGLE_Y = 160;
    static final int LOCK_Y = 176;
    static final int CHANNEL_Y = 192;
    static final int SORT_Y = 208;
    static final int VIEW_Y = 224;

    static final int ICON_TEXTURE_X = 219;
    static final int ACTIVE_ICON_TEXTURE_X = 235;
    static final int CRAFTING_ICON_TEXTURE_Y = 27;
    static final int LOCK_ICON_TEXTURE_Y = 43;
    static final int CHANNEL_ICON_TEXTURE_Y = 59;
    static final int SORT_ICON_TEXTURE_Y = 75;
    static final int VIEW_ICON_TEXTURE_Y = 203;

    static final int CRAFT_BUTTON_X = 179;
    static final int CRAFT_BUTTON_WIDTH = 16;
    static final int CRAFT_BUTTON_HEIGHT = 9;
    static final int CRAFT_TO_CHANNEL_Y = 143;
    static final int CRAFT_TO_INVENTORY_Y = 156;
    static final int CRAFT_AND_DROP_Y = 169;
    static final int CRAFT_TO_CHANNEL_TEXTURE_Y = 0;
    static final int CRAFT_AND_DROP_TEXTURE_Y = 9;
    static final int CRAFT_TO_INVENTORY_TEXTURE_Y = 18;

    private static final BackgroundSlice[] STORAGE_BACKGROUND = {
            new BackgroundSlice(0, 0, 68),
            new BackgroundSlice(68, 17, 51),
            new BackgroundSlice(119, 17, 51),
            new BackgroundSlice(170, 122, 6),
            new BackgroundSlice(176, 122, 6),
            new BackgroundSlice(182, 122, 3),
            new BackgroundSlice(185, 125, 54),
            new BackgroundSlice(239, 190, 26)
    };

    private static final BackgroundSlice[] CRAFTING_BACKGROUND = {
            new BackgroundSlice(0, 0, 68),
            new BackgroundSlice(68, 17, 51),
            new BackgroundSlice(119, 17, 17),
            new BackgroundSlice(133, 69, 52),
            new BackgroundSlice(185, 125, 54),
            new BackgroundSlice(239, 190, 26)
    };

    private TesseractScreenLayout() {
    }

    static BackgroundSlice[] background(boolean crafting) {
        return crafting ? CRAFTING_BACKGROUND : STORAGE_BACKGROUND;
    }

    static int toggleTextureX(boolean active) {
        return active ? ACTIVE_ICON_TEXTURE_X : ICON_TEXTURE_X;
    }

    static int sortTextureY(int sortType) {
        return SORT_ICON_TEXTURE_Y + (sortType & 7) * CONTROL_SIZE;
    }

    static int viewTextureY(int viewType) {
        return VIEW_ICON_TEXTURE_Y + Math.floorMod(viewType, 3) * CONTROL_SIZE;
    }

    record BackgroundSlice(int destinationY, int sourceY, int height) {
    }
}
