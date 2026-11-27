package committee.nova.mods.avaritia.common.item.misc;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.common.enchant.InitEnchantment;
import committee.nova.mods.avaritia.api.iface.item.InitEnchantItem;
import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.init.registry.*;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.UUID;
import java.util.function.Consumer;


public class NeutronHarnessItem extends Item implements InitEnchantItem {

    public NeutronHarnessItem() {
        super(ModItems.properties()
                .component(DataComponents.EQUIPPABLE, createHarnessEquippable())
                .stacksTo(1)
                .rarity(ModRarities.RARE)
                .fireResistant()
                .setNoCombineRepair()
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
    private static final Identifier ARMOR_ID = Const.rl("neutron_harness_armor");
    private static final Identifier TOUGHNESS_ID = Const.rl("neutron_harness_armor_toughness");
    private static final Identifier FLYING_SPEED_ID = Const.rl("neutron_harness_flying_speed");
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
    private final InitEnchantment ALL_DAMAGE_PROTECTION = new InitEnchantment(Enchantments.PROTECTION, 10);
    @Override
    public boolean supportsEnchantment(@NonNull ItemStack stack, @NonNull Holder<Enchantment> enchantment) {
        return true;
    }

    @Override
    public int getEnchantmentLevel(@NonNull ItemInstance stack, @NonNull Holder<Enchantment> enchantment) {
        return 10;
    }


    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public boolean hasCustomEntity(@NotNull ItemStack stack) {
        return true;
    }
    @Nullable
    @Override
    public Entity createEntity(@NotNull Level level, Entity location, @NotNull ItemStack stack) {
        return ImmortalItemEntity.create(ModEntityTypes.IMMORTAL.get(), level, location, stack);
    }

    @Override
    public int getInitEnchantLevel(ItemInstance stack, Holder<Enchantment> enchantmentHolder) {
        if (enchantmentHolder.is(Enchantments.PROTECTION)) {
            return 10;
        }
        return 0;
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NonNull TooltipDisplay display, @NonNull Consumer<Component> builder,
                                @NotNull TooltipFlag isAdvanced) {
        this.ALL_DAMAGE_PROTECTION.appendHoverText(context, builder);
    }
}
