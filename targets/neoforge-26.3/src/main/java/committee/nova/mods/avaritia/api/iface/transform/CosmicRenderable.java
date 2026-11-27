package committee.nova.mods.avaritia.api.iface.transform;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public interface CosmicRenderable {

    void renderCosmicLayer(
            ItemStack stack,
            ItemDisplayContext context,
            PoseStack poseStack,
            SubmitNodeCollector source,
            int light,
            int overlay
    );
}