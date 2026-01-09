package nico.farmingfellas.common.zone;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class Zone {

    private final UUID zoneId;

    private BlockPos cornerA;
    private BlockPos cornerB;

    public Zone(UUID zoneId) {
        this.zoneId = zoneId;
    }

    public void setCorners(BlockPos cornerA, BlockPos cornerB) {
        this.cornerA = cornerA;
        this.cornerB = cornerB;
    }

    public UUID getZoneId() {
        return this.zoneId;
    }

    @Override
    public int hashCode() {
        return zoneId.hashCode() * 31;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Zone zone && zone.zoneId.equals(this.zoneId);
    }

    public NbtElement asNbt() {
        NbtCompound nbt = new NbtCompound();

        nbt.putLong("corner_a", cornerA.asLong());
        nbt.putLong("corner_b", cornerB.asLong());
        nbt.putUuid("zone_id", zoneId);

        return nbt;
    }

    public static Zone fromNbt(NbtCompound nbt) {
        Zone zone = new Zone(nbt.getUuid("zone_id"));
        zone.setCorners(
                BlockPos.fromLong(nbt.getLong("corner_a")),
                BlockPos.fromLong(nbt.getLong("corner_b"))
        );

        return zone;
    }
}
