package nico.farmingfellas.common.entity.farming.goal;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import nico.farmingfellas.common.block.ModBlocks;
import nico.farmingfellas.common.block.fertilized_farmland.FertilizedFarmlandBlock;
import nico.farmingfellas.common.entity.base.FellaGolemEntity;
import nico.farmingfellas.common.entity.base.GolemAnimationState;
import nico.farmingfellas.common.entity.farming.FarmingFellaEntity;
import nico.farmingfellas.common.item.ModItems;
import nico.farmingfellas.common.item.custom.FertilizerItem;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class FertilizeGoal extends Goal {

    private FarmingFellaEntity golem;

    public FertilizeGoal(FarmingFellaEntity golem) {
        this.golem = golem;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean shouldContinue() {
        return findSuitableBlock() != null && golem.containsAny(stack -> stack.getItem() instanceof FertilizerItem);
    }

    @Override
    public void tick() {
        super.tick();

        BlockPos pos = findSuitableBlock();

        if (pos != null) {
            tryFertilizingFarmland(pos);
        }
    }

    private void tryFertilizingFarmland(BlockPos pos) {
        World world = golem.getWorld();

        if (pos.getSquaredDistance(golem.getPos()) > 2.25f) {
            Vec3d $ = pos.toCenterPos().add(0, -0.5, 0);
            golem.getNavigation().startMovingTo($.x, $.y, $.z, 1);
            return;
        }

        golem.setState(GolemAnimationState.WORKING);
        golem.setStackInHand(Hand.MAIN_HAND, ModItems.FERTILIZER_ITEM.getDefaultStack());

        if (FellaGolemEntity.spawnBlockParticlesAndWait(10, golem, pos, pos.up(), (cooldown) -> {
            golem.lookAt(pos.toCenterPos());
            if (cooldown % 3 == 0) {
                Random random = world.getRandom();
                golem.playSound(SoundEvents.BLOCK_ROOTED_DIRT_PLACE, 0.25f, 1f + (random.nextBetween(-10, 10) / 100f));
            }
        })) return;
        golem.playSound(SoundEvents.BLOCK_ROOTED_DIRT_BREAK, 1, 1);

        golem.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
        var state = world.getBlockState(pos);
        world.setBlockState(
                pos,
                ModBlocks.FERTILIZED_FARMLAND.getDefaultState().with(FarmlandBlock.MOISTURE, state.get(FarmlandBlock.MOISTURE)),
                Block.NOTIFY_ALL
        );
        golem.getInventory().removeItem(ModItems.FERTILIZER_ITEM, 1);

        golem.setState(GolemAnimationState.IDLE);
    }

    @Override
    public boolean canStart() {
        return !golem.hasCookie() && golem.containsAny(stack -> stack.getItem() instanceof FertilizerItem) && findSuitableBlock() != null;
    }

    private BlockPos findSuitableBlock() {
        BlockPos origin = golem.getBlockPos();
        World world = golem.getWorld();

        // Map of crop position -> squared distance
        Map<BlockPos, Double> farmlandDistances = new HashMap<>();

        golem.getZone().ifPresent(zone -> {
            zone.forEach(blockPos -> {
                BlockState blockState = world.getBlockState(blockPos);
                Block block = blockState.getBlock();
                boolean isFarmland = block instanceof FarmlandBlock && !(block instanceof FertilizedFarmlandBlock);

                if (isFarmland) {
                    farmlandDistances.put(blockPos, origin.getSquaredDistance(blockPos));
                }
            });
        });

        // Choose nearest valid crop
        return farmlandDistances.entrySet()
                .stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}
