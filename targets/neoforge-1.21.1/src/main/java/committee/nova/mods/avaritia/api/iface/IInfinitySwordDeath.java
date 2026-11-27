package committee.nova.mods.avaritia.api.iface;

import net.minecraft.world.damagesource.DamageSource;

/** Runs the native server-player death lifecycle for a terminal infinity-sword hit. */
public interface IInfinitySwordDeath {
    void avaritia$dieFromInfinitySword(DamageSource source);
}
