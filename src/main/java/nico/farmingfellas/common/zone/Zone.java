package nico.farmingfellas.common.zone;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;
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

    public void setCornerA(BlockPos cornerA) {
        this.cornerA = cornerA;
    }

    public void setCornerB(BlockPos cornerB) {
        this.cornerB = cornerB;
    }

    public Optional<BlockPos> getCornerA() {
        return Optional.ofNullable(cornerA);
    }

    public Optional<BlockPos> getCornerB() {
        return Optional.ofNullable(cornerB);
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

    public NbtCompound asNbt() {
        NbtCompound nbt = new NbtCompound();

        if (cornerA != null) nbt.putLong("corner_a", cornerA.asLong());
        if (cornerB != null) nbt.putLong("corner_b", cornerB.asLong());
        nbt.putUuid("zone_id", zoneId);

        return nbt;
    }

    public static Zone fromNbt(NbtCompound nbt) {
        Zone zone = new Zone(nbt.getUuid("zone_id"));
        if (nbt.contains("corner_a")) zone.setCornerA(BlockPos.fromLong(nbt.getLong("corner_a")));
        if (nbt.contains("corner_b")) zone.setCornerB(BlockPos.fromLong(nbt.getLong("corner_b")));

        return zone;
    }

    public void copyData(Zone otherZone) {
        this.cornerA = otherZone.cornerA;
        this.cornerB = otherZone.cornerB;
    }
}
