package committee.nova.mods.avaritia.common.component;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class InfinityBucketCreatures {
    public static final int MAX_ENTRIES = InfinityBucketBudget.MAX_CREATURE_ENTRIES;
    public static final InfinityBucketCreatures EMPTY = new InfinityBucketCreatures(List.of());
    public static final Codec<InfinityBucketCreatures> CODEC = InfinityBucketCreature.CODEC.sizeLimitedListOf(MAX_ENTRIES)
            .xmap(InfinityBucketCreatures::new, InfinityBucketCreatures::creatures)
            .validate(InfinityBucketBudget::validateCreatures);
    public static final StreamCodec<RegistryFriendlyByteBuf, InfinityBucketCreatures> STREAM_CODEC = InfinityBucketCreature.STREAM_CODEC
            .apply(ByteBufCodecs.list(MAX_ENTRIES))
            .map(InfinityBucketCreatures::new, InfinityBucketCreatures::creatures)
            .map(creatures -> InfinityBucketBudget.validateCreatures(creatures).result().orElse(EMPTY), creatures -> creatures);

    private final List<InfinityBucketCreature> creatures;
    private final int hashCode;

    public InfinityBucketCreatures(List<InfinityBucketCreature> creatures) {
        List<InfinityBucketCreature> copy = new ArrayList<>(creatures.size());
        for (InfinityBucketCreature creature : creatures) {
            if (creature != null) {
                copy.add(creature);
            }
        }
        this.creatures = List.copyOf(copy);
        this.hashCode = this.creatures.hashCode();
    }

    public List<InfinityBucketCreature> creatures() {
        return creatures;
    }

    public List<InfinityBucketCreature> copyCreatures() {
        return new ArrayList<>(creatures);
    }

    public boolean isEmpty() {
        return creatures.isEmpty();
    }

    public int size() {
        return creatures.size();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof InfinityBucketCreatures other)) {
            return false;
        }
        return Objects.equals(creatures, other.creatures);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
