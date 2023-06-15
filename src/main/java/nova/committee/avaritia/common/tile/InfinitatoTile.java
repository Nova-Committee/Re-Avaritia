package nova.committee.avaritia.common.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import nova.committee.avaritia.api.common.tile.BaseTileEntity;
import nova.committee.avaritia.init.registry.ModSounds;
import nova.committee.avaritia.init.registry.ModTileEntities;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/22 17:32
 * Version: 1.0
 */
public class InfinitatoTile extends BaseTileEntity {

    private static final String NAME = "name";

    public static int jumpTicks = 0;
    public static int nextTick = 0;
    public static BlockPos tilePos;
    public String name = "";

    public InfinitatoTile(BlockPos pos, BlockState state) {
        super(ModTileEntities.infinitato_tile, pos, state);
        tilePos = pos;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, InfinitatoTile tile) {
        if (level.isClientSide) return;

        if (jumpTicks > 0) {
            jumpTicks--;
            if (jumpTicks == 20 || jumpTicks == 0) {
                level.explode(null, tilePos.getX() + 0.5, tilePos.getY(), tilePos.getZ() + 0.5, 0.0f, Level.ExplosionInteraction.BLOCK);
            }
        }
        if (nextTick > 0)
            nextTick--;
    }

    public void interact() {
        jump();
        if (level != null) {
            if (name.equalsIgnoreCase("shia labeouf") && !level.isClientSide && nextTick == 0) {
                nextTick = 40;
                level.playSound(null, worldPosition, ModSounds.botania_doit, SoundSource.BLOCKS, 1F, 1F);
            }
            double radius = 10.5;
            int time = 3600;
            List<LivingEntity> inspired =
                    level.getEntitiesOfClass(LivingEntity.class, new AABB(
                            (getBlockPos().getX() - radius), (getBlockPos().getY() - radius), (getBlockPos().getZ() - radius), (getBlockPos().getX() + radius), (getBlockPos().getY() + radius), (getBlockPos().getZ() + radius))
                    );

            for (LivingEntity ent : inspired) {
                double dx = ent.getX() - getBlockPos().getX();
                double dy = ent.getY() - getBlockPos().getY();
                double dz = ent.getZ() - getBlockPos().getZ();
                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

                if (dist <= radius) {
                    ent.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, time, 1));
                    ent.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, time, 0));
                    ent.addEffect(new MobEffectInstance(MobEffects.JUMP, time, 0));
                    ent.addEffect(new MobEffectInstance(MobEffects.REGENERATION, time, 1));
                    ent.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, time, 1));
                    ent.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, time, 1));
                    ent.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, time, 0));
                    ent.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, time, 0));
                    ent.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, time, 0));
                    ent.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, time, 4));
                    ent.addEffect(new MobEffectInstance(MobEffects.SATURATION, time, 4));
                }
            }
        }

    }

    public void jump() {
        if (jumpTicks == 0)
            jumpTicks = 40;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString(NAME, name);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        name = tag.getString(NAME);
    }
}
