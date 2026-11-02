package committee.nova.mods.avaritia.client.render;

import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * 26.3 起 {@link RenderType#draw} 移除，绘制阶段只持有 {@link PreparedRenderType}，
 * 无法直接还原其来源 {@link RenderType}（{@code AvaritiaShaderUniforms} 的 per-render-type
 * uniform 仍以 RenderType 实例为键）。因此在 {@code RenderType#prepare} 时登记映射，
 * 绘制时由 {@code RenderPassMixin} 查询；每帧渲染结束后统一清空。
 *
 * @author cnlimiter
 */
public final class RenderTypeDrawTracker {
    private static final Map<PreparedRenderType, RenderType> RENDER_TYPES = new IdentityHashMap<>();

    private RenderTypeDrawTracker() {
    }

    public static void register(PreparedRenderType preparedRenderType, RenderType renderType) {
        RENDER_TYPES.put(preparedRenderType, renderType);
    }

    public static @Nullable RenderType get(PreparedRenderType preparedRenderType) {
        return RENDER_TYPES.get(preparedRenderType);
    }

    public static void clear() {
        RENDER_TYPES.clear();
    }
}
