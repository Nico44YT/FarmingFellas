package nico.farmingfellas.common.block.sapling_holder;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import nico.farmingfellas.common.item.ModItems;

public class SaplingHolderBlock extends Block {
    public SaplingHolderBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        return new ItemStack(ModItems.SAPLING_HOLDER_KIT);
    }
}
