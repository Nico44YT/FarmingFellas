package nico.farmingfellas.common.block;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import nico.farmingfellas.FarmingFellasMain;
import nico.farmingfellas.common.block.fertilized_farmland.FertilizedFarmlandBlock;
import nico.farmingfellas.common.block.fertilizer.FertilizerHolderBlock;
import nico.farmingfellas.common.block.fertilizer.FertilizerHolderBlockEntity;
import nico.farmingfellas.common.item.ModItems;

import java.util.function.Function;

public class ModBlocks {

    public static final Block FERTILIZER_HOLDER = register("fertilizer_holder", AbstractBlock.Settings.copy(Blocks.OAK_PLANKS), FertilizerHolderBlock::new, new Item.Settings());
    public static final Block FERTILIZED_FARMLAND = register("fertilized_farmland", AbstractBlock.Settings.copy(Blocks.FARMLAND).ticksRandomly(), FertilizedFarmlandBlock::new, new Item.Settings());

    public static final BlockEntityType<FertilizerHolderBlockEntity> FERTILIZED_HOLDER_TYPE = registerType("fertilizer_holder", FertilizerHolderBlockEntity::new, new Block[]{FERTILIZER_HOLDER});

    public static void register() {

    }

    private static <T extends Block> T register(String name, AbstractBlock.Settings settings, Function<AbstractBlock.Settings, T> factory) {
        return Registry.register(Registries.BLOCK, FarmingFellasMain.id(name), factory.apply(settings));
    }

    private static <T extends Block> T register(String name, AbstractBlock.Settings blockSettings, Function<AbstractBlock.Settings, T> factory, Item.Settings itemSettings) {
        var block = register(name, blockSettings, factory);
        ModItems.register(name, itemSettings, ($) -> new BlockItem(block, $));
        return block;
    }

    private static <T extends BlockEntity> BlockEntityType<T> registerType(String name, FabricBlockEntityTypeBuilder.Factory<T> factory, Block[] blocks) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, FarmingFellasMain.id(name), FabricBlockEntityTypeBuilder.create(factory, blocks).build());
    }
}
