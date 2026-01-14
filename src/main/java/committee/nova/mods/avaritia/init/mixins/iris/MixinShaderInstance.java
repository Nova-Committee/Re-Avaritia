package committee.nova.mods.avaritia.init.mixins.iris;

import committee.nova.mods.avaritia.Const;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.VertexFormat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.irisshaders.iris.compat.SkipList;
import net.irisshaders.iris.mixinterface.ShaderInstanceInterface;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;

@Mixin({ ShaderInstance.class })
public abstract class MixinShaderInstance implements ShaderInstanceInterface {

    @Inject(method = { "<init>(Lnet/minecraft/server/packs/resources/ResourceProvider;Lnet/minecraft/resources/ResourceLocation;Lcom/mojang/blaze3d/vertex/VertexFormat;)V" }, at = { @At("TAIL") }, order = 1001)
    public void init(ResourceProvider resourceProvider, ResourceLocation shaderLocation, VertexFormat vertexFormat, CallbackInfo callbackInfo) {
        if (shaderLocation.getNamespace().equals(Const.MOD_ID)) {
            this.setShouldSkip(SkipList.NONE_FORCE);
        }
    }
}