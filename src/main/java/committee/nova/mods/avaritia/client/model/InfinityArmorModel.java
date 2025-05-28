package committee.nova.mods.avaritia.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.api.client.util.ColorUtils;
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
import net.minecraft.client.renderer.RenderStateShard;
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
public class InfinityArmorModel extends HumanoidModel<Player> {

    public static ResourceLocation MASK = Const.rl("models/infinity_armor_mask");
    public static ResourceLocation MASK_INV = Const.rl("models/infinity_armor_mask_inv");
    public static ResourceLocation WING = Const.rl("models/infinity_armor_mask_wings");
    private static boolean modelRender;
    private static boolean playerFlying;
    private static boolean player;
    private static boolean legs = true;

    private final Minecraft mc;
    private final MultiBufferSource bufferSource;
    private final Random random;
    private final HumanoidModel<Player> humanoidModel;


    public InfinityArmorModel(ModelPart pRoot, final int x) {
        super(createMesh(new CubeDeformation(1.0f), 0.0f).getRoot().bake(64, 64));
        this.mc = Minecraft.getInstance();
        this.bufferSource = this.mc.renderBuffers().bufferSource();
        this.random = new Random();
        this.humanoidModel = new HumanoidModel<>(createMesh(new CubeDeformation(0.0f), 0.0f).getRoot().bake(64, 64));
    }

    public InfinityArmorModel(ModelPart pRoot) {
        super(pRoot);
        this.mc = Minecraft.getInstance();
        this.bufferSource = this.mc.renderBuffers().bufferSource();
        this.random = new Random();
        this.humanoidModel = new HumanoidModel<>(createMesh(new CubeDeformation(0.0f), 0.0f).getRoot().bake(64, 64));
    }

