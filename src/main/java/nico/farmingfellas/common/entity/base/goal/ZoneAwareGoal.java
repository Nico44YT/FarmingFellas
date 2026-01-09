package nico.farmingfellas.common.entity.base.goal;

import net.minecraft.entity.mob.PathAwareEntity;
import nico.farmingfellas.common.entity.base.ZoneHolderEntity;

public class ZoneAwareGoal<T extends PathAwareEntity & ZoneHolderEntity> extends PathGoal<T> {
    public ZoneAwareGoal(T entity, float speed) {
        super(entity, speed);
    }

    @Override
    public boolean canStart() {
        return super.canStart() && entity.isInZone(targetPosition);
    }
}
