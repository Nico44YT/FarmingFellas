package nico.farmingfellas;

import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import nico.farmingfellas.common.entity.ModEntities;
import nico.farmingfellas.screen.ModHandledScreens;

public class FarmingFellasMain implements ModInitializer {

    public static final String MOD_ID = "farming_fellas";

    @Override
    public void onInitialize() {
        ModEntities.register();

        ModHandledScreens.register();
    }

    public static Identifier id(String name) {
        return Identifier.of(MOD_ID, name);
    }
}
