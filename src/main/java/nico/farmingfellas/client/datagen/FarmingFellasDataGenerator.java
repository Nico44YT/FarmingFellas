package nico.farmingfellas.client.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.data.DataProvider;
import nico.farmingfellas.client.datagen.provider.ModLootTableProvider;
import nico.farmingfellas.client.datagen.provider.ModModelProvider;
import nico.farmingfellas.client.datagen.provider.ModRecipeProvider;
import nico.farmingfellas.client.datagen.provider.ModTagProvider;

public class FarmingFellasDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

        pack.addProvider(ModModelProvider::new);

        pack.addProvider(ModLootTableProvider::new);
        pack.addProvider(ModTagProvider.ModBlockTagProvider::new);
        pack.addProvider(ModRecipeProvider::new);
    }
}
