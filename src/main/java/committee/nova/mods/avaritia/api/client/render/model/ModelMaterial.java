package committee.nova.mods.avaritia.api.client.render.model;

import committee.nova.mods.avaritia.api.client.render.pipeline.attribute.AttributeKey;
import committee.nova.mods.avaritia.util.client.colour.Colour;
import committee.nova.mods.avaritia.util.client.colour.ColourRGBA;
import committee.nova.mods.avaritia.util.java.Copyable;
import org.jetbrains.annotations.Nullable;

/**
 * Created by covers1624 on 11/4/22.
 */
public class ModelMaterial implements Copyable<ModelMaterial> {

    public static final AttributeKey<ModelMaterial> MATERIAL_KEY = new AttributeKey<>("material") {
        @Override
        public ModelMaterial createDefault(int length) {
            return new ModelMaterial();
        }

        @Override
        public ModelMaterial copy(ModelMaterial src, int length) {
            return src.copy();
        }

        @Override
        public ModelMaterial copyRange(ModelMaterial src, int srcpos, ModelMaterial dest, int destpos, int length) {
            return srcpos == 0 && destpos == 0 ? src : dest;
        }
    };

    @Nullable
    public String name;
    public Colour ambientColour = new ColourRGBA(0x000000FF);
    @Nullable
    public String ambientColourMap;
    public Colour diffuseColour = new ColourRGBA(0xFFFFFFFF);
    @Nullable
    public String diffuseColourMap;
    public Colour specularColour = new ColourRGBA(0x000000FF);
    public float specularHighlight = 0F;
    @Nullable
    public String specularColourMap;

    public float dissolve = 1.0F;
    public float illumination = 2.0F;

    public ModelMaterial() {
    }

    public ModelMaterial(ModelMaterial other) {
        name = other.name;
        ambientColour = other.ambientColour;
        ambientColourMap = other.ambientColourMap;
        diffuseColour = other.diffuseColour;
        diffuseColourMap = other.diffuseColourMap;
        specularColour = other.specularColour;
        specularHighlight = other.specularHighlight;
        specularColourMap = other.specularColourMap;
        dissolve = other.dissolve;
        illumination = other.illumination;
    }

    @Override
    public ModelMaterial copy() {
        return new ModelMaterial(this);
    }
}
