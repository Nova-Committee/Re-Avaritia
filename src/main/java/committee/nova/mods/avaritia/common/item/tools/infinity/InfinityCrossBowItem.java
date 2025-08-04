package committee.nova.mods.avaritia.common.item.tools.infinity;

import com.google.common.collect.Lists;
import committee.nova.mods.avaritia.api.iface.ITooltip;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.Predicate;

/**
 * @author: cu6
 */
public class InfinityCrossBowItem extends ProjectileWeaponItem implements ITooltip {
    // NBT标签：存储已装填的发射物
    private static final String TAG_CHARGED_PROJECTILES = "ChargedProjectiles";
    private static final String TAG_CHARGED = "Charged";
    public static final Predicate<ItemStack> SUPPORTED_PROJECTILES = (stack) ->
            stack.is(Items.ARROW) ||
                    stack.is(Items.FIREWORK_ROCKET) ||
                    stack.is(Items.FIRE_CHARGE) ||
                    stack.is(Items.ENDER_PEARL);
    public static final Predicate<ItemStack> ALL_SUPPORTED_PROJECTILES = SUPPORTED_PROJECTILES;



    public InfinityCrossBowItem() {
        super(new Properties()
                .stacksTo(1)
                .rarity(ModRarities.COSMIC)
                .fireResistant()
        );
    }
    // 判断武器是否已装填
    public static boolean isCharged(ItemStack launcher) {
        CompoundTag tag = launcher.getTag();
        return tag != null && tag.getBoolean(TAG_CHARGED);
    }
    // 设置武器装填状态
    public static void setCharged(ItemStack launcher, boolean charged) {
        launcher.getOrCreateTag().putBoolean(TAG_CHARGED, charged);
    }

    // 加载发射物到武器中
    private static boolean tryLoadProjectiles(LivingEntity shooter, ItemStack launcher) {
        int multiShotLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MULTISHOT, launcher);
        int count = multiShotLevel == 0 ? 1 : 3; // 多重射击时发射3个
        boolean isCreative = shooter instanceof Player && ((Player) shooter).getAbilities().instabuild;

        ItemStack ammo = shooter.getProjectile(launcher); // 从玩家手中获取发射物
        ItemStack ammoCopy = ammo.copy();

