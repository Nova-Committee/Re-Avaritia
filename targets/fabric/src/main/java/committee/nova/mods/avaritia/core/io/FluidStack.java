package committee.nova.mods.avaritia.core.io;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import java.util.Objects;
public class FluidStack {
    public static final FluidStack EMPTY = new FluidStack(Fluids.EMPTY, 0);
    private final Fluid fluid;
    private int amount;
    private CompoundTag tag;
    public FluidStack(Fluid fluid, int amount) { this.fluid = fluid; this.amount = Math.max(0, amount); }
    public FluidStack(Fluid fluid, int amount, CompoundTag tag) { this(fluid, amount); this.tag = tag == null ? null : tag.copy(); }
    public Fluid getFluid() { return fluid; }
    public int getAmount() { return isEmpty() ? 0 : amount; }
    public void setAmount(int amount) { if (this != EMPTY) this.amount = Math.max(0, amount); }
    public void grow(int amount) { setAmount(this.amount + amount); }
    public void shrink(int amount) { setAmount(this.amount - amount); }
    public boolean isEmpty() { return fluid == Fluids.EMPTY || amount <= 0; }
    public boolean hasTag() { return tag != null && !tag.isEmpty(); }
    public CompoundTag getTag() { return tag; }
    public void setTag(CompoundTag tag) { if (this != EMPTY) this.tag = tag; }
    public FluidStack copy() { return isEmpty() ? EMPTY : new FluidStack(fluid, amount, tag); }
    public boolean isFluidEqual(FluidStack other) { return fluid == other.fluid && Objects.equals(tag, other.tag); }
    public boolean isFluidStackIdentical(FluidStack other) { return isFluidEqual(other) && getAmount() == other.getAmount(); }
    public static boolean areFluidStackTagsEqual(FluidStack left, FluidStack right) { return Objects.equals(left.tag, right.tag); }
    public net.minecraft.network.chat.Component getDisplayName() { return net.minecraft.network.chat.Component.translatable(fluid.defaultFluidState().createLegacyBlock().getBlock().getDescriptionId()); }
    public CompoundTag writeToNBT(CompoundTag nbt) { nbt.putString("FluidName", BuiltInRegistries.FLUID.getKey(fluid).toString()); nbt.putInt("Amount", amount); if (tag != null) nbt.put("Tag", tag.copy()); return nbt; }
    public static FluidStack loadFluidStackFromNBT(CompoundTag nbt) { ResourceLocation id = ResourceLocation.tryParse(nbt.getString("FluidName")); if (id == null || !BuiltInRegistries.FLUID.containsKey(id)) return EMPTY; return new FluidStack(BuiltInRegistries.FLUID.get(id), nbt.getInt("Amount"), nbt.contains("Tag", 10) ? nbt.getCompound("Tag") : null); }
}
