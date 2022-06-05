package nova.committee.avaritia.api.client.render;

import net.minecraft.client.resources.model.BakedModel;
import nova.committee.avaritia.Static;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/6/4 12:43
 * Version: 1.0
 */
public class CreateCustomRenderedItemModel extends CustomRenderedItemModel {

    public CreateCustomRenderedItemModel(BakedModel template, String basePath) {
        super(template, Static.MOD_ID, basePath);
    }

}
