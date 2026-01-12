package nico.farmingfellas.common.entity.farming.goal.harvest;

import net.minecraft.block.*;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import nico.farmingfellas.common.entity.farming.FarmingFellaEntity;
import nico.farmingfellas.common.entity.base.goal.HarvestCropGoal;

public class NetherWartHarvestGoal extends HarvestCropGoal<FarmingFellaEntity> {
    public NetherWartHarvestGoal(FarmingFellaEntity golem) {
        super(golem);
    }

    @Override
    public boolean isValidCrop(World world, BlockPos pos, BlockState state) {
        return state.getBlock() instanceof NetherWartBlock && state.get(NetherWartBlock.AGE) == NetherWartBlock.field_31199; //field_31199 = MAX_AGE
    }

    @Override
    public boolean harvest(FarmingFellaEntity golem, World world, BlockPos pos, BlockState state) {
        golem.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);

        if(FarmingFellaEntity.spawnBlockParticlesAndWait(30, golem, pos, (cooldown) -> {
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
        BlockPos soulSandPos = pos.down();
        BlockState floorState = world.getBlockState(soulSandPos);

        if (!floorState.isOf(Blocks.SOUL_SAND)) {
            return true;
        }

        ItemStack seedStack = findSeed(golem);

        if (seedStack == null) return true;

        world.setBlockState(
                pos,
                Blocks.NETHER_WART.getDefaultState().with(NetherWartBlock.AGE, 0),
                Block.NOTIFY_ALL
        );

        seedStack.decrement(1);
        return true;
    }

    public static ItemStack findSeed(Inventory inv) {
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.isEmpty()) continue;

            if (Items.NETHER_WART == stack.getItem()) {
                return stack;
            }
        }
        return null;
    }
}
