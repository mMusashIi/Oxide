package com.ordana.oxide.recipe;

import com.google.gson.JsonObject;
import com.ordana.oxide.items.SFStackView;
import com.ordana.oxide.items.VarnishSprayer;
import com.ordana.oxide.reg.ModRecipes;
import com.ordana.oxide.reg.ModTags;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class ChargeSprayerRecipe extends CustomRecipe {

    private final Ingredient sprayerIngredient;
    private final int chargesPerBottle;
    private final boolean canOverflow;

    public ChargeSprayerRecipe(ResourceLocation id, CraftingBookCategory category, Ingredient arrow,
                               int chargesPerItem, boolean canOverflow) {
        super(id, category);
        this.sprayerIngredient = arrow;
        this.chargesPerBottle = chargesPerItem;
        this.canOverflow = canOverflow;
    }

    private int getBottlesToAdd(ItemStack sprayer, ItemStack charge, Level level) {
        SFStackView sprayerContent = VarnishSprayer.getFluidComponent(sprayer);
        var bottleContent = SoftFluidStack.fromItem(charge);
        if (bottleContent == null) return 0;
        SoftFluidStack bottleFluid = bottleContent.getFirst();
        if (bottleFluid.isEmpty()) return 0;
        if (!bottleFluid.is(ModTags.CAN_GO_IN_SPRAY)) return 0;
        if (sprayerContent.isEmpty() || sprayerContent.sameFluidSameComponents(bottleFluid)) {
            return bottleFluid.getCount();
        }
        return 0;
    }

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn) {

        ItemStack sprayer = null;
        ItemStack fluidBottleItem = null;
        int newTotalCharges = 0;

        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (sprayerIngredient.test(stack)) {
                if (sprayer != null) {
                    return false;
                }
                sprayer = stack;
            } else if (!stack.isEmpty()) {
                if (fluidBottleItem != null) return false;
                fluidBottleItem = stack;
            }
        }
        if (sprayer == null || fluidBottleItem == null) return false;
        
        int bottlesToAdd = getBottlesToAdd(sprayer, fluidBottleItem, worldIn);
        if (bottlesToAdd == 0) return false;
        newTotalCharges += chargesPerBottle * bottlesToAdd;

        return sprayer != null && fluidBottleItem != null && (canOverflow || newTotalCharges <=
                VarnishSprayer.getMaxCharges(sprayer));
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess access) {
        int newTotalCharges = 0;
        ItemStack arrow = null;
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (sprayerIngredient.test(stack)) {
                arrow = stack;
            }
        }
        if (arrow == null) return ItemStack.EMPTY;

        int maxCharges = VarnishSprayer.getMaxCharges(arrow);
        ItemStack returnSpray = arrow.copy();
        SoftFluidStack sf = VarnishSprayer.getFluidComponent(returnSpray)
                .toMutable();
        sf.setCount(Math.min(maxCharges, sf.getCount() + newTotalCharges)); // Note: logic was flawed originally, newTotalCharges wasn't populated from getBottlesToAdd.
        VarnishSprayer.setFluidComponent(returnSpray, sf);

        return returnSpray;

    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.CHARGE_SPRAYER.get();
    }

    public static class Serializer implements RecipeSerializer<ChargeSprayerRecipe> {

        @Override
        public ChargeSprayerRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            CraftingBookCategory category = CraftingBookCategory.CODEC.byName(GsonHelper.getAsString(json, "category", "misc"), CraftingBookCategory.MISC);
            Ingredient ingredient = Ingredient.fromJson(json.get("ingredient"));
            int chargesPerItem = GsonHelper.getAsInt(json, "charges_per_item", 1);
            boolean canOverflow = GsonHelper.getAsBoolean(json, "can_overfill", false);
            return new ChargeSprayerRecipe(recipeId, category, ingredient, chargesPerItem, canOverflow);
        }

        @Override
        public ChargeSprayerRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            CraftingBookCategory category = buffer.readEnum(CraftingBookCategory.class);
            Ingredient ingredient = Ingredient.fromNetwork(buffer);
            int chargesPerItem = buffer.readVarInt();
            boolean canOverflow = buffer.readBoolean();
            return new ChargeSprayerRecipe(recipeId, category, ingredient, chargesPerItem, canOverflow);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ChargeSprayerRecipe recipe) {
            buffer.writeEnum(recipe.category());
            recipe.sprayerIngredient.toNetwork(buffer);
            buffer.writeVarInt(recipe.chargesPerBottle);
            buffer.writeBoolean(recipe.canOverflow);
        }
    }
}
