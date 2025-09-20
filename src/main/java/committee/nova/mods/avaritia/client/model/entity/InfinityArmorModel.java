package committee.nova.mods.avaritia.client.model.entity;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.client.AvaritiaForgeClient;
import committee.nova.mods.avaritia.client.shader.AvaritiaRenderTypes;
import committee.nova.mods.avaritia.client.shader.AvaritiaShaders;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/23 23:50
 * Version: 1.0
 */
public class InfinityArmorModel extends HumanoidModel<LivingEntity> {

    public final ModelPart root = createLayer().bakeRoot();
    public final ModelPart bodyRoot = createBodyLayer(new CubeDeformation(1.0F)).bakeRoot();

    private boolean isSilm;

    public InfinityArmorModel(ModelPart root, boolean isSilm) {
        super(root);
        this.isSilm = isSilm;
    }

    public static LayerDefinition createLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        CubeDeformation cubeDeformation = new CubeDeformation(0.0F);
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

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(this.body, this.rightArm, this.leftArm, this.rightLeg, this.leftLeg);
    }

    public void setScale(ModelPart modelPart, float scale) {
        modelPart.xScale = scale;
        modelPart.yScale = scale;
        modelPart.zScale = scale;
    }

    @Override
    public void setupAnim(@NotNull LivingEntity livingEntity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(livingEntity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        ModelPart leftWing = root.getChild("left_wing");
        leftWing.xRot = this.body.xRot;
        leftWing.yRot = this.body.yRot + (float) (Math.PI * 0.4);
        leftWing.zRot = this.body.zRot;
        ModelPart rightWing = root.getChild("right_wing");
        rightWing.xRot = this.body.xRot;
        rightWing.yRot = this.body.yRot + (float) (-Math.PI * 0.4);
        rightWing.zRot = this.body.zRot;

        if (livingEntity instanceof ArmorStand armorStand) {
            this.head.y = 1.0F;
            this.head.xRot = (float) (Math.PI / 180.0) * armorStand.getHeadPose().getX();
            this.head.yRot = (float) (Math.PI / 180.0) * armorStand.getHeadPose().getY();
            this.head.zRot = (float) (Math.PI / 180.0) * armorStand.getHeadPose().getZ();
            this.leftArm.xRot = (float) (Math.PI / 180.0) * armorStand.getLeftArmPose().getX();
            this.leftArm.yRot = (float) (Math.PI / 180.0) * armorStand.getLeftArmPose().getY();
            this.leftArm.zRot = (float) (Math.PI / 180.0) * armorStand.getLeftArmPose().getZ();
            this.rightArm.xRot = (float) (Math.PI / 180.0) * armorStand.getRightArmPose().getX();
            this.rightArm.yRot = (float) (Math.PI / 180.0) * armorStand.getRightArmPose().getY();
            this.rightArm.zRot = (float) (Math.PI / 180.0) * armorStand.getRightArmPose().getZ();
            this.leftLeg.xRot = (float) (Math.PI / 180.0) * armorStand.getLeftLegPose().getX();
            this.leftLeg.yRot = (float) (Math.PI / 180.0) * armorStand.getLeftLegPose().getY();
            this.leftLeg.zRot = (float) (Math.PI / 180.0) * armorStand.getLeftLegPose().getZ();
            this.rightLeg.xRot = (float) (Math.PI / 180.0) * armorStand.getRightLegPose().getX();
            this.rightLeg.yRot = (float) (Math.PI / 180.0) * armorStand.getRightLegPose().getY();
            this.rightLeg.zRot = (float) (Math.PI / 180.0) * armorStand.getRightLegPose().getZ();
            this.hat.copyFrom(this.head);
        }

        ModelPart head = this.bodyRoot.getChild("head");
        head.copyFrom(this.head);
        ModelPart hat = this.bodyRoot.getChild("hat");
        hat.copyFrom(this.hat);

        ModelPart body = this.bodyRoot.getChild("body");
        body.copyFrom(this.body);

        if (livingEntity instanceof Zombie zombie) {
            AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, zombie.isAggressive(), this.attackTime, ageInTicks);
        }

        ModelPart leftArm = this.bodyRoot.getChild("left_arm");
        leftArm.copyFrom(this.leftArm);
        if (!isSilm) {
            this.setScale(this.leftArm, 1.01F);
        } else {
            this.leftArm.x -= 0.3F;
            this.leftArm.xScale = 0.8F;
            this.leftArm.yScale = 1.01F;
        }
        this.setScale(leftArm, 1.0F);
        ModelPart rightArm = this.bodyRoot.getChild("right_arm");
        rightArm.copyFrom(this.rightArm);
        if (!isSilm) {
            this.setScale(this.rightArm, 1.01F);
        } else {
            this.rightArm.x += 0.3F;
            this.rightArm.xScale = 0.8F;
            this.rightArm.yScale = 1.01F;
        }
        this.setScale(rightArm, 1.0F);

        ModelPart leftLeg = this.bodyRoot.getChild("left_leg");
        leftLeg.copyFrom(this.leftLeg);
        ModelPart rightLeg = this.bodyRoot.getChild("right_leg");
        rightLeg.copyFrom(this.rightLeg);
        ModelPart leftBoot = this.bodyRoot.getChild("left_boot");
        leftBoot.copyFrom(this.leftLeg);
        ModelPart rightBoot = this.bodyRoot.getChild("right_boot");
        rightBoot.copyFrom(this.rightLeg);

    }

    public void render(LivingEntity livingEntity, PoseStack poseStack, MultiBufferSource multiBufferSource, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        RenderType COSMIC_ARMOR_RENDER_TYPE = AvaritiaRenderTypes.COSMIC_ARMOR;

        Minecraft mc = Minecraft.getInstance();

        Item headItem = livingEntity.getItemBySlot(EquipmentSlot.HEAD).getItem();
        Item chestItem = livingEntity.getItemBySlot(EquipmentSlot.CHEST).getItem();
        Item legsItem = livingEntity.getItemBySlot(EquipmentSlot.LEGS).getItem();
        Item feetItem = livingEntity.getItemBySlot(EquipmentSlot.FEET).getItem();

        long time = mc.level.getGameTime();

        double pulse = Math.sin(time / 10.0D) * 0.5D + 0.5D;
        double pulse_mag_sqr = pulse * pulse * pulse * pulse * pulse * pulse;

        float yaw = 0.0F;
        float pitch = 0.0F;
        float scale = 1.0F;

        if (AvaritiaForgeClient.inventoryRender) {
            scale = 100.0F;
        } else {
            yaw = (float) ((livingEntity.getYRot() * 2.0F) * Math.PI / 360.0D);
            pitch = -((float) ((livingEntity.getXRot() * 2.0F) * Math.PI / 360.0D));
        }

        AvaritiaShaders.cosmicArmorTime.set(time % Integer.MAX_VALUE);
        AvaritiaShaders.cosmicArmorYaw.set(yaw);
        AvaritiaShaders.cosmicArmorPitch.set(pitch);
        AvaritiaShaders.cosmicArmorExternalScale.set(scale);
        AvaritiaShaders.cosmicArmorOpacity.set(0.9F);
        AvaritiaShaders.cosmicArmorUVs.set(AvaritiaShaders.COSMIC_UVS);

        if (livingEntity instanceof Player player) {
            if (chestItem == ModItems.infinity_chestplate.get() && (player.getAbilities().flying)) {
                poseStack.pushPose();
                ModelPart leftWing = root.getChild("left_wing");
                ModelPart rightWing = root.getChild("right_wing");
                leftWing.render(poseStack, vertexConsumer, packedLight, packedOverlay);
                rightWing.render(poseStack, vertexConsumer, packedLight, packedOverlay);

                leftWing.render(poseStack, Res.ARMOR_WING_MASK.wrap(multiBufferSource.getBuffer(COSMIC_ARMOR_RENDER_TYPE)), packedLight, packedOverlay, red, green, blue, alpha);
                rightWing.render(poseStack, Res.ARMOR_WING_MASK.wrap(multiBufferSource.getBuffer(COSMIC_ARMOR_RENDER_TYPE)), packedLight, packedOverlay, red, green, blue, alpha);

                leftWing.render(poseStack, multiBufferSource.getBuffer(AvaritiaRenderTypes.WingGlow(Res.WING_GLOW_TEX)), packedLight, packedOverlay, 0.84F, 1.0F, 0.95F, (float) (pulse_mag_sqr * 0.5D));
                rightWing.render(poseStack, multiBufferSource.getBuffer(AvaritiaRenderTypes.WingGlow(Res.WING_GLOW_TEX)), packedLight, packedOverlay, 0.84F, 1.0F, 0.95F, (float) (pulse_mag_sqr * 0.5D));
                poseStack.popPose();
            }
        }

        if (headItem == ModItems.infinity_helmet.get()) {
            poseStack.pushPose();

            ModelPart head = this.bodyRoot.getChild("head");
            head.render(poseStack, Res.ARMOR_MASK.wrap(multiBufferSource.getBuffer(COSMIC_ARMOR_RENDER_TYPE)), packedLight, packedOverlay, red, green, blue, alpha);
            ModelPart hat = this.bodyRoot.getChild("hat");
            hat.render(poseStack, Res.ARMOR_MASK.wrap(multiBufferSource.getBuffer(COSMIC_ARMOR_RENDER_TYPE)), packedLight, packedOverlay, red, green, blue, alpha);

            poseStack.popPose();
        }

        if (chestItem == ModItems.infinity_chestplate.get()) {
            poseStack.pushPose();

            ModelPart body = this.bodyRoot.getChild("body");
            body.render(poseStack, Res.ARMOR_MASK.wrap(multiBufferSource.getBuffer(COSMIC_ARMOR_RENDER_TYPE)), packedLight, packedOverlay, red, green, blue, alpha);
            body.render(poseStack, multiBufferSource.getBuffer(AvaritiaRenderTypes.WingGlow(Res.EYE_TEX)), packedLight, packedOverlay, 0.84F, 1.0F, 0.95F, (float) (pulse_mag_sqr * 0.5D));
            ModelPart leftArm = this.bodyRoot.getChild("left_arm");
            leftArm.render(poseStack, Res.ARMOR_MASK.wrap(multiBufferSource.getBuffer(COSMIC_ARMOR_RENDER_TYPE)), packedLight, packedOverlay, red, green, blue, alpha);
            leftArm.render(poseStack, multiBufferSource.getBuffer(AvaritiaRenderTypes.WingGlow(Res.EYE_TEX)), packedLight, packedOverlay, 0.84F, 1.0F, 0.95F, (float) (pulse_mag_sqr * 0.5D));
            ModelPart rightArm = this.bodyRoot.getChild("right_arm");
            rightArm.render(poseStack, Res.ARMOR_MASK.wrap(multiBufferSource.getBuffer(COSMIC_ARMOR_RENDER_TYPE)), packedLight, packedOverlay, red, green, blue, alpha);
            rightArm.render(poseStack, multiBufferSource.getBuffer(AvaritiaRenderTypes.WingGlow(Res.EYE_TEX)), packedLight, packedOverlay, 0.84F, 1.0F, 0.95F, (float) (pulse_mag_sqr * 0.5D));

            poseStack.popPose();

        }
        if (legsItem == ModItems.infinity_pants.get()) {
            poseStack.pushPose();

            ModelPart leftLeg = this.bodyRoot.getChild("left_leg");
            leftLeg.render(poseStack, Res.ARMOR_MASK.wrap(multiBufferSource.getBuffer(COSMIC_ARMOR_RENDER_TYPE)), packedLight, packedOverlay, red, green, blue, alpha);
            leftLeg.render(poseStack, multiBufferSource.getBuffer(AvaritiaRenderTypes.WingGlow(Res.EYE_TEX)), packedLight, packedOverlay, 0.84F, 1.0F, 0.95F, (float) (pulse_mag_sqr * 0.5D));
            ModelPart rightLeg = this.bodyRoot.getChild("right_leg");
            rightLeg.render(poseStack, Res.ARMOR_MASK.wrap(multiBufferSource.getBuffer(COSMIC_ARMOR_RENDER_TYPE)), packedLight, packedOverlay, red, green, blue, alpha);
            rightLeg.render(poseStack, multiBufferSource.getBuffer(AvaritiaRenderTypes.WingGlow(Res.EYE_TEX)), packedLight, packedOverlay, 0.84F, 1.0F, 0.95F, (float) (pulse_mag_sqr * 0.5D));

            poseStack.popPose();
        }

        if (feetItem == ModItems.infinity_boots.get()) {
            poseStack.pushPose();

            ModelPart leftBoot = this.bodyRoot.getChild("left_boot");
            leftBoot.render(poseStack, Res.ARMOR_MASK.wrap(multiBufferSource.getBuffer(COSMIC_ARMOR_RENDER_TYPE)), packedLight, packedOverlay, red, green, blue, alpha);
            ModelPart rightBoot = this.bodyRoot.getChild("right_boot");
            rightBoot.render(poseStack, Res.ARMOR_MASK.wrap(multiBufferSource.getBuffer(COSMIC_ARMOR_RENDER_TYPE)), packedLight, packedOverlay, red, green, blue, alpha);

            poseStack.popPose();
        }

        if (headItem == ModItems.infinity_helmet.get() && chestItem == ModItems.infinity_chestplate.get() && legsItem == ModItems.infinity_pants.get() && feetItem == ModItems.infinity_boots.get()) {

            poseStack.pushPose();

            ModelPart hat = this.bodyRoot.getChild("hat");

            // Random random = new Random();
            // long frame = time / 3;
            // random.setSeed(frame * 1723609L);
            // float hue = random.nextFloat() * 6.0F;
            // float hue = time / 100.0F;
            float hue = (System.currentTimeMillis() - AvaritiaForgeClient.lastTime) / 2000.0F;

            int rgb = Color.HSBtoRGB(hue, 1.0F, 1.0F);
            float r = ((rgb >> 16) & 0xFF) / 255.0F;
            float g = ((rgb >> 8) & 0xFF) / 255.0F;
            float b = ((rgb >> 0) & 0xFF) / 255.0F;

            hat.render(poseStack, multiBufferSource.getBuffer(AvaritiaRenderTypes.Glow(Res.EYE_TEX)), packedLight, packedOverlay, r, g, b, alpha);

            super.renderToBuffer(poseStack, Res.ARMOR_MASK_INV.wrap(multiBufferSource.getBuffer(COSMIC_ARMOR_RENDER_TYPE)), packedLight, packedOverlay, red, green, blue, alpha);

            poseStack.popPose();
        }
    }

    public static Material material(final ResourceLocation t) {
        return new Material(InventoryMenu.BLOCK_ATLAS, t);
    }
}
