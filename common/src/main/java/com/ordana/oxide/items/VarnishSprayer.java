package com.ordana.oxide.items;

import com.ordana.oxide.OxideClient;
import com.ordana.oxide.entities.SprayParticleEntity;
import com.ordana.oxide.reg.ModNBTKeys;
import com.ordana.oxide.reg.ModTags;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
//import net.mehvahdjukaar.moonlight.api.misc.FabricOverride;
import net.mehvahdjukaar.moonlight.api.misc.ForgeOverride;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VarnishSprayer extends Item
        //implements IThirdPersonAnimationProvider //TODO: add this for fancy animation
{

    public VarnishSprayer(Properties properties) {
        super(properties);
    }

    // In 1.20.1 max_drops is stored in NBT, default is 128
    public static int getMaxCharges(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(ModNBTKeys.MAX_DROPS)) {
            return stack.getTag().getInt(ModNBTKeys.MAX_DROPS);
        }
        return 128; // default
    }

    public static void setMaxCharges(ItemStack stack, int value) {
        stack.getOrCreateTag().putInt(ModNBTKeys.MAX_DROPS, value);
    }

    @NotNull
    public static SFStackView getFluidComponent(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(ModNBTKeys.FLUID)) {
            CompoundTag fluidTag = tag.getCompound(ModNBTKeys.FLUID);
            if (!fluidTag.isEmpty()) {
                return SFStackView.load(fluidTag);
            }
        }
        return SFStackView.of(SoftFluidStack.empty());
    }

    public static void setFluidComponent(ItemStack stack, SoftFluidStack fluid) {
        SFStackView view = SFStackView.of(fluid);
        if (view.isEmpty()) {
            if (stack.hasTag()) stack.getTag().remove(ModNBTKeys.FLUID);
        } else {
            stack.getOrCreateTag().put(ModNBTKeys.FLUID, view.save());
        }
    }

    //fill water
    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();

        FluidState state = context.getLevel().getFluidState(pos);
        if (!state.isEmpty()) {
            SoftFluidStack fluidThatBlockContains = SoftFluidStack.fromFluid(state);
            if (!fluidThatBlockContains.isEmpty() && fluidThatBlockContains.is(ModTags.CAN_GO_IN_SPRAY)) {
                var myFluid = getFluidComponent(stack);
                boolean full = getMaxCharges(stack) <= myFluid.getCount();
                if (!full) {
                    int bottles = fluidThatBlockContains.getCount();
                    //TODO: fill

                }
            }

        }
        return super.useOn(context);
    }

    //start using
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        SFStackView fluid = getFluidComponent(itemstack);

        if (!fluid.isEmpty()) {
            //TODO: play sound here
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(itemstack);
        }
        return InteractionResultHolder.fail(itemstack);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000; //arbitrary high value, we will stop using manually. same as bow. arm gets tired i guess
    }

    //spray here
    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        super.onUseTick(level, livingEntity, stack, remainingUseDuration);

        if (remainingUseDuration > getUseDuration(stack) - 20) return;
        if (remainingUseDuration % 20 != 0) return;

        SFStackView fluid = getFluidComponent(stack);
        if (fluid.isEmpty()) {
            livingEntity.stopUsingItem();
            return;
        }
        //shrink fluid stack count
        if (livingEntity instanceof Player player) {
            if (!player.isCreative()) {
                SoftFluidStack mutable = fluid.toMutable();
                if (fluid.getCount() >= 1) mutable.shrink(1);
                setFluidComponent(stack, mutable);
            }
        }

        level.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), SoundEvents.AZALEA_LEAVES_BREAK, SoundSource.NEUTRAL, 1F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));

        for (int y = -3; y < 3; ++y) {
            for (int x = -3; x < 3; ++x) {
                SprayParticleEntity fluidDrop = new SprayParticleEntity(level, livingEntity, fluid.copyWithCount(1));
                fluidDrop.shootFromRotation(livingEntity, livingEntity.getXRot() + (y * 5 * level.random.nextFloat()), livingEntity.getYRot() + (x * 5 * level.random.nextFloat()), 1.0F + (5 * level.random.nextFloat()), 1.0F + (level.random.nextFloat() / 2), 1.0F);
                level.addFreshEntity(fluidDrop);
            }
        }
    }


    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
        if (PlatHelper.getPhysicalSide().isClient()) {
            Level clientLevel = OxideClient.getClientLevel();
            SFStackView fluid = getFluidComponent(stack);
            fluid.addToTooltip(stack, clientLevel, tooltipComponents, tooltipFlag);
        }
    }

    @Environment(EnvType.CLIENT)
    @Override
    public boolean isBarVisible(ItemStack stack) {
        SFStackView fluid = getFluidComponent(stack);
        return !fluid.isEmpty();
    }

    @Environment(EnvType.CLIENT)
    @Override
    public int getBarWidth(ItemStack stack) {
        SFStackView fluid = getFluidComponent(stack);
        if (fluid.isEmpty()) return 0;
        int maxCharges = getMaxCharges(stack);
        return Math.round(((((float) maxCharges + fluid.getCount()) / maxCharges * 13f) - 13));
    }

    @Environment(EnvType.CLIENT)
    @Override
    public int getBarColor(ItemStack stack) {
        Level clientLevel = OxideClient.getClientLevel();
        SFStackView fluid = getFluidComponent(stack);
        if (fluid.isEmpty()) return -1;
        return fluid.getParticleColor(clientLevel, BlockPos.ZERO);
    }


    //@FabricOverride
    public boolean allowNbtUpdateAnimation(Player player, InteractionHand hand, ItemStack oldStack, ItemStack newStack) {
        SFStackView sf = getFluidComponent(oldStack);
        SFStackView sf2 = getFluidComponent(newStack);
        if (!sf.isEmpty() && !sf2.isEmpty()) {
            if (sf.getFluid() == sf2.getFluid()) {
                return sf.getCount() == sf2.getCount();
            }
        }
        return true;
    }

    @ForgeOverride
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (slotChanged) return true;
        SFStackView sf = getFluidComponent(oldStack);
        SFStackView sf2 = getFluidComponent(newStack);
        if (!sf.isEmpty() && !sf2.isEmpty()) {
            if (sf.getFluid() == sf2.getFluid()) {
                return sf.getCount() == sf2.getCount();
            }
        }
        return true;
    }

}
