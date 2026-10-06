package committee.nova.mods.avaritia.core.io;
public final class FluidTransfer {
    private FluidTransfer() {}
    public static FluidStack tryFluidTransfer(FluidHandler destination, FluidHandler source, int amount, boolean execute) { return tryFluidTransfer(destination, source, source.drain(amount, FluidHandler.FluidAction.SIMULATE), execute); }
    public static FluidStack tryFluidTransfer(FluidHandler destination, FluidHandler source, FluidStack requested, boolean execute) { FluidStack available = source.drain(requested, FluidHandler.FluidAction.SIMULATE); if (available.isEmpty()) return FluidStack.EMPTY; int accepted = destination.fill(available, FluidHandler.FluidAction.SIMULATE); if (accepted <= 0) return FluidStack.EMPTY; available.setAmount(accepted); if (!execute) return available; FluidStack drained = source.drain(available, FluidHandler.FluidAction.EXECUTE); int filled = destination.fill(drained, FluidHandler.FluidAction.EXECUTE); if (filled < drained.getAmount()) { FluidStack remainder = drained.copy(); remainder.setAmount(drained.getAmount() - filled); source.fill(remainder, FluidHandler.FluidAction.EXECUTE); } drained.setAmount(filled); return drained; }
}
