package committee.nova.mods.avaritia.core.io;
public interface EnergyStorage {
    int receiveEnergy(int amount, boolean simulate);
    int extractEnergy(int amount, boolean simulate);
    int getEnergyStored();
    int getMaxEnergyStored();
    boolean canExtract();
    boolean canReceive();
}
