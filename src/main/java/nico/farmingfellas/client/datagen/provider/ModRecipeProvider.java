package nico.farmingfellas.client.datagen.provider;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.criterion.UsingItemCriterion;
import net.minecraft.block.Blocks;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.tag.ItemTags;
import nico.farmingfellas.FarmingFellasMain;
import nico.farmingfellas.common.block.ModBlocks;
import nico.farmingfellas.common.item.ModItems;

import java.util.List;
import java.util.function.Consumer;

public class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generate(Consumer<RecipeJsonProvider> consumer) {
        consumer.accept(new ShapelessRecipeJsonBuilder.ShapelessRecipeJsonProvider(
                ModItems.FERTILIZER_ITEM.getRegistryEntry().getKey().get().getValue(),
                ModItems.FERTILIZER_ITEM,
                8,
                FarmingFellasMain.id("fertilizer").toString(),
                CraftingRecipeCategory.MISC,
                List.of(
                        Ingredient.ofItems(Items.BONE_MEAL),
                        Ingredient.fromTag(ItemTags.VILLAGER_PLANTABLE_SEEDS),
                        Ingredient.ofItems(Items.ROTTEN_FLESH)
                ),
                Advancement.Builder.create().criterion(hasItem(ModItems.FERTILIZER_ITEM), conditionsFromItem(ModItems.FERTILIZER_ITEM)),
                ModItems.FERTILIZER_ITEM.getRegistryEntry().getKey().get().getValue()
        ));


        ShapedRecipeJsonBuilder.create(
                RecipeCategory.MISC,
                ModBlocks.FERTILIZER_HOLDER
        )
                .input('#', ItemTags.WOODEN_FENCES)
                .input('_', ItemTags.PLANKS)
                .pattern("# #")
                .pattern("# #")
                .pattern("___")
                .criterion(hasItem(ModItems.FERTILIZER_ITEM), conditionsFromItem(ModItems.FERTILIZER_ITEM))
                .offerTo(consumer, ModBlocks.FERTILIZER_HOLDER.getRegistryEntry().getKey().get().getValue());

        ShapedRecipeJsonBuilder.create(
                RecipeCategory.DECORATIONS,
                ModItems.SAPLING_HOLDER_KIT
        )
                .input('#', Items.STICK)
                .input('S', Items.STRING)
                .pattern("#S#")
                .pattern("#S#")
                .criterion(hasItem(Items.STICK), conditionsFromTag(ItemTags.DIRT))
                .offerTo(consumer, ModItems.SAPLING_HOLDER_KIT.getRegistryEntry().getKey().get().getValue());
    }


}
