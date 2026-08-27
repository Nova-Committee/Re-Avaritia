package committee.nova.mods.avaritia.client.model.entity;

import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.client.AvaritiaClient;
import committee.nova.mods.avaritia.client.shader.AvaritiaRenderTypes;
import committee.nova.mods.avaritia.client.shader.AvaritiaShaderUniforms;
import committee.nova.mods.avaritia.client.shader.AvaritiaShaders;
import committee.nova.mods.avaritia.init.registry.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.Item;

import java.util.function.Function;

public class InfinityArmorModel<S extends HumanoidRenderState> extends HumanoidModel<S> {
    private static final int WHITE = ARGB.colorFromFloat(1.0F, 1.0F, 1.0F, 1.0F);
    // PlayerModel 的外层皮肤为 0.25F，无尽护腿的最小外壳为 0.40F。
    private static final float COSMIC_BODY_DEFORMATION = 0.35F;
    private static final float HELMET_EYE_DEPTH = -5.01F;

    public final ModelPart root = createLayer().bakeRoot();
    public final ModelPart bodyRoot = createBodyLayer(new CubeDeformation(1.0F)).bakeRoot();
    private final ModelPart cosmicBodyRoot;

    private final boolean isSilm;

    public InfinityArmorModel(ModelPart root, boolean isSilm) {
        super(root);
        this.isSilm = isSilm;
        this.cosmicBodyRoot = LayerDefinition.create(
                PlayerModel.createMesh(new CubeDeformation(COSMIC_BODY_DEFORMATION), isSilm),
                64,
                64
        ).bakeRoot();
    }

