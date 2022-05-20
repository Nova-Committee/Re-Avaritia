package nova.committee.avaritia.init;

import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.ModList;
import nova.committee.avaritia.util.lang.Tooltip;

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
    public static final Tooltip EJECT = new Tooltip("tooltip.avaritia.eject");
    public static final Tooltip EJECTING = new Tooltip("tooltip.avaritia.ejecting");
    public static final Tooltip NUM_ITEMS = new Tooltip("tooltip.avaritia.num_items");

    public static final Tooltip CRAFTING = new Tooltip("tooltip.avaritia.crafting");

    public static final Tooltip PROGRESS_EMPTY = new Tooltip("tooltip.avaritia.progress_empty");

    public static Component getAddedByTooltip(String modid) {
        var name = ModList.get().getModFileById(modid).getMods().get(0).getDisplayName();
        return ADDED_BY.args(name).build();
    }
}
