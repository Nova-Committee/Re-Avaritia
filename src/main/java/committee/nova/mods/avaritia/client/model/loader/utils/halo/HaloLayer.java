package committee.nova.mods.avaritia.client.model.loader.utils.halo;

import net.minecraft.resources.Identifier;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * halo 图层的烘焙结果：贴图、参数，以及 GUI 裁剪/布局需要的边界。
 */
public record HaloLayer(Identifier texture, HaloSetting setting, Vector3fc[] extents) {
    public HaloLayer(Identifier texture, HaloSetting setting) {
        this(texture, setting, computeExtents(setting.size()));
    }

    private static Vector3fc[] computeExtents(int size) {
        float min = (float) (0.5 - HaloUtils.haloRadius(size));
        float max = (float) (0.5 + HaloUtils.haloRadius(size));
        return new Vector3fc[]{
                new Vector3f(min, min, 0.0F),
                new Vector3f(max, max, 0.0F)
        };
    }
}
