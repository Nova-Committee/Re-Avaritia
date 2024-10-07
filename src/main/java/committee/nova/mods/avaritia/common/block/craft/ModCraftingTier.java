package committee.nova.mods.avaritia.common.block.craft;

import net.minecraft.world.level.block.SoundType;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/10/6 22:53
 * @Description:
 */
public enum ModCraftingTier {

    SCULK("sculk_crafting_table", SoundType.SCULK_CATALYST, 9, 25, 500),
    NETHER("nether_crafting_table", SoundType.NETHERRACK, 25, 50, 1000),
    END("end_crafting_table", SoundType.SOUL_SOIL, 49, 75, 1500),
    EXTREME("extreme_crafting_table", SoundType.GLASS, 81, 100,2000);

    public final String name;
    public final SoundType sound;
    public final int size;
    public final int hardness;
    public final int resistance;

    ModCraftingTier(String name, SoundType sound, int size, int hardness, int resistance) {
        this.name = name;
        this.sound = sound;
        this.size = size;
        this.hardness = hardness;
        this.resistance = resistance;
    }
}
