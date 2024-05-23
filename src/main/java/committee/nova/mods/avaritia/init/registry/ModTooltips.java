package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.util.lang.Tooltip;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 12:45
 * Version: 1.0
 */
public class ModTooltips {
    public static final Tooltip ADDED_BY = new Tooltip("tooltip.avaritia.added_by");
    public static final Tooltip SINGULARITY_ID = new Tooltip("tooltip.avaritia.singularity_id");

    public static final Tooltip EMPTY = new Tooltip("tooltip.avaritia.empty");
    public static final Tooltip NUM_ITEMS = new Tooltip("tooltip.avaritia.num_items");
    public static final Tooltip CRAFTING = new Tooltip("tooltip.avaritia.crafting");
    public static final Tooltip TIME_CONSUME = new Tooltip("tooltip.avaritia.time_consume");
    public static final Tooltip PROGRESS = new Tooltip("tooltip.avaritia.progress");


    public static Component getAddedByTooltip(String modid) {
        var name = FabricLoader.getInstance().getModContainer(modid).orElseThrow().getMetadata().getName();
        return ADDED_BY.args(name).build();
    }
}
