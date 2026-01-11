package nico.farmingfellas.common.entity.base;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import nico.farmingfellas.common.zone.Zone;

import java.util.Optional;
import java.util.UUID;

public interface ZoneHolderEntity {
    void setZone(UUID uuid);

    Optional<Zone> getZone();

    default void setZone(Zone zone) {
        setZone(zone.getZoneId());
    }
    default boolean isInZone(BlockPos pos) {
        return getZone().isPresent() && getZone().get().isInZone(pos);
    }

    ActionResult assignZone(Optional<UUID> optionalZoneId, ItemStack stack, PlayerEntity user);
}
