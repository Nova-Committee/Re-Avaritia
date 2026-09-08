package committee.nova.mods.avaritia.init.data.provider;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.init.registry.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

/**
 * Author cnlimiter
 * CreateTime 2023/6/17 23:51
 * Name ModEntityTags
 * Description
 */

public class ModEntityTags extends EntityTypeTagsProvider {

    public ModEntityTags(PackOutput output, CompletableFuture<HolderLookup.Provider> future, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, future, Const.MOD_ID, existingFileHelper);
    }

    @Override
    public @NotNull String getName() {
        return "Avaritia Entity Type Tags";
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        //vanilla
        tag(EntityTypeTags.IMPACT_PROJECTILES).add(ModEntities.ENDER_PEARL.get());
        ForgeRegistries.ENTITY_TYPES.getValues().forEach(entityType -> {
            if (entityType.getCategory() == MobCategory.CREATURE) {
                tag(ModTags.NEUTRAL_CREATURES).add(entityType);
            }
        });
        tag(ModTags.AQUATIC_CAPTURABLE).add(
                net.minecraft.world.entity.EntityType.COD,
                net.minecraft.world.entity.EntityType.SALMON,
                net.minecraft.world.entity.EntityType.PUFFERFISH,
                net.minecraft.world.entity.EntityType.TROPICAL_FISH,
                net.minecraft.world.entity.EntityType.AXOLOTL,
                net.minecraft.world.entity.EntityType.TADPOLE,
                net.minecraft.world.entity.EntityType.SQUID,
                net.minecraft.world.entity.EntityType.GLOW_SQUID,
                net.minecraft.world.entity.EntityType.DOLPHIN
        );
        ForgeRegistries.ENTITY_TYPES.getValues().forEach(entityType -> {
            MobCategory category = entityType.getCategory();
            if (category == MobCategory.WATER_CREATURE
                    || category == MobCategory.UNDERGROUND_WATER_CREATURE
                    || category == MobCategory.WATER_AMBIENT
                    || category == MobCategory.AXOLOTLS) {
                tag(ModTags.AQUATIC_CAPTURABLE).add(entityType);
            }
        });
        tag(ModTags.CAPTURING_NOT_SUPPORTED);
    }
}
