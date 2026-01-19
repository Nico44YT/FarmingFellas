package nico.farmingfellas.client.datagen.provider;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.block.Blocks;
import nico.farmingfellas.common.block.ModBlocks;

public class ModLootTableProvider extends FabricBlockLootTableProvider {
    public ModLootTableProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generate() {
        this.addDrop(ModBlocks.FERTILIZER_HOLDER);

        this.addDrop(ModBlocks.FERTILIZED_FARMLAND, Blocks.DIRT);
    }
}
