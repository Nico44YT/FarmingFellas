package nico.farmingfellas.common.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import nico.farmingfellas.FarmingFellasMain;
import nico.farmingfellas.common.entity.farming.FarmingFellaEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ModEntities {
    private static final EntityDimensions DEFAULT_DIMENSIONS = EntityDimensions.fixed(0.75f, 0.95f);
    private static Map<EntityType<? extends LivingEntity>, Supplier<DefaultAttributeContainer.Builder>> ATTRIBUTES = new HashMap<>();

    public static final EntityType<FarmingFellaEntity> FARMING_GOLEM = register("farming_golem", FarmingFellaEntity::new, FarmingFellaEntity::createGolemAttributes);


    public static void register() {
        ModEntities.ATTRIBUTES.forEach((entityType, builderSupplier) -> FabricDefaultAttributeRegistry.register(entityType, builderSupplier.get()));
    }

    private static <T extends LivingEntity> FabricEntityTypeBuilder<T> defaultFactory(EntityType.EntityFactory<T> factory) {
        return FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, factory).dimensions(ModEntities.DEFAULT_DIMENSIONS).spawnableFarFromPlayer();
    }
    private static <T extends LivingEntity> EntityType<T> register(String name, EntityType.EntityFactory<T> factory, Supplier<DefaultAttributeContainer.Builder> attributes) {
        return register(name, defaultFactory(factory), attributes);
    }
    private static <T extends LivingEntity> EntityType<T> register(String name, FabricEntityTypeBuilder<T> typeBuilder, Supplier<DefaultAttributeContainer.Builder> attributes) {
        return register(name, typeBuilder.build(), attributes);
    }
    private static <T extends LivingEntity> EntityType<T> register(String name, EntityType<T> type, Supplier<DefaultAttributeContainer.Builder> attributes) {
        var $ = Registry.register(Registries.ENTITY_TYPE, FarmingFellasMain.id(name), type);
        ATTRIBUTES.put($, attributes);
        return $;
    }

}
