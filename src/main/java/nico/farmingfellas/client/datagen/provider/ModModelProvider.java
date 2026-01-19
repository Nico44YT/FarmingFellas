package nico.farmingfellas.client.datagen.provider;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.client.*;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import nico.farmingfellas.common.block.ModBlocks;
import nico.farmingfellas.common.item.ModItems;
import nico.farmingfellas.common.item.SimpleItemModel;

public class ModModelProvider extends FabricModelProvider {

    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        registerFarmland(blockStateModelGenerator, ModBlocks.FERTILIZED_FARMLAND);
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        ModItems.items.forEach(item -> {
            if (item instanceof SimpleItemModel) {
                itemModelGenerator.register(item, Models.GENERATED);
            }
        });
    }

    private void registerFarmland(BlockStateModelGenerator blockStateModelGenerator, Block block) {
        TextureMap textureMap = (new TextureMap()).put(TextureKey.DIRT, TextureMap.getId(Blocks.DIRT)).put(TextureKey.TOP, TextureMap.getId(block));
        TextureMap textureMap2 = (new TextureMap()).put(TextureKey.DIRT, TextureMap.getId(Blocks.DIRT)).put(TextureKey.TOP, TextureMap.getSubId(block, "_moist"));
        Identifier identifier = Models.TEMPLATE_FARMLAND.upload(block, textureMap, blockStateModelGenerator.modelCollector);
        Identifier identifier2 = Models.TEMPLATE_FARMLAND.upload(TextureMap.getSubId(block, "_moist"), textureMap2, blockStateModelGenerator.modelCollector);
        blockStateModelGenerator.blockStateCollector.accept(VariantsBlockStateSupplier.create(block).coordinate(BlockStateModelGenerator.createValueFencedModelMap(Properties.MOISTURE, 7, identifier2, identifier)));
    }
}
