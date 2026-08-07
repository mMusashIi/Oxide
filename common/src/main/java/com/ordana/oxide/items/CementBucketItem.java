package com.ordana.oxide.items;

import com.ordana.oxide.reg.ModBlocks;
import com.ordana.oxide.reg.ModItems;
import com.ordana.oxide.reg.ModNBTKeys;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public class CementBucketItem extends BlockItem {

    public CementBucketItem(Properties properties) {
        super(ModBlocks.WET_CEMENT.get(), properties);
    }

    public void setAmount(ItemStack stack, int amount) {
        stack.getOrCreateTag().putInt(ModNBTKeys.CEMENT, amount);
    }

    public int getAmount(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getInt(ModNBTKeys.CEMENT) : 0;
    }

    public static ItemStack create(int amount) {
        ItemStack stack = new ItemStack(ModItems.CEMENT_BUCKET.get());
        stack.getOrCreateTag().putInt(ModNBTKeys.CEMENT, amount);
        return stack;
    }


    @Environment(EnvType.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag tooltipFlag) {
        tooltip.add(Component.translatable("tooltip.oxide.cement_bucket", getAmount(stack), "128").setStyle(Style.EMPTY.applyFormat(ChatFormatting.GRAY)));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        InteractionResult interactionResult = this.place(new BlockPlaceContext(context));
        if (!interactionResult.consumesAction() && context.getItemInHand().getItem().isEdible()) {
            InteractionResult interactionResult2 = super.use(context.getLevel(), context.getPlayer(), context.getHand()).getResult();
            return interactionResult2 == InteractionResult.CONSUME ? InteractionResult.CONSUME_PARTIAL : interactionResult2;
        } else {
            return interactionResult;
        }
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        if (!this.getBlock().isEnabled(context.getLevel().enabledFeatures())) {
            return InteractionResult.FAIL;
        } else if (!context.canPlace()) {
            return InteractionResult.FAIL;
        } else {
            BlockPlaceContext blockPlaceContext = this.updatePlacementContext(context);
            if (blockPlaceContext == null) {
                return InteractionResult.FAIL;
            } else {
                BlockState blockState = this.getPlacementState(blockPlaceContext);
                if (blockState == null) {
                    return InteractionResult.FAIL;
                } else if (!this.placeBlock(blockPlaceContext, blockState)) {
                    return InteractionResult.FAIL;
                } else {
                    BlockPos blockPos = blockPlaceContext.getClickedPos();
                    Level level = blockPlaceContext.getLevel();
                    Player player = blockPlaceContext.getPlayer();
                    ItemStack itemStack = blockPlaceContext.getItemInHand();
                    BlockState blockState2 = level.getBlockState(blockPos);
                    if (blockState2.is(blockState.getBlock())) {
                        updateCustomBlockEntityTag(level, player, blockPos, itemStack);
                        blockState2.getBlock().setPlacedBy(level, blockPos, blockState2, player, itemStack);
                        if (player instanceof ServerPlayer) {
                            CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer) player, blockPos, itemStack);
                        }
                    }

                    SoundType soundType = blockState2.getSoundType();
                    level.playSound(player, blockPos, this.getPlaceSound(blockState2), SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
                    level.gameEvent(GameEvent.BLOCK_PLACE, blockPos, GameEvent.Context.of(player, blockState2));

                    if (player == null || !player.getAbilities().instabuild) {
                        if (getAmount(itemStack) > 1) this.setAmount(itemStack, getAmount(itemStack) - 1);
                        else {
                            itemStack.shrink(1);
                            if (player != null) {
                                player.setItemInHand(context.getHand(), Items.BUCKET.getDefaultInstance());
                            }
                        }
                    }

                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }
        }
    }

    @Override
    @Nullable
    protected BlockState getPlacementState(BlockPlaceContext context) {
        var state = context.getLevel().getBlockState(context.getClickedPos());
        BlockState blockState = state.is(ModBlocks.REBAR.get())
                ? ModBlocks.CEMENTED_REBAR.get().getStateForPlacement(context)
                : this.getBlock().getStateForPlacement(context);
        return blockState != null && this.canPlace(context, blockState) ? blockState : null;
    }
}
