package com.avaritia.api.client.render.model;

import com.avaritia.api.client.render.pipeline.attribute.AttributeKey;
import com.avaritia.api.client.util.color.Color;
import com.avaritia.api.client.util.color.ColorRGBA;
import com.avaritia.api.utils.java.Copyable;
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
    public Color ambientColor = new ColorRGBA(0x000000FF);
    @Nullable
    public String ambientColourMap;
    public Color diffuseColor = new ColorRGBA(0xFFFFFFFF);
    @Nullable
    public String diffuseColourMap;
    public Color specularColor = new ColorRGBA(0x000000FF);
    public float specularHighlight = 0F;
    @Nullable
    public String specularColourMap;

    public float dissolve = 1.0F;
    public float illumination = 2.0F;

    public ModelMaterial() {
    }

    public ModelMaterial(ModelMaterial other) {
        name = other.name;
        ambientColor = other.ambientColor;
        ambientColourMap = other.ambientColourMap;
        diffuseColor = other.diffuseColor;
        diffuseColourMap = other.diffuseColourMap;
        specularColor = other.specularColor;
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
