package nico.farmingfellas.screen;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlag;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import nico.farmingfellas.FarmingFellasMain;
import nico.farmingfellas.screen.custom.Generic3x2ContainerScreenHandler;

public class ModHandledScreens {
    public static final ScreenHandlerType<Generic3x2ContainerScreenHandler> GENERIC_3X2 = register("generic_3x2", Generic3x2ContainerScreenHandler::new);

    public static void register() {

    }

    private static <T extends ScreenHandler> ScreenHandlerType<T> register(String id, ScreenHandlerType.Factory<T> factory) {
        return Registry.register(Registries.SCREEN_HANDLER, FarmingFellasMain.id(id), new ScreenHandlerType<>(factory, FeatureFlags.VANILLA_FEATURES));
    }

    private static <T extends ScreenHandler> ScreenHandlerType<T> register(String id, ScreenHandlerType.Factory<T> factory, FeatureFlag... requiredFeatures) {
        return Registry.register(Registries.SCREEN_HANDLER, FarmingFellasMain.id(id), new ScreenHandlerType<>(factory, FeatureFlags.FEATURE_MANAGER.featureSetOf(requiredFeatures)));
    }
}
