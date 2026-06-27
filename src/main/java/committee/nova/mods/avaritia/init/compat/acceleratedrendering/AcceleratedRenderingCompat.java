package committee.nova.mods.avaritia.init.compat.acceleratedrendering;

import committee.nova.mods.avaritia.Const;
import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.configs.FeatureStatus;
import com.github.argon4w.acceleratedrendering.features.items.AcceleratedItemRenderingFeature;

/**
 * 适配 AcceleratedRendering 的封装类
 * 
 * @author HowXu <dev@howxu.cn>
 */
public final class AcceleratedRenderingCompat {
    private AcceleratedRenderingCompat() {
    }

    public static boolean beginHaloRender() {
        if (       !CoreFeature.isLoaded()
                || !AcceleratedItemRenderingFeature.isEnabled()
                || !AcceleratedItemRenderingFeature.shouldUseAcceleratedPipeline()) {
            return false;
        }

        // 临时屏蔽 AR的GUI加速功能 AR的API是修改全局的GUI_ACCELERATION_CONTROLLER_STACK
        // 不会在endHaloRender时还原 否则无效
        // 除非AR主动Call resetGuiAcceleration 或者强制更新这个队列 否则GUI_ACCELERATION这个功能将
        // 在当前游戏会话失效，但是不会影响全局配置文件

        // 防止过量的FeatureStatus.DISABLED被写入
        if(AcceleratedItemRenderingFeature.getGUIAccelerationSetting() != FeatureStatus.DISABLED){
            AcceleratedItemRenderingFeature.dontAccelerateInGui();
            Const.LOGGER.info("Disable AcceleratedRendering GUI feature for the moment");
        }
        return true;
    }

    public static void endHaloRender() {
        // AcceleratedItemRenderingFeature.resetGuiAcceleration();
        // Do not release this
    }
}