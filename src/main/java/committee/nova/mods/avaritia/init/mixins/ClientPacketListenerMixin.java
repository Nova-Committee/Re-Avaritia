package committee.nova.mods.avaritia.init.mixins;

import com.mojang.authlib.GameProfile;
import committee.nova.mods.avaritia.api.util.RecipeUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.telemetry.WorldSessionTelemetryManager;
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
public abstract class ClientPacketListenerMixin {
    @Shadow
    @Final
    private RecipeManager recipeManager;

    public ClientPacketListenerMixin() {
    }

    @Inject(
            at = {@At("RETURN")},
            method = {"<init>"}
    )
    public void avaritia$constructor(Minecraft pMinecraft, Screen pCallbackScreen, Connection pConnection, ServerData pServerData, GameProfile pLocalGameProfile, WorldSessionTelemetryManager pTelemetryManager, CallbackInfo ci) {
        RecipeUtils.setRecipeManager(this.recipeManager);
    }
}
