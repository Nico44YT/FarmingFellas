package nico.farmingfellas.client.renderer;

import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.mixin.client.rendering.EntityModelLayersAccessor;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import nico.farmingfellas.FarmingFellasMain;
import nico.farmingfellas.client.renderer.entity.model.*;

import java.util.OptionalDouble;

public class ModRenderLayers {

    public static final EntityModelLayer NO_ZONE_WARNING_LAYER = registerMain("no_zone_warning");

    public static final EntityModelLayer FARMING_GOLEM_LAYER = registerMain("farming_golem");
    public static final EntityModelLayer LUMBERJACK_GOLEM_LAYER = registerMain("lumberjack_golem");
    public static final EntityModelLayer MINING_GOLEM_LAYER = registerMain("mining_golem");
    public static final EntityModelLayer BEEKEEPER_GOLEM_LAYER = registerMain("beekeeper_golem");

    public static final RenderLayer ZONE_OVERLAY_LAYER = RenderLayer.of(
            FarmingFellasMain.id("zone_overlay").toString(),
            VertexFormats.POSITION_COLOR,
            VertexFormat.DrawMode.QUADS,
            0x20000,
            false,
            true,
            RenderLayer.MultiPhaseParameters.builder()
                    .program(RenderLayer.COLOR_PROGRAM)
                    .transparency(RenderLayer.TRANSLUCENT_TRANSPARENCY)
                    .cull(RenderLayer.DISABLE_CULLING)
                    .writeMaskState(RenderLayer.COLOR_MASK)
                    .layering(RenderLayer.POLYGON_OFFSET_LAYERING)
                    .build(true)
    );

    public static final RenderLayer ZONE_OVERLAY_LINES_LAYER = RenderLayer.of(
            FarmingFellasMain.id("zone_overlay_lines").toString(),
            VertexFormats.LINES,
            VertexFormat.DrawMode.LINES,
            256,
            false,
            true,
            RenderLayer.MultiPhaseParameters.builder()
                    .program(RenderLayer.LINES_PROGRAM)
                    .transparency(RenderLayer.TRANSLUCENT_TRANSPARENCY)
                    .cull(RenderLayer.DISABLE_CULLING)
                    .writeMaskState(RenderLayer.ALL_MASK)
                    .layering(RenderLayer.POLYGON_OFFSET_LAYERING)
                    .target(RenderLayer.ITEM_ENTITY_TARGET)
                    .lineWidth(new RenderPhase.LineWidth(OptionalDouble.of(5)))
                    .build(false)
    );

    public static void register() {
        EntityModelLayerRegistry.registerModelLayer(FARMING_GOLEM_LAYER, FarmingGolemModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(LUMBERJACK_GOLEM_LAYER, LumberjackGolemModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(MINING_GOLEM_LAYER, MiningGolemModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(BEEKEEPER_GOLEM_LAYER, BeekeeperGolemModel::getTexturedModelData);

        EntityModelLayerRegistry.registerModelLayer(NO_ZONE_WARNING_LAYER, ZoneMissingModel::getTexturedModelData);
    }

    private static EntityModelLayer registerMain(String id) {
        return register(id, "main");
    }

    private static EntityModelLayer register(String id, String layer) {
        EntityModelLayer entityModelLayer = create(id, layer);
        if (!EntityModelLayersAccessor.getLayers().add(entityModelLayer)) {
            throw new IllegalStateException("Duplicate registration for " + entityModelLayer);
        } else {
            return entityModelLayer;
        }
    }

    private static EntityModelLayer create(String id, String layer) {
        return new EntityModelLayer(FarmingFellasMain.id(id), layer);
    }
}
