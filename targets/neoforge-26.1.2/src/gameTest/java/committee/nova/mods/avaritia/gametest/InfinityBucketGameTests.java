package committee.nova.mods.avaritia.gametest;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.component.InfinityBucketBudget;
import committee.nova.mods.avaritia.common.component.InfinityBucketCreature;
import committee.nova.mods.avaritia.common.item.misc.InfinityBucketItem;
import committee.nova.mods.avaritia.common.menu.InfinityBucketMenu;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.fish.TropicalFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.TagValueInput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@EventBusSubscriber(modid = "avaritia_gametest")
public final class InfinityBucketGameTests {
    private static final Identifier EMPTY_STRUCTURE = Identifier.withDefaultNamespace("empty");
    private static final int TROPICAL_VARIANT = 0x050D0001;
    private static final TropicalFish.Variant NAMED_FISH_VARIANT = new TropicalFish.Variant(TROPICAL_VARIANT);
    private static final Identifier NAMED_TROPICAL = Const.rl("infinity_bucket_named_tropical");
    private static final Identifier STACKED_AXOLOTL = Const.rl("infinity_bucket_stacked_axolotl");
    private static final Identifier FULL_CREATURES = Const.rl("infinity_bucket_full_creatures");
    private static final Identifier NEAR_MAX_WATER = Const.rl("infinity_bucket_near_max_water");
    private static final Identifier OVERSIZED = Const.rl("infinity_bucket_oversized");
    private static final Identifier EXTRACT_FLUID_TADPOLE = Const.rl("infinity_bucket_extract_fluid_tadpole");
    private static final Identifier BLOCKED_RELEASE = Const.rl("infinity_bucket_blocked_release");
    private static final Identifier COLLECT_WATER = Const.rl("infinity_bucket_collect_water");
    private static final Identifier EXTRACT_NAMED = Const.rl("infinity_bucket_extract_named");
    private static final Identifier EXTRACT_STACKED = Const.rl("infinity_bucket_extract_stacked");
    private static final Identifier EXTRACT_NO_WATER = Const.rl("infinity_bucket_extract_no_water");

    private InfinityBucketGameTests() {
    }

    @SubscribeEvent
    public static void registerFunctions(RegisterEvent event) {
        event.register(Registries.TEST_FUNCTION, NAMED_TROPICAL, () -> InfinityBucketGameTests::namedTropicalFishRoundTrip);
        event.register(Registries.TEST_FUNCTION, STACKED_AXOLOTL, () -> InfinityBucketGameTests::stackedAxolotlTransfersOne);
        event.register(Registries.TEST_FUNCTION, FULL_CREATURES, () -> InfinityBucketGameTests::fullCreatureListRejectsCod);
        event.register(Registries.TEST_FUNCTION, NEAR_MAX_WATER, () -> InfinityBucketGameTests::nearMaxWaterRejectsSalmon);
        event.register(Registries.TEST_FUNCTION, OVERSIZED, () -> InfinityBucketGameTests::oversizedPufferfishRejected);
        event.register(Registries.TEST_FUNCTION, EXTRACT_FLUID_TADPOLE, () -> InfinityBucketGameTests::extractFluidRejectsTadpole);
        event.register(Registries.TEST_FUNCTION, BLOCKED_RELEASE, () -> InfinityBucketGameTests::blockedCreatureReleaseDoesNotCollectWater);
        event.register(Registries.TEST_FUNCTION, COLLECT_WATER, () -> InfinityBucketGameTests::fluidSelectionStillCollectsSourceWater);
        event.register(Registries.TEST_FUNCTION, EXTRACT_NAMED, () -> InfinityBucketGameTests::emptyBucketExtractsNamedTropicalFish);
        event.register(Registries.TEST_FUNCTION, EXTRACT_STACKED, () -> InfinityBucketGameTests::stackedEmptyBucketsExtractOneAxolotl);
        event.register(Registries.TEST_FUNCTION, EXTRACT_NO_WATER, () -> InfinityBucketGameTests::extractCreatureRejectsWhenWaterMissing);
    }

