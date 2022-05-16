package nova.committee.avaritia.client.model;// Made with Blockbench 4.2.4
// Exported for Minecraft version 1.17 - 1.18 with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import nova.committee.avaritia.Static;

import javax.annotation.Nonnull;

public class WingModel extends AgeableListModel<Player> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(Static.MOD_ID, "wing_model"), "main");
	private final ModelPart bone;

	public WingModel(ModelPart rootPart) {
		this.bone = rootPart.getChild("bone");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition right_r1 = bone.addOrReplaceChild("right_r1", CubeListBuilder.create().texOffs(0, 8).addBox(-8.0F, -13.0F, 0.0F, 8.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 2.9671F, 0.0F));

		PartDefinition left_r1 = bone.addOrReplaceChild("left_r1", CubeListBuilder.create().texOffs(0, 8).mirror().addBox(0.0F, -13.0F, 0.0F, 8.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -2.9671F, 0.0F));

		return LayerDefinition.create(meshdefinition, 16, 16);
	}


	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		bone.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}


	@Override
	@Nonnull
	protected Iterable<ModelPart> headParts() {
		return ImmutableList.of();
	}

	@Override
	@Nonnull
	protected Iterable<ModelPart> bodyParts() {
		return ImmutableList.of(this.bone);
	}

	@Override
	public void setupAnim(Player pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {

	}
}
