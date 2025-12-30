package nico.farmingfellas.common.entity.farming.goal.harvest;

import net.minecraft.block.BlockState;
import net.minecraft.block.CaveVines;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import nico.farmingfellas.common.entity.farming.FarmingFellaEntity;
import nico.farmingfellas.common.entity.farming.goal.HarvestCropGoal;

public class GlowBerryHarvestGoal extends HarvestCropGoal {
    public GlowBerryHarvestGoal(FarmingFellaEntity golem) {
        super(golem);
    }

    @Override
    public boolean isValidCrop(World world, BlockPos pos, BlockState state) {
        return state.getBlock() instanceof CaveVines && state.get(CaveVines.BERRIES);
    }

    @Override
    public boolean harvest(FarmingFellaEntity golem, World world, BlockPos pos, BlockState state) {
        golem.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);

        if(FarmingFellaEntity.spawnBlockParticlesAndWait(20, golem, pos, (cooldown) -> {
            golem.lookAt(pos.toCenterPos());

            if (cooldown % 3 == 0) {
                Random random = world.getRandom();
                golem.playSound(state.getSoundGroup().getBreakSound(), 0.25f, 1f + (random.nextBetween(-10, 10) / 100f));
            }
        })) return false;

        CaveVines.pickBerries(
                golem,
                state,
                world,
                pos
        );
        return true;
    }

    @Override
    public boolean replant(FarmingFellaEntity golem, World world, BlockPos pos, BlockState state) {
        return true;
    }
}
