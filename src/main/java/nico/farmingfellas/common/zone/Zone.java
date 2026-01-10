package nico.farmingfellas.common.zone;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class Zone {

    private final UUID zoneId;

    private BlockPos cornerA;
    private BlockPos cornerB;

    private long lastUpdateTime;

    public Zone(UUID zoneId) {
        this.zoneId = zoneId;
    }

    public void setCorners(@Nullable World world, BlockPos cornerA, BlockPos cornerB) {
        if(world != null) this.lastUpdateTime = world.getTime();
        this.cornerA = cornerA;
        this.cornerB = cornerB;
    }

    public void setCornerA(@Nullable World world, BlockPos cornerA) {
        if(world != null) this.lastUpdateTime = world.getTime();
        this.cornerA = cornerA;
    }

    public void setCornerB(@Nullable World world, BlockPos cornerB) {
        if(world != null) this.lastUpdateTime = world.getTime();
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

    public long getLastUpdateTime() {
        return this.lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
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
        nbt.putLong("last_update_time", lastUpdateTime);
        nbt.putUuid("zone_id", zoneId);

        return nbt;
    }

    public static Zone fromNbt(NbtCompound nbt) {
        Zone zone = new Zone(nbt.getUuid("zone_id"));
        if (nbt.contains("corner_a")) zone.setCornerA(null, BlockPos.fromLong(nbt.getLong("corner_a")));
        if (nbt.contains("corner_b")) zone.setCornerB(null, BlockPos.fromLong(nbt.getLong("corner_b")));
        zone.lastUpdateTime = nbt.getLong("last_update_time");

        return zone;
    }

    public void copyData(Zone otherZone) {
        this.cornerA = otherZone.cornerA;
        this.cornerB = otherZone.cornerB;
        this.lastUpdateTime = otherZone.lastUpdateTime;
    }
}
