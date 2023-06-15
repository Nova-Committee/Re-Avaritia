package nova.committee.avaritia.api.client.render;

import net.minecraft.world.item.Item;
import net.minecraftforge.client.IItemRenderProperties;
import nova.committee.avaritia.init.handler.ModelsHandler;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/6/4 12:34
 * Version: 1.0
 */
public class SimpleCustomRenderer implements IItemRenderProperties {

    protected CustomRenderedItemModelRenderer<?> renderer;

    protected SimpleCustomRenderer(CustomRenderedItemModelRenderer<?> renderer) {
        this.renderer = renderer;
    }

    public static SimpleCustomRenderer create(Item item, CustomRenderedItemModelRenderer<?> renderer) {
        ModelsHandler.getCustomRenderedItems().register(() -> item, renderer::createModel);
        return new SimpleCustomRenderer(renderer);
    }

    @Override
    public CustomRenderedItemModelRenderer<?> getItemStackRenderer() {
        return renderer;
    }

}
