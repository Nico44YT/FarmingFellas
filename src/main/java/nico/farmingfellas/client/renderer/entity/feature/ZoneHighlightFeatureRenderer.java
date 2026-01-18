package nico.farmingfellas.client.renderer.entity.feature;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Hand;
import nico.farmingfellas.common.entity.base.FellaGolemEntity;
import nico.farmingfellas.common.entity.base.ZoneHolderEntity;
import nico.farmingfellas.common.item.custom.ZoneItem;

import java.util.Optional;
import java.util.UUID;

public class ZoneHighlightFeatureRenderer<T extends FellaGolemEntity & ZoneHolderEntity, M extends EntityModel<T>> extends FeatureRenderer<T, M> {

    public ZoneHighlightFeatureRenderer(FeatureRendererContext<T, M> context, EntityRendererFactory.Context rendererFactoryContext, M model) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        Optional<UUID> optionalUUID = entity.getDataTracker().get(FellaGolemEntity.ZONE_ID);
        optionalUUID.ifPresent(zoneId -> {
            var mainHandStack = MinecraftClient.getInstance().player.getStackInHand(Hand.MAIN_HAND);
            if(mainHandStack.getItem() instanceof ZoneItem) {
                try{
                    UUID stackZoneId = mainHandStack.getSubNbt(ZoneItem.ZONE_DATA).getUuid(ZoneItem.ZONE_ID);
                    if(!zoneId.equals(stackZoneId)) return;

                    VertexConsumer consumer = vertexConsumers.getBuffer(
                            RenderLayer.getEntityTranslucent(getTexture(entity))
                    );

                    // RGBA overlay color
                    float r = 1.0f;
                    float g = 0.0f;
                    float b = 1.0f;
                    float a = 0.75f;

                    getContextModel().render(
                            matrices,
                            consumer,
                            light,
                            OverlayTexture.DEFAULT_UV,
                            r, g, b, a
                    );
                } catch (RuntimeException e) {

                }
            }
        });
    }
}
