package committee.nova.mods.avaritia.client.compat;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.client.shader.AvaritiaShaders;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.world.item.ItemDisplayContext;

import java.lang.reflect.Method;

/**
 * Iris/Oculus 兼容层。
 * <p>26.3 的自定义 {@link RenderPipeline} 必须通过 Iris API 登记到对应的 shader program，
 * 否则开启光影后 Iris 的 pipeline override 列表里找不到 {@code avaritia:cosmic} 等程序，
 * 会在手部/实体 pass 中打印 “Missing program … in override list”，并可能触发驱动层崩溃。
 * 这里全部使用反射，未安装 Iris 时不会加载任何 Iris 类。</p>
 */
public final class IrisCompat {
    private static final String IRIS_API_CLASS = "net.irisshaders.iris.api.v0.IrisApi";
    private static final String IRIS_PROGRAM_CLASS = "net.irisshaders.iris.api.v0.IrisProgram";

    /** 光影开关只可能在设置界面变化，没必要对每个物品每帧做一次反射调用。 */
    private static final long SHADER_PACK_CHECK_INTERVAL_NS = 1_000_000_000L;

    private static Boolean irisLoaded;
    private static boolean pipelinesAssigned;
    private static boolean shaderPackEnabled;
    private static long lastShaderPackCheck = -SHADER_PACK_CHECK_INTERVAL_NS;

    private IrisCompat() {
    }

    public static boolean isShaderPackEnabled() {
        if (!isLoaded()) {
            return false;
        }
        long now = System.nanoTime();
        if (now - lastShaderPackCheck < SHADER_PACK_CHECK_INTERVAL_NS) {
            return shaderPackEnabled;
        }
        lastShaderPackCheck = now;

        try {
            Class<?> irisApi = Class.forName(IRIS_API_CLASS);

            Object api = irisApi.getMethod("getInstance").invoke(null);

            shaderPackEnabled = (boolean) irisApi
                    .getMethod("isShaderPackInUse")
                    .invoke(api);
        } catch (Throwable ignored) {
            shaderPackEnabled = false;
        }
        return shaderPackEnabled;
    }

    /**
     * 在自定义管线注册完成后调用，把管线映射到 Iris 的实体半透明程序。
     * Iris 会用光影包里的 gbuffers_entities 程序替换掉我们的 cosmic 程序，
     * 代价是光影下失去自定义星空效果，但可以避免 “Missing program” 与随之而来的渲染崩溃。
     */
    public static void assignPipelines() {
        if (pipelinesAssigned || !isLoaded()) {
            return;
        }
        pipelinesAssigned = true;

        try {
            Class<?> apiClass = Class.forName(IRIS_API_CLASS);
            Class<?> programClass = Class.forName(IRIS_PROGRAM_CLASS);
            Object api = apiClass.getMethod("getInstance").invoke(null);
            Method assign = apiClass.getMethod("assignPipeline", RenderPipeline.class, programClass);
            Object translucent = enumConstant(programClass, "ENTITIES_TRANSLUCENT");

            assign.invoke(api, AvaritiaShaders.COSMIC_SHADER, translucent);
            assign.invoke(api, AvaritiaShaders.COSMIC_ARMOR_SHADER, translucent);
            assign.invoke(api, AvaritiaShaders.HELL_SHADER, translucent);
            assign.invoke(api, AvaritiaShaders.ETERNAL_SHADER, translucent);
            assign.invoke(api, AvaritiaShaders.UNSTABLE_SHADER, translucent);
            assign.invoke(api, AvaritiaShaders.BLACK_HOLE_SHADER, translucent);

            Const.LOGGER.info("Assigned Avaritia custom render pipelines to Iris ENTITIES_TRANSLUCENT program");
        } catch (ClassNotFoundException e) {
            // Iris 未安装：无需任何处理。
        } catch (Throwable t) {
            Const.LOGGER.warn("Failed to register Avaritia render pipelines with Iris", t);
        }
    }

    public static boolean shouldDefer(ItemDisplayContext ctx) {

        if (!isShaderPackEnabled()) {
            return false;
        }

        return switch (ctx) {

            case FIRST_PERSON_LEFT_HAND,
                 FIRST_PERSON_RIGHT_HAND,
                 THIRD_PERSON_LEFT_HAND,
                 THIRD_PERSON_RIGHT_HAND -> true;

            default -> false;
        };
    }

    private static boolean isLoaded() {
        if (irisLoaded == null) {
            irisLoaded = Const.isLoad("iris");
        }
        return irisLoaded;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object enumConstant(Class<?> enumClass, String name) {
        return Enum.valueOf((Class<? extends Enum>) enumClass, name);
    }
}
