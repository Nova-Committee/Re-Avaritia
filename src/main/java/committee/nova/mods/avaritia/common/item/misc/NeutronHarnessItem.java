package committee.nova.mods.avaritia.common.item.misc;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.iface.IUndamageable;
import committee.nova.mods.avaritia.api.iface.InitEnchantItem;
import committee.nova.mods.avaritia.init.registry.ModArmorMaterial;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import committee.nova.mods.avaritia.init.registry.ModTooltips;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.equipment.Equippable;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class NeutronHarnessItem extends Item implements InitEnchantItem, IUndamageable {

    public NeutronHarnessItem() {
        super(ModItems.properties()
                .component(DataComponents.EQUIPPABLE, createHarnessEquippable())
                .stacksTo(1)
                .rarity(ModRarities.RARE)
                .fireResistant()
                .attributes(ItemAttributeModifiers.builder()
                        .add(Attributes.ARMOR,
                                new AttributeModifier(
                                        ARMOR_ID,
                                        50.0,
                                        AttributeModifier.Operation.ADD_VALUE
                                ),
                                EquipmentSlotGroup.BODY
                        )
                        .add(Attributes.ARMOR_TOUGHNESS,
                                new AttributeModifier(
                                        TOUGHNESS_ID,
                                        10.0,
                                        AttributeModifier.Operation.ADD_VALUE
                                ),
                                EquipmentSlotGroup.BODY
                        )
                        .add(Attributes.FLYING_SPEED,
                                new AttributeModifier(
                                        FLYING_SPEED_ID,
                                        0.95,
                                        AttributeModifier.Operation.ADD_VALUE
                                ),
                                EquipmentSlotGroup.BODY
                        )
                        .build()
                ));
    }
    private static final Identifier ARMOR_ID = Identifier.fromNamespaceAndPath(Const.MOD_ID, "neutron_harness_armor");
    private static final Identifier TOUGHNESS_ID = Identifier.fromNamespaceAndPath(Const.MOD_ID, "neutron_harness_armor_toughness");
    private static final Identifier FLYING_SPEED_ID = Identifier.fromNamespaceAndPath(Const.MOD_ID, "neutron_harness_flying_speed");
    private static Equippable createHarnessEquippable() {
        HolderGetter<EntityType<?>> entityGetter = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.ENTITY_TYPE);
        return Equippable.builder(EquipmentSlot.BODY)
                .setEquipSound(SoundEvents.HARNESS_EQUIP)
                .setAsset(ModArmorMaterial.NEUTRON_HARNESS_ASSET)
                .setAllowedEntities(entityGetter.getOrThrow(EntityTypeTags.CAN_EQUIP_HARNESS))
                .setEquipOnInteract(true)
                .setCanBeSheared(true)
                .setShearingSound(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.HARNESS_UNEQUIP))
                .build();
    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public int getInitEnchantLevel(ItemInstance stack, Holder<Enchantment> enchantmentHolder) {
        if (enchantmentHolder.is(Enchantments.PROTECTION)) {
            return 10;
        }
        return 0;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NonNull TooltipDisplay display,
                                @NotNull Consumer<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        var registries = context.registries();
        if (registries != null) {
            var enchantments = registries.lookupOrThrow(Registries.ENCHANTMENT);
            tooltipComponents.accept(ModTooltips.INIT_ENCHANT.args(Enchantment.getFullname(enchantments.getOrThrow(Enchantments.PROTECTION), 10)).build());
        }
        super.appendHoverText(stack, context, display, tooltipComponents, isAdvanced);
    }
}
