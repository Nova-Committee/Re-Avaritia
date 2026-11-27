package committee.nova.mods.avaritia.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestRegistry;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.gametest.framework.TestFunction;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

/** Registers static tests with explicitly namespaced structures, without a loader test API. */
public final class NativeTestRegistry {
    private NativeTestRegistry() {}

    public static void register(Class<?> testClass) {
        for (Method method : testClass.getDeclaredMethods()) {
            GameTest annotation = method.getAnnotation(GameTest.class);
            if (annotation == null) continue;
            String name = testClass.getSimpleName().toLowerCase(Locale.ROOT)
                    + "." + method.getName().toLowerCase(Locale.ROOT);
            GameTestRegistry.getAllTestFunctions().add(new TestFunction(
                    annotation.batch(), name, annotation.template(),
                    StructureUtils.getRotationForRotationSteps(annotation.rotationSteps()),
                    annotation.timeoutTicks(), annotation.setupTicks(), annotation.required(),
                    annotation.requiredSuccesses(), annotation.attempts(), helper -> {
                try {
                    method.invoke(null, helper);
                } catch (InvocationTargetException exception) {
                    Throwable cause = exception.getCause();
                    if (cause instanceof RuntimeException runtime) throw runtime;
                    if (cause instanceof Error error) throw error;
                    throw new IllegalStateException(name, cause);
                } catch (ReflectiveOperationException exception) {
                    throw new IllegalStateException(name, exception);
                }
            }));
            GameTestRegistry.getAllTestClassNames().add(testClass.getSimpleName());
        }
    }
}
