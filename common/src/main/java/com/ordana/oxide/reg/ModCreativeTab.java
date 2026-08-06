package com.ordana.oxide.reg;

import com.ordana.oxide.Oxide;
import com.ordana.oxide.configs.CommonConfigs;
import net.mehvahdjukaar.moonlight.api.misc.RegSupplier;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ItemLike;
import com.ordana.oxide.items.CementBucketItem;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ModCreativeTab {

    public static final RegSupplier<CreativeModeTab> MOD_TAB = !CommonConfigs.General.CREATIVE_TAB.get() ? null :
            RegHelper.registerCreativeModeTab(Oxide.res("oxide"),
                    (c) -> c.title(Component.translatable("itemGroup.oxide.oxide"))
                            .icon(() -> ModBlocks.RUSTED_CUT_IRON.get().asItem().getDefaultInstance()));

    private static final Set<Item> HIDDEN_ITEMS = new HashSet<>();
    private static final List<ItemStack> NON_HIDDEN_ITEMS = new ArrayList<>();


    public static void init() {
        RegHelper.addItemsToTabsRegistration(ModCreativeTab::registerItemsToTabs);
    }

    private static boolean isRunningSetup = false;

    public static void setup() {
        isRunningSetup = true;
        List<Item> all = new ArrayList<>(BuiltInRegistries.ITEM.entrySet().stream().filter(e -> e.getKey().location().getNamespace()
                .equals(Oxide.MOD_ID)).map(Map.Entry::getValue).toList());
        Map<ResourceKey<CreativeModeTab>, List<ItemStack>> map = new HashMap<>();
        CreativeModeTabs.tabs().forEach(t -> map.putIfAbsent(BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(t).get(), new ArrayList<>()));
        // Use a simple accumulator instead of anonymous ItemToTabEvent (which is a record in this Moonlight version)
        RegHelper.ItemToTabEvent dummy = new RegHelper.ItemToTabEvent((tab, predicate, reverse, list) -> {
            var l = map.computeIfAbsent(tab, t -> new ArrayList<>());
            if (reverse) {
                l.addAll(new ArrayList<>(list));
            } else l.addAll(list);
        });
        registerItemsToTabs(dummy);
        for (var e : map.values()) {
            NON_HIDDEN_ITEMS.addAll(e);
        }

        for (var v : NON_HIDDEN_ITEMS) {
            all.remove(v.getItem());
        }
        HIDDEN_ITEMS.addAll(all);
        isRunningSetup = false;
    }

    public static void registerItemsToTabs(RegHelper.ItemToTabEvent e) {
        if (MOD_TAB != null && !isRunningSetup) {
            List<ItemStack> toAdd = new ArrayList<>();
            for (var i : NON_HIDDEN_ITEMS) {
                if (toAdd.stream().noneMatch(a -> ItemStack.isSameItemSameTags(a, i))) {
                    toAdd.add(i);
                }
            }

            e.add(MOD_TAB.getHolder().unwrapKey().get(), toAdd.toArray(ItemStack[]::new));
            return;
        }

        after(e, Items.IRON_BLOCK, CreativeModeTabs.BUILDING_BLOCKS,

                ModBlocks.PLATE_IRON, ModBlocks.PLATE_IRON_STAIRS, ModBlocks.PLATE_IRON_SLAB,
                ModBlocks.WEATHERED_PLATE_IRON, ModBlocks.WEATHERED_PLATE_IRON_STAIRS, ModBlocks.WEATHERED_PLATE_IRON_SLAB,
                ModBlocks.RUSTED_PLATE_IRON, ModBlocks.RUSTED_PLATE_IRON_STAIRS, ModBlocks.RUSTED_PLATE_IRON_SLAB,

                ModBlocks.CUT_IRON, ModBlocks.CUT_IRON_STAIRS, ModBlocks.CUT_IRON_SLAB,
                ModBlocks.WEATHERED_CUT_IRON, ModBlocks.WEATHERED_CUT_IRON_STAIRS, ModBlocks.WEATHERED_CUT_IRON_SLAB,
                ModBlocks.RUSTED_CUT_IRON, ModBlocks.RUSTED_CUT_IRON_STAIRS, ModBlocks.RUSTED_CUT_IRON_SLAB,

                ModBlocks.IRON_SCAFFOLD, ModBlocks.IRON_SCAFFOLD_STAIRS, ModBlocks.IRON_SCAFFOLD_SLAB,
                ModBlocks.WEATHERED_IRON_SCAFFOLD, ModBlocks.WEATHERED_IRON_SCAFFOLD_STAIRS, ModBlocks.WEATHERED_IRON_SCAFFOLD_SLAB,
                ModBlocks.RUSTED_IRON_SCAFFOLD, ModBlocks.RUSTED_IRON_SCAFFOLD_STAIRS, ModBlocks.RUSTED_IRON_SCAFFOLD_SLAB,

                ModBlocks.IRON_PILLAR, ModBlocks.WEATHERED_IRON_PILLAR, ModBlocks.RUSTED_IRON_PILLAR,
                ModBlocks.HEAVY_IRON_DOOR, ModBlocks.WEATHERED_HEAVY_IRON_DOOR, ModBlocks.RUSTED_HEAVY_IRON_DOOR,
                ModBlocks.HEAVY_IRON_TRAPDOOR, ModBlocks.WEATHERED_HEAVY_IRON_TRAPDOOR, ModBlocks.RUSTED_HEAVY_IRON_TRAPDOOR,

                ModBlocks.HEAVY_IRON_BARS, ModBlocks.WEATHERED_HEAVY_IRON_BARS, ModBlocks.RUSTED_HEAVY_IRON_BARS,
                ModBlocks.WROUGHT_IRON_FENCE, ModBlocks.WEATHERED_WROUGHT_IRON_FENCE, ModBlocks.RUSTED_WROUGHT_IRON_FENCE,
                ModBlocks.WROUGHT_IRON_FENCE_GATE, ModBlocks.WEATHERED_WROUGHT_IRON_FENCE_GATE, ModBlocks.RUSTED_WROUGHT_IRON_FENCE_GATE
        );

        after(e, Items.CHAIN, CreativeModeTabs.BUILDING_BLOCKS,
                ModBlocks.HEAVY_IRON_CHAIN, ModBlocks.WEATHERED_HEAVY_IRON_CHAIN, ModBlocks.RUSTED_HEAVY_IRON_CHAIN
        );

        after(e, Items.LADDER, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModBlocks.HEAVY_IRON_LADDER, ModBlocks.WEATHERED_HEAVY_IRON_LADDER, ModBlocks.RUSTED_HEAVY_IRON_LADDER
        );

        after(e, Items.CHAIN, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModBlocks.HEAVY_IRON_CHAIN
        );

        afterStacks(e, Items.IRON_TRAPDOOR, CreativeModeTabs.BUILDING_BLOCKS,
                ModItems.RUSTY_NAIL.get().getDefaultInstance(),
                ModItems.VARNISH_SPRAYER.get().getDefaultInstance(),
                ModItems.CEMENT_POWDER_BUCKET.get().getDefaultInstance(),
                CementBucketItem.create(128),
                ModBlocks.REBAR.get().asItem().getDefaultInstance(),
                ModBlocks.CEMENT.get().asItem().getDefaultInstance(),
                ModBlocks.CEMENT_SLAB.get().asItem().getDefaultInstance(),
                ModBlocks.WEATHERED_CEMENT.get().asItem().getDefaultInstance(),
                ModBlocks.WEATHERED_CEMENT_SLAB.get().asItem().getDefaultInstance(),
                ModBlocks.CRACKED_CEMENT.get().asItem().getDefaultInstance(),
                ModBlocks.CRACKED_CEMENT_SLAB.get().asItem().getDefaultInstance(),
                ModBlocks.CRACKED_WEATHERED_CEMENT.get().asItem().getDefaultInstance(),
                ModBlocks.CRACKED_WEATHERED_CEMENT_SLAB.get().asItem().getDefaultInstance(),
                ModBlocks.REINFORCED_CEMENT.get().asItem().getDefaultInstance(),
                ModBlocks.WEATHERED_REINFORCED_CEMENT.get().asItem().getDefaultInstance(),
                ModBlocks.CRACKED_REINFORCED_CEMENT.get().asItem().getDefaultInstance(),
                ModBlocks.CRACKED_WEATHERED_REINFORCED_CEMENT.get().asItem().getDefaultInstance(),
                ModBlocks.CEMENT_BLOCK.get().asItem().getDefaultInstance(),
                ModBlocks.CEMENT_BLOCK_SLAB.get().asItem().getDefaultInstance(),
                ModBlocks.WEATHERED_CEMENT_BLOCK.get().asItem().getDefaultInstance(),
                ModBlocks.WEATHERED_CEMENT_BLOCK_SLAB.get().asItem().getDefaultInstance(),
                ModBlocks.CINDER_BLOCKS.get().asItem().getDefaultInstance(),
                ModBlocks.CINDER_BLOCK_SLAB.get().asItem().getDefaultInstance(),
                ModBlocks.CINDER_BLOCK.get().asItem().getDefaultInstance(),
                ModBlocks.CINDER_BRICKS.get().asItem().getDefaultInstance(),
                ModBlocks.CINDER_BRICK_SLAB.get().asItem().getDefaultInstance(),
                ModBlocks.CINDER_BRICK_STAIRS.get().asItem().getDefaultInstance(),
                ModBlocks.CINDER_BRICK_WALL.get().asItem().getDefaultInstance(),
                ModBlocks.WEATHERED_CINDER_BRICKS.get().asItem().getDefaultInstance(),
                ModBlocks.WEATHERED_CINDER_BRICK_SLAB.get().asItem().getDefaultInstance(),
                ModBlocks.WEATHERED_CINDER_BRICK_STAIRS.get().asItem().getDefaultInstance(),
                ModBlocks.WEATHERED_CINDER_BRICK_WALL.get().asItem().getDefaultInstance(),
                ModBlocks.CLINKER_BRICKS.get().asItem().getDefaultInstance(),
                ModBlocks.CLINKER_BRICK_SLAB.get().asItem().getDefaultInstance(),
                ModBlocks.CLINKER_BRICK_STAIRS.get().asItem().getDefaultInstance(),
                ModBlocks.CLINKER_BRICK_WALL.get().asItem().getDefaultInstance(),
                ModBlocks.STOCK_BRICKS.get().asItem().getDefaultInstance(),
                ModBlocks.STOCK_BRICK_SLAB.get().asItem().getDefaultInstance(),
                ModBlocks.STOCK_BRICK_STAIRS.get().asItem().getDefaultInstance(),
                ModBlocks.STOCK_BRICK_WALL.get().asItem().getDefaultInstance(),
                ModBlocks.CREAM_BRICKS.get().asItem().getDefaultInstance(),
                ModBlocks.CREAM_BRICK_SLAB.get().asItem().getDefaultInstance(),
                ModBlocks.CREAM_BRICK_STAIRS.get().asItem().getDefaultInstance(),
                ModBlocks.CREAM_BRICK_WALL.get().asItem().getDefaultInstance()
        );


        after(e, Items.IRON_BLOCK, CreativeModeTabs.BUILDING_BLOCKS,
                ModBlocks.WHITE_CORRUGATED_IRON, ModBlocks.LIGHT_GRAY_CORRUGATED_IRON, ModBlocks.GRAY_CORRUGATED_IRON, ModBlocks.BLACK_CORRUGATED_IRON,ModBlocks.BROWN_CORRUGATED_IRON,ModBlocks.RED_CORRUGATED_IRON,ModBlocks.ORANGE_CORRUGATED_IRON,ModBlocks.YELLOW_CORRUGATED_IRON,ModBlocks.LIME_CORRUGATED_IRON,ModBlocks.GREEN_CORRUGATED_IRON,ModBlocks.CYAN_CORRUGATED_IRON,ModBlocks.LIGHT_BLUE_CORRUGATED_IRON,ModBlocks.BLUE_CORRUGATED_IRON,ModBlocks.PURPLE_CORRUGATED_IRON,ModBlocks.MAGENTA_CORRUGATED_IRON,ModBlocks.PINK_CORRUGATED_IRON,
                ModBlocks.WEATHERED_WHITE_CORRUGATED_IRON, ModBlocks.WEATHERED_LIGHT_GRAY_CORRUGATED_IRON, ModBlocks.WEATHERED_GRAY_CORRUGATED_IRON, ModBlocks.WEATHERED_BLACK_CORRUGATED_IRON,ModBlocks.WEATHERED_BROWN_CORRUGATED_IRON,ModBlocks.WEATHERED_RED_CORRUGATED_IRON,ModBlocks.WEATHERED_ORANGE_CORRUGATED_IRON,ModBlocks.WEATHERED_YELLOW_CORRUGATED_IRON,ModBlocks.WEATHERED_LIME_CORRUGATED_IRON,ModBlocks.WEATHERED_GREEN_CORRUGATED_IRON,ModBlocks.WEATHERED_CYAN_CORRUGATED_IRON,ModBlocks.WEATHERED_LIGHT_BLUE_CORRUGATED_IRON,ModBlocks.WEATHERED_BLUE_CORRUGATED_IRON,ModBlocks.WEATHERED_PURPLE_CORRUGATED_IRON,ModBlocks.WEATHERED_MAGENTA_CORRUGATED_IRON,ModBlocks.WEATHERED_PINK_CORRUGATED_IRON,
                ModBlocks.RUSTED_CORRUGATED_IRON,

                ModBlocks.WHITE_CORRUGATED_IRON_STAIRS, ModBlocks.LIGHT_GRAY_CORRUGATED_IRON_STAIRS, ModBlocks.GRAY_CORRUGATED_IRON_STAIRS, ModBlocks.BLACK_CORRUGATED_IRON_STAIRS,ModBlocks.BROWN_CORRUGATED_IRON_STAIRS,ModBlocks.RED_CORRUGATED_IRON_STAIRS,ModBlocks.ORANGE_CORRUGATED_IRON_STAIRS,ModBlocks.YELLOW_CORRUGATED_IRON_STAIRS,ModBlocks.LIME_CORRUGATED_IRON_STAIRS,ModBlocks.GREEN_CORRUGATED_IRON_STAIRS,ModBlocks.CYAN_CORRUGATED_IRON_STAIRS,ModBlocks.LIGHT_BLUE_CORRUGATED_IRON_STAIRS,ModBlocks.BLUE_CORRUGATED_IRON_STAIRS,ModBlocks.PURPLE_CORRUGATED_IRON_STAIRS,ModBlocks.MAGENTA_CORRUGATED_IRON_STAIRS,ModBlocks.PINK_CORRUGATED_IRON_STAIRS,
                ModBlocks.WEATHERED_WHITE_CORRUGATED_IRON_STAIRS, ModBlocks.WEATHERED_LIGHT_GRAY_CORRUGATED_IRON_STAIRS, ModBlocks.WEATHERED_GRAY_CORRUGATED_IRON_STAIRS, ModBlocks.WEATHERED_BLACK_CORRUGATED_IRON_STAIRS,ModBlocks.WEATHERED_BROWN_CORRUGATED_IRON_STAIRS,ModBlocks.WEATHERED_RED_CORRUGATED_IRON_STAIRS,ModBlocks.WEATHERED_ORANGE_CORRUGATED_IRON_STAIRS,ModBlocks.WEATHERED_YELLOW_CORRUGATED_IRON_STAIRS,ModBlocks.WEATHERED_LIME_CORRUGATED_IRON_STAIRS,ModBlocks.WEATHERED_GREEN_CORRUGATED_IRON_STAIRS,ModBlocks.WEATHERED_CYAN_CORRUGATED_IRON_STAIRS,ModBlocks.WEATHERED_LIGHT_BLUE_CORRUGATED_IRON_STAIRS,ModBlocks.WEATHERED_BLUE_CORRUGATED_IRON_STAIRS,ModBlocks.WEATHERED_PURPLE_CORRUGATED_IRON_STAIRS,ModBlocks.WEATHERED_MAGENTA_CORRUGATED_IRON_STAIRS,ModBlocks.WEATHERED_PINK_CORRUGATED_IRON_STAIRS,
                ModBlocks.RUSTED_CORRUGATED_IRON_STAIRS,

                ModBlocks.WHITE_CORRUGATED_IRON_SLAB, ModBlocks.LIGHT_GRAY_CORRUGATED_IRON_SLAB, ModBlocks.GRAY_CORRUGATED_IRON_SLAB, ModBlocks.BLACK_CORRUGATED_IRON_SLAB,ModBlocks.BROWN_CORRUGATED_IRON_SLAB,ModBlocks.RED_CORRUGATED_IRON_SLAB,ModBlocks.ORANGE_CORRUGATED_IRON_SLAB,ModBlocks.YELLOW_CORRUGATED_IRON_SLAB,ModBlocks.LIME_CORRUGATED_IRON_SLAB,ModBlocks.GREEN_CORRUGATED_IRON_SLAB,ModBlocks.CYAN_CORRUGATED_IRON_SLAB,ModBlocks.LIGHT_BLUE_CORRUGATED_IRON_SLAB,ModBlocks.BLUE_CORRUGATED_IRON_SLAB,ModBlocks.PURPLE_CORRUGATED_IRON_SLAB,ModBlocks.MAGENTA_CORRUGATED_IRON_SLAB,ModBlocks.PINK_CORRUGATED_IRON_SLAB,
                ModBlocks.WEATHERED_WHITE_CORRUGATED_IRON_SLAB, ModBlocks.WEATHERED_LIGHT_GRAY_CORRUGATED_IRON_SLAB, ModBlocks.WEATHERED_GRAY_CORRUGATED_IRON_SLAB, ModBlocks.WEATHERED_BLACK_CORRUGATED_IRON_SLAB,ModBlocks.WEATHERED_BROWN_CORRUGATED_IRON_SLAB,ModBlocks.WEATHERED_RED_CORRUGATED_IRON_SLAB,ModBlocks.WEATHERED_ORANGE_CORRUGATED_IRON_SLAB,ModBlocks.WEATHERED_YELLOW_CORRUGATED_IRON_SLAB,ModBlocks.WEATHERED_LIME_CORRUGATED_IRON_SLAB,ModBlocks.WEATHERED_GREEN_CORRUGATED_IRON_SLAB,ModBlocks.WEATHERED_CYAN_CORRUGATED_IRON_SLAB,ModBlocks.WEATHERED_LIGHT_BLUE_CORRUGATED_IRON_SLAB,ModBlocks.WEATHERED_BLUE_CORRUGATED_IRON_SLAB,ModBlocks.WEATHERED_PURPLE_CORRUGATED_IRON_SLAB,ModBlocks.WEATHERED_MAGENTA_CORRUGATED_IRON_SLAB,ModBlocks.WEATHERED_PINK_CORRUGATED_IRON_SLAB,
                ModBlocks.RUSTED_CORRUGATED_IRON_SLAB
        );

        after(e, Items.PINK_CONCRETE_POWDER, CreativeModeTabs.COLORED_BLOCKS,
                ModBlocks.WHITE_CORRUGATED_IRON, ModBlocks.LIGHT_GRAY_CORRUGATED_IRON, ModBlocks.GRAY_CORRUGATED_IRON, ModBlocks.BLACK_CORRUGATED_IRON,ModBlocks.BROWN_CORRUGATED_IRON,ModBlocks.RED_CORRUGATED_IRON,ModBlocks.ORANGE_CORRUGATED_IRON,ModBlocks.YELLOW_CORRUGATED_IRON,ModBlocks.LIME_CORRUGATED_IRON,ModBlocks.GREEN_CORRUGATED_IRON,ModBlocks.CYAN_CORRUGATED_IRON,ModBlocks.LIGHT_BLUE_CORRUGATED_IRON,ModBlocks.BLUE_CORRUGATED_IRON,ModBlocks.PURPLE_CORRUGATED_IRON,ModBlocks.MAGENTA_CORRUGATED_IRON,ModBlocks.PINK_CORRUGATED_IRON,
                ModBlocks.WHITE_CORRUGATED_IRON_STAIRS, ModBlocks.LIGHT_GRAY_CORRUGATED_IRON_STAIRS, ModBlocks.GRAY_CORRUGATED_IRON_STAIRS, ModBlocks.BLACK_CORRUGATED_IRON_STAIRS,ModBlocks.BROWN_CORRUGATED_IRON_STAIRS,ModBlocks.RED_CORRUGATED_IRON_STAIRS,ModBlocks.ORANGE_CORRUGATED_IRON_STAIRS,ModBlocks.YELLOW_CORRUGATED_IRON_STAIRS,ModBlocks.LIME_CORRUGATED_IRON_STAIRS,ModBlocks.GREEN_CORRUGATED_IRON_STAIRS,ModBlocks.CYAN_CORRUGATED_IRON_STAIRS,ModBlocks.LIGHT_BLUE_CORRUGATED_IRON_STAIRS,ModBlocks.BLUE_CORRUGATED_IRON_STAIRS,ModBlocks.PURPLE_CORRUGATED_IRON_STAIRS,ModBlocks.MAGENTA_CORRUGATED_IRON_STAIRS,ModBlocks.PINK_CORRUGATED_IRON_STAIRS,
                ModBlocks.WHITE_CORRUGATED_IRON_SLAB, ModBlocks.LIGHT_GRAY_CORRUGATED_IRON_SLAB, ModBlocks.GRAY_CORRUGATED_IRON_SLAB, ModBlocks.BLACK_CORRUGATED_IRON_SLAB,ModBlocks.BROWN_CORRUGATED_IRON_SLAB,ModBlocks.RED_CORRUGATED_IRON_SLAB,ModBlocks.ORANGE_CORRUGATED_IRON_SLAB,ModBlocks.YELLOW_CORRUGATED_IRON_SLAB,ModBlocks.LIME_CORRUGATED_IRON_SLAB,ModBlocks.GREEN_CORRUGATED_IRON_SLAB,ModBlocks.CYAN_CORRUGATED_IRON_SLAB,ModBlocks.LIGHT_BLUE_CORRUGATED_IRON_SLAB,ModBlocks.BLUE_CORRUGATED_IRON_SLAB,ModBlocks.PURPLE_CORRUGATED_IRON_SLAB,ModBlocks.MAGENTA_CORRUGATED_IRON_SLAB,ModBlocks.PINK_CORRUGATED_IRON_SLAB
        );
    }

    private static void after(RegHelper.ItemToTabEvent event, Item target,
                              ResourceKey<CreativeModeTab> tab, Supplier<?>... items) {
        after(event, i -> i.is(target), tab, items);
    }

    private static void after(RegHelper.ItemToTabEvent event, Predicate<ItemStack> targetPred,
                              ResourceKey<CreativeModeTab> tab, Supplier<?>... items) {
        if (items.length == 0) return;
        Object first = items[0].get();
        if (first instanceof ItemStack) {
            ItemStack[] entries = Arrays.stream(items).map(s -> (ItemStack) s.get()).toArray(ItemStack[]::new);
            event.addAfter(tab, targetPred, entries);
        } else {
            ItemLike[] entries = Arrays.stream(items).map(s -> (ItemLike) s.get()).toArray(ItemLike[]::new);
            event.addAfter(tab, targetPred, entries);
        }
    }

    private static void afterStacks(RegHelper.ItemToTabEvent event, Item target,
                                    ResourceKey<CreativeModeTab> tab, ItemStack... stacks) {
        event.addAfter(tab, i -> i.is(target), stacks);
    }
}
