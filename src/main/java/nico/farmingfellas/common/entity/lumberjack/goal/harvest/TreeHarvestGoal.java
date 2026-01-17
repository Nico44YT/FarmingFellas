package nico.farmingfellas.common.entity.lumberjack.goal.harvest;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SaplingBlock;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import nico.farmingfellas.common.entity.base.goal.HarvestCropGoal;
import nico.farmingfellas.common.entity.lumberjack.LumberjackFellaEntity;

import java.util.ArrayList;
import java.util.List;

public class TreeHarvestGoal extends HarvestCropGoal<LumberjackFellaEntity> {

    private List<BlockPos> cachedLogs;
    private BlockPos cachedRoot;

    public TreeHarvestGoal(LumberjackFellaEntity golem) {
        super(golem);
    }

    @Override
    public boolean isValidCrop(World world, BlockPos pos, BlockState state) {
        if (!state.isIn(BlockTags.LOGS)) return false;

        BlockState below = world.getBlockState(pos.down());
        return below.isOf(Blocks.DIRT)
                || below.isOf(Blocks.GRASS_BLOCK)
                || below.isOf(Blocks.COARSE_DIRT)
                || below.isOf(Blocks.PODZOL);
    }

    @Override
    public boolean harvest(LumberjackFellaEntity golem, World world, BlockPos pos, BlockState state) {

        if (cachedLogs == null || !pos.equals(cachedRoot)) {
            cachedRoot = pos;
            cachedLogs = findTreeLogs(world, pos);

            if (cachedLogs.isEmpty()) {
                cachedLogs = null;
                return true;
            }
        }

        golem.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);

        int workTime = 10 + cachedLogs.size() * 4;

        if (LumberjackFellaEntity.spawnBlockParticlesAndWait(
                workTime, golem, pos,
                (cooldown) -> {
                    BlockPos target = cachedLogs.get(cooldown % cachedLogs.size());
                    golem.lookAt(target.toCenterPos());
                }
        )) return false;

        for (BlockPos logPos : cachedLogs) {
            if (world.getBlockState(logPos).isIn(BlockTags.LOGS)) {
                world.breakBlock(logPos, true, golem);
            }
        }

        cachedLogs = null;
        cachedRoot = null;
        return true;
    }

    private static final int MAX_RADIUS = 4;
    private static final int MAX_HEIGHT = 12;

    private static List<BlockPos> findTreeLogs(World world, BlockPos origin) {
        List<BlockPos> logs = new ArrayList<>();

        int ox = origin.getX();
        int oy = origin.getY();
        int oz = origin.getZ();

        for (int y = 0; y <= MAX_HEIGHT; y++) {
            for (int x = -MAX_RADIUS; x <= MAX_RADIUS; x++) {
                for (int z = -MAX_RADIUS; z <= MAX_RADIUS; z++) {

                    BlockPos pos = new BlockPos(ox + x, oy + y, oz + z);
                    if (world.getBlockState(pos).isIn(BlockTags.LOGS)) {
                        logs.add(pos);
                    }
                }
            }
        }

        return logs;
    }

    @Override
    public boolean replant(LumberjackFellaEntity golem, World world, BlockPos pos, BlockState state) {
        BlockState ground = world.getBlockState(pos.down());

        if (!(ground.isOf(Blocks.DIRT)
                || ground.isOf(Blocks.GRASS_BLOCK)
                || ground.isOf(Blocks.COARSE_DIRT)
                || ground.isOf(Blocks.PODZOL))) {
            return true;
        }

        ItemStack saplingStack = findSapling(golem);
        if (saplingStack == null) return true;

        Block sapling = ((BlockItem) saplingStack.getItem()).getBlock();

        world.setBlockState(pos, sapling.getDefaultState(), Block.NOTIFY_ALL);
        saplingStack.decrement(1);
        return true;
    }

    private static ItemStack findSapling(Inventory inv) {
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.isEmpty()) continue;

            if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof SaplingBlock) {
                return stack;
            }
        }
        return null;
    }
}
