package committee.nova.mods.avaritia.init.registry;

import net.minecraft.world.level.block.SoundType;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/10/6 22:53
 * @Description:
 */
public enum ModCraftTier {

    SCULK("sculk_crafting_table", SoundType.SCULK_CATALYST,  25, 500, 3, 30, 17, 8, 84, 8, 142, 124, 35),
    NETHER("nether_crafting_table", SoundType.NETHERRACK, 50, 1000, 5, 14, 18, 8, 124, 8, 182, 143, 54),
    END("end_crafting_table", SoundType.SOUL_SOIL, 75, 1500, 7, 8, 18, 20, 160, 20, 218, 172, 71),
    EXTREME("extreme_crafting_table", SoundType.GLASS,  100,2000, 9, 8, 18, 39, 196, 39, 254, 206, 89);

    public final String name;
    public final SoundType sound;
    public final int hardness;
    public final int resistance;
    public final int size, mainX, mainY, playerInvX, playerInvY, hotBarX, hotBarY, outX, outY;

    ModCraftTier(String name, SoundType sound, int hardness, int resistance, int size, int mainX, int mainY, int playerInvX, int playerInvY, int hotBarX, int hotBarY, int outX, int outY) {
        this.name = name;
        this.sound = sound;
        this.hardness = hardness;
        this.resistance = resistance;
        this.size = size;
        this.mainX = mainX;
        this.mainY = mainY;
        this.playerInvX = playerInvX;
        this.playerInvY = playerInvY;
        this.hotBarX = hotBarX;
        this.hotBarY = hotBarY;
        this.outX = outX;
        this.outY = outY;
    }
}
