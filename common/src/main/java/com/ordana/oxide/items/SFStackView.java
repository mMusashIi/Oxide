package com.ordana.oxide.items;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

// immutable view of a SoftFluidStack
public class SFStackView {

    private final SoftFluidStack fluid;

    private SFStackView(SoftFluidStack stack) {
        this.fluid = stack.copy();
    }

    public static SFStackView of(SoftFluidStack stack) {
        return new SFStackView(stack);
    }

    public SFStackView copyWithCount(int count) {
        return of(this.fluid.copyWithCount(count));
    }

    public static SFStackView load(CompoundTag nbt) {
        return of(SoftFluidStack.load(nbt));
    }

    public CompoundTag save() {
        return this.fluid.save(new CompoundTag());
    }

    public int getCount() {
        return this.fluid.getCount();
    }

    public SoftFluid getFluid() {
        return this.fluid.fluid();
    }

    public boolean isEmpty() {
        return this.fluid.isEmpty();
    }

    public boolean is(TagKey<SoftFluid> tag) {
        return this.fluid.is(tag);
    }

    public boolean sameFluidSameComponents(SoftFluidStack stack){
        return this.fluid.isFluidEqual(stack);
    }

    public SoftFluidStack toMutable() {
        return this.fluid.copy();
    }

    public int getParticleColor(Level level, @Nullable BlockPos pos) {
        //TODO: try still, particle and flowing color. idk which might work best
        return fluid.getParticleColor(level, pos);
    }

    public void addToTooltip(ItemStack stack, @Nullable Level level, List<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        if (!this.fluid.isEmpty()) {
            Component fluidName = fluid.fluid().getTranslatedName();

            //tooltip is broen here, a bug I have in ML
            tooltipAdder.add(Component.translatable("tooltip.oxide.fluid",
                    fluidName, fluid.getCount()).withStyle(ChatFormatting.GRAY));

            if (fluid.hasTag() && (fluid.getTag().contains("Potion") || fluid.getTag().contains("CustomPotionEffects"))) {
                ItemStack fakeStack = new ItemStack(Items.POTION);
                fakeStack.setTag(fluid.getTag());
                PotionUtils.addPotionTooltip(fakeStack, tooltipAdder, 1.0F);
            }
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof SFStackView that)) return false;
        return Objects.equals(fluid, that.fluid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(fluid);
    }

    /** EntityDataSerializer for 1.20.1 — read/write via NBT CompoundTag */
    public static final EntityDataSerializer<SFStackView> SERIALIZER = EntityDataSerializer.simple(
            (buf, value) -> {
                CompoundTag tag = value.save();
                buf.writeNbt(tag);
            },
            (buf) -> {
                CompoundTag tag = buf.readNbt();
                return tag != null ? SFStackView.load(tag) : SFStackView.of(SoftFluidStack.empty());
            }
    );
}
