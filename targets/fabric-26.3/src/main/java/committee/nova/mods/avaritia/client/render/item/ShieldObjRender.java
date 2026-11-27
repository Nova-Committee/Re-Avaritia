package committee.nova.mods.avaritia.client.render.item;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.client.render.mesh.SimpleMesh;
import committee.nova.mods.avaritia.client.render.mesh.SimpleObjMeshLoader;
import committee.nova.mods.avaritia.client.shader.AvaritiaRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3fc;

import java.util.Map;
import java.util.function.Consumer;

/**
 * 通用 OBJ 网格物品特殊渲染器，用于无尽盾各模式模型。
 * <p>
 * 26.3 中 vanilla 已不识别旧版 "avaritia:obj" loader，改为在 items/*.json 中
 * 使用 minecraft:special + avaritia:shield_obj 引用本渲染器。
 */
public class ShieldObjRender implements NoDataSpecialModelRenderer {
    private final Map<String, SimpleMesh> meshes;

    public ShieldObjRender(Identifier objModel) {
        this.meshes = SimpleObjMeshLoader.load(objModel);
    }

    @Override
    public void submit(@NotNull PoseStack poseStack, @NotNull SubmitNodeCollector output, int packedLight, int packedOverlay, boolean hasFoilType, int outlineColor) {
        if (this.meshes.isEmpty()) {
            return;
        }
        output.submitCustomGeometry(poseStack, AvaritiaRenderTypes.SHIELD_OBJ, (pose, vertexConsumer) -> {
            for (SimpleMesh mesh : this.meshes.values()) {
                mesh.render(pose, vertexConsumer, 0xFFFFFFFF, packedLight, packedOverlay);
            }
        });
    }



    @Override
    public void getExtents(@NotNull Consumer<Vector3fc> output) {
    }

    public record Unbaked(Identifier model) implements SpecialModelRenderer.Unbaked<Void> {
        public static final MapCodec<ShieldObjRender.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(Identifier.CODEC.fieldOf("model").forGetter(ShieldObjRender.Unbaked::model))
                        .apply(instance, ShieldObjRender.Unbaked::new));

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<Void>> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<Void> bake(SpecialModelRenderer.BakingContext context) {
            return new ShieldObjRender(this.model);
        }
    }
}
