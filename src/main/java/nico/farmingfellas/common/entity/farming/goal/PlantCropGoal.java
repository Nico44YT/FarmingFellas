package nico.farmingfellas.common.entity.farming.goal;

import net.minecraft.block.*;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import nico.farmingfellas.common.entity.base.FellaGolemEntity;
import nico.farmingfellas.common.entity.farming.FarmingFellaEntity;
import nico.farmingfellas.common.entity.farming.goal.harvest.CropBlockHarvestGoal;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class PlantCropGoal extends Goal {

    private FarmingFellaEntity golem;

    public PlantCropGoal(FarmingFellaEntity golem) {
        this.golem = golem;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }


    @Override
    public boolean shouldContinue() {
        return findEmptyFarmland() != null;
    }

    @Override
    public void tick() {
        super.tick();

        BlockPos pos = findEmptyFarmland();

        if (pos != null) {
            tryPlantEmptyFarmland(pos);

        }
    }

    private CropBlock chooseCropForPosition(BlockPos pos) {
        World world = golem.getWorld();
        int radius = 1;

        Map<CropBlock, Integer> nearbyCrops = new HashMap<>();

        // 1. Scan nearby crops
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                BlockPos scanPos = pos.add(x, 0, z);
                BlockState state = world.getBlockState(scanPos);

                if (state.getBlock() instanceof CropBlock crop) {
                    nearbyCrops.merge(crop, 1, Integer::sum);
                }
            }
        }

        // 2. Choose dominant nearby crop that we can plant
        CropBlock best = null;
        int bestCount = 0;

        for (Map.Entry<CropBlock, Integer> entry : nearbyCrops.entrySet()) {
            CropBlock crop = entry.getKey();
            int count = entry.getValue();

            if (count > bestCount && CropBlockHarvestGoal.findSeed(golem, crop) != null) {
                best = crop;
                bestCount = count;
            }
        }

        if (best != null) {
            return best;
        }

        // 3. Fallback: any seed in inventory
        return findAnyPlantableCropFromInventory();
    }

    private void tryPlantEmptyFarmland(BlockPos pos) {
        World world = golem.getWorld();

        if (!(world.getBlockState(pos.down()).getBlock() instanceof FarmlandBlock)) {
            return;
        }

        if (!world.getBlockState(pos).isAir()) {
            return;
        }

        CropBlock cropToPlant = chooseCropForPosition(pos);
        if (cropToPlant == null) return;

        ItemStack seeds = CropBlockHarvestGoal.findSeed(golem, cropToPlant);
        if (seeds == null) return;

        if (pos.getSquaredDistance(golem.getPos()) > 2) {
            Vec3d $ = pos.toCenterPos().add(0, -0.5, 0);
            golem.getNavigation().startMovingTo($.x, $.y, $.z, 1);
            return;
        }

        golem.setStackInHand(Hand.MAIN_HAND, seeds);

        if (FellaGolemEntity.spawnBlockParticlesAndWait(20, golem, pos.down(), pos, (cooldown) -> {
            golem.lookAt(pos.down().toCenterPos());
            if (cooldown % 3 == 0) {
                Random random = world.getRandom();
                golem.playSound(SoundEvents.BLOCK_ROOTED_DIRT_STEP, 0.25f, 1f + (random.nextBetween(-10, 10) / 100f));
            }
        })) return;
        golem.playSound(SoundEvents.ITEM_CROP_PLANT, 1, 1);

        golem.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
        world.setBlockState(
                pos,
                cropToPlant.withAge(0),
                Block.NOTIFY_ALL
        );

        seeds.decrement(1);
    }

    private CropBlock findAnyPlantableCropFromInventory() {
        for (int i = 0; i < golem.size(); i++) {
            ItemStack stack = golem.getStack(i);
            if (stack.isEmpty()) continue;

            Item item = stack.getItem();

            if (item instanceof AliasedBlockItem aliased
                    && aliased.getBlock() instanceof CropBlock crop) {
                return crop;
            }
        }
        return null;
    }

    @Override
    public boolean canStart() {
        return !golem.hasCookie();
    }

    private BlockPos findEmptyFarmland() {
        BlockPos origin = golem.getBlockPos();
        World world = golem.getWorld();
        int radius = 6;

        // Map of crop position -> squared distance
        Map<BlockPos, Double> farmlandDistances = new HashMap<>();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = origin.add(x, y, z);
                    BlockState state = world.getBlockState(pos);

                    // Sweet berry bushes
                    if (state.getBlock() instanceof AirBlock && world.getBlockState(pos.down()).getBlock() instanceof FarmlandBlock) {
                        double distSq = origin.getSquaredDistance(
                                pos.getX(), pos.getY(), pos.getZ()
                        );

                        farmlandDistances.put(pos, distSq);
                    }
                }
            }
        }

        // Choose nearest valid crop
        return farmlandDistances.entrySet()
                .stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}
