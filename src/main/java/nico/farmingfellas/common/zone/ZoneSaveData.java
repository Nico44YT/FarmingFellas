package nico.farmingfellas.common.zone;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import nico.farmingfellas.FarmingFellasMain;

import java.util.*;

public class ZoneSaveData extends PersistentState {
    public static final Identifier ID = FarmingFellasMain.id("zone_save_data");
    public HashMap<World, Set<Zone>> zoneMap = new HashMap<>();

    public static ZoneSaveData ofEmpty() {
        return new ZoneSaveData();
    }

    public static ZoneSaveData get(MinecraftServer server) {
        return server.getWorld(World.OVERWORLD).getPersistentStateManager().getOrCreate(nbt -> ZoneSaveData.fromNbt(server, nbt), ZoneSaveData::ofEmpty, ID.toString());
    }

    public static Set<Zone> getZones(ServerWorld serverWorld) {
        return get(serverWorld.getServer()).zoneMap.get(serverWorld);
    }

    public static Optional<Zone> getZone(ServerWorld serverWorld, UUID zoneId) {
        return getZones(serverWorld).stream().filter(zone -> zone.getZoneId().equals(zoneId)).findFirst();
    }

    public static ZoneSaveData fromNbt(MinecraftServer server, NbtCompound nbt) {
        ZoneSaveData saveData = new ZoneSaveData();

        nbt.getKeys().forEach(worldKey -> {
            RegistryKey<World> worldRegistryKey = RegistryKey.of(RegistryKeys.WORLD, Identifier.tryParse(worldKey));
            World world = server.getWorld(worldRegistryKey);

            Set<Zone> zones = new HashSet<>();

            nbt.getCompound(worldKey).getKeys().forEach(zoneId -> {
                zones.add(Zone.fromNbt(nbt.getCompound(zoneId)));
            });

            saveData.zoneMap.put(world, zones);
        });

        return saveData;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        zoneMap.forEach((world, zones) -> {
            NbtCompound worldZones = new NbtCompound();

            for(Zone zone : zones) worldZones.put(zone.getZoneId().toString(), zone.asNbt());

            nbt.put(world.getRegistryKey().getValue().toString(), worldZones);
        });

        return nbt;
    }
}
