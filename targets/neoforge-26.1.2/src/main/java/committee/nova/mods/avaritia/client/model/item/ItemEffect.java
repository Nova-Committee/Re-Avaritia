package committee.nova.mods.avaritia.client.model.item;

import committee.nova.mods.avaritia.api.utils.RenderUtils;
import committee.nova.mods.avaritia.client.model.loader.AvaritiaItemModelRenderers;
import committee.nova.mods.avaritia.client.shader.AvaritiaRenderTypeHelper;
import committee.nova.mods.avaritia.client.shader.AvaritiaRenderTypes;
import committee.nova.mods.avaritia.client.shader.AvaritiaShaderUniforms;
import committee.nova.mods.avaritia.client.shader.AvaritiaShaders;
import committee.nova.mods.avaritia.common.item.resources.MatterClusterItem;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import static committee.nova.mods.avaritia.client.shader.AvaritiaShaders.COSMIC_UVS;
import static committee.nova.mods.avaritia.client.shader.AvaritiaShaders.ETERNAL_UVS;

/**
 * 物品遮罩效果的运行时参数集合。
 * <p>
 * 每个枚举值负责选择默认 RenderType、shader pipeline、uniform 类型、UV 序列和透明度策略。
 */
public enum ItemEffect {
    COSMIC,
    HELL,
    ETERNAL,
    UNSTABLE;

    private static final AtomicInteger RENDER_TYPE_SEQUENCE = new AtomicInteger();

    /**
     * 用于烘焙 mask quad 的基础 RenderType；实际提交时会优先创建带独立 pipeline 状态的新 RenderType。
     */
    public RenderType renderType() {
        return switch (this) {
            case COSMIC -> AvaritiaRenderTypes.COSMIC;
            case HELL -> AvaritiaRenderTypes.HELL;
            case ETERNAL -> AvaritiaRenderTypes.ETERNAL;
            case UNSTABLE -> AvaritiaRenderTypes.UNSTABLE;
        };
    }

    public AvaritiaItemModelRenderers.EffectLayerArgument createArgument(List<BakedQuad> quads, List<BakedQuad> baseQuads,
                                                                         @Nullable ClientLevel level,
                                                                         @Nullable ItemOwner owner,
                                                                         ItemDisplayContext displayContext, ItemStack stack) {
        long time = level != null ? level.getGameTime() : 0L;
        LivingEntity entity = owner != null ? owner.asLivingEntity() : null;
        boolean itemHeldByEntity = entity != null && displayContext != ItemDisplayContext.GUI;
        float yaw = itemHeldByEntity ? (float) (entity.getYRot() * 2.0F * Math.PI / 360.0F) : 0.0F;
        float pitch = itemHeldByEntity ? -(float) (entity.getXRot() * 2.0F * Math.PI / 360.0F) : 0.0F;
        float scale = displayContext == ItemDisplayContext.GUI ? 100.0F : 1.0F;
        return new AvaritiaItemModelRenderers.EffectLayerArgument(
                quads,
                AvaritiaItemModelRenderers.itemRenderTypes(baseQuads),
                this.newRenderType(),
                this.uniformEffect(),
                time % Integer.MAX_VALUE,
                yaw,
                pitch,
                scale,
                this.opacity(stack),
                this.uvs());
    }

    /**
     * 每次创建独立 RenderType，避免多个物品在同一帧收集时互相覆盖 AvaritiaCosmic uniform 参数。
     */
    private RenderType newRenderType() {
        RenderPipeline pipeline = this.pipeline();
        if (pipeline == null) {
            return this.renderType();
        }
        String name = "avaritia_" + this.name().toLowerCase(Locale.ROOT) + "_item_" + RENDER_TYPE_SEQUENCE.incrementAndGet();
        // The overlay quads are already offset outwards along their own face
        // normals. A global view-space offset can invert under item display
        // transforms and place the mask behind the wrapped model.
        return AvaritiaRenderTypeHelper.textured(name, pipeline, RenderUtils.COSMIC_TEXTURE_ISOLATED, true, true, true, false);
    }

    private @Nullable RenderPipeline pipeline() {
        return switch (this) {
            case COSMIC -> AvaritiaShaders.COSMIC_SHADER;
            case HELL -> AvaritiaShaders.HELL_SHADER;
            case ETERNAL -> AvaritiaShaders.ETERNAL_SHADER;
            case UNSTABLE -> AvaritiaShaders.UNSTABLE_SHADER;
        };
    }

    private AvaritiaShaderUniforms.Effect uniformEffect() {
        return switch (this) {
            case COSMIC -> AvaritiaShaderUniforms.Effect.COSMIC;
            case HELL -> AvaritiaShaderUniforms.Effect.HELL;
            case ETERNAL -> AvaritiaShaderUniforms.Effect.ETERNAL;
            case UNSTABLE -> AvaritiaShaderUniforms.Effect.UNSTABLE;
        };
    }

    private float[] uvs() {
        return switch (this) {
            case COSMIC, HELL -> COSMIC_UVS;
            case ETERNAL, UNSTABLE -> ETERNAL_UVS;
        };
    }

    /**
     * Matter Cluster 的透明度跟填充量相关；不稳定效果保留更强的覆盖亮度。
     */
    private float opacity(ItemStack stack) {
        if (stack.getItem() instanceof MatterClusterItem) {
            return MatterClusterItem.getClusterSize(MatterClusterItem.getClusterItems(stack)) / (float) MatterClusterItem.CAPACITY;
        }
        return this == UNSTABLE ? 1.5F : 1.0F;
    }
}