    private static RenderType mask2(final ResourceLocation tex) {
        return RenderType.create("", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 0, RenderType.CompositeState.builder()
                .setShaderState(new RenderStateShard.ShaderStateShard(() -> AvaritiaShaders.COSMIC_SHADER))
                .setTextureState(new RenderStateShard.TextureStateShard(tex, false, false))
                .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
                .setLightmapState(RenderType.LIGHTMAP)
                .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                .setCullState(RenderType.NO_CULL)
                .createCompositeState(true));
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

    private void renderToBufferWing(@NotNull PoseStack pPoseStack, @NotNull VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, int color) {
        final ModelPart h = this.rebuildWings().bakeRoot();
        ModelPart bipedRightWing = h.getChild("bipedRightWing");
        ModelPart bipedLeftWing = h.getChild("bipedLeftWing");
        bipedRightWing.render(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, color);
        bipedLeftWing.render(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, color);
    }

    @Override
    public void renderToBuffer(@NotNull PoseStack pPoseStack, @NotNull VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, int color) {
        final InfinityArmorModel model = new InfinityArmorModel(this.rebuildWings().bakeRoot(), 0);
        this.copyBipedAngles(this, this.humanoidModel);
        super.renderToBuffer(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, color);
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
        AvaritiaShaders.cosmicOpacity.set(1.0f);
        if (AvaritiaShaders.inventoryRender) {
            AvaritiaShaders.cosmicExternalScale.set(25.0f);
        } else {
            AvaritiaShaders.cosmicExternalScale.set(1.0f);
            AvaritiaShaders.cosmicYaw.set((float) (this.mc.player.getYRot() * 2.0f * 3.141592653589793 / 360.0));
            AvaritiaShaders.cosmicPitch.set(-(float) (this.mc.player.getXRot() * 2.0f * 3.141592653589793 / 360.0));
        }

        pPoseStack.pushPose();
        pPoseStack.scale(f, f, f);
        pPoseStack.translate(0.0, this.babyYHeadOffset / 16.0f * f3, 0.0);
        this.head.render(pPoseStack, material(MASK).buffer(this.bufferSource, AvaritiaRenderTypes::mask), pPackedLight, pPackedOverlay, color);
        if (InfinityArmorModel.modelRender && !InfinityArmorModel.player) {
            this.hatsOver().forEach(t -> t.render(pPoseStack, material(MASK_INV).buffer(this.bufferSource, AvaritiaRenderTypes::mask), pPackedLight, pPackedOverlay, color));
        }
        pPoseStack.popPose();

        pPoseStack.pushPose();
        pPoseStack.scale(f2, f2, f2);
        pPoseStack.translate(0.0, this.bodyYOffset / 16.0f * f3, 0.0);
        this.bodyParts().forEach(t -> t.render(pPoseStack, material(MASK).buffer(this.bufferSource, AvaritiaRenderTypes::mask), pPackedLight, pPackedOverlay, color));
        if (InfinityArmorModel.modelRender && !InfinityArmorModel.player) {
            this.bodyPartsOver().forEach(t -> t.render(pPoseStack, material(MASK_INV).buffer(this.bufferSource, AvaritiaRenderTypes::mask), pPackedLight, pPackedOverlay, color));
        }
        this.bodyParts().forEach(t -> t.render(pPoseStack, this.vertex(AvaritiaRenderTypes.glow(Res.EYE_TEX)), pPackedLight, pPackedOverlay, (int) (pulse_mag_sqr * 0.5)));
        pPoseStack.popPose();

        pPoseStack.pushPose();
        this.random.setSeed(time / 3L * 1723609L);
        final int col = ColorUtils.HSBToRGB(this.random.nextFloat() * 6.0f, 1.0f, 1.0f);
        pPoseStack.scale(f, f, f);
        pPoseStack.translate(0.0, this.babyYHeadOffset / 16.0f * f3, -0.029999999329447746);
        this.hat.render(pPoseStack, material(MASK).buffer(this.bufferSource, AvaritiaRenderTypes::mask), pPackedLight, pPackedOverlay, color);
        if (InfinityArmorModel.modelRender && !AvaritiaShaders.inventoryRender) {
            this.hat.render(pPoseStack, this.vertex(AvaritiaRenderTypes.COSMIC_ARMOR), pPackedLight, pPackedOverlay, col);
        }
        pPoseStack.popPose();

        if (InfinityArmorModel.playerFlying && !AvaritiaShaders.inventoryRender) {
            pPoseStack.pushPose();
            this.rebuildWings();
            pPoseStack.scale(f2, f2, f2);
            pPoseStack.translate(0.0, this.bodyYOffset / 16.0f * f3, 0.0);
            model.renderToBufferWing(pPoseStack, this.mc.renderBuffers().bufferSource().getBuffer(RenderType.armorCutoutNoCull(Res.WING_TEX)), pPackedLight, pPackedOverlay, color);
            //Static.LOGGER.info(material(WING));
            model.renderToBufferWing(pPoseStack, material(WING).buffer(this.bufferSource, AvaritiaRenderTypes::mask), pPackedLight, pPackedOverlay, color);
            model.renderToBufferWing(pPoseStack, this.mc.renderBuffers().bufferSource().getBuffer(AvaritiaRenderTypes.wing(Res.WING_GLOW_TEX)), pPackedLight, pPackedOverlay, ColorUtils.HSBToRGB(0.84f, 1.0f, 0.95f));
            pPoseStack.popPose();
        }
    }

    public void update(final LivingEntity e, final ItemStack itemStack, final EquipmentSlot equipmentSlot) {
        InfinityArmorModel.modelRender = false;
        InfinityArmorModel.playerFlying = false;
        InfinityArmorModel.player = false;
        final ItemStack hats = e.getItemBySlot(EquipmentSlot.HEAD);
        final ItemStack chest = e.getItemBySlot(EquipmentSlot.CHEST);
        final ItemStack leg = e.getItemBySlot(EquipmentSlot.LEGS);
        final ItemStack foot = e.getItemBySlot(EquipmentSlot.FEET);
        final boolean hasHat = hats.getItem() == ModItems.infinity_helmet.get();
        final boolean hasChest = chest.getItem() == ModItems.infinity_chestplate.get();
        final boolean hasLeg = leg.getItem() == ModItems.infinity_pants.get();
        final boolean hasFoot = foot.getItem() == ModItems.infinity_boots.get();
        if (hasHat && hasChest && hasLeg && hasFoot) {
            InfinityArmorModel.modelRender = true;
        }
        if (e instanceof Player) {
            InfinityArmorModel.player = true;
            if (hasChest && ((Player) e).getAbilities().flying) {
                InfinityArmorModel.playerFlying = true;
            }
        }
        this.crouching = e.isCrouching();
        this.young = e.isBaby();
        this.riding = e.isPassenger();
    }

    public VertexConsumer vertex(final RenderType t) {
        return this.bufferSource.getBuffer(t);
    }

    @Override
    public @NotNull Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(this.body, this.rightArm, this.leftArm, this.rightLeg, this.leftLeg);
    }

    public Iterable<ModelPart> hatsOver() {
        return ImmutableList.of(this.humanoidModel.hat, this.humanoidModel.head);
    }

    public Iterable<ModelPart> bodyPartsOver() {
        return ImmutableList.of(this.humanoidModel.body, this.humanoidModel.rightArm, this.humanoidModel.leftArm, this.humanoidModel.rightLeg, this.humanoidModel.leftLeg);
    }

    private void copyPartAngles(final ModelPart from, final ModelPart to) {
        to.xRot = from.xRot;
        to.yRot = from.yRot;
        to.zRot = from.zRot;
        to.x = from.x;
        to.y = from.y;
        to.z = from.z;
    }

    private void copyBipedAngles(final HumanoidModel<Player> from, final HumanoidModel<Player> to) {
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
                this.playerParts().forEach(t -> t.render(pPoseStack, InfinityArmorModel.material(MASK_INV).buffer(pBuffer, InfinityArmorModel::mask2), pPackedLight, 1, ColorUtils.HSBToRGB(1.0f, 1.0f, 1.0f)));
            }
        }
    }
}
