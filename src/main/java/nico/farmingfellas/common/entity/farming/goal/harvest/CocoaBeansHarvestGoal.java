package nico.farmingfellas.common.entity.farming.goal.harvest;

import net.minecraft.block.*;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import nico.farmingfellas.common.entity.farming.FarmingFellaEntity;
import nico.farmingfellas.common.entity.farming.goal.HarvestCropGoal;

public class CocoaBeansHarvestGoal extends HarvestCropGoal {
    public CocoaBeansHarvestGoal(FarmingFellaEntity golem) {
        super(golem);
    }

    @Override
    public boolean isValidCrop(World world, BlockPos pos, BlockState state) {
        return state.getBlock() instanceof CocoaBlock && state.get(CocoaBlock.AGE) == CocoaBlock.MAX_AGE;
    }

    @Override
    public boolean harvest(FarmingFellaEntity golem, World world, BlockPos pos, BlockState state) {
        golem.setStackInHand(Hand.MAIN_HAND, Items.IRON_AXE.getDefaultStack());

        if(FarmingFellaEntity.spawnBlockParticlesAndWait(40, golem, pos, (cooldown) -> {
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
        Direction logDir = null;
        for (Direction value : Direction.values()) {
            if(value == Direction.DOWN || value == Direction.UP) continue;
            BlockPos possibleLogPos = pos.offset(value);
            BlockState possibleLogState = world.getBlockState(possibleLogPos);

            if(possibleLogState.isIn(BlockTags.JUNGLE_LOGS)) {
                logDir = value;
                break;
            }
        }

        ItemStack seedStack = findSeed(golem);

        if (seedStack == null) return true;

        world.setBlockState(
                pos,
                Blocks.COCOA.getDefaultState().with(CocoaBlock.AGE, 0).with(HorizontalFacingBlock.FACING, logDir),
                Block.NOTIFY_ALL
        );

        seedStack.decrement(1);
        return true;
    }

    public static ItemStack findSeed(Inventory inv) {
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.isEmpty()) continue;

            if (Items.COCOA_BEANS == stack.getItem()) {
                return stack;
            }
        }
        return null;
    }
}
