package nico.farmingfellas.common.item.custom;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.ActionResult;
import nico.farmingfellas.common.block.ModBlocks;
import nico.farmingfellas.common.item.SimpleItemModel;

public class SaplingHolderKitItem extends Item implements SimpleItemModel {
    public SaplingHolderKitItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var world = context.getWorld();
        var pos = context.getBlockPos();
        var state = world.getBlockState(pos);
        var stack = context.getStack();

        if(state.isIn(BlockTags.DIRT)) {
            stack.decrement(1);
            world.setBlockState(pos, ModBlocks.SAPLING_HOLDER.getDefaultState(), Block.NOTIFY_NEIGHBORS);
            return ActionResult.SUCCESS;
        }

        return super.useOnBlock(context);
    }
}