        for (int i = 0; i < count; i++) {
            if (i > 0) ammo = ammoCopy.copy(); // 多重射击时复制发射物

            // 创造模式下即使没有发射物，也默认装填箭
            if (ammo.isEmpty() && isCreative) {
                ammo = new ItemStack(Items.ARROW);
                ammoCopy = ammo.copy();
            }

            // 加载单个发射物（失败则整体装填失败）
            if (!loadProjectile(shooter, launcher, ammo, i > 0, isCreative)) {
                return false;
            }
        }
        return true;
    }

    private static boolean loadProjectile(LivingEntity shooter, ItemStack launcher, ItemStack ammo, boolean isMultiShot, boolean isCreative) {
        if (ammo.isEmpty()) return false;

        // 创造模式不消耗发射物，生存模式消耗1个
        ItemStack toCharge;
        if (!isCreative && !isMultiShot) {
            toCharge = ammo.split(1);
            if (ammo.isEmpty() && shooter instanceof Player) {
                ((Player) shooter).getInventory().removeItem(ammo);
            }
        } else {
            toCharge = ammo.copy();
        }

        // 将发射物存入武器NBT
        addChargedProjectile(launcher, toCharge);
        return true;
    }
    private static void addChargedProjectile(ItemStack launcher, ItemStack ammo) {
        CompoundTag tag = launcher.getOrCreateTag();
        ListTag projectilesTag = tag.getList(TAG_CHARGED_PROJECTILES, 10); // 10 = NBT类型为Compound
        CompoundTag ammoTag = new CompoundTag();
        ammo.save(ammoTag); // 保存发射物到NBT
        projectilesTag.add(ammoTag);
        tag.put(TAG_CHARGED_PROJECTILES, projectilesTag);
    }

    // 获取已装填的发射物列表
    public static List<ItemStack> getChargedProjectiles(ItemStack launcher) {
        List<ItemStack> projectiles = Lists.newArrayList();
        CompoundTag tag = launcher.getTag();
        if (tag != null && tag.contains(TAG_CHARGED_PROJECTILES, 9)) { // 9 = NBT类型为List
            ListTag listTag = tag.getList(TAG_CHARGED_PROJECTILES, 10);
            for (int i = 0; i < listTag.size(); i++) {
                projectiles.add(ItemStack.of(listTag.getCompound(i)));
            }
        }
        return projectiles;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack launcher = player.getItemInHand(hand);
        if (isCharged(launcher)) {
            // 已装填：执行发射
            performShooting(level, player, hand, launcher);
            setCharged(launcher, false); // 发射后清空装填
            return InteractionResultHolder.consume(launcher);
        } else if (!player.getProjectile(launcher).isEmpty()) {
            // 未装填但有发射物：开始蓄力
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(launcher);
        } else {
            // 无发射物：失败
            return InteractionResultHolder.fail(launcher);
        }
    }

    @Override
    public void releaseUsing(ItemStack launcher, Level level, LivingEntity shooter, int timeLeft) {
        int useTime = this.getUseDuration(launcher) - timeLeft;
        float power = getPowerForTime(useTime); // 计算蓄力进度
        if (power >= 1.0F && !isCharged(launcher)) {
            // 蓄力完成：装填发射物
            if (tryLoadProjectiles(shooter, launcher)) {
                setCharged(launcher, true);
                // 播放装填完成音效
                level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(),
                        SoundEvents.CROSSBOW_LOADING_END, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
    }

    private float getPowerForTime(int useTime) {
        float power = (float) useTime / 20.0F; // 假设20tick（1秒）满蓄力
        return Math.min(power, 1.0F); // 上限1.0
    }

    private void performShooting(Level level, LivingEntity shooter, InteractionHand hand, ItemStack launcher) {
        List<ItemStack> projectiles = getChargedProjectiles(launcher);
        if (projectiles.isEmpty()) return;

        // 多重射击角度偏移（左右各10度）
        float[] angles = {0.0F, -10.0F, 10.0F};
        int multiShotLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MULTISHOT, launcher);
        int count = multiShotLevel == 0 ? 1 : 3;

        for (int i = 0; i < count; i++) {
            ItemStack ammo = projectiles.get(i % projectiles.size()); // 循环取发射物
            if (ammo.isEmpty()) continue;

            // 根据发射物类型生成实体
            if (ammo.is(Items.ARROW)) {
                shootArrow(level, shooter, launcher, ammo, angles[i]);
            } else if (ammo.is(Items.FIREWORK_ROCKET)) {
                shootFirework(level, shooter, launcher, ammo, angles[i]);
            } else if (ammo.is(Items.FIRE_CHARGE)) {
                shootFireball(level, shooter, launcher, angles[i]);
            }else if (ammo.is(Items.FIRE_CHARGE)) {
                shootEnderPearl(level, shooter, launcher, angles[i]);
            }
        }

        // 发射后清空NBT中的发射物
        clearChargedProjectiles(launcher);
        launcher.hurtAndBreak(1, shooter, (e) -> e.broadcastBreakEvent(hand)); // 消耗耐久
    }

    private void shootArrow(Level level, LivingEntity shooter, ItemStack launcher, ItemStack arrowStack, float angle) {
        if (level.isClientSide) return;
        AbstractArrow arrow = ((ArrowItem) arrowStack.getItem()).createArrow(level, arrowStack, shooter);
        arrow.setShotFromCrossbow(true); // 标记为弩发射（可用于区分普通箭）
        arrow.setCritArrow(true); // 暴击效果
        setupProjectileMotion(shooter, arrow, 3.0F, 1.0F, angle); // 设置运动轨迹
        level.addFreshEntity(arrow); // 加入世界
    }

    private void shootFirework(Level level, LivingEntity shooter, ItemStack launcher, ItemStack fireworkStack, float angle) {
        if (level.isClientSide) return;
        FireworkRocketEntity firework = new FireworkRocketEntity(level, fireworkStack,
                shooter.getX(), shooter.getEyeY() - 0.15F, shooter.getZ(), true);
        setupProjectileMotion(shooter, firework, 1.6F, 0.5F, angle); // 烟花速度较慢
        level.addFreshEntity(firework);
    }

    // 发射火焰弹（小型火球）
    private void shootFireball(Level level, LivingEntity shooter, ItemStack launcher, float angle) {
        if (level.isClientSide) return;
        SmallFireball fireball = new SmallFireball(level, shooter,
                shooter.getLookAngle().x, shooter.getLookAngle().y, shooter.getLookAngle().z);
        // 火焰弹位置调整到玩家手部
        fireball.setPos(shooter.getX() + shooter.getLookAngle().x * 0.5,
                shooter.getEyeY() - 0.1,
                shooter.getZ() + shooter.getLookAngle().z * 0.5);
        setupProjectileMotion(shooter, fireball, 2.0F, 0.3F, angle); // 火焰弹速度中等
        level.addFreshEntity(fireball);
    }
    //末影珍珠
    private void shootEnderPearl(Level level, LivingEntity shooter, ItemStack launcher, float angle) {
        if (level.isClientSide) return;

        // 1. 修复构造方法参数（根据常见的Throwable实体构造方式调整）
        // 假设ThrownEnderpearl的正确构造是：Level + 发射者
        ThrownEnderpearl enderpearl = new ThrownEnderpearl(level, shooter);

        // 2. 正确设置实体位置（发射者手部位置）
        Vec3 look = shooter.getLookAngle(); // 获取视线方向向量
        enderpearl.setPos(
                shooter.getX() + look.x * 0.5,  // X轴：发射者位置 + 视线方向×0.5（手部前方）
                shooter.getEyeY() - 0.1,        // Y轴：发射者眼睛高度 - 0.1（略低位置）
                shooter.getZ() + look.z * 0.5   // Z轴：同X轴逻辑
        );

        // 4. 播放发射音效（末影珍珠投掷音效）
        level.playSound(null,
                shooter.getX(), shooter.getY(), shooter.getZ(),
                SoundEvents.ENDER_PEARL_THROW,
                SoundSource.PLAYERS,
                1.0F, 1.0F
        );

        // 5. 将实体加入世界
        level.addFreshEntity(enderpearl);
    }

    // 统一设置 projectile 运动轨迹（角度、速度、精度）
    private void setupProjectileMotion(LivingEntity shooter, Projectile projectile, float velocity, float inaccuracy, float angle) {
        Vec3 look = shooter.getViewVector(1.0F);
        Quaternionf rotation = new Quaternionf().setAngleAxis(angle * ((float) Math.PI / 180F), 0, 1, 0); // 绕Y轴旋转
        Vector3f direction = new Vector3f((float) look.x, (float) look.y, (float) look.z).rotate(rotation);
        projectile.shoot(direction.x, direction.y, direction.z, velocity, inaccuracy);
    }
    // 清空已装填的发射物
    private void clearChargedProjectiles(ItemStack launcher) {
        launcher.getOrCreateTag().remove(TAG_CHARGED_PROJECTILES);
    }
    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000; // 最大使用时间（足够长即可）
    }
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.CROSSBOW; // 复用弩的蓄力动画
    }
    @Override
    public Predicate<ItemStack> getSupportedHeldProjectiles() {
        return SUPPORTED_PROJECTILES;
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return ALL_SUPPORTED_PROJECTILES;
    }

    @Override
    public int getDefaultProjectileRange() {
        return 12;
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }
}
