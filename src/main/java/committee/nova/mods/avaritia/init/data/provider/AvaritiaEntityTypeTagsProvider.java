package committee.nova.mods.avaritia.init.data.provider;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.registry.ModEntityTypes;
import committee.nova.mods.avaritia.init.registry.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class AvaritiaEntityTypeTagsProvider extends EntityTypeTagsProvider {
    public AvaritiaEntityTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, Const.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        tag(ModTags.INFINITY_BUCKET_AQUATIC).add(EntityType.SQUID, EntityType.GLOW_SQUID, EntityType.DOLPHIN);
        tag(ModTags.INFINITY_BUCKET_BOSSES).add(EntityType.ENDER_DRAGON, EntityType.WITHER, EntityType.WARDEN, EntityType.ELDER_GUARDIAN);
        tag(ModTags.INFINITY_BUCKET_CAPTURING_NOT_SUPPORTED);
        tag(EntityTypeTags.IMPACT_PROJECTILES).add(ModEntityTypes.ENDER_PEARL.get());
        // Every creature entity is collected, including ones added by other mods. Those must be
        // written as optional entries: a required reference to an absent mod's entity makes the
        // whole tag fail to load, which would silently disable the neutral-creature protection.
        BuiltInRegistries.ENTITY_TYPE.forEach(entityType -> {
            if (entityType.getCategory() == MobCategory.CREATURE) {
                Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
                if (id != null && !"minecraft".equals(id.getNamespace())) {
                    tag(ModTags.NEUTRAL_CREATURES).addOptional(entityType);
                } else {
                    tag(ModTags.NEUTRAL_CREATURES).add(entityType);
                }
            }
        });
    }
}
