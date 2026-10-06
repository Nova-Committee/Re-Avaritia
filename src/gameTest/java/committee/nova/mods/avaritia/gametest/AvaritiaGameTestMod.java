package committee.nova.mods.avaritia.gametest;

import committee.nova.mods.avaritia.init.compat.trinkets.TrinketsIntegration;
import committee.nova.mods.avaritia.init.handler.PackResourceHandler;
import net.fabricmc.api.ModInitializer;
import committee.nova.mods.avaritia.Const;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GlobalTestReporter;
import net.minecraft.gametest.framework.LogTestReporter;
import net.minecraft.SharedConstants;

public final class AvaritiaGameTestMod implements ModInitializer {
    @Override
    public void onInitialize() {
        SharedConstants.IS_RUNNING_IN_IDE = true;
        PackResourceHandler.registerResources("avaritia_gametest");
        GlobalTestReporter.replaceWith(new LogTestReporter() {
            @Override
            public void onTestSuccess(GameTestInfo info) {
                Const.LOGGER.info("Native GameTest passed: {}", info.getTestName());
            }

            @Override
            public void onTestFailed(GameTestInfo info) {
                super.onTestFailed(info);
                Const.LOGGER.error("Native GameTest failed: {}", info.getTestName(), info.getError());
            }
        });
        NativeTestRegistry.register(InfinityRingLifecycleGameTests.class);
        NativeTestRegistry.register(InfinityRingSpawnGameTests.class);
        NativeTestRegistry.register(InfinityBucketGameTests.class);
        NativeTestRegistry.register(NeutronRingLegacyRecoveryGameTests.class);
        NativeTestRegistry.register(NeutronSpacePreviewGameTests.class);
        NativeTestRegistry.register(NativeStorageGameTests.class);
        NativeTestRegistry.register(NativeGameplayGameTests.class);
        NativeTestRegistry.register(NativeBlazeBowGameTests.class);
        NativeTestRegistry.register(NativeBlockStateGameTests.class);
        if (TrinketsIntegration.available()) NativeTestRegistry.register(NativeTrinketsGameTests.class);
    }
}
