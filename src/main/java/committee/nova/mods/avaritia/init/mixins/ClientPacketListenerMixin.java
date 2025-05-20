package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.api.utils.RecipeUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.network.Connection;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/5/19 00:22
 * @Description:
 */
@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Shadow
    @Final
    private RecipeManager recipeManager;

    public ClientPacketListenerMixin() {
    }

    @Inject(
            at = {@At("RETURN")},
            method = {"<init>"}
    )
    public void cucumber$constructor(Minecraft minecraft, Connection connection, CommonListenerCookie commonListenerCookie, CallbackInfo ci) {
        RecipeUtils.setRecipeManager(this.recipeManager);
    }
}
