package committee.nova.mods.avaritia.init.mixins.iris;

import java.util.List;

import committee.nova.mods.avaritia.client.model.loader.CosmicBakeModel;
import committee.nova.mods.avaritia.client.shader.AvaritiaRenderTypes;
import committee.nova.mods.avaritia.client.shader.AvaritiaShaders;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.irisshaders.iris.Iris;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;


@Mixin(CosmicBakeModel.class)
public class MixinBakedModelCosmic {

    @Shadow
    @Final
    private List<ResourceLocation> maskSprite;


    @Inject(method = "renderItem", at = @At("HEAD"), cancellable = true)
    public void renderItem(ItemStack stack, ItemDisplayContext transformType, PoseStack pStack, MultiBufferSource source, int packedLight, int packedOverlay, CallbackInfo ci) {
        if (Iris.getIrisConfig().areShadersEnabled() && Iris.getCurrentPack().isPresent()) {
            if (transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || transformType == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
                ci.cancel();

                RenderType COSMIC_RENDER_TYPE = AvaritiaRenderTypes.COSMIC;
                Minecraft mc = Minecraft.getInstance();
                ItemRenderer itemRenderer = mc.getItemRenderer();
                TextureAtlas textureAtlas = mc.getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS);

                float yaw = 0.0F;
                float pitch = 0.0F;
                float scale = 1.0F;

                yaw = (float) ((mc.player.getYRot() * 2.0F) * Math.PI / 360.0D);
                pitch = -((float) ((mc.player.getXRot() * 2.0F) * Math.PI / 360.0D));

                AvaritiaShaders.cosmicTime.set(mc.level.getGameTime() % Integer.MAX_VALUE);
                AvaritiaShaders.cosmicYaw.set(yaw);
                AvaritiaShaders.cosmicPitch.set(pitch);
                AvaritiaShaders.cosmicExternalScale.set(scale);
                AvaritiaShaders.cosmicOpacity.set(1.0F);
                AvaritiaShaders.cosmicUVs.set(AvaritiaShaders.COSMIC_UVS);

                if (!this.maskSprite.isEmpty()) {
                    TextureAtlasSprite[] textureAtlasSprites = new TextureAtlasSprite[] {
                            textureAtlas.getSprite(this.maskSprite.get(0))
                    };

                    VertexConsumer vertexConsumer = source.getBuffer(COSMIC_RENDER_TYPE);

                    itemRenderer.renderQuadList(pStack, vertexConsumer, ((CosmicBakeModel)(Object)this).bakeItem(List.of(textureAtlasSprites)), stack, packedLight, packedOverlay);
                }

                CosmicBakeModel instance = (CosmicBakeModel) (Object) this;

                for (BakedModel bakedModel : instance.getRenderPasses(stack, true)) {
                    for (RenderType renderType : bakedModel.getRenderTypes(stack, true)) {
                        itemRenderer.renderModelLists(bakedModel, stack, packedLight, packedOverlay, pStack, source.getBuffer(renderType));
                    }
                }
            }
        }
    }
}
