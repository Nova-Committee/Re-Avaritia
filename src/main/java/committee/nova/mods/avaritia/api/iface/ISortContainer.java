package committee.nova.mods.avaritia.api.iface;

import committee.nova.mods.avaritia.api.utils.math.SortingType;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/3/3 11:30
 * @Description:
 */
public interface ISortContainer {
    int getDifferenceInAdditionalSlots();

    /**
     * Get additional slots for this player - always multiple of 9
     */
    int getAdditionalSlots();

    void setAdditionalSlots(int additionalSlots);

    /**
     * Mark as needing to update additional slots of infinitory
     */
    void needToUpdateInfinitorySize();

    /**
     * Mark as needing to sort inventory
     */
    void needToSort();

    /**
     * Mark as needing to update client
     */
    void needToUpdateClient();

    SortingType getSortingType();

    void setSortingType(SortingType type);

    boolean getSortingAscending();

    void setSortAscending(boolean sortAscending);

    /**
     * Recalculate additional slots based on main and infinitory sizes / fullness
     */
    void updateInfinitorySize();

    /**
     * Sync additional slots, sorting type, and sorting ascending server -> client
     */
    void syncInfinitoryValues();
}
