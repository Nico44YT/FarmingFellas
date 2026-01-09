package nico.farmingfellas.common.entity.base.goal;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;

public class PathGoal<T extends PathAwareEntity> extends Goal {

    protected T entity;
    protected float speed;

    protected BlockPos targetPosition;

    public PathGoal(T entity, float speed) {
        this.entity = entity;
        this.speed = speed;
    }

    public void setTargetPosition(BlockPos blockPos) {
        this.targetPosition = blockPos;
    }

    @Override
    public boolean canStart() {
        return targetPosition != null && entity.getNavigation().isValidPosition(targetPosition);
    }

    @Override
    public void start() {
        this.entity.getNavigation().startMovingTo(
                targetPosition.getX() + 0.5,
                targetPosition.getY(),
                targetPosition.getZ() + 0.5,
                speed
        );
    }
}
