package nico.farmingfellas.common.entity.lumberjack.goal.harvest;

import net.minecraft.block.*;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import nico.farmingfellas.common.block.ModBlocks;
import nico.farmingfellas.common.entity.base.FellaGolemEntity;
import nico.farmingfellas.common.entity.base.GolemAnimationState;
import nico.farmingfellas.common.entity.lumberjack.LumberjackFellaEntity;

import java.util.*;

public class SaplingPlantGoal extends Goal {

    private LumberjackFellaEntity golem;

    public SaplingPlantGoal(LumberjackFellaEntity golem) {
        this.golem = golem;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean shouldContinue() {
        return findEmptySaplingHolder() != null;
    }

    @Override
    public void tick() {
        super.tick();

        BlockPos pos = findEmptySaplingHolder();

        if (pos != null) {
            tryPlantEmptySaplingHolder(pos);
        }
    }

    private SaplingBlock chooseSaplingForPosition(BlockPos pos) {
        World world = golem.getWorld();
        int radius = 1;

        Map<SaplingBlock, Integer> nearbySaplings = new HashMap<>();

        // 1. Scan nearby saplings
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                BlockPos scanPos = pos.add(x, 0, z);
                BlockState state = world.getBlockState(scanPos);

                if (state.getBlock() instanceof SaplingBlock sapling) {
                    nearbySaplings.merge(sapling, 1, Integer::sum);
                }
            }
        }

        // 2. Choose dominant nearby saplings that we can plant
        SaplingBlock best = null;
        int bestCount = 0;

        for (Map.Entry<SaplingBlock, Integer> entry : nearbySaplings.entrySet()) {
            SaplingBlock saplingBlock = entry.getKey();
            int count = entry.getValue();

            if (count > bestCount && SaplingPlantGoal.findSapling(golem, saplingBlock) != null) {
                best = saplingBlock;
                bestCount = count;
            }
        }

        if (best != null) {
            return best;
        }

        // 3. Fallback: any seed in inventory
        var stack = findAnySaplingFromInventory();
        if(stack == null || stack.isEmpty()) return null;

        return (SaplingBlock) ((BlockItem)stack.getItem()).getBlock();
    }

    private void tryPlantEmptySaplingHolder(BlockPos pos) {
        World world = golem.getWorld();

        if (!(world.getBlockState(pos.down()).isOf(ModBlocks.SAPLING_HOLDER))) {
            return;
        }

        if (!world.getBlockState(pos).isAir()) {
            return;
        }

        SaplingBlock saplingToPlant = chooseSaplingForPosition(pos);
        if (saplingToPlant == null) return;

        ItemStack saplingStack = SaplingPlantGoal.findSapling(golem, saplingToPlant);
        if (saplingStack == null) return;

        if (pos.getSquaredDistance(golem.getPos()) > 2.25f) {
            Vec3d $ = pos.toCenterPos().add(0, -0.5, 0);
            golem.getNavigation().startMovingTo($.x, $.y, $.z, 1);
            return;
        }

        golem.setState(GolemAnimationState.WORKING);
        golem.setStackInHand(Hand.MAIN_HAND, saplingStack);

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
                saplingToPlant.getDefaultState().with(SaplingBlock.STAGE, 0),
                Block.NOTIFY_ALL
        );

        saplingStack.decrement(1);
        golem.setState(GolemAnimationState.IDLE);
    }

    @Override
    public boolean canStart() {
        return !golem.hasCookie() && findEmptySaplingHolder() != null;
    }

    private BlockPos findEmptySaplingHolder() {
        BlockPos origin = golem.getBlockPos();
        World world = golem.getWorld();

        // Map of crop position -> squared distance
        Map<BlockPos, Double> saplingHolderPositions = new HashMap<>();

        golem.getZone().ifPresent(zone -> {
            zone.forEach(blockPos -> {
                BlockState state = world.getBlockState(blockPos);

                if (state.getBlock() instanceof AirBlock && world.getBlockState(blockPos.down()).isOf(ModBlocks.SAPLING_HOLDER)) {
                    double distSq = origin.getSquaredDistance(
                            blockPos.getX(), blockPos.getY(), blockPos.getZ()
                    );

                    saplingHolderPositions.put(blockPos, distSq);
                }
            });
        });

        // Choose nearest valid crop
        return saplingHolderPositions.entrySet()
                .stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private ItemStack findAnySaplingFromInventory() {
        var saplings = findSapling(golem.getInventory());
        return !saplings.isEmpty() ? saplings.get(0) : null;
    }

    private static ItemStack findSapling(Inventory inv, SaplingBlock saplingBlock) {
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.isEmpty()) continue;

            if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof SaplingBlock block && block.equals(saplingBlock)) {
                return stack;
            }
        }

        return null;
    }

    private static List<ItemStack> findSapling(Inventory inv) {
        List<ItemStack> saplingStacks = new ArrayList<>();

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.isEmpty()) continue;

            if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof SaplingBlock) {
                saplingStacks.add(stack);
            }
        }

        return saplingStacks;
    }
}
