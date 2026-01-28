package nico.farmingfellas.common.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import nico.farmingfellas.FarmingFellasMain;
import nico.farmingfellas.common.entity.beekeeper.BeekeeperFellaEntity;
import nico.farmingfellas.common.entity.farming.FarmingFellaEntity;
import nico.farmingfellas.common.entity.lumberjack.LumberjackFellaEntity;
import nico.farmingfellas.common.entity.mining.MiningFellaEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ModEntities {
    private static final EntityDimensions DEFAULT_DIMENSIONS = EntityDimensions.fixed(0.75f, 0.95f);
    private static Map<EntityType<? extends LivingEntity>, Supplier<DefaultAttributeContainer.Builder>> ATTRIBUTES = new HashMap<>();

    public static final EntityType<FarmingFellaEntity> FARMING_GOLEM = register("farming_golem", FarmingFellaEntity::new, FarmingFellaEntity::createGolemAttributes);
    public static final EntityType<LumberjackFellaEntity> LUMBERJACK_GOLEM = register("lumberjack_golem", LumberjackFellaEntity::new, LumberjackFellaEntity::createGolemAttributes);
    public static final EntityType<MiningFellaEntity> MINING_GOLEM = register("mining_golem", MiningFellaEntity::new, MiningFellaEntity::createGolemAttributes);
    public static final EntityType<BeekeeperFellaEntity> BEEKEEPER_GOLEM = register("beekeeper_golem", BeekeeperFellaEntity::new, BeekeeperFellaEntity::createGolemAttributes);

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
        var registeredType = Registry.register(Registries.ENTITY_TYPE, FarmingFellasMain.id(name), type);
        ATTRIBUTES.put(registeredType, attributes);
        return registeredType;
    }

}
