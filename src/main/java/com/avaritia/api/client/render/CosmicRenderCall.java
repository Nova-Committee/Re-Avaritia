package com.avaritia.api.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.avaritia.api.iface.transform.CosmicRenderable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class CosmicRenderCall {

    public final CosmicRenderable model;

    public final ItemStack stack;

    public final ItemDisplayContext context;

    public final int light;
    public final int overlay;

    public final Matrix4f pose;
    public final Matrix3f normal;

    public CosmicRenderCall(
            CosmicRenderable model,
            ItemStack stack,
            ItemDisplayContext context,
            PoseStack poseStack,
            int light,
            int overlay
    ) {
        this.model = model;
        this.stack = stack;
        this.context = context;

        this.light = light;
        this.overlay = overlay;

        PoseStack.Pose pose = poseStack.last();

        this.pose = new Matrix4f(pose.pose());
        this.normal = new Matrix3f(pose.normal());
    }
}
