package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.Const;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.sprite.AtlasManager;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Set;

/**
 * 注册模组自定义 halo 图集（assets/avaritia/atlases/halo.json）。
 * NeoForge 经 RegisterTextureAtlasesEvent 注册，Fabric 无对应事件，
 * 在 AtlasManager 构造尾部追加同名 AtlasConfig。
 */
@Mixin(AtlasManager.class)
public abstract class ClientAtlasManagerMixin {

    @Shadow
    @Final
    private Map<Identifier, AtlasManager.AtlasEntry> atlasByTexture;

    @Shadow
    @Final
    private Map<Identifier, AtlasManager.AtlasEntry> atlasById;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void avaritia$registerHaloAtlas(TextureManager textureManager, int maxMipmapLevels, CallbackInfo ci) {
        if (this.atlasById.containsKey(Const.HALO_ATLAS_INFO)) {
            return;
        }
        AtlasManager.AtlasConfig config = new AtlasManager.AtlasConfig(
                Const.HALO_ATLAS_LOCATION, Const.HALO_ATLAS_INFO, false, Set.of());
        TextureAtlas atlas = new TextureAtlas(config.textureId());
        textureManager.register(config.textureId(), atlas);
        AtlasManager.AtlasEntry entry = new AtlasManager.AtlasEntry(atlas, config);
        this.atlasByTexture.put(config.textureId(), entry);
        this.atlasById.put(config.definitionLocation(), entry);
    }
}
