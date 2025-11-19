package committee.nova.mods.avaritia.common.ingredient;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import committee.nova.mods.avaritia.init.mixins.accessor.MappedRegistryAccessor;
import committee.nova.mods.avaritia.init.registry.enums.Mods;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.common.crafting.ICustomIngredient;

import java.util.List;
import java.util.stream.Stream;


/**
 * @author cnlimiter
 * from <a href="https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/src/main/java/com/simibubi/create/foundation/data/SimpleDatagenIngredient.java">...</a>
 */
public class SimpleDatagenIngredient implements ICustomIngredient {
    /*
      "ingredients": [
          {
              "item": "mod:compat_item"
          }
      ]
     */
    private static final MapCodec<SimpleDatagenIngredient> INTERNAL_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("item").forGetter(i -> i.mod.asResource(i.id))
    ).apply(instance, location -> {
        for (Mods mod : Mods.values()) {
            if (mod.getId().equals(location.getNamespace())) {
                return new SimpleDatagenIngredient(mod, location.getPath());
            }
        }
        throw new AssertionError("ID " + location.getNamespace() + " doesn't correspond to any compat mod." +
                " SimpleDatagenIngredient is not meant for deserialization anyway");
    }));

    private static final MapCodec<SimpleDatagenIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            INTERNAL_CODEC.codec().listOf().fieldOf("ingredients").forGetter(List::of)
    ).apply(instance, list -> {
        assert list.size() == 1 : "SimpleDatagenIngredient should only be serialized as a single-element list, and shouldn't be deserialized anyway";
        return list.getFirst();
    }));
    private static final IngredientType<?> INGREDIENT_TYPE = new IngredientType<>(CODEC);

    private final Mods mod;
    private final String id;

    public SimpleDatagenIngredient(Mods mod, String id) {
        this.mod = mod;
        this.id = id;
    }

    @Override
    public boolean test(@NotNull ItemStack stack) {
        return stack.getItemHolder().getKey().location().equals(mod.asResource(id));
    }

    @Override
    public @NotNull Stream<ItemStack> getItems() {
        return Stream.empty();
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    private static boolean didRegistryInjection = false;

    @Override
    public @NotNull IngredientType<?> getType() {
        if (!didRegistryInjection) {
            // Need to do some registry injection to get the Registry#byNameCodec to encode the right type for this
            // getResourceKey and getId
            // byValue and toId
            // Holder.Reference: key
            if (NeoForgeRegistries.INGREDIENT_TYPES instanceof MappedRegistryAccessor<?> mra) {
                @SuppressWarnings("unchecked")
                MappedRegistryAccessor<IngredientType<?>> mra$ = (MappedRegistryAccessor<IngredientType<?>>) mra;

                IngredientType<?> baseType = NeoForgeMod.COMPOUND_INGREDIENT_TYPE.get();

                int wrappedId = mra$.getToId().getOrDefault(baseType, -1);
                ResourceKey<IngredientType<?>> wrappedKey = NeoForgeMod.COMPOUND_INGREDIENT_TYPE.getKey();

                mra$.getToId().put(INGREDIENT_TYPE, wrappedId);
                //noinspection DataFlowIssue - it is ok to pass null as the owner, because this is only being used for serialization
                mra$.getByValue().put(INGREDIENT_TYPE, Holder.Reference.createStandAlone(null, wrappedKey));

				/*
				{
					"type": "neoforge:compound",
					"ingredients": [
						{
							"item": "mod:compat_item"
						}
					]
				}
				 */

                didRegistryInjection = true;
            } else {
                throw new AssertionError("SimpleDatagenIngredient will not be able to" +
                        " serialize without injecting into a registry. Expected" +
                        " NeoForgeRegistries.INGREDIENT_TYPES to be of class MappedRegistry, is of class " +
                        NeoForgeRegistries.INGREDIENT_TYPES.getClass()
                );
            }
        }
        return INGREDIENT_TYPE;
    }

}
