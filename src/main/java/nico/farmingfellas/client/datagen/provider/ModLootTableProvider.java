package nico.farmingfellas.client.datagen.provider;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.block.Blocks;
import net.minecraft.data.server.loottable.LootTableGenerator;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import nico.farmingfellas.common.block.ModBlocks;
import nico.farmingfellas.common.item.ModItems;

public class ModLootTableProvider extends FabricBlockLootTableProvider {
    public ModLootTableProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generate() {
        this.addDrop(ModBlocks.FERTILIZER_HOLDER);

        this.addDrop(ModBlocks.FERTILIZED_FARMLAND, Blocks.DIRT);

        this.addDrop(ModBlocks.SAPLING_HOLDER, drop ->
                LootTable.builder()
                        .pool(this.addSurvivesExplosionCondition(
                                Blocks.DIRT,
                                LootPool.builder()
                                        .rolls(ConstantLootNumberProvider.create(1.0F))
                                        .with(ItemEntry.builder(Blocks.DIRT))
                        ))
                        .pool(this.addSurvivesExplosionCondition(
                                Blocks.DIRT,
                                LootPool.builder()
                                        .rolls(ConstantLootNumberProvider.create(1.0F))
                                        .with(ItemEntry.builder(ModItems.SAPLING_HOLDER_KIT))
                        ))
        );
    }
}