    @SubscribeEvent
    public static void register(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
                Const.rl("infinity_bucket"), new TestEnvironmentDefinition.AllOf());
        register(event, NAMED_TROPICAL, environment);
        register(event, STACKED_AXOLOTL, environment);
        register(event, FULL_CREATURES, environment);
        register(event, NEAR_MAX_WATER, environment);
        register(event, OVERSIZED, environment);
        register(event, EXTRACT_FLUID_TADPOLE, environment);
        register(event, BLOCKED_RELEASE, environment);
        register(event, COLLECT_WATER, environment);
        register(event, EXTRACT_NAMED, environment);
        register(event, EXTRACT_STACKED, environment);
        register(event, EXTRACT_NO_WATER, environment);
    }

    private static void register(RegisterGameTestsEvent event, Identifier id, Holder<TestEnvironmentDefinition<?>> environment) {
        event.registerTest(id, new FunctionGameTestInstance(
                ResourceKey.create(Registries.TEST_FUNCTION, id),
                new TestData<>(environment, EMPTY_STRUCTURE, 100, 0, true)));
    }

    private static void namedTropicalFishRoundTrip(GameTestHelper helper) {
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
        helper.assertTrue(creatures.getFirst().typeId().equals(Identifier.fromNamespaceAndPath("minecraft", "tropical_fish")),
                "stored creature should be a tropical fish");

        BlockPos water = new BlockPos(8, 2, 4);
        helper.setBlock(water, Blocks.WATER);
        helper.assertTrue(useLookingDownAt(helper, fixture, water).consumesAction(),
                "using the bucket on source water should release the selected fish");
        helper.assertBlockPresent(Blocks.WATER, water);

        List<TropicalFish> fish = helper.getEntities(EntityType.TROPICAL_FISH, water, 1.0);
        helper.assertTrue(fish.size() == 1, "exactly one live tropical fish should spawn");
        TropicalFish spawned = fish.getFirst();
        helper.assertTrue(spawned.getCustomName() != null && "Harbor fish".equals(spawned.getCustomName().getString()),
                "released fish should keep its name");
        helper.assertTrue(spawned.getHealth() == 2.5F, "released fish should keep its health");
        helper.assertTrue(spawned.isNoAi(), "released fish should keep NoAI");
        helper.assertTrue(spawned.fromBucket(), "released fish should be marked fromBucket");

        ItemStack probe = new ItemStack(Items.TROPICAL_FISH_BUCKET);
        spawned.saveToBucketTag(probe);
        helper.assertTrue(NAMED_FISH_VARIANT.pattern() == probe.get(DataComponents.TROPICAL_FISH_PATTERN)
                        && NAMED_FISH_VARIANT.baseColor() == probe.get(DataComponents.TROPICAL_FISH_BASE_COLOR)
                        && NAMED_FISH_VARIANT.patternColor() == probe.get(DataComponents.TROPICAL_FISH_PATTERN_COLOR),
                "saveToBucketTag should yield the original variant");
        helper.assertTrue(InfinityBucketItem.getCreatures(fixture.bucket).isEmpty(),
                "stored creature should be removed after release");
        assertStoredWater(helper, fixture.bucket, FluidType.BUCKET_VOLUME);
        helper.succeed();
    }

    private static void stackedAxolotlTransfersOne(GameTestHelper helper) {
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
        helper.assertTrue(creatures.getFirst().typeId().equals(Identifier.fromNamespaceAndPath("minecraft", "axolotl")),
                "stored creature should be an axolotl");

        CompoundTag tag = creatures.getFirst().entityData().copy();
        tag.putString("id", creatures.getFirst().typeId().toString());
        Entity restored = EntityType.create(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), tag),
                helper.getLevel(), EntitySpawnReason.LOAD).orElse(null);
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

    private static void fullCreatureListRejectsCod(GameTestHelper helper) {
        Fixture fixture = fixture(helper);
        helper.assertTrue(InfinityBucketItem.trySetCreatures(fixture.bucket, dummyCreatures(InfinityBucketBudget.MAX_CREATURE_ENTRIES)),
                "fixture should own a full creature list");
        ItemStack source = new ItemStack(Items.COD_BUCKET);
        assertRejected(helper, fixture, source, InfinityBucketMenu.ACTION_INSERT_CARRIED,
                "full creature list should reject a whole cod bucket");
        helper.succeed();
    }

    private static void nearMaxWaterRejectsSalmon(GameTestHelper helper) {
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

    private static void oversizedPufferfishRejected(GameTestHelper helper) {
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

    private static void extractFluidRejectsTadpole(GameTestHelper helper) {
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

    private static void blockedCreatureReleaseDoesNotCollectWater(GameTestHelper helper) {
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

    private static void fluidSelectionStillCollectsSourceWater(GameTestHelper helper) {
        Fixture fixture = fixture(helper);
        BlockPos water = new BlockPos(8, 2, 4);
        helper.setBlock(water, Blocks.WATER);

        helper.assertTrue(useLookingDownAt(helper, fixture, water).consumesAction(),
                "fluid mode should still collect source water");
        helper.assertTrue(helper.getBlockState(water).isAir(), "collected water source should be removed");
        assertStoredWater(helper, fixture.player.getInventory().getItem(0), FluidType.BUCKET_VOLUME);
        helper.succeed();
    }

    private static void emptyBucketExtractsNamedTropicalFish(GameTestHelper helper) {
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
        helper.assertTrue(NAMED_FISH_VARIANT.pattern() == extracted.get(DataComponents.TROPICAL_FISH_PATTERN)
                        && NAMED_FISH_VARIANT.baseColor() == extracted.get(DataComponents.TROPICAL_FISH_BASE_COLOR)
                        && NAMED_FISH_VARIANT.patternColor() == extracted.get(DataComponents.TROPICAL_FISH_PATTERN_COLOR),
                "extracted bucket should keep the variant");
        helper.assertTrue(InfinityBucketItem.getCreatures(fixture.bucket).isEmpty(),
                "extracted creature should leave the list");
        helper.assertTrue(InfinityBucketItem.getFluids(fixture.bucket).isEmpty(),
                "extracting a fish bucket should consume one bucket of water");
        helper.assertEntityNotPresent(EntityType.TROPICAL_FISH);
        helper.succeed();
    }

    private static void stackedEmptyBucketsExtractOneAxolotl(GameTestHelper helper) {
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
        helper.assertTrue(extracted.get(DataComponents.AXOLOTL_VARIANT) == Axolotl.Variant.BLUE
                        && data.getIntOr("Age", 0) == -1200
                        && data.getFloatOr("Health", 0) == 7.0F,
                "extracted axolotl should keep variant, age and health");
        helper.assertTrue(InfinityBucketItem.getCreatures(fixture.bucket).isEmpty(),
                "extracted axolotl should leave the list");
        helper.succeed();
    }

    private static void extractCreatureRejectsWhenWaterMissing(GameTestHelper helper) {
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
        stack.set(DataComponents.TROPICAL_FISH_PATTERN, NAMED_FISH_VARIANT.pattern());
        stack.set(DataComponents.TROPICAL_FISH_BASE_COLOR, NAMED_FISH_VARIANT.baseColor());
        stack.set(DataComponents.TROPICAL_FISH_PATTERN_COLOR, NAMED_FISH_VARIANT.patternColor());
        CompoundTag data = new CompoundTag();
        data.putFloat("Health", 2.5F);
        data.putBoolean("NoAI", true);
        stack.set(DataComponents.BUCKET_ENTITY_DATA, CustomData.of(data));
        return stack;
    }

    private static ItemStack axolotlBucket() {
        ItemStack stack = new ItemStack(Items.AXOLOTL_BUCKET);
        stack.set(DataComponents.AXOLOTL_VARIANT, Axolotl.Variant.BLUE);
        CompoundTag data = new CompoundTag();
        data.putInt("Age", -1200);
        data.putFloat("Health", 7.0F);
        stack.set(DataComponents.BUCKET_ENTITY_DATA, CustomData.of(data));
        return stack;
    }

    private static List<InfinityBucketCreature> dummyCreatures(int count) {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", "minecraft:cod");
        Identifier id = Identifier.fromNamespaceAndPath("minecraft", "cod");
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
        return fixture.bucket.use(helper.getLevel(), fixture.player, InteractionHand.MAIN_HAND);
    }

    private static Fixture fixture(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.create(helper, "AvaritiaGT");
        player.setGameMode(GameType.CREATIVE);
        player.getInventory().clearContent();
        ItemStack bucket = new ItemStack(ModItems.infinity_bucket.get());
        UUID nonce = InfinityBucketItem.stampMenuNonce(bucket);
        BlockPos pos = helper.absolutePos(new BlockPos(4, 1, 4));
        player.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        player.getInventory().setItem(0, bucket);
        InfinityBucketMenu menu = new InfinityBucketMenu(1, player.getInventory(), 0, nonce);
        player.containerMenu = menu;
        return new Fixture(player, bucket, menu);
    }

    private record Fixture(ServerPlayer player, ItemStack bucket, InfinityBucketMenu menu) {
    }
}
