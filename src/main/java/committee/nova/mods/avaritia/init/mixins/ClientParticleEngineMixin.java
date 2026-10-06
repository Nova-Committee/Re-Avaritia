package committee.nova.mods.avaritia.init.mixins;
import committee.nova.mods.avaritia.client.particle.*;
import committee.nova.mods.avaritia.init.registry.ModParticles;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ParticleEngine.class)
public abstract class ClientParticleEngineMixin {
 @Inject(method="registerProviders",at=@At("TAIL")) private void avaritia$particles(CallbackInfo ci) {
  ParticleEngine engine=(ParticleEngine)(Object)this;
  engine.register(ModParticles.CHARGE.get(), (ParticleEngine.SpriteParticleRegistration<net.minecraft.core.particles.SimpleParticleType>)ChargeParticle.Factory::new);
  engine.register(ModParticles.SHOCKWAVE_PARTICLE.get(), (ParticleEngine.SpriteParticleRegistration<ShockwaveParticleOptions>)ShockwaveParticle.Provider::new);
 }
}
