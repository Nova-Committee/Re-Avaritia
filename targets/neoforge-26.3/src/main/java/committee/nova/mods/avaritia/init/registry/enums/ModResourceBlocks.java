package committee.nova.mods.avaritia.init.registry.enums;

/**
 * 资源方块基础参数。
 */
public enum ModResourceBlocks {
    BLAZE(50, 1000, 1000, 9),
    CRYSTAL(100, 2000, 2000, 11),
    NEUTRON(8888, 8888, 8888, 13),
    INFINITY(9999, 9999, 9999, 15);

    public final float resistance, hardness, enchantPower;
    public final int lightLevel;

    ModResourceBlocks(float resistance, float hardness, float enchantPower, int lightLevel) {
        this.resistance = resistance;
        this.hardness = hardness;
        this.enchantPower = enchantPower;
        this.lightLevel = lightLevel;
    }
}
