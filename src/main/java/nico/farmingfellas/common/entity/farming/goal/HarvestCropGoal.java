package nico.farmingfellas.common.entity.farming.goal;

import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import nico.farmingfellas.common.entity.farming.FarmingFellaEntity;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class HarvestCropGoal extends Goal {
    private final FarmingFellaEntity golem;
    private BlockPos targetCrop;

    public HarvestCropGoal(FarmingFellaEntity golem) {
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

        if(!harvest(golem, world, targetCrop, state)) return;

        collectNearbyItems();

        if(!replant(golem, world, targetCrop, state)) return;

        targetCrop = null;
    }

    protected BlockPos findHarvestableCrop() {
        BlockPos origin = golem.getBlockPos();
        World world = golem.getWorld();

        if(golem.isZoneSet()) return findHarvestableCropInZone();

        int radius = 5;

        // Map of crop position -> squared distance
        Map<BlockPos, Double> cropDistances = new HashMap<>();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = origin.add(x, y, z);
                    BlockState state = world.getBlockState(pos);

                    if(!golem.isPositionAccessible(pos)) continue;

                    if (isValidCrop(world, pos, world.getBlockState(pos))) {

                        if (!golem.hasFreeSlot() && state.getBlock() instanceof CropBlock crop) {
                            if (golem.containsAny(stack -> stack.getItem().equals(crop.getSeedsItem()))) {
                                continue;
                            }
                        }

                        double distSq = origin.getSquaredDistance(
                                pos.getX(), pos.getY(), pos.getZ()
                        );

                        cropDistances.put(pos, distSq);
                    }
                }
            }
        }

        // Choose nearest valid crop
        return cropDistances.entrySet()
                .stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private BlockPos findHarvestableCropInZone() {
        Pair<BlockPos, BlockPos> corners = golem.getZone().get();
        World world = golem.getWorld();
        BlockPos origin = golem.getBlockPos();

        BlockPos c1 = corners.getLeft();
        BlockPos c2 = corners.getRight();

        int minX = Math.min(c1.getX(), c2.getX());
        int maxX = Math.max(c1.getX(), c2.getX());
        int minY = Math.min(c1.getY(), c2.getY());
        int maxY = Math.max(c1.getY(), c2.getY());
        int minZ = Math.min(c1.getZ(), c2.getZ());
        int maxZ = Math.max(c1.getZ(), c2.getZ());

        BlockPos closest = null;
        double closestDist = Double.MAX_VALUE;

        BlockPos.Mutable pos = new BlockPos.Mutable();

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    pos.set(x, y, z);

                    BlockState state = world.getBlockState(pos);
                    if (!isValidCrop(world, pos, state)) continue;

                    double dist = origin.getSquaredDistance(x + 0.5, y, z + 0.5);
                    if (dist < closestDist) {
                        closestDist = dist;
                        closest = pos.toImmutable();
                    }
                }
            }
        }

        return closest;
    }

    public boolean isNearCrop() {
        return golem.squaredDistanceTo(targetCrop.getX() + 0.5, targetCrop.getY(), targetCrop.getZ() + 0.5) <= 2.25;
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

    public abstract boolean harvest(FarmingFellaEntity golem, World world, BlockPos pos, BlockState state);

    public abstract boolean replant(FarmingFellaEntity golem, World world, BlockPos pos, BlockState state);
}
