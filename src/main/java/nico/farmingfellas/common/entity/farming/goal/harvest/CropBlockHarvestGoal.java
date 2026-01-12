package nico.farmingfellas.common.entity.farming.goal.harvest;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import nico.farmingfellas.common.entity.farming.FarmingFellaEntity;
import nico.farmingfellas.common.entity.base.goal.HarvestCropGoal;

public class CropBlockHarvestGoal extends HarvestCropGoal<FarmingFellaEntity> {
    public CropBlockHarvestGoal(FarmingFellaEntity golem) {
        super(golem);
    }

    @Override
    public boolean isValidCrop(World world, BlockPos pos, BlockState state) {
        return state.getBlock() instanceof CropBlock crop && crop.isMature(state);
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

        world.breakBlock(pos, true, golem);
        return true;
    }

    @Override
    public boolean replant(FarmingFellaEntity golem, World world, BlockPos pos, BlockState state) {
        CropBlock crop = (CropBlock)state.getBlock();
        BlockPos farmlandPos = pos.down();

        if (!(world.getBlockState(farmlandPos).getBlock() instanceof FarmlandBlock)) {
            return true;
        }

        ItemStack seedStack = findSeed(golem, crop);

        if (seedStack == null) return true;

        world.setBlockState(
                pos,
                crop.withAge(0),
                Block.NOTIFY_ALL
        );

        seedStack.decrement(1);
        return true;
    }

    public static ItemStack findSeed(Inventory inv, CropBlock crop) {
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.isEmpty()) continue;

            if (crop.getSeedsItem() == stack.getItem()) {
                return stack;
            }
        }
        return null;
    }
}
