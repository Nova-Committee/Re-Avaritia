package committee.nova.mods.avaritia.gametest;

import com.mojang.authlib.GameProfile;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.component.InfinityBucketBudget;
import committee.nova.mods.avaritia.common.component.InfinityBucketCreature;
import committee.nova.mods.avaritia.common.item.misc.InfinityBucketItem;
import committee.nova.mods.avaritia.common.menu.InfinityBucketMenu;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@GameTestHolder(Const.MOD_ID)
@PrefixGameTestTemplate(false)
public final class InfinityBucketGameTests {
    private static final String TEMPLATE = "portable_ui_empty";
    private static final int TROPICAL_VARIANT = 0x050D0001;

    private InfinityBucketGameTests() {
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void namedTropicalFishRoundTrip(GameTestHelper helper) {
        Fixture fixture = fixture(helper);
        ItemStack source = tropicalFishBucket();
        fixture.menu.setCarried(source);

        helper.assertTrue(fixture.menu.clickMenuButton(fixture.player, InfinityBucketMenu.ACTION_INSERT_CARRIED),
                "named tropical fish should deposit");
        helper.assertTrue(fixture.menu.getCarried().is(Items.BUCKET) && fixture.menu.getCarried().getCount() == 1,
                "deposit should return exactly one empty bucket");
        helper.assertTrue(emptyBuckets(fixture.player) == 0, "empty bucket should stay on the cursor");
        helper.assertEntityNotPresent(EntityType.TROPICAL_FISH);
        assertStoredWater(helper, fixture.bucket, FluidType.BUCKET_VOLUME);
        List<InfinityBucketCreature> creatures = InfinityBucketItem.getCreatures(fixture.bucket);
        helper.assertTrue(creatures.size() == 1, "one tropical fish should be stored");
        helper.assertTrue(creatures.getFirst().typeId().equals(ResourceLocation.fromNamespaceAndPath("minecraft", "tropical_fish")),
                "stored creature should be a tropical fish");

        BlockPos water = new BlockPos(8, 2, 4);
        helper.setBlock(water, Blocks.WATER);
        helper.assertTrue(useLookingDownAt(helper, fixture, water).consumesAction(),
                "using the bucket on source water should release the selected fish");
        helper.assertBlockPresent(Blocks.WATER, water);

        List<TropicalFish> fish = helper.getEntities(EntityType.TROPICAL_FISH);
        helper.assertTrue(fish.size() == 1, "exactly one live tropical fish should spawn");
        TropicalFish spawned = fish.getFirst();
        helper.assertTrue(spawned.getCustomName() != null && "Harbor fish".equals(spawned.getCustomName().getString()),
                "released fish should keep its name");
        helper.assertTrue(spawned.getHealth() == 2.5F, "released fish should keep its health");
        helper.assertTrue(spawned.isNoAi(), "released fish should keep NoAI");
        helper.assertTrue(spawned.fromBucket(), "released fish should be marked fromBucket");

        ItemStack probe = new ItemStack(Items.TROPICAL_FISH_BUCKET);
        spawned.saveToBucketTag(probe);
        helper.assertTrue(probe.getOrDefault(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY)
                        .copyTag().getInt("BucketVariantTag") == TROPICAL_VARIANT,
                "saveToBucketTag should yield the original variant");
        helper.assertTrue(InfinityBucketItem.getCreatures(fixture.bucket).isEmpty(),
                "stored creature should be removed after release");
        assertStoredWater(helper, fixture.bucket, FluidType.BUCKET_VOLUME);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void stackedAxolotlTransfersOne(GameTestHelper helper) {
        Fixture fixture = fixture(helper);
        ItemStack source = axolotlBucket();
        source.setCount(2);
        ItemStack remaining = source.copyWithCount(1);
        fixture.menu.setCarried(source);

        helper.assertTrue(fixture.menu.clickMenuButton(fixture.player, InfinityBucketMenu.ACTION_INSERT_CARRIED),
                "stacked axolotl buckets should transfer one");
        helper.assertTrue(ItemStack.matches(remaining, fixture.menu.getCarried()),
                "cursor should retain the other byte-equivalent axolotl bucket");
        helper.assertTrue(emptyBuckets(fixture.player) == 1, "exactly one empty bucket should enter inventory");
        helper.assertEntityNotPresent(EntityType.AXOLOTL);
        assertStoredWater(helper, fixture.bucket, FluidType.BUCKET_VOLUME);

        List<InfinityBucketCreature> creatures = InfinityBucketItem.getCreatures(fixture.bucket);
        helper.assertTrue(creatures.size() == 1, "one axolotl should be stored");
        helper.assertTrue(creatures.getFirst().typeId().equals(ResourceLocation.fromNamespaceAndPath("minecraft", "axolotl")),
                "stored creature should be an axolotl");

        CompoundTag tag = creatures.getFirst().entityData().copy();
        tag.putString("id", creatures.getFirst().typeId().toString());
        Entity restored = EntityType.create(tag, helper.getLevel()).orElse(null);
        helper.assertTrue(restored instanceof Axolotl axolotl
                        && axolotl.getVariant() == Axolotl.Variant.BLUE
                        && axolotl.getAge() == -1200
                        && axolotl.getHealth() == 7.0F,
                "restored axolotl should keep variant, age and health");
        if (restored != null) {
            restored.discard();
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void fullCreatureListRejectsCod(GameTestHelper helper) {
        Fixture fixture = fixture(helper);
        helper.assertTrue(InfinityBucketItem.trySetCreatures(fixture.bucket, dummyCreatures(InfinityBucketBudget.MAX_CREATURE_ENTRIES)),
                "fixture should own a full creature list");
        ItemStack source = new ItemStack(Items.COD_BUCKET);
        assertRejected(helper, fixture, source, InfinityBucketMenu.ACTION_INSERT_CARRIED,
                "full creature list should reject a whole cod bucket");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void nearMaxWaterRejectsSalmon(GameTestHelper helper) {
        Fixture fixture = fixture(helper);
        helper.assertTrue(InfinityBucketItem.trySetFluids(fixture.bucket,
                        List.of(new FluidStack(Fluids.WATER, Integer.MAX_VALUE - 500))),
                "fixture should own near-max water");
        ItemStack source = new ItemStack(Items.SALMON_BUCKET);
        assertRejected(helper, fixture, source, InfinityBucketMenu.ACTION_INSERT_CARRIED,
                "near-max water should reject a whole salmon bucket");
        helper.assertTrue(InfinityBucketItem.getCreatures(fixture.bucket).isEmpty(),
                "rejected salmon must not deposit a creature");
        List<FluidStack> fluids = InfinityBucketItem.getFluids(fixture.bucket);
        helper.assertTrue(fluids.size() == 1 && fluids.getFirst().getFluid() == Fluids.WATER
                        && fluids.getFirst().getAmount() == Integer.MAX_VALUE - 500,
                "rejected salmon must not add partial water");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void oversizedPufferfishRejected(GameTestHelper helper) {
        Fixture fixture = fixture(helper);
        ItemStack source = new ItemStack(Items.PUFFERFISH_BUCKET);
        CompoundTag data = new CompoundTag();
        data.putByteArray("extra", new byte[InfinityBucketBudget.MAX_CREATURE_BYTES]);
        source.set(DataComponents.BUCKET_ENTITY_DATA, CustomData.of(data));
        assertRejected(helper, fixture, source, InfinityBucketMenu.ACTION_INSERT_CARRIED,
                "oversized pufferfish payload should be rejected unchanged");
        helper.assertTrue(InfinityBucketItem.getCreatures(fixture.bucket).isEmpty(),
                "oversized pufferfish must not be stored");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void extractFluidRejectsTadpole(GameTestHelper helper) {
        Fixture fixture = fixture(helper);
        helper.assertTrue(InfinityBucketItem.trySetFluids(fixture.bucket, List.of(new FluidStack(Fluids.WATER, 1000))),
                "fixture should own stored water");
        int empties = emptyBuckets(fixture.player);
        ItemStack source = new ItemStack(Items.TADPOLE_BUCKET);
        assertRejected(helper, fixture, source, InfinityBucketMenu.ACTION_EXTRACT_FLUID,
                "extract-fluid must not consume a tadpole bucket");
        assertStoredWater(helper, fixture.bucket, 1000);
        helper.assertTrue(emptyBuckets(fixture.player) == empties, "empty-bucket count must stay unchanged");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void blockedCreatureReleaseDoesNotCollectWater(GameTestHelper helper) {
        Fixture fixture = fixture(helper);
        fixture.menu.setCarried(tropicalFishBucket());
        helper.assertTrue(fixture.menu.clickMenuButton(fixture.player, InfinityBucketMenu.ACTION_INSERT_CARRIED),
                "tropical fish should deposit");
        ItemStack before = fixture.bucket.copy();
        BlockPos target = new BlockPos(8, 2, 4);
        BlockState blockedWater = Blocks.OAK_SLAB.defaultBlockState().setValue(SlabBlock.WATERLOGGED, true);
        helper.setBlock(target, blockedWater);

        helper.assertTrue(useLookingDownAt(helper, fixture, target) == InteractionResult.FAIL,
                "a colliding creature must not be released or fall back to collecting water");
        helper.assertTrue(ItemStack.matches(before, fixture.bucket),
                "failed release must preserve the creature, stored fluid and selection");
        helper.assertTrue(helper.getBlockState(target).equals(blockedWater),
                "failed release must leave the target waterlogged");
        helper.assertEntityNotPresent(EntityType.TROPICAL_FISH);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void fluidSelectionStillCollectsSourceWater(GameTestHelper helper) {
        Fixture fixture = fixture(helper);
        BlockPos water = new BlockPos(8, 2, 4);
        helper.setBlock(water, Blocks.WATER);

        helper.assertTrue(useLookingDownAt(helper, fixture, water).consumesAction(),
                "fluid mode should still collect source water");
        helper.assertTrue(helper.getBlockState(water).isAir(), "collected water source should be removed");
        assertStoredWater(helper, fixture.bucket, FluidType.BUCKET_VOLUME);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void emptyBucketExtractsNamedTropicalFish(GameTestHelper helper) {
        Fixture fixture = fixture(helper);
        fixture.menu.setCarried(tropicalFishBucket());
        helper.assertTrue(fixture.menu.clickMenuButton(fixture.player, InfinityBucketMenu.ACTION_INSERT_CARRIED),
                "named tropical fish should deposit");
        fixture.menu.setCarried(new ItemStack(Items.BUCKET));
        helper.assertTrue(fixture.menu.clickMenuButton(fixture.player, InfinityBucketMenu.ACTION_EXTRACT_CREATURE),
                "empty bucket should extract the stored fish");
        ItemStack extracted = fixture.menu.getCarried();
        helper.assertTrue(extracted.is(Items.TROPICAL_FISH_BUCKET) && extracted.getCount() == 1,
                "cursor should hold the tropical fish bucket");
        helper.assertTrue(extracted.has(DataComponents.CUSTOM_NAME)
                        && "Harbor fish".equals(extracted.getHoverName().getString()),
                "extracted bucket should keep the custom name");
        helper.assertTrue(extracted.getOrDefault(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY)
                        .copyTag().getInt("BucketVariantTag") == TROPICAL_VARIANT,
                "extracted bucket should keep the variant");
        helper.assertTrue(InfinityBucketItem.getCreatures(fixture.bucket).isEmpty(),
                "extracted creature should leave the list");
        helper.assertTrue(InfinityBucketItem.getFluids(fixture.bucket).isEmpty(),
                "extracting a fish bucket should consume one bucket of water");
        helper.assertEntityNotPresent(EntityType.TROPICAL_FISH);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void stackedEmptyBucketsExtractOneAxolotl(GameTestHelper helper) {
        Fixture fixture = fixture(helper);
        fixture.menu.setCarried(axolotlBucket());
        helper.assertTrue(fixture.menu.clickMenuButton(fixture.player, InfinityBucketMenu.ACTION_INSERT_CARRIED),
                "axolotl should deposit");
        fixture.menu.setCarried(new ItemStack(Items.BUCKET, 2));
        helper.assertTrue(fixture.menu.clickMenuButton(fixture.player, InfinityBucketMenu.ACTION_EXTRACT_CREATURE),
                "stacked empty buckets should extract one axolotl");
        helper.assertTrue(fixture.menu.getCarried().is(Items.BUCKET) && fixture.menu.getCarried().getCount() == 1,
                "cursor should keep the remaining empty bucket");
        ItemStack extracted = ItemStack.EMPTY;
        for (int i = 0; i < fixture.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = fixture.player.getInventory().getItem(i);
            if (stack.is(Items.AXOLOTL_BUCKET)) {
                extracted = stack;
                break;
            }
        }
        helper.assertTrue(extracted.is(Items.AXOLOTL_BUCKET) && extracted.getCount() == 1,
                "exactly one axolotl bucket should enter inventory");
        CompoundTag data = extracted.getOrDefault(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY).copyTag();
        helper.assertTrue(data.getInt("Variant") == 4 && data.getInt("Age") == -1200 && data.getFloat("Health") == 7.0F,
                "extracted axolotl should keep variant, age and health");
        helper.assertTrue(InfinityBucketItem.getCreatures(fixture.bucket).isEmpty(),
                "extracted axolotl should leave the list");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void extractCreatureRejectsWhenWaterMissing(GameTestHelper helper) {
        Fixture fixture = fixture(helper);
        helper.assertTrue(InfinityBucketItem.trySetCreatures(fixture.bucket, dummyCreatures(1)),
                "fixture should own a stored creature");
        assertRejected(helper, fixture, new ItemStack(Items.BUCKET), InfinityBucketMenu.ACTION_EXTRACT_CREATURE,
                "extracting a fish requires a bucket of matching fluid");
        helper.succeed();
    }

    private static void assertRejected(GameTestHelper helper, Fixture fixture, ItemStack source, int action, String message) {
        ItemStack carriedBefore = source.copy();
        ItemStack bucketBefore = fixture.bucket.copy();
        int emptyBefore = emptyBuckets(fixture.player);
        fixture.menu.setCarried(source);
        helper.assertFalse(fixture.menu.clickMenuButton(fixture.player, action), message);
        helper.assertTrue(ItemStack.matches(carriedBefore, fixture.menu.getCarried()), "carried stack mutated");
        helper.assertTrue(ItemStack.matches(bucketBefore, fixture.bucket), "infinity bucket mutated");
        helper.assertTrue(emptyBuckets(fixture.player) == emptyBefore, "rejected transfer returned an empty bucket");
    }

    private static void assertStoredWater(GameTestHelper helper, ItemStack bucket, int amount) {
        List<FluidStack> fluids = InfinityBucketItem.getFluids(bucket);
        helper.assertTrue(fluids.size() == 1 && fluids.getFirst().getFluid() == Fluids.WATER
                        && fluids.getFirst().getAmount() == amount,
                "stored water amount should be " + amount);
    }

    private static ItemStack tropicalFishBucket() {
        ItemStack stack = new ItemStack(Items.TROPICAL_FISH_BUCKET);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Harbor fish"));
        CompoundTag data = new CompoundTag();
        data.putInt("BucketVariantTag", TROPICAL_VARIANT);
        data.putFloat("Health", 2.5F);
        data.putBoolean("NoAI", true);
        stack.set(DataComponents.BUCKET_ENTITY_DATA, CustomData.of(data));
        return stack;
    }

    private static ItemStack axolotlBucket() {
        ItemStack stack = new ItemStack(Items.AXOLOTL_BUCKET);
        CompoundTag data = new CompoundTag();
        data.putInt("Variant", 4);
        data.putInt("Age", -1200);
        data.putFloat("Health", 7.0F);
        stack.set(DataComponents.BUCKET_ENTITY_DATA, CustomData.of(data));
        return stack;
    }

    private static List<InfinityBucketCreature> dummyCreatures(int count) {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", "minecraft:cod");
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("minecraft", "cod");
        List<InfinityBucketCreature> creatures = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            creatures.add(new InfinityBucketCreature(id, tag));
        }
        return creatures;
    }

    private static int emptyBuckets(Player player) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(Items.BUCKET)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static InteractionResult useLookingDownAt(GameTestHelper helper, Fixture fixture, BlockPos target) {
        BlockPos above = helper.absolutePos(target.above(2));
        fixture.player.setPos(above.getX() + 0.5D, above.getY(), above.getZ() + 0.5D);
        fixture.player.setYRot(0.0F);
        fixture.player.setXRot(90.0F);
        return fixture.bucket.use(helper.getLevel(), fixture.player, InteractionHand.MAIN_HAND).getResult();
    }

    private static Fixture fixture(GameTestHelper helper) {
        FakePlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "AvaritiaGT"));
        player.setGameMode(GameType.CREATIVE);
        player.getInventory().clearContent();
        ItemStack bucket = new ItemStack(ModItems.infinity_bucket.get());
        BlockPos pos = helper.absolutePos(new BlockPos(4, 1, 4));
        player.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        player.getInventory().setItem(0, bucket);
        InfinityBucketMenu menu = new InfinityBucketMenu(1, player.getInventory(), 0);
        player.containerMenu = menu;
        return new Fixture(player, bucket, menu);
    }

    private record Fixture(FakePlayer player, ItemStack bucket, InfinityBucketMenu menu) {
    }
}
