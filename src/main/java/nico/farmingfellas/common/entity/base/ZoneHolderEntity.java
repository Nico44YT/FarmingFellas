package nico.farmingfellas.common.entity.base;

import net.minecraft.util.math.BlockPos;
import nico.farmingfellas.common.zone.Zone;

import java.util.UUID;

public interface ZoneHolderEntity {
    boolean isInZone(BlockPos targetPosition);
    void setZone(UUID uuid);

    default void setZone(Zone zone) {
        setZone(zone.getZoneId());
    }
}
