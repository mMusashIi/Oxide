package com.ordana.oxide.blocks.dispenser;


import com.ordana.oxide.items.CementBucketItem;
import net.mehvahdjukaar.moonlight.api.util.DispenserHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.block.DispenserBlock;

public class CementBucketDispenserBehavior extends DispenserHelper.AdditionalDispenserBehavior {

    public CementBucketDispenserBehavior(Item item) {
        super(item);
    }

    protected InteractionResultHolder<ItemStack> customBehavior(BlockSource source, ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof CementBucketItem bi) {
            Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
            BlockPos blockpos = source.getPos().relative(direction);
            
            boolean wasLastAmount = bi.getAmount(stack) == 1;

            InteractionResult result = bi.place(new DirectionalPlaceContext(source.getLevel(), blockpos, direction, stack, direction));
            
            if (result.consumesAction() && wasLastAmount) {
                // bi.place internally shrinks the stack if amount becomes 0.
                // AdditionalDispenserBehavior's fillItemInDispenser ALSO shrinks the input stack by 1.
                // We grow the stack by 1 to offset the double-shrink.
                stack.grow(1);
                return new InteractionResultHolder<>(result, new ItemStack(Items.BUCKET));
            }

            return new InteractionResultHolder<>(result, stack);
        }

        return InteractionResultHolder.pass(stack);
    }


}
