package committee.nova.mods.avaritia.api.common.crafting;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * Minecraft 1.21.11 declares {@link RecipeSerializer} as an interface exposing
 * {@code codec()} and {@code streamCodec()}. The 26.1 sources instantiated it as a
 * concrete class with a {@code (Codec, StreamCodec)} constructor, so this record
 * preserves that construction style for the backport.
 */
public record SimpleRecipeSerializer<T extends Recipe<?>>(
        MapCodec<T> codec,
        StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) implements RecipeSerializer<T> {
}
