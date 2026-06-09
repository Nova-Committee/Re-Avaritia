package committee.nova.mods.avaritia.init.data.provider;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.registry.ModDamageTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class AvaritiaDamageTypeTagsProvider extends TagsProvider<DamageType> {
    public AvaritiaDamageTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, Registries.DAMAGE_TYPE, registries, Const.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        addInfinity(DamageTypeTags.BYPASSES_ARMOR);
        addInfinity(DamageTypeTags.BYPASSES_SHIELD);
        addInfinity(DamageTypeTags.BYPASSES_INVULNERABILITY);
        addInfinity(DamageTypeTags.BYPASSES_COOLDOWN);
        addInfinity(DamageTypeTags.BYPASSES_EFFECTS);
        addInfinity(DamageTypeTags.BYPASSES_RESISTANCE);
        addInfinity(DamageTypeTags.BYPASSES_ENCHANTMENTS);
        addInfinity(DamageTypeTags.ALWAYS_HURTS_ENDER_DRAGONS);
    }

    private void addInfinity(TagKey<DamageType> tag) {
        getOrCreateRawBuilder(tag).addElement(ModDamageTypes.INFINITY.identifier());
    }
}
