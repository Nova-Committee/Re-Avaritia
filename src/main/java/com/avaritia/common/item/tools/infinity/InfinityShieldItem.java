package com.avaritia.common.item.tools.infinity;

import com.avaritia.api.iface.item.IUndamageable;
import com.avaritia.init.registry.ModRarities;
import net.minecraft.world.item.ShieldItem;

public class InfinityShieldItem extends ShieldItem implements IUndamageable {
    public InfinityShieldItem() {
        super((new Properties())
                .rarity(ModRarities.COSMIC.getValue())
                .stacksTo(1)
                .fireResistant());
    }

//    @Override
//    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
//        consumer.accept(new IClientItemExtensions() {
//            private InfinityShieldRender renderer;
//
//            @Override
//            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
//                if (renderer == null) {
//                    renderer = new InfinityShieldRender(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
//                }
//                return renderer;
//            }
//        });
//    }

}
