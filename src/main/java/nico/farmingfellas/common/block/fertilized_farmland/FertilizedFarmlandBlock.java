package nico.farmingfellas.common.block.fertilized_farmland;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public class FertilizedFarmlandBlock extends FarmlandBlock {
    public FertilizedFarmlandBlock(Settings settings) {
        super(settings);
    }

    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.randomTick(state, world, pos, random);

        var aboveState = world.getBlockState(pos.up());
        if (hasCrop(world, pos) && aboveState.getBlock() instanceof CropBlock crop) crop.randomTick(aboveState, world, pos, random);

        if (random.nextBetween(1, 20) == 1) {
            world.setBlockState(pos, Blocks.FARMLAND.getDefaultState().with(FarmlandBlock.MOISTURE, state.get(FarmlandBlock.MOISTURE)));
        }
    }
}
