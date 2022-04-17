package nova.committee.avaritia.init;

import nova.committee.avaritia.Static;
import nova.committee.avaritia.api.client.model.BaseModelCache;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/3 11:40
 * Version: 1.0
 */
public class ModModelCache extends BaseModelCache {
    public static final ModModelCache instance = new ModModelCache();

    public final OBJModelData hemisphere = registerOBJ(Static.rl("models/entity/hemisphere.obj"));

}
