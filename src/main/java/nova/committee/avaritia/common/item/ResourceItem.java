package nova.committee.avaritia.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import nova.committee.avaritia.init.registry.ModTab;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/1 21:25
 * Version: 1.0
 */
public class ResourceItem extends Item {
    private final Rarity rarity;
    private final String name;

    public ResourceItem(Rarity rarity, String registryName) {
        super(new Properties().tab(ModTab.TAB));
        setRegistryName(registryName);
        this.rarity = rarity;
        this.name = registryName;
    }

    @Override
    public Rarity getRarity(ItemStack p_41461_) {
        return rarity;
    }


    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> components, TooltipFlag p_41424_) {
        components.add(new TextComponent(ChatFormatting.DARK_GRAY + "" + ChatFormatting.ITALIC + I18n.get("tooltip." + name + ".desc")));
    }
}
