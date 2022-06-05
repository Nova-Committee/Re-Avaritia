package nova.committee.avaritia.api.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import nova.committee.avaritia.Static;

public enum SpecialTextures {
    BLANK("blank.png"),
    CHECKERED("checkerboard.png"),
    THIN_CHECKERED("thin_checkerboard.png"),
    CUTOUT_CHECKERED("cutout_checkerboard.png"),
    HIGHLIGHT_CHECKERED("highlighted_checkerboard.png"),
    SELECTION("selection.png"),

    ;

    public static final String ASSET_PATH = "textures/special/";
    private ResourceLocation location;

    private SpecialTextures(String filename) {
        location = new ResourceLocation(Static.MOD_ID, ASSET_PATH + filename);
    }

    public void bind() {
        Minecraft.getInstance()
                .getTextureManager()
                .bindForSetup(location);
    }

    public ResourceLocation getLocation() {
        return location;
    }
}
