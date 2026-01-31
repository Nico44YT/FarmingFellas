package nico.farmingfellas.common.entity.base.goal;

import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import nico.farmingfellas.common.entity.base.FellaGolemEntity;
import nico.farmingfellas.common.entity.base.GolemAnimationState;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class HarvestCropGoal<T extends FellaGolemEntity> extends Goal {
    protected final T golem;
    protected BlockPos targetCrop;

    public HarvestCropGoal(T golem) {
        this.golem = golem;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public void start() {
        golem.getNavigation().startMovingTo(
                targetCrop.getX() + 0.5,
                targetCrop.getY(),
                targetCrop.getZ() + 0.5,
                1.0
        );
    }

    @Override
    public boolean canStart() {
        if (golem.hasCookie()) return false;
        if (golem.isFull()) return false;
        targetCrop = findHarvestableCrop();
        return targetCrop != null;
    }

    @Override
    public boolean shouldContinue() {
        if (golem.isFull()) return false;
        if (targetCrop == null) return false;
        return isValidCrop(golem.getWorld(), targetCrop, golem.getWorld().getBlockState(targetCrop));
    }

    @Override
    public void tick() {
        if (targetCrop == null) return;

        if (!golem.isNavigating() || golem.getNavigation().isIdle()) {
            start();
        }

        BlockState state = golem.getWorld().getBlockState(targetCrop);
        World world = golem.getWorld();

        if (!isValidCrop(world, targetCrop, state)) {
            targetCrop = null;
            return;
        }

        if (!isNearCrop()) return;

        golem.setState(GolemAnimationState.WORKING);
        if (!harvest(golem, world, targetCrop, state)) return;

        collectNearbyItems();

        if (!replant(golem, world, targetCrop, state)) return;

        targetCrop = null;
        golem.setState(GolemAnimationState.IDLE);
    }

    protected BlockPos findHarvestableCrop() {
        BlockPos origin = golem.getBlockPos();
        World world = golem.getWorld();

        // Map of crop position -> squared distance
        Map<BlockPos, Double> cropDistances = new HashMap<>();

        golem.getZone().ifPresent(zone -> {
            zone.forEach(blockPos -> {
                BlockState state = world.getBlockState(blockPos);

                if (isValidCrop(world, blockPos, state)) {
                    boolean canHarvest = false;
                    if (!golem.hasFreeSlot() && state.getBlock() instanceof CropBlock crop) {
                        if (golem.containsAny(stack -> stack.getItem().equals(crop.getSeedsItem()))) {
                            canHarvest = true;
                        }
                    } else {
                        canHarvest = true;
                    }

                    if (canHarvest) {
                        double distSq = origin.getSquaredDistance(blockPos.getX(), blockPos.getY(), blockPos.getZ());

                        cropDistances.put(blockPos, distSq);
                    }

                }
            });
        });

        // Choose nearest valid crop
        return cropDistances.entrySet()
                .stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public boolean isNearCrop() {
        return golem.squaredDistanceTo(targetCrop.getX() + 0.5, targetCrop.getY(), targetCrop.getZ() + 0.5) <= 2.35;
    }

    private void collectNearbyItems() {
        World world = golem.getWorld();

        Box box = new Box(targetCrop).expand(1.5);

        List<ItemEntity> items = world.getEntitiesByClass(
                ItemEntity.class,
                box,
                ItemEntity::isAlive
        );

        for (ItemEntity itemEntity : items) {
            ItemStack stack = itemEntity.getStack();
            ItemStack remainder = golem.insertStack(stack);

            if (remainder.isEmpty()) {
                itemEntity.discard();
            } else {
                itemEntity.setStack(remainder);
            }
        }
    }

    public abstract boolean isValidCrop(World world, BlockPos pos, BlockState state);

    public abstract boolean harvest(T golem, World world, BlockPos pos, BlockState state);

    public abstract boolean replant(T golem, World world, BlockPos pos, BlockState state);

}
