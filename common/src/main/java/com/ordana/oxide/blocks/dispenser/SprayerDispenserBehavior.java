package com.ordana.oxide.blocks.dispenser;


import com.ordana.oxide.entities.SprayParticleEntity;
import com.ordana.oxide.items.VarnishSprayer;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.BlockSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

public class SprayerDispenserBehavior extends ProjectileBehavior {

    public SprayerDispenserBehavior(Item item) {
        super(item);
    }

    @Override
    protected Projectile getProjectileEntity(BlockSource source, Position position, ItemStack stack) {
        var fluid = VarnishSprayer.getFluidComponent(stack);
        SprayParticleEntity fluidDrop = new SprayParticleEntity(source.getLevel(), position.x(), position.y(), position.z(), fluid);
        return fluidDrop;
    }


    @Override
    protected InteractionResultHolder<ItemStack> customBehavior(BlockSource source, ItemStack stack) {
        Level level = source.getLevel();
        Position dispensePosition = DispenserBlock.getDispensePosition(source);
        Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
        BlockPos frontPos = source.getPos().relative(direction);
        //this will make it so stuff can only shoot when no collision block is in front so we can run other behaviors too
        if (!level.getBlockState(frontPos).getCollisionShape(level, frontPos).isEmpty()) {
            return InteractionResultHolder.fail(stack);
        }
        var fluid = VarnishSprayer.getFluidComponent(stack);
        if (fluid.getCount() == 0) return InteractionResultHolder.fail(stack);

        for (int y = -3; y < 3; ++y) {
            for (int x = -3; x < 3; ++x) {
                Projectile projectileEntity = this.getProjectileEntity(source, dispensePosition,  stack);
                projectileEntity.shoot(direction.getStepX() + ((float) level.random.nextIntBetweenInclusive(-5, 5) / 10), direction.getStepY() + 0.1F + ((float) level.random.nextIntBetweenInclusive(-5, 5) / 10), direction.getStepZ() + ((float) level.random.nextIntBetweenInclusive(-5, 5) / 10), this.getProjectileVelocity(), 1);
                level.addFreshEntity(projectileEntity);
            }
        }
        SoftFluidStack mutable = fluid.toMutable();
        if (fluid.getCount() >= 1) mutable.shrink(1);
        VarnishSprayer.setFluidComponent(stack, mutable);
        return InteractionResultHolder.success(stack);
    }


    @Override
    protected SoundEvent getSound() {
        return SoundEvents.SNOWBALL_THROW;
    }

    @Override
    protected float getProjectileInaccuracy() {
        return 4;
    }

    @Override
    protected float getProjectileVelocity() {
        return 1;
    }


}