    public static LayerDefinition createLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        CubeDeformation cubeDeformation = new CubeDeformation(0.0F);
        partDefinition.addOrReplaceChild("helmet_eye", CubeListBuilder.create().texOffs(9, 11).addBox(-3.0F, -5.0F, HELMET_EYE_DEPTH, 6.0F, 1.0F, 0.0F, cubeDeformation), PartPose.ZERO);
        partDefinition.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -11.6F, 0.0F, 0.0F, 32.0F, 32.0F, cubeDeformation), PartPose.offsetAndRotation(-1.5F, 0.0F, 2.0F, 0.0F, (float) (Math.PI * 0.4), 0.0F));
        partDefinition.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(0.0F, -11.6F, 0.0F, 0.0F, 32.0F, 32.0F, cubeDeformation), PartPose.offsetAndRotation(1.5F, 0.0F, 2.0F, 0.0F, (float) (-Math.PI * 0.4), 0.0F));
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    public static LayerDefinition createBodyLayer(CubeDeformation cubDeformation) {
        MeshDefinition meshDefinition = HumanoidModel.createMesh(cubDeformation, 0.0F);
        PartDefinition partDefinition = meshDefinition.getRoot();

        partDefinition.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, cubDeformation.extend(-0.1F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        partDefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 48).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubDeformation.extend(-0.6F)), PartPose.offset(1.9F, 12.0F, 0.0F));
        partDefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubDeformation.extend(-0.6F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

        partDefinition.addOrReplaceChild("left_boot", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubDeformation.extend(-0.1F)), PartPose.offset(1.9F, 12.0F, 0.0F));
        partDefinition.addOrReplaceChild("right_boot", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubDeformation.extend(-0.1F)), PartPose.offset(-1.9F, 12.0F, 0.0F));
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    public void setScale(ModelPart modelPart, float scale) {
        modelPart.xScale = scale;
        modelPart.yScale = scale;
        modelPart.zScale = scale;
    }

    private static void copyPartPose(ModelPart target, ModelPart source) {
        target.loadPose(source.storePose());
    }

    @Override
    public void setupAnim(S state) {
        super.setupAnim(state);

        ModelPart leftWing = root.getChild("left_wing");
        leftWing.xRot = this.body.xRot;
        leftWing.yRot = this.body.yRot + (float) (Math.PI * 0.4);
        leftWing.zRot = this.body.zRot;
        ModelPart rightWing = root.getChild("right_wing");
        rightWing.xRot = this.body.xRot;
        rightWing.yRot = this.body.yRot + (float) (-Math.PI * 0.4);
        rightWing.zRot = this.body.zRot;

        ModelPart head = this.bodyRoot.getChild("head");
        copyPartPose(head, this.head);
        ModelPart hat = this.bodyRoot.getChild("hat");
        copyPartPose(hat, this.hat);
        copyPartPose(this.root.getChild("helmet_eye"), this.head);

        ModelPart body = this.bodyRoot.getChild("body");
        copyPartPose(body, this.body);

        ModelPart leftArm = this.bodyRoot.getChild("left_arm");
        copyPartPose(leftArm, this.leftArm);
        if (!isSilm) {
            this.setScale(leftArm, 1.01F);
        } else {
            leftArm.x -= 0.3F;
            leftArm.xScale = 0.8F;
            leftArm.yScale = 1.01F;
            leftArm.zScale = 1.0F;
        }
        ModelPart rightArm = this.bodyRoot.getChild("right_arm");
        copyPartPose(rightArm, this.rightArm);
        if (!isSilm) {
            this.setScale(rightArm, 1.01F);
        } else {
            rightArm.x += 0.3F;
            rightArm.xScale = 0.8F;
            rightArm.yScale = 1.01F;
            rightArm.zScale = 1.0F;
        }

        ModelPart leftLeg = this.bodyRoot.getChild("left_leg");
        copyPartPose(leftLeg, this.leftLeg);
        ModelPart rightLeg = this.bodyRoot.getChild("right_leg");
        copyPartPose(rightLeg, this.rightLeg);
        ModelPart leftBoot = this.bodyRoot.getChild("left_boot");
        copyPartPose(leftBoot, this.leftLeg);
        ModelPart rightBoot = this.bodyRoot.getChild("right_boot");
        copyPartPose(rightBoot, this.rightLeg);

        copyPartPose(this.cosmicBodyRoot.getChild("body"), this.body);
        copyPartPose(this.cosmicBodyRoot.getChild("left_arm"), this.leftArm);
        copyPartPose(this.cosmicBodyRoot.getChild("right_arm"), this.rightArm);
        copyPartPose(this.cosmicBodyRoot.getChild("left_leg"), this.leftLeg);
        copyPartPose(this.cosmicBodyRoot.getChild("right_leg"), this.rightLeg);
    }

    public void render(S state, PoseStack poseStack, SubmitNodeCollector output, RenderType wingRenderType, int packedLight, int packedOverlay) {
        RenderType cosmicArmorRenderType = AvaritiaRenderTypes.COSMIC_ARMOR;

        Minecraft mc = Minecraft.getInstance();

        Item headItem = state.headEquipment.getItem();
        Item chestItem = state.chestEquipment.getItem();
        Item legsItem = state.legsEquipment.getItem();
        Item feetItem = state.feetEquipment.getItem();
        boolean fullInfinityArmor = headItem == ModItems.infinity_helmet.get()
                && chestItem == ModItems.infinity_chestplate.get()
                && legsItem == ModItems.infinity_pants.get()
                && feetItem == ModItems.infinity_boots.get();

        long time = mc.level != null ? mc.level.getGameTime() : System.currentTimeMillis() / 50L;

        double pulse = Math.sin(time / 10.0D) * 0.5D + 0.5D;
        double pulseMagSqr = pulse * pulse * pulse * pulse * pulse * pulse;

        float yaw = 0.0F;
        float pitch = 0.0F;
        float scale = 1.0F;

        if (AvaritiaClient.inventoryRender) {
            scale = 100.0F;
        } else {
            yaw = (float) ((state.yRot * 2.0F) * Math.PI / 360.0D);
            pitch = -((float) ((state.xRot * 2.0F) * Math.PI / 360.0D));
        }

        AvaritiaShaderUniforms.set(AvaritiaShaderUniforms.Effect.COSMIC_ARMOR, time % Integer.MAX_VALUE, yaw, pitch, scale, 1.25F, AvaritiaShaders.COSMIC_UVS);

        boolean flying = Boolean.TRUE.equals(state.getRenderData(AvaritiaClient.INFINITY_ARMOR_FLYING)) || state.isFallFlying;
        if (fullInfinityArmor && flying) {
            poseStack.pushPose();
            ModelPart leftWing = root.getChild("left_wing");
            ModelPart rightWing = root.getChild("right_wing");
            submitPart(poseStack, output, wingRenderType, leftWing, packedLight, packedOverlay, WHITE);
            submitPart(poseStack, output, wingRenderType, rightWing, packedLight, packedOverlay, WHITE);

            submitPart(poseStack, output, cosmicArmorRenderType, leftWing, Res.ARMOR_WING_MASK::wrap, packedLight, packedOverlay, WHITE);
            submitPart(poseStack, output, cosmicArmorRenderType, rightWing, Res.ARMOR_WING_MASK::wrap, packedLight, packedOverlay, WHITE);

            int wingGlow = ARGB.colorFromFloat((float) (pulseMagSqr * 0.5D), 0.84F, 1.0F, 0.95F);
            submitPart(poseStack, output, AvaritiaRenderTypes.WingGlow(Res.WING_GLOW_TEX), leftWing, packedLight, packedOverlay, wingGlow);
            submitPart(poseStack, output, AvaritiaRenderTypes.WingGlow(Res.WING_GLOW_TEX), rightWing, packedLight, packedOverlay, wingGlow);
            poseStack.popPose();
        }

        if (headItem == ModItems.infinity_helmet.get()) {
            poseStack.pushPose();

            ModelPart helmetEye = this.root.getChild("helmet_eye");
            submitPart(poseStack, output, cosmicArmorRenderType, helmetEye, Res.ARMOR_MASK::wrap, packedLight, packedOverlay, WHITE);

            poseStack.popPose();
        }

        if (chestItem == ModItems.infinity_chestplate.get()) {
            poseStack.pushPose();

            ModelPart body = this.bodyRoot.getChild("body");
            submitPart(poseStack, output, cosmicArmorRenderType, body, Res.ARMOR_MASK::wrap, packedLight, packedOverlay, WHITE);
            int eyeGlow = ARGB.colorFromFloat((float) (pulseMagSqr * 0.5D), 0.84F, 1.0F, 0.95F);
            submitPart(poseStack, output, AvaritiaRenderTypes.WingGlow(Res.EYE_TEX), body, packedLight, packedOverlay, eyeGlow);
            ModelPart leftArm = this.bodyRoot.getChild("left_arm");
            submitPart(poseStack, output, cosmicArmorRenderType, leftArm, Res.ARMOR_MASK::wrap, packedLight, packedOverlay, WHITE);
            submitPart(poseStack, output, AvaritiaRenderTypes.WingGlow(Res.EYE_TEX), leftArm, packedLight, packedOverlay, eyeGlow);
            ModelPart rightArm = this.bodyRoot.getChild("right_arm");
            submitPart(poseStack, output, cosmicArmorRenderType, rightArm, Res.ARMOR_MASK::wrap, packedLight, packedOverlay, WHITE);
            submitPart(poseStack, output, AvaritiaRenderTypes.WingGlow(Res.EYE_TEX), rightArm, packedLight, packedOverlay, eyeGlow);

            poseStack.popPose();
        }

        if (legsItem == ModItems.infinity_pants.get()) {
            poseStack.pushPose();

            ModelPart leftLeg = this.bodyRoot.getChild("left_leg");
            submitPart(poseStack, output, cosmicArmorRenderType, leftLeg, Res.ARMOR_MASK::wrap, packedLight, packedOverlay, WHITE);
            int legGlow = ARGB.colorFromFloat((float) (pulseMagSqr * 0.5D), 0.84F, 1.0F, 0.95F);
            submitPart(poseStack, output, AvaritiaRenderTypes.WingGlow(Res.EYE_TEX), leftLeg, packedLight, packedOverlay, legGlow);
            ModelPart rightLeg = this.bodyRoot.getChild("right_leg");
            submitPart(poseStack, output, cosmicArmorRenderType, rightLeg, Res.ARMOR_MASK::wrap, packedLight, packedOverlay, WHITE);
            submitPart(poseStack, output, AvaritiaRenderTypes.WingGlow(Res.EYE_TEX), rightLeg, packedLight, packedOverlay, legGlow);

            poseStack.popPose();
        }

        if (feetItem == ModItems.infinity_boots.get()) {
            poseStack.pushPose();

            ModelPart leftBoot = this.bodyRoot.getChild("left_boot");
            submitPart(poseStack, output, cosmicArmorRenderType, leftBoot, Res.ARMOR_MASK::wrap, packedLight, packedOverlay, WHITE);
            ModelPart rightBoot = this.bodyRoot.getChild("right_boot");
            submitPart(poseStack, output, cosmicArmorRenderType, rightBoot, Res.ARMOR_MASK::wrap, packedLight, packedOverlay, WHITE);

            poseStack.popPose();
        }

        if (fullInfinityArmor) {
            submitBodyParts(poseStack, output, cosmicArmorRenderType, Res.ARMOR_MASK_INV::wrap, packedLight, packedOverlay, WHITE);
        }
    }

    private void submitBodyParts(PoseStack poseStack, SubmitNodeCollector output, RenderType renderType, Function<VertexConsumer, VertexConsumer> wrapper, int packedLight, int packedOverlay, int color) {
        output.submitCustomGeometry(poseStack, renderType, (pose, vertexConsumer) -> {
            PoseStack modelPose = new PoseStack();
            modelPose.last().set(pose);
            VertexConsumer wrapped = wrapper.apply(vertexConsumer);
            this.cosmicBodyRoot.getChild("body").render(modelPose, wrapped, packedLight, packedOverlay, color);
            this.cosmicBodyRoot.getChild("right_arm").render(modelPose, wrapped, packedLight, packedOverlay, color);
            this.cosmicBodyRoot.getChild("left_arm").render(modelPose, wrapped, packedLight, packedOverlay, color);
            this.cosmicBodyRoot.getChild("right_leg").render(modelPose, wrapped, packedLight, packedOverlay, color);
            this.cosmicBodyRoot.getChild("left_leg").render(modelPose, wrapped, packedLight, packedOverlay, color);
        });
    }

    private void submitPart(PoseStack poseStack, SubmitNodeCollector output, RenderType renderType, ModelPart part, int packedLight, int packedOverlay, int color) {
        output.submitCustomGeometry(poseStack, renderType, (pose, vertexConsumer) -> {
            PoseStack modelPose = new PoseStack();
            modelPose.last().set(pose);
            part.render(modelPose, vertexConsumer, packedLight, packedOverlay, color);
        });
    }

    private void submitPart(PoseStack poseStack, SubmitNodeCollector output, RenderType renderType, ModelPart part, Function<VertexConsumer, VertexConsumer> wrapper, int packedLight, int packedOverlay, int color) {
        output.submitCustomGeometry(poseStack, renderType, (pose, vertexConsumer) -> {
            PoseStack modelPose = new PoseStack();
            modelPose.last().set(pose);
            part.render(modelPose, wrapper.apply(vertexConsumer), packedLight, packedOverlay, color);
        });
    }
}
