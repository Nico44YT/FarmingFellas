package nico.farmingfellas.common.entity.farming.goal.harvest;

import net.minecraft.block.AttachedStemBlock;
import net.minecraft.block.BlockState;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import nico.farmingfellas.common.entity.farming.FarmingFellaEntity;
import nico.farmingfellas.common.entity.base.goal.HarvestCropGoal;

public class StemBlockHarvestGoal extends HarvestCropGoal<FarmingFellaEntity> {
    public StemBlockHarvestGoal(FarmingFellaEntity golem) {
        super(golem);
    }

    @Override
    public boolean isValidCrop(World world, BlockPos pos, BlockState state) {
        return state.getBlock() instanceof AttachedStemBlock;
    }

    @Override
    public boolean harvest(FarmingFellaEntity golem, World world, BlockPos pos, BlockState state) {
        golem.setStackInHand(Hand.MAIN_HAND, Items.IRON_AXE.getDefaultStack());

        BlockPos blockPos = pos.offset(state.get(AttachedStemBlock.FACING), 1);
        if(FarmingFellaEntity.spawnBlockParticlesAndWait(60, golem, blockPos, (cooldown) -> {
            golem.lookAt(blockPos.toCenterPos());

            if (cooldown % 3 == 0) {
                Random random = world.getRandom();
                golem.playSound(state.getSoundGroup().getBreakSound(), 0.25f, 1f + (random.nextBetween(-10, 10) / 100f));
            }
        })) return false;

        world.breakBlock(blockPos, true, golem);
        return true;
    }

    @Override
    public boolean replant(FarmingFellaEntity golem, World world, BlockPos pos, BlockState state) {
        return true;
    }
}
