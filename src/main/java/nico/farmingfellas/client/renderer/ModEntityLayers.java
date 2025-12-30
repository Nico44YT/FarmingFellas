package nico.farmingfellas.client.renderer;

import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.mixin.client.rendering.EntityModelLayersAccessor;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import nico.farmingfellas.FarmingFellasMain;
import nico.farmingfellas.client.renderer.entity.model.FarmingGolemModel;

public class ModEntityLayers {

    public static final EntityModelLayer FARMING_GOLEM_LAYER = registerMain("farming_golem");

    public static void register() {
        EntityModelLayerRegistry.registerModelLayer(FARMING_GOLEM_LAYER, FarmingGolemModel::getTexturedModelData);
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
