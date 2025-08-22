package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.api.utils.lang.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.ModList;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 12:45
 * Version: 1.0
 */
public class ModTooltips {
    public static final Tooltip ADDED_BY = new Tooltip("tooltip.avaritia.added_by");
    public static final Tooltip SINGULARITY_ID = new Tooltip("tooltip.avaritia.singularity_id");
    public static final Tooltip ACTIVE = new Tooltip("tooltip.avaritia.active");
    public static final Tooltip CHANGED = new Tooltip("tooltip.avaritia.changed");
    public static final Tooltip INACTIVE = new Tooltip("tooltip.avaritia.inactive");
    public static final Tooltip SWITCH = new Tooltip("tooltip.avaritia.switch");
    public static final Tooltip EMPTY = new Tooltip("tooltip.avaritia.empty");
    public static final Tooltip NUM_ITEMS = new Tooltip("tooltip.avaritia.num_items");
    public static final Tooltip CRAFTING = new Tooltip("tooltip.avaritia.crafting");
    public static final Tooltip SMITHING = new Tooltip("tooltip.avaritia.smithing");
    public static final Tooltip COMPRESS = new Tooltip("tooltip.avaritia.compress");
    public static final Tooltip TIME_CONSUME = new Tooltip("tooltip.avaritia.time_consume");
    public static final Tooltip PROGRESS = new Tooltip("tooltip.avaritia.progress");
    public static final Tooltip INIT_ENCHANT = new Tooltip("tooltip.avaritia.init_enchant");
    public static final Tooltip DURABILITY = new Tooltip("tooltip.avaritia.durability");


    public static Component getAddedByTooltip(String modid) {
        var name = ModList.get().getModFileById(modid).getMods().get(0).getDisplayName();
        return ADDED_BY.args(name).build();
    }
}
