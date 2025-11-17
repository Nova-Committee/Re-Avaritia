package committee.nova.mods.avaritia.client.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.NotNull;

/**
 * @author cnlimiter
 */
public class InfinityTridentModel extends Model {
    private final ModelPart root = createLayer().bakeRoot();

    public InfinityTridentModel() {
        super(RenderType::entitySolid);
    }

    public static LayerDefinition createLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition partdefinition1 = partdefinition.addOrReplaceChild("pole", CubeListBuilder.create().texOffs(0, 6).addBox(-0.5F, 2.0F, -0.5F, 1.0F, 25.0F, 1.0F), PartPose.ZERO);
        partdefinition1.addOrReplaceChild("base", CubeListBuilder.create().texOffs(4, 0).addBox(-1.5F, 0.0F, -0.5F, 3.0F, 2.0F, 1.0F), PartPose.ZERO);
        partdefinition1.addOrReplaceChild("left_spike", CubeListBuilder.create().texOffs(4, 3).addBox(-2.5F, -3.0F, -0.5F, 1.0F, 4.0F, 1.0F), PartPose.ZERO);
        partdefinition1.addOrReplaceChild("middle_spike", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F), PartPose.ZERO);
        partdefinition1.addOrReplaceChild("right_spike", CubeListBuilder.create().texOffs(4, 3).mirror().addBox(1.5F, -3.0F, -0.5F, 1.0F, 4.0F, 1.0F), PartPose.ZERO);
        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
