package committee.nova.mods.avaritia.core.singularity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SingularityCountTest {
    @Test
    void projectEBoostRaisesSmallCountsToTenThousand() {
        assertEquals(10_000, SingularityCountResolver.resolve(1_000, true, true));
    }

    @Test
    void projectEBoostPreservesLargerConfiguredCounts() {
        assertEquals(12_000, SingularityCountResolver.resolve(12_000, true, true));
    }

    @Test
    void disabledOrMissingProjectEDoesNotChangeConfiguredCount() {
        assertEquals(1_000, SingularityCountResolver.resolve(1_000, false, true));
        assertEquals(1_000, SingularityCountResolver.resolve(1_000, true, false));
    }
}
