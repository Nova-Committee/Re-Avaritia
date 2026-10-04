package committee.nova.mods.avaritia.init.data.provider;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.registry.ModDamageTypes;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Set;

public final class AvaritiaRegistriesProvider {
    /** 世界层数据包注册表（DamageType 等） */
    private static final RegistrySetBuilder WORLD_BUILDER = new RegistrySetBuilder()
            .add(Registries.DAMAGE_TYPE, ModDamageTypes::bootstrap);

    /**
     * 可重载层数据包注册表。26.3 起战利品表、进度与配方均为数据包注册表对象，
     * 由各自的 Provider 以 (Single|Multi)RegistryBootstrap 形式接入。
     */
    private static final RegistrySetBuilder RELOADABLE_BUILDER = new RegistrySetBuilder()
            .add(Registries.LOOT_TABLE, AvaritiaLootTableProvider.create())
            .add(Registries.ADVANCEMENT, AvaritiaAdvancementProvider.create())
            .add(committee.nova.mods.avaritia.init.data.provider.recipe.AvaritiaRecipeProvider.asBootstrap());

    private AvaritiaRegistriesProvider() {
    }

    public static void register(GatherDataEvent.Client event) {
        // DamageType 属于世界层数据包注册表，交给 NeoForge 生成内置 datapack 条目。
        event.createWorldRegistryObjects(WORLD_BUILDER);
        // 配方/进度会写入 minecraft 命名空间（音乐唱片配方等），modIds 过滤需包含 minecraft。
        event.createReloadableRegistryObjects(RELOADABLE_BUILDER, Set.of(Const.MOD_ID, "minecraft"), "avaritia");
    }
}
