package committee.nova.mods.avaritia.client.model;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.client.render.model.BaseModelCache;

/**
 * @author cnlimiter
 */
public class AvaritiaModelCache extends BaseModelCache {
    public static final AvaritiaModelCache INSTANCE = new AvaritiaModelCache();


    public final BaseModelCache.OBJModelData INFINITY_SHIELD = registerOBJ("models/obj/infinity_shield.obj");

    protected AvaritiaModelCache() {
        super(Const.MOD_ID);
    }
}
