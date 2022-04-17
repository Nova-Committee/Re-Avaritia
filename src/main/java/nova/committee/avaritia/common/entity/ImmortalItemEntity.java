package nova.committee.avaritia.common.entity;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 14:33
 * Version: 1.0
 */
public class ImmortalItemEntity extends ItemEntity {


    public ImmortalItemEntity(EntityType<? extends ItemEntity> type, Level p_i50217_2_) {
        super(type, p_i50217_2_);
        this.setPickUpDelay(20);
    }


    @Override
    public boolean hurt(DamageSource source, float p_70097_2_) {
        return source.getMsgId().equals("outOfWorld");
    }

    @Override
    public void tick() {
        super.tick();
    }
}
