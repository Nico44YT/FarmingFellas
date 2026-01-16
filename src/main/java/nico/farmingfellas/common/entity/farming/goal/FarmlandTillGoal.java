package nico.farmingfellas.common.entity.farming.goal;

import net.fabricmc.fabric.mixin.content.registry.HoeItemAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import nico.farmingfellas.common.entity.base.FellaGolemEntity;
import nico.farmingfellas.common.entity.base.GolemAnimationState;
import nico.farmingfellas.common.entity.farming.FarmingFellaEntity;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class FarmlandTillGoal extends Goal {

    private FarmingFellaEntity golem;

    public FarmlandTillGoal(FarmingFellaEntity golem) {
        this.golem = golem;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean shouldContinue() {
        return findSuitableBlock() != null;
    }

    @Override
    public void tick() {
        super.tick();

        BlockPos pos = findSuitableBlock();

        if (pos != null) {
            golem.setState(GolemAnimationState.WORKING);
            tryTillingDirt(pos);
        }
    }

    private void tryTillingDirt(BlockPos pos) {
        World world = golem.getWorld();

        if (!world.getBlockState(pos.up()).isAir()) {
            return;
        }

        if (pos.getSquaredDistance(golem.getPos()) > 2) {
            Vec3d $ = pos.toCenterPos().add(0, -0.5, 0);
            golem.getNavigation().startMovingTo($.x, $.y, $.z, 1);
            return;
        }

        golem.setStackInHand(Hand.MAIN_HAND, Items.IRON_HOE.getDefaultStack());

        if (FellaGolemEntity.spawnBlockParticlesAndWait(30, golem, pos, pos.up(), (cooldown) -> {
            golem.lookAt(pos.toCenterPos());
            if (cooldown % 3 == 0) {
                Random random = world.getRandom();
                golem.playSound(SoundEvents.BLOCK_ROOTED_DIRT_PLACE, 0.25f, 1f + (random.nextBetween(-10, 10) / 100f));
            }
        })) return;
        golem.playSound(SoundEvents.ITEM_CROP_PLANT, 1, 1);

        golem.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
        world.setBlockState(
                pos,
                Blocks.FARMLAND.getDefaultState(),
                Block.NOTIFY_ALL
        );

        golem.setState(GolemAnimationState.IDLE);
    }

    @Override
    public boolean canStart() {
        return !golem.hasCookie() && findSuitableBlock() != null;
    }

    private BlockPos findSuitableBlock() {
        BlockPos origin = golem.getBlockPos();
        World world = golem.getWorld();

        // Map of crop position -> squared distance
        Map<BlockPos, Double> farmlandDistances = new HashMap<>();

        golem.getZone().ifPresent(zone -> {
            zone.forEach(blockPos -> {
                int probabilityScore = 0;

                if(!HoeItemAccessor.getTillingActions().containsKey(world.getBlockState(blockPos).getBlock())) return;

                for (Direction checkDirection : Direction.HORIZONTAL) {
                    var checkPos = blockPos.offset(checkDirection, 1);

                    boolean isFarmland = world.getBlockState(checkPos).getBlock() instanceof FarmlandBlock;

                    probabilityScore += (isFarmland ? 1 : 0);
                }

                if (probabilityScore >= 2) {
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
