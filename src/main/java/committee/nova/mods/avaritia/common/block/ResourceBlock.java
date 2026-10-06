package committee.nova.mods.avaritia.common.block;

import committee.nova.mods.avaritia.api.common.block.BaseBlock;
import committee.nova.mods.avaritia.init.registry.enums.ModResourceBlocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

/**
 * Name: Avaritia-forge / ResourceBlock
 * @author cnlimiter
 * CreateTime: 2023/8/13 13:20
 * Description:
 */

public class ResourceBlock extends BaseBlock {
    ModResourceBlocks type;

    public ResourceBlock(ModResourceBlocks type) {
        super(MapColor.METAL, SoundType.METAL, type.hardness, type.resistance, type.lightLevel);
        this.type = type;
    }

    public float getEnchantmentPower() {
        return this.type.enchantPower;
    }
}
