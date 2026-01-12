package nico.farmingfellas.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import nico.farmingfellas.client.renderer.ModRenderLayers;
import nico.farmingfellas.client.renderer.entity.FellaGolemRenderer;
import nico.farmingfellas.client.renderer.entity.model.FarmingGolemModel;
import nico.farmingfellas.client.renderer.entity.model.LumberjackGolemModel;
import nico.farmingfellas.client.renderer.item.GolemItemRenderer;
import nico.farmingfellas.client.renderer.world.ZoneAreaRenderer;
import nico.farmingfellas.common.entity.ModEntities;
import nico.farmingfellas.screen.ModHandledScreens;
import nico.farmingfellas.screen.custom.Generic3x2ContainerScreen;

public class FarmingFellasClient implements ClientModInitializer {

    public static GolemItemRenderer golemItemRenderer;

    @Override
    public void onInitializeClient() {
        ModRenderLayers.register();

        EntityRendererRegistry.register(ModEntities.FARMING_GOLEM, ctx -> new FellaGolemRenderer<>(ctx, new FarmingGolemModel<>(ctx.getPart(ModRenderLayers.FARMING_GOLEM_LAYER))));
        EntityRendererRegistry.register(ModEntities.LUMBERJACK_GOLEM, ctx -> new FellaGolemRenderer<>(ctx, new LumberjackGolemModel<>(ctx.getPart(ModRenderLayers.LUMBERJACK_GOLEM_LAYER))));

        HandledScreens.register(ModHandledScreens.GENERIC_3X2, Generic3x2ContainerScreen::new);

        WorldRenderEvents.AFTER_TRANSLUCENT.register(ZoneAreaRenderer::renderZone);

        golemItemRenderer = new GolemItemRenderer(MinecraftClient.getInstance(), null, null, null, null);
    }
}
