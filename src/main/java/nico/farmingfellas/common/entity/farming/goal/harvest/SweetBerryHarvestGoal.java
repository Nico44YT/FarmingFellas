package nico.farmingfellas.common.entity.farming.goal.harvest;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import nico.farmingfellas.common.entity.farming.FarmingFellaEntity;
import nico.farmingfellas.common.entity.base.goal.HarvestCropGoal;

public class SweetBerryHarvestGoal extends HarvestCropGoal<FarmingFellaEntity> {
    public SweetBerryHarvestGoal(FarmingFellaEntity golem) {
        super(golem);
    }

    @Override
    public boolean isValidCrop(World world, BlockPos pos, BlockState state) {
        return state.getBlock() instanceof SweetBerryBushBlock && state.get(SweetBerryBushBlock.AGE) == SweetBerryBushBlock.MAX_AGE;
    }

    @Override
    public boolean harvest(FarmingFellaEntity golem, World world, BlockPos pos, BlockState state) {
        golem.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);

        if (FarmingFellaEntity.spawnBlockParticlesAndWait(20, golem, pos, (cooldown) -> {
            golem.lookAt(pos.toCenterPos());

            if (cooldown % 3 == 0) {
                Random random = world.getRandom();
                golem.playSound(state.getSoundGroup().getBreakSound(), 0.25f, 1f + (random.nextBetween(-10, 10) / 100f));
            }
        })) return false;

        int j = 1 + world.random.nextInt(2);
        SweetBerryBushBlock.dropStack(world, pos, new ItemStack(Items.SWEET_BERRIES, j + 1));
        BlockState blockState = state.with(SweetBerryBushBlock.AGE, 1);
        world.setBlockState(pos, blockState, Block.NOTIFY_LISTENERS);
        world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(golem, blockState));

        return true;
    }

    @Override
    public boolean replant(FarmingFellaEntity golem, World world, BlockPos pos, BlockState state) {
        return true;
    }
}
