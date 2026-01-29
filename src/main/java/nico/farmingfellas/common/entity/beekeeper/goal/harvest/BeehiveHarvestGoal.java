package nico.farmingfellas.common.entity.beekeeper.goal.harvest;

import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import nico.farmingfellas.common.entity.base.goal.HarvestCropGoal;
import nico.farmingfellas.common.entity.beekeeper.BeekeeperFellaEntity;
import nico.farmingfellas.common.entity.farming.FarmingFellaEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

public class BeehiveHarvestGoal extends HarvestCropGoal<BeekeeperFellaEntity> {
    public BeehiveHarvestGoal(BeekeeperFellaEntity golem) {
        super(golem);
    }

    @Override
    public boolean isValidCrop(World world, BlockPos pos, BlockState state) {
        return state.getBlock() instanceof BeehiveBlock && state.get(BeehiveBlock.HONEY_LEVEL) == BeehiveBlock.FULL_HONEY_LEVEL;
    }

    @Override
    public boolean harvest(BeekeeperFellaEntity golem, World world, BlockPos pos, BlockState state) {
        golem.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);

        if(FarmingFellaEntity.spawnBlockParticlesAndWait(20, golem, pos, (cooldown) -> {
            golem.lookAt(pos.toCenterPos());

            if (cooldown % 3 == 0) {
                Random random = world.getRandom();
                golem.playSound(SoundEvents.BLOCK_BEEHIVE_WORK, 0.25f, 1f + (random.nextBetween(-10, 10) / 100f));
            }
        })) return false;

        if(!CampfireBlock.isLitCampfireInRange(world, pos)) angerBees(golem, world, pos, state, BeehiveBlockEntity.BeeState.EMERGENCY);
        ((BeehiveBlock)state.getBlock()).takeHoney(world, state, pos);

        return true;
    }

    @Override
    public boolean replant(BeekeeperFellaEntity golem, World world, BlockPos pos, BlockState state) {
        return false;
    }

    public void angerBees(@Nullable LivingEntity target, World world, BlockPos pos, BlockState state, BeehiveBlockEntity.BeeState beeState) {
        BeehiveBlockEntity beehiveBlockEntity = (BeehiveBlockEntity) world.getBlockEntity(pos);
        if(beehiveBlockEntity == null) return;

        List<Entity> list = beehiveBlockEntity.tryReleaseBee(state, beeState);
        if (target != null) {

            for (Entity entity : list) {
                if (entity instanceof BeeEntity beeEntity) {
                    if (target.getPos().squaredDistanceTo(entity.getPos()) <= 16.0) {
                        if (!beehiveBlockEntity.isSmoked()) {
                            beeEntity.setTarget(target);
                        } else {
                            beeEntity.setCannotEnterHiveTicks(400);
                        }
                    }
                }
            }
        }

    }
}
