package committee.nova.mods.avaritia.init.registry.enums;


import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.utils.text.ILangEntry;
import net.minecraft.Util;

/**
 * @project: Avaritia
 * @author: cnlimiter
 * @createTime: 2025/5/24 19:47
 * @apiNote:
 */
public enum ModLang implements ILangEntry {

    CURRENT_MODE("mode", "current"),
    DEFAULT_MODE("mode", "default"),
    ADVANCE_MODE("mode", "advance"),
    RANGE_MODE("mode", "range"),
    MODE_SWITCH("mode", "switch");


    private final String key;

    ModLang(String type, String path) {
        this(Util.makeDescriptionId(type, Const.rl(path)));
    }

    ModLang(String key) {
        this.key = key;
    }

    @Override
    public String getTranslationKey() {
        return key;
    }
}
