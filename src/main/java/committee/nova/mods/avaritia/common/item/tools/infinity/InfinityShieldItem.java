package committee.nova.mods.avaritia.common.item.tools.infinity;

import committee.nova.mods.avaritia.init.registry.ModItems;

import committee.nova.mods.avaritia.api.iface.item.IUndamageable;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.component.BlocksAttacks;

import java.util.List;
import java.util.Optional;

public class InfinityShieldItem extends ShieldItem implements IUndamageable {
    public InfinityShieldItem() {
        super((ModItems.properties())
                .rarity(ModRarities.COSMIC.getValue())
                .stacksTo(1)
                .fireResistant()
                .equippableUnswappable(EquipmentSlot.OFFHAND)
                .delayedComponent(
                        DataComponents.BLOCKS_ATTACKS,
                        context -> new BlocksAttacks(
                                0.01F,
                                0F,
                                List.of(new BlocksAttacks.DamageReduction(180.0F, Optional.empty(), 9999.0F, 1.0F)),
                                new BlocksAttacks.ItemDamageFunction(9999.0F, 0F, 0.01F),
                                Optional.of(context.getOrThrow(DamageTypeTags.BYPASSES_SHIELD)),
                                Optional.of(SoundEvents.SHIELD_BLOCK),
                                Optional.of(SoundEvents.SHIELD_BREAK)
                        )
                )
                .component(DataComponents.BREAK_SOUND, SoundEvents.SHIELD_BREAK));

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
