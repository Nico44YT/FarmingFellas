package nico.farmingfellas.client.datagen.provider;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import nico.farmingfellas.common.block.ModBlocks;

import java.util.concurrent.CompletableFuture;

public class ModTagProvider {
    public static class ModBlockTagProvider extends FabricTagProvider<Block> {

        public ModBlockTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
            super(output, RegistryKeys.BLOCK, registryLookup);
        }

        @Override
        protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
            getOrCreateTagBuilder(BlockTags.SHOVEL_MINEABLE).add(ModBlocks.FERTILIZED_FARMLAND);
            getOrCreateTagBuilder(BlockTags.BIG_DRIPLEAF_PLACEABLE).add(ModBlocks.FERTILIZED_FARMLAND);

            getOrCreateTagBuilder(BlockTags.AXE_MINEABLE).add(ModBlocks.FERTILIZER_HOLDER);

        }
    }
}
