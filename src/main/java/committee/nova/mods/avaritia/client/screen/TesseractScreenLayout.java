package committee.nova.mods.avaritia.client.screen;

/** 1.20.1 超立方体主界面的布局与材质坐标契约。 */
final class TesseractScreenLayout {
    static final int WIDTH = 218;
    static final int HEIGHT = 283;
    static final int TITLE_X = 8;
    static final int TITLE_Y = 5;
    static final int INVENTORY_TITLE_X = 23;
    static final int INVENTORY_TITLE_Y = 184;

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
    static final int CRAFT_TO_CHANNEL_Y = 146;
    static final int CRAFT_TO_INVENTORY_Y = 159;
    static final int CRAFT_AND_DROP_Y = 172;
    static final int CRAFT_TO_CHANNEL_TEXTURE_Y = 0;
    static final int CRAFT_AND_DROP_TEXTURE_Y = 9;
    static final int CRAFT_TO_INVENTORY_TEXTURE_Y = 18;

    // The inventory atlas uses a +67 Y offset, matching the menu's rows at 195 and hotbar at 258.
    // Keep the separator and complete lower panel: shortening them detaches slots from their frames.
    private static final BackgroundSlice[] STORAGE_BACKGROUND = {
            new BackgroundSlice(0, 0, 68),
            new BackgroundSlice(68, 17, 51),
            new BackgroundSlice(119, 17, 51),
            new BackgroundSlice(170, 122, 6),
            new BackgroundSlice(176, 122, 6),
            new BackgroundSlice(182, 122, 6),
            new BackgroundSlice(188, 122, 4),
            new BackgroundSlice(192, 125, 91)
    };

    private static final BackgroundSlice[] CRAFTING_BACKGROUND = {
            new BackgroundSlice(0, 0, 68),
            new BackgroundSlice(68, 17, 51),
            new BackgroundSlice(119, 17, 17),
            new BackgroundSlice(136, 69, 147)
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
