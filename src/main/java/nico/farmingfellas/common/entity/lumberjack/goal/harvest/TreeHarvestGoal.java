package nico.farmingfellas.common.entity.lumberjack.goal.harvest;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SaplingBlock;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import nico.farmingfellas.common.block.ModBlocks;
import nico.farmingfellas.common.entity.base.GolemAnimationState;
import nico.farmingfellas.common.entity.base.goal.HarvestCropGoal;
import nico.farmingfellas.common.entity.lumberjack.LumberjackFellaEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreeHarvestGoal extends HarvestCropGoal<LumberjackFellaEntity> {

    private int workAmount;
    private Map<TagKey<Block>, List<BlockPos>> cachedBlocks;
    private BlockPos cachedRoot;

    public TreeHarvestGoal(LumberjackFellaEntity golem) {
        super(golem);
    }

    @Override
    public void stop() {
        super.stop();

        golem.setState(GolemAnimationState.IDLE);
    }

    @Override
    public boolean isValidCrop(World world, BlockPos pos, BlockState state) {
        if (!state.isIn(BlockTags.LOGS)) return false;

        BlockState below = world.getBlockState(pos.down());
        return below.isIn(BlockTags.DIRT) || below.isOf(ModBlocks.SAPLING_HOLDER);
    }

    @Override
    public boolean harvest(LumberjackFellaEntity golem, World world, BlockPos pos, BlockState state) {
        if (cachedBlocks == null || !pos.equals(cachedRoot)) {
            cachedRoot = pos;
            cachedBlocks = findTreeLogs(world, pos);

            if (cachedBlocks.isEmpty()) {
                cachedBlocks = null;
                golem.setState(GolemAnimationState.IDLE);
                return true;
            }
        }

        for(Map.Entry<TagKey<Block>, List<BlockPos>> entry : new ArrayList<>(cachedBlocks.entrySet())) {
            var filteredList = entry.getValue().stream().filter(
                    $ -> world.getBlockState($).isIn(BlockTags.LOGS) || world.getBlockState($).isIn(BlockTags.LEAVES)
            ).toList();

            if(filteredList.isEmpty()) cachedBlocks.remove(entry.getKey());
            else cachedBlocks.put(entry.getKey(), filteredList);
        }

        if(cachedBlocks.isEmpty()) {
            cachedBlocks = null;
            cachedRoot = null;
            golem.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
            return false;
        }

        golem.setStackInHand(Hand.MAIN_HAND, Items.IRON_AXE.getDefaultStack());

        int workTime = Math.min(10 + workAmount, 20 * 16 * 5);

        if (LumberjackFellaEntity.spawnBlockParticlesAndWait(
                workTime, golem, pos,
                (cooldown) -> {
                    golem.lookAt(cachedRoot.toCenterPos());

                    if (cooldown % 3 == 0) {
                        Random random = world.getRandom();
                        golem.playSound(state.getSoundGroup().getBreakSound(), 0.25f, 1f + (random.nextBetween(-10, 10) / 100f));
                    }
                }
        )) return false;

        cachedBlocks.forEach((tag, blockPosList) -> blockPosList.forEach(blockPos -> {
            BlockState $ = world.getBlockState(blockPos);
            if(world.getBlockState(blockPos).isIn(tag)) {
                Block.getDroppedStacks($, (ServerWorld) world, pos, null, golem, Items.IRON_AXE.getDefaultStack()).forEach(stack -> Block.dropStack(world, pos, golem.getInventory().addStack(stack)));
                world.breakBlock(blockPos, false, golem);
            }
        }));

        cachedBlocks = null;
        cachedRoot = null;
        golem.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
        return true;
    }

    private static final int MAX_RADIUS = 16;
    private static final int MAX_HEIGHT = 32;

    private Map<TagKey<Block>, List<BlockPos>> findTreeLogs(World world, BlockPos origin) {
        Map<TagKey<Block>, List<BlockPos>> map = new HashMap<>();
        workAmount = 0;

        findBlocks(BlockTags.LOGS, 5, world, origin, map);
        findBlocks(BlockTags.LEAVES, 0, world, origin, map);

        return map;
    }

    private void findBlocks(TagKey<Block> tag, int workAmountAddition, World world, BlockPos origin, Map<TagKey<Block>, List<BlockPos>> map) {
        List<BlockPos> blocks = new ArrayList<>();

        int ox = origin.getX();
        int oy = origin.getY();
        int oz = origin.getZ();

        for (int y = 0; y <= MAX_HEIGHT; y++) {
            for (int x = -MAX_RADIUS; x <= MAX_RADIUS; x++) {
                for (int z = -MAX_RADIUS; z <= MAX_RADIUS; z++) {

                    BlockPos pos = new BlockPos(ox + x, oy + y, oz + z);
                    if (world.getBlockState(pos).isIn(tag)) {
                        workAmount += workAmountAddition;
                        blocks.add(pos);
                    }
                }
            }
        }

        map.put(tag, blocks);
    }

    @Override
    public boolean replant(LumberjackFellaEntity golem, World world, BlockPos pos, BlockState state) {
        return false;
    }
}
