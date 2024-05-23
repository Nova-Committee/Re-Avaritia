package committee.nova.mods.avaritia.client.model;// Made with Blockbench 4.2.4
// Exported for Minecraft version 1.17 - 1.18 with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import committee.nova.mods.avaritia.Static;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class GapingVoidModel<T extends Entity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(Static.MOD_ID, "gaping_void"), "main");
    private final ModelPart gaping_void;

    public GapingVoidModel(ModelPart root) {
        this.gaping_void = root.getChild("gaping_void");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition gaping_void = partdefinition.addOrReplaceChild("gaping_void", CubeListBuilder.create(), PartPose.offset(0.0F, 16.0F, 0.0F));

        PartDefinition bone = gaping_void.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition bone2 = bone.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, 7.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

        PartDefinition cube1_r1 = bone2.addOrReplaceChild("cube1_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -1.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube1_r2 = bone2.addOrReplaceChild("cube1_r2", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -1.0F, 0.0F, -1.1781F, 0.0F));

        PartDefinition cube1_r3 = bone2.addOrReplaceChild("cube1_r3", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -1.0F, 0.0F, -0.3927F, 0.0F));

        PartDefinition cube1_r4 = bone2.addOrReplaceChild("cube1_r4", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -1.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone3 = bone.addOrReplaceChild("bone3", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 2.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition cube1_r5 = bone3.addOrReplaceChild("cube1_r5", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube1_r6 = bone3.addOrReplaceChild("cube1_r6", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.1781F, 0.0F));

        PartDefinition cube1_r7 = bone3.addOrReplaceChild("cube1_r7", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.0F));

        PartDefinition cube1_r8 = bone3.addOrReplaceChild("cube1_r8", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone5 = bone.addOrReplaceChild("bone5", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 2.0F, 1.5708F, 0.0F, 1.5708F));

        PartDefinition cube1_r9 = bone5.addOrReplaceChild("cube1_r9", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube1_r10 = bone5.addOrReplaceChild("cube1_r10", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.1781F, 0.0F));

        PartDefinition cube1_r11 = bone5.addOrReplaceChild("cube1_r11", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.0F));

        PartDefinition cube1_r12 = bone5.addOrReplaceChild("cube1_r12", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone7 = bone.addOrReplaceChild("bone7", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -2.0F, -1.5708F, 0.0F, -1.5708F));

        PartDefinition cube1_r13 = bone7.addOrReplaceChild("cube1_r13", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube1_r14 = bone7.addOrReplaceChild("cube1_r14", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.1781F, 0.0F));

        PartDefinition cube1_r15 = bone7.addOrReplaceChild("cube1_r15", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.0F));

        PartDefinition cube1_r16 = bone7.addOrReplaceChild("cube1_r16", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone4 = gaping_void.addOrReplaceChild("bone4", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.0F));

        PartDefinition bone6 = bone4.addOrReplaceChild("bone6", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, 7.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

        PartDefinition cube1_r17 = bone6.addOrReplaceChild("cube1_r17", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -1.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube1_r18 = bone6.addOrReplaceChild("cube1_r18", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -1.0F, 0.0F, -1.1781F, 0.0F));

        PartDefinition cube1_r19 = bone6.addOrReplaceChild("cube1_r19", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -1.0F, 0.0F, -0.3927F, 0.0F));

        PartDefinition cube1_r20 = bone6.addOrReplaceChild("cube1_r20", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -1.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone8 = bone4.addOrReplaceChild("bone8", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 2.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition cube1_r21 = bone8.addOrReplaceChild("cube1_r21", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube1_r22 = bone8.addOrReplaceChild("cube1_r22", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.1781F, 0.0F));

        PartDefinition cube1_r23 = bone8.addOrReplaceChild("cube1_r23", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.0F));

        PartDefinition cube1_r24 = bone8.addOrReplaceChild("cube1_r24", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone9 = bone4.addOrReplaceChild("bone9", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 2.0F, 1.5708F, 0.0F, 1.5708F));

        PartDefinition cube1_r25 = bone9.addOrReplaceChild("cube1_r25", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube1_r26 = bone9.addOrReplaceChild("cube1_r26", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.1781F, 0.0F));

        PartDefinition cube1_r27 = bone9.addOrReplaceChild("cube1_r27", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.0F));

        PartDefinition cube1_r28 = bone9.addOrReplaceChild("cube1_r28", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone10 = bone4.addOrReplaceChild("bone10", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -2.0F, -1.5708F, 0.0F, -1.5708F));

        PartDefinition cube1_r29 = bone10.addOrReplaceChild("cube1_r29", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube1_r30 = bone10.addOrReplaceChild("cube1_r30", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.1781F, 0.0F));

        PartDefinition cube1_r31 = bone10.addOrReplaceChild("cube1_r31", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.0F));

        PartDefinition cube1_r32 = bone10.addOrReplaceChild("cube1_r32", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone21 = gaping_void.addOrReplaceChild("bone21", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition bone22 = bone21.addOrReplaceChild("bone22", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, 7.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

        PartDefinition cube1_r33 = bone22.addOrReplaceChild("cube1_r33", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -1.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube1_r34 = bone22.addOrReplaceChild("cube1_r34", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -1.0F, 0.0F, -1.1781F, 0.0F));

        PartDefinition cube1_r35 = bone22.addOrReplaceChild("cube1_r35", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -1.0F, 0.0F, -0.3927F, 0.0F));

        PartDefinition cube1_r36 = bone22.addOrReplaceChild("cube1_r36", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -1.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone23 = bone21.addOrReplaceChild("bone23", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 2.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition cube1_r37 = bone23.addOrReplaceChild("cube1_r37", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube1_r38 = bone23.addOrReplaceChild("cube1_r38", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.1781F, 0.0F));

        PartDefinition cube1_r39 = bone23.addOrReplaceChild("cube1_r39", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.0F));

        PartDefinition cube1_r40 = bone23.addOrReplaceChild("cube1_r40", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone24 = bone21.addOrReplaceChild("bone24", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 2.0F, 1.5708F, 0.0F, 1.5708F));

        PartDefinition cube1_r41 = bone24.addOrReplaceChild("cube1_r41", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube1_r42 = bone24.addOrReplaceChild("cube1_r42", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.1781F, 0.0F));

        PartDefinition cube1_r43 = bone24.addOrReplaceChild("cube1_r43", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.0F));

        PartDefinition cube1_r44 = bone24.addOrReplaceChild("cube1_r44", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone25 = bone21.addOrReplaceChild("bone25", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -2.0F, -1.5708F, 0.0F, -1.5708F));

        PartDefinition cube1_r45 = bone25.addOrReplaceChild("cube1_r45", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube1_r46 = bone25.addOrReplaceChild("cube1_r46", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.1781F, 0.0F));

        PartDefinition cube1_r47 = bone25.addOrReplaceChild("cube1_r47", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.0F));

        PartDefinition cube1_r48 = bone25.addOrReplaceChild("cube1_r48", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone26 = gaping_void.addOrReplaceChild("bone26", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.1781F, 0.0F));

        PartDefinition bone27 = bone26.addOrReplaceChild("bone27", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, 7.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

        PartDefinition cube1_r49 = bone27.addOrReplaceChild("cube1_r49", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -1.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube1_r50 = bone27.addOrReplaceChild("cube1_r50", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -1.0F, 0.0F, -1.1781F, 0.0F));

        PartDefinition cube1_r51 = bone27.addOrReplaceChild("cube1_r51", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -1.0F, 0.0F, -0.3927F, 0.0F));

        PartDefinition cube1_r52 = bone27.addOrReplaceChild("cube1_r52", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -1.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone28 = bone26.addOrReplaceChild("bone28", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 2.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition cube1_r53 = bone28.addOrReplaceChild("cube1_r53", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube1_r54 = bone28.addOrReplaceChild("cube1_r54", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.1781F, 0.0F));

        PartDefinition cube1_r55 = bone28.addOrReplaceChild("cube1_r55", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.0F));

        PartDefinition cube1_r56 = bone28.addOrReplaceChild("cube1_r56", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone29 = bone26.addOrReplaceChild("bone29", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 2.0F, 1.5708F, 0.0F, 1.5708F));

        PartDefinition cube1_r57 = bone29.addOrReplaceChild("cube1_r57", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube1_r58 = bone29.addOrReplaceChild("cube1_r58", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.1781F, 0.0F));

        PartDefinition cube1_r59 = bone29.addOrReplaceChild("cube1_r59", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.0F));

        PartDefinition cube1_r60 = bone29.addOrReplaceChild("cube1_r60", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone30 = bone26.addOrReplaceChild("bone30", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -2.0F, -1.5708F, 0.0F, -1.5708F));

        PartDefinition cube1_r61 = bone30.addOrReplaceChild("cube1_r61", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube1_r62 = bone30.addOrReplaceChild("cube1_r62", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.1781F, 0.0F));

        PartDefinition cube1_r63 = bone30.addOrReplaceChild("cube1_r63", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.0F));

        PartDefinition cube1_r64 = bone30.addOrReplaceChild("cube1_r64", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone31 = gaping_void.addOrReplaceChild("bone31", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone32 = bone31.addOrReplaceChild("bone32", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, 7.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

        PartDefinition cube1_r65 = bone32.addOrReplaceChild("cube1_r65", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -1.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube1_r66 = bone32.addOrReplaceChild("cube1_r66", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -1.0F, 0.0F, -1.1781F, 0.0F));

        PartDefinition cube1_r67 = bone32.addOrReplaceChild("cube1_r67", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -1.0F, 0.0F, -0.3927F, 0.0F));

        PartDefinition cube1_r68 = bone32.addOrReplaceChild("cube1_r68", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -1.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone33 = bone31.addOrReplaceChild("bone33", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 2.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition cube1_r69 = bone33.addOrReplaceChild("cube1_r69", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube1_r70 = bone33.addOrReplaceChild("cube1_r70", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.1781F, 0.0F));

        PartDefinition cube1_r71 = bone33.addOrReplaceChild("cube1_r71", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.0F));

        PartDefinition cube1_r72 = bone33.addOrReplaceChild("cube1_r72", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone34 = bone31.addOrReplaceChild("bone34", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 2.0F, 1.5708F, 0.0F, 1.5708F));

        PartDefinition cube1_r73 = bone34.addOrReplaceChild("cube1_r73", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube1_r74 = bone34.addOrReplaceChild("cube1_r74", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.1781F, 0.0F));

        PartDefinition cube1_r75 = bone34.addOrReplaceChild("cube1_r75", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.0F));

        PartDefinition cube1_r76 = bone34.addOrReplaceChild("cube1_r76", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone35 = bone31.addOrReplaceChild("bone35", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -2.0F, -1.5708F, 0.0F, -1.5708F));

        PartDefinition cube1_r77 = bone35.addOrReplaceChild("cube1_r77", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube1_r78 = bone35.addOrReplaceChild("cube1_r78", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.1781F, 0.0F));

        PartDefinition cube1_r79 = bone35.addOrReplaceChild("cube1_r79", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.0F));

        PartDefinition cube1_r80 = bone35.addOrReplaceChild("cube1_r80", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone36 = gaping_void.addOrReplaceChild("bone36", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.3927F, 0.0F));

        PartDefinition bone37 = bone36.addOrReplaceChild("bone37", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, 7.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

        PartDefinition cube1_r81 = bone37.addOrReplaceChild("cube1_r81", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -1.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube1_r82 = bone37.addOrReplaceChild("cube1_r82", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -1.0F, 0.0F, -1.1781F, 0.0F));

        PartDefinition cube1_r83 = bone37.addOrReplaceChild("cube1_r83", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -1.0F, 0.0F, -0.3927F, 0.0F));

        PartDefinition cube1_r84 = bone37.addOrReplaceChild("cube1_r84", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -1.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone38 = bone36.addOrReplaceChild("bone38", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 2.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition cube1_r85 = bone38.addOrReplaceChild("cube1_r85", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube1_r86 = bone38.addOrReplaceChild("cube1_r86", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.1781F, 0.0F));

        PartDefinition cube1_r87 = bone38.addOrReplaceChild("cube1_r87", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.0F));

        PartDefinition cube1_r88 = bone38.addOrReplaceChild("cube1_r88", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone39 = bone36.addOrReplaceChild("bone39", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 2.0F, 1.5708F, 0.0F, 1.5708F));

        PartDefinition cube1_r89 = bone39.addOrReplaceChild("cube1_r89", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube1_r90 = bone39.addOrReplaceChild("cube1_r90", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.1781F, 0.0F));

        PartDefinition cube1_r91 = bone39.addOrReplaceChild("cube1_r91", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.0F));

        PartDefinition cube1_r92 = bone39.addOrReplaceChild("cube1_r92", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone40 = bone36.addOrReplaceChild("bone40", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -2.0F, -1.5708F, 0.0F, -1.5708F));

        PartDefinition cube1_r93 = bone40.addOrReplaceChild("cube1_r93", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube1_r94 = bone40.addOrReplaceChild("cube1_r94", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.1781F, 0.0F));

        PartDefinition cube1_r95 = bone40.addOrReplaceChild("cube1_r95", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.0F));

        PartDefinition cube1_r96 = bone40.addOrReplaceChild("cube1_r96", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone11 = gaping_void.addOrReplaceChild("bone11", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

        PartDefinition bone12 = bone11.addOrReplaceChild("bone12", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, 7.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

        PartDefinition cube1_r97 = bone12.addOrReplaceChild("cube1_r97", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -1.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube1_r98 = bone12.addOrReplaceChild("cube1_r98", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -1.0F, 0.0F, -1.1781F, 0.0F));

        PartDefinition cube1_r99 = bone12.addOrReplaceChild("cube1_r99", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -1.0F, 0.0F, -0.3927F, 0.0F));

        PartDefinition cube1_r100 = bone12.addOrReplaceChild("cube1_r100", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -1.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone13 = bone11.addOrReplaceChild("bone13", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 2.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition cube1_r101 = bone13.addOrReplaceChild("cube1_r101", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube1_r102 = bone13.addOrReplaceChild("cube1_r102", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.1781F, 0.0F));

        PartDefinition cube1_r103 = bone13.addOrReplaceChild("cube1_r103", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.0F));

        PartDefinition cube1_r104 = bone13.addOrReplaceChild("cube1_r104", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone14 = bone11.addOrReplaceChild("bone14", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 2.0F, 1.5708F, 0.0F, 1.5708F));

        PartDefinition cube1_r105 = bone14.addOrReplaceChild("cube1_r105", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube1_r106 = bone14.addOrReplaceChild("cube1_r106", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.1781F, 0.0F));

        PartDefinition cube1_r107 = bone14.addOrReplaceChild("cube1_r107", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.0F));

        PartDefinition cube1_r108 = bone14.addOrReplaceChild("cube1_r108", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone15 = bone11.addOrReplaceChild("bone15", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -2.0F, -1.5708F, 0.0F, -1.5708F));

        PartDefinition cube1_r109 = bone15.addOrReplaceChild("cube1_r109", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube1_r110 = bone15.addOrReplaceChild("cube1_r110", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.1781F, 0.0F));

        PartDefinition cube1_r111 = bone15.addOrReplaceChild("cube1_r111", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.0F));

        PartDefinition cube1_r112 = bone15.addOrReplaceChild("cube1_r112", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone16 = gaping_void.addOrReplaceChild("bone16", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.1781F, 0.0F));

        PartDefinition bone17 = bone16.addOrReplaceChild("bone17", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, 7.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

        PartDefinition cube1_r113 = bone17.addOrReplaceChild("cube1_r113", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -1.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube1_r114 = bone17.addOrReplaceChild("cube1_r114", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -1.0F, 0.0F, -1.1781F, 0.0F));

        PartDefinition cube1_r115 = bone17.addOrReplaceChild("cube1_r115", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -1.0F, 0.0F, -0.3927F, 0.0F));

        PartDefinition cube1_r116 = bone17.addOrReplaceChild("cube1_r116", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -1.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone18 = bone16.addOrReplaceChild("bone18", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 2.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition cube1_r117 = bone18.addOrReplaceChild("cube1_r117", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube1_r118 = bone18.addOrReplaceChild("cube1_r118", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.1781F, 0.0F));

        PartDefinition cube1_r119 = bone18.addOrReplaceChild("cube1_r119", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.0F));

        PartDefinition cube1_r120 = bone18.addOrReplaceChild("cube1_r120", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone19 = bone16.addOrReplaceChild("bone19", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 2.0F, 1.5708F, 0.0F, 1.5708F));

        PartDefinition cube1_r121 = bone19.addOrReplaceChild("cube1_r121", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube1_r122 = bone19.addOrReplaceChild("cube1_r122", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.1781F, 0.0F));

        PartDefinition cube1_r123 = bone19.addOrReplaceChild("cube1_r123", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.0F));

        PartDefinition cube1_r124 = bone19.addOrReplaceChild("cube1_r124", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone20 = bone16.addOrReplaceChild("bone20", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -2.0F, -1.5708F, 0.0F, -1.5708F));

        PartDefinition cube1_r125 = bone20.addOrReplaceChild("cube1_r125", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube1_r126 = bone20.addOrReplaceChild("cube1_r126", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.1781F, 0.0F));

        PartDefinition cube1_r127 = bone20.addOrReplaceChild("cube1_r127", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.0F));

        PartDefinition cube1_r128 = bone20.addOrReplaceChild("cube1_r128", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        gaping_void.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
