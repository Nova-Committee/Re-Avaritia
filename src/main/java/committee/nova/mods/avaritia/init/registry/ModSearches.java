package committee.nova.mods.avaritia.init.registry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.searchtree.FullTextSearchTree;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/4/13 17:01
 * @Description:
 */
public class ModSearches {
    public static final SearchRegistry.Key<EntityType<?>> LIVING_ENTITY_KEY = new SearchRegistry.Key<>();

    public static void onClientSetup() {
        Minecraft.getInstance().getSearchTreeManager().register(LIVING_ENTITY_KEY, (entities) -> {
                    return new FullTextSearchTree<>((entity) -> {
                        return ForgeRegistries.ENTITY_TYPES.getValues().stream()
                                .map((entity1) -> entity1.getDescription().getString())
                                .filter((name) -> entity.getDescription().getString().equals(name));
                    }, (living) -> {
                        return entities.stream()
                                .map(ForgeRegistries.ENTITY_TYPES::getKey);

                    }, entities);

                }
        );
    }
}
