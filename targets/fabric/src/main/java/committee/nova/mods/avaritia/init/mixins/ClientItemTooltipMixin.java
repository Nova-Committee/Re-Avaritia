package committee.nova.mods.avaritia.init.mixins;
import committee.nova.mods.avaritia.client.AvaritiaForgeClient;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(ItemStack.class)
public abstract class ClientItemTooltipMixin {
 @Inject(method="getTooltipLines",at=@At("RETURN")) private void avaritia$tooltip(Player player,TooltipFlag flag,CallbackInfoReturnable<List<Component>> cir) { AvaritiaForgeClient.getTooltip(cir.getReturnValue()); AvaritiaForgeClient.onItemTooltip((ItemStack)(Object)this,cir.getReturnValue()); }
}
