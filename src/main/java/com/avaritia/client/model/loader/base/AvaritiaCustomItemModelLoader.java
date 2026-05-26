package com.avaritia.client.model.loader.base;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.DelegateUnbakedModel;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;

public class AvaritiaCustomItemModelLoader implements UnbakedModelLoader<UnbakedModel> {
    private final AvaritiaCustomItemModel.LoaderFactory factory;
    private final String[] customKeys;

    public AvaritiaCustomItemModelLoader(AvaritiaCustomItemModel.LoaderFactory factory, String... customKeys) {
        this.factory = factory;
        this.customKeys = customKeys;
    }

    @Override
    public UnbakedModel read(JsonObject jsonObject, JsonDeserializationContext context) throws JsonParseException {
        JsonObject clean = jsonObject.deepCopy();
        clean.remove("loader");
        for (String customKey : customKeys) {
            clean.remove(customKey);
        }
        UnbakedModel delegate = context.deserialize(clean, UnbakedModel.class);
        return new DelegateUnbakedModel(delegate) {
            @Override
            public Identifier parent() {
                Identifier parent = delegate.parent();
                return parent != null ? parent : Identifier.withDefaultNamespace("item/generated");
            }
        };
    }
}
