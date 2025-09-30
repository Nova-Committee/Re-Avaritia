package committee.nova.mods.avaritia.common.item.block;

import committee.nova.mods.avaritia.client.render.item.InfinityChestItemRender;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

/**
 * @author: cnlimiter
 */
public class InfinityChestItem extends BlockItem {
    public InfinityChestItem(Block block) {
        super(block, new Properties().rarity(ModRarities.LEGEND));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private InfinityChestItemRender renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new InfinityChestItemRender(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
                }
                return renderer;
            }
        });
    }
}
