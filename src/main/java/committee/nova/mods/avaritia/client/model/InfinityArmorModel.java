package committee.nova.mods.avaritia.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.client.AvaritiaForgeClient;
import committee.nova.mods.avaritia.client.shader.AvaritiaRenderTypes;
import committee.nova.mods.avaritia.client.shader.AvaritiaShaders;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.util.ToolUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/23 23:50
 * Version: 1.0
 */
public class InfinityArmorModel extends HumanoidModel<LivingEntity> {

    private boolean modelRender = false;
    private boolean playerFlying = false;
    private boolean player = false;
    private static boolean legs = true;

    private final Minecraft mc;
    private final MultiBufferSource bufferSource;

    public InfinityArmorModel() {
        super(createMesh(new CubeDeformation(1.0f), 0.0f).getRoot().bake(64, 64));
        this.mc = Minecraft.getInstance();
        this.bufferSource = this.mc.renderBuffers().bufferSource();
    }


    public static MeshDefinition createMesh(final CubeDeformation deformation, final float f, final boolean islegs) {
        InfinityArmorModel.legs = islegs;
        final int heightoffset = 0;
        final int legoffset = islegs ? 32 : 0;
        final MeshDefinition meshDefinition = new MeshDefinition();
        final PartDefinition p = meshDefinition.getRoot();
        p.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, deformation), PartPose.offset(0.0f, 0.0f + f, 0.0f));
        p.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, new CubeDeformation(0.5f)), PartPose.offset(0.0f, 0.0f, 0.0f));
        p.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f, deformation), PartPose.offset(0.0f, 0.0f + f, 0.0f));
        p.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, deformation), PartPose.offset(-5.0f, 2.0f + f, 0.0f));
        p.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, deformation), PartPose.offset(5.0f, 2.0f + f, 0.0f));
        p.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, deformation), PartPose.offset(-1.9f, 12.0f + f, 0.0f));
        p.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, deformation), PartPose.offset(1.9f, 12.0f + f, 0.0f));
        if (islegs) {
            p.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16 + legoffset).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f, new CubeDeformation(0.5f)), PartPose.offset(0.0f, (float) (0 + heightoffset), 0.0f));
            p.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16 + legoffset).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(0.5f)), PartPose.offset(-1.9f, (float) (12 + heightoffset), 0.0f));
            p.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16 + legoffset).mirror().addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(0.5f)), PartPose.offset(1.9f, (float) (12 + heightoffset), 0.0f));
        }
        return meshDefinition;
    }

    public static Material material(final ResourceLocation t) {
        return new Material(InventoryMenu.BLOCK_ATLAS, t);
    }

    private LayerDefinition rebuildWings() {
        final MeshDefinition m = new MeshDefinition();
        final PartDefinition p = m.getRoot();
        p.addOrReplaceChild("bipedRightWing",
                CubeListBuilder.create()
                        .texOffs(0, 0).mirror()
                        .addBox(0.0f, -11.6f, 0.0f, 0.0f, 32.0f, 32.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(-1.5f, 0.0f, 2.0f, 0.0f, 1.2566371f, 0.0f));
        p.addOrReplaceChild("bipedLeftWing",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(0.0f, -11.6f, 0.0f, 0.0f, 32.0f, 32.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(1.5f, 0.0f, 2.0f, 0.0f, -1.2566371f, 0.0f));
        return LayerDefinition.create(m, 64, 64);
    }

    private void renderToBufferWing(@NotNull PoseStack pPoseStack, @NotNull VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha) {
        final ModelPart h = this.rebuildWings().bakeRoot();
        ModelPart bipedRightWing = h.getChild("bipedRightWing");
        ModelPart bipedLeftWing = h.getChild("bipedLeftWing");
        bipedRightWing.render(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
        bipedLeftWing.render(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
    }

    @Override
    public void renderToBuffer(@NotNull PoseStack pPoseStack, @NotNull VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha) {
        super.renderToBuffer(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
        final InfinityArmorModel model = new InfinityArmorModel();
        this.copyBipedAngles(this, model);
        final long time = this.mc.player.level().getGameTime();
        final double pulse = Math.sin(time / 10.0) * 0.5 + 0.5;
        final double pulse_mag_sqr = pulse * pulse * pulse * pulse * pulse * pulse;
        float f;
        float f2;
        float f3;
        if (this.young) {
            f = 1.5f / this.babyHeadScale;
            f2 = 1.0f / this.babyBodyScale;
            f3 = 1.0f;
        } else {
            f = 1.0f;
            f2 = 0.9f;
            f3 = 0.0f;
        }

        AvaritiaShaders.cosmicArmorTime.set(mc.level.getGameTime() % Integer.MAX_VALUE);
        AvaritiaShaders.cosmicArmorOpacity.set(1.0f);
        if (AvaritiaForgeClient.inventoryRender) {
            AvaritiaShaders.cosmicArmorExternalScale.set(25.0f);
        } else {
            AvaritiaShaders.cosmicArmorExternalScale.set(1.0f);
            AvaritiaShaders.cosmicArmorYaw.set((float) (this.mc.player.getYRot() * 2.0f * 3.141592653589793 / 360.0));
            AvaritiaShaders.cosmicArmorPitch.set(-(float) (this.mc.player.getXRot() * 2.0f * 3.141592653589793 / 360.0));
        }

        pPoseStack.pushPose();
        pPoseStack.scale(f, f, f);
        pPoseStack.translate(0.0, this.babyYHeadOffset / 16.0f * f3, 0.0);
        this.head.render(pPoseStack, material(Res.ARMOR_MASK).buffer(this.bufferSource,  AvaritiaRenderTypes::armorMask), pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
        if (this.modelRender && !this.player) {
            this.headParts().forEach(t -> t.render(pPoseStack, material(Res.ARMOR_MASK_INV).buffer(this.bufferSource,  AvaritiaRenderTypes::armorMask), pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha));
        }
        pPoseStack.popPose();

        pPoseStack.pushPose();
        pPoseStack.scale(f2, f2, f2);
        pPoseStack.translate(0.0, this.bodyYOffset / 16.0f * f3, 0.0);
        this.bodyParts().forEach(t -> t.render(pPoseStack, material(Res.ARMOR_MASK).buffer(this.bufferSource,  AvaritiaRenderTypes::armorMask), pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha));
        if (this.modelRender) {
            this.bodyParts().forEach(t -> t.render(pPoseStack, material(Res.ARMOR_MASK_INV).buffer(this.bufferSource,  AvaritiaRenderTypes::armorMask), pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha));
        }
        this.bodyParts().forEach(t -> t.render(pPoseStack, this.vertex(AvaritiaRenderTypes.Glow(Res.EYE_TEX)), pPackedLight, pPackedOverlay, 0.84f, 1.0f, 0.95f, (float) (pulse_mag_sqr * 0.5)));
        pPoseStack.popPose();

        if (this.playerFlying && !AvaritiaForgeClient.inventoryRender) {
            pPoseStack.pushPose();
            this.rebuildWings();
            pPoseStack.scale(f2, f2, f2);
            pPoseStack.translate(0.0, this.bodyYOffset / 16.0f * f3, 0.0);
            model.renderToBufferWing(pPoseStack, this.vertex(RenderType.armorCutoutNoCull(Res.WING_TEX)), pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
            //Const.LOGGER.info(material(WING));
            model.renderToBufferWing(pPoseStack, material(Res.WING_TEX).buffer(this.bufferSource, AvaritiaRenderTypes::armorMask), pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
            model.renderToBufferWing(pPoseStack, this.vertex(AvaritiaRenderTypes.WingGlow(Res.WING_GLOW_TEX)), pPackedLight, pPackedOverlay, 0.84f, 1.0f, 0.95f, (float) (pulse_mag_sqr * 0.5));
            pPoseStack.popPose();
        }
    }

    public void update(final LivingEntity e, final ItemStack itemStack, final EquipmentSlot equipmentSlot) {
        final ItemStack itemBySlot = e.getItemBySlot(equipmentSlot);
        final boolean hasHat = itemBySlot.getItem() == ModItems.infinity_helmet.get();
        final boolean hasChest = itemBySlot.getItem() == ModItems.infinity_chestplate.get();
        final boolean hasLeg = itemBySlot.getItem() == ModItems.infinity_pants.get();
        final boolean hasFoot = itemBySlot.getItem() == ModItems.infinity_boots.get();

        this.modelRender = false;
        this.playerFlying = false;
        this.player = false;

        if (hasHat && hasChest && hasLeg && hasFoot) {
            this.modelRender = true;
        }
        if (e instanceof Player) {
            this.player = true;
            if (hasChest && ((Player) e).getAbilities().flying) {
                this.playerFlying = true;
            }
        }
        this.crouching = e.isCrouching();
        this.young = e.isBaby();
        this.riding = e.isPassenger();
    }

    public VertexConsumer vertex(final RenderType t) {
        return this.bufferSource.getBuffer(t);
    }


    private void copyPartAngles(final ModelPart from, final ModelPart to) {
        to.xRot = from.xRot;
        to.yRot = from.yRot;
        to.zRot = from.zRot;
        to.x = from.x;
        to.y = from.y;
        to.z = from.z;
    }

    private void copyBipedAngles(final HumanoidModel<LivingEntity> from, final HumanoidModel<LivingEntity> to) {
        this.copyPartAngles(from.head, to.head);
        this.copyPartAngles(from.hat, to.hat);
        this.copyPartAngles(from.body, to.body);
        this.copyPartAngles(from.leftArm, to.leftArm);
        this.copyPartAngles(from.leftLeg, to.leftLeg);
        this.copyPartAngles(from.rightArm, to.rightArm);
        this.copyPartAngles(from.rightLeg, to.rightLeg);
    }

    public static class PlayerRender extends RenderLayer<Player, PlayerModel<Player>> {
        public PlayerRender(final RenderLayerParent<Player, PlayerModel<Player>> t) {
            super(t);
        }

        public Iterable<ModelPart> playerParts() {
            return ImmutableList.of((this.getParentModel()).head, (this.getParentModel()).hat, (this.getParentModel()).body, (this.getParentModel()).leftArm, this.getParentModel().rightArm, (this.getParentModel()).leftLeg, (this.getParentModel()).rightLeg);
        }

        @Override
        public void render(final @NotNull PoseStack pPoseStack, final @NotNull MultiBufferSource pBuffer, final int pPackedLight, final @NotNull Player l, final float pLimbSwing, float pLimbSwingAmount, float pPartialTick, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
            if (ToolUtils.isInfinite(l)) {
                AvaritiaShaders.cosmicOpacity.set(2.0f);
                this.playerParts().forEach(t -> t.render(pPoseStack, InfinityArmorModel.material(Res.ARMOR_MASK_INV).buffer(pBuffer, AvaritiaRenderTypes::armorMask), pPackedLight, 1, 1.0f, 1.0f, 1.0f, 1.0f));
            }
        }
    }
}
