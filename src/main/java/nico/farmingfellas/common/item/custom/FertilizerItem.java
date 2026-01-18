package nico.farmingfellas.common.item.custom;

import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import nico.farmingfellas.common.block.ModBlocks;
import nico.farmingfellas.common.block.fertilized_farmland.FertilizedFarmlandBlock;
import nico.farmingfellas.common.block.fertilizer.FertilizerHolderBlock;
import nico.farmingfellas.common.block.fertilizer.FertilizerHolderBlockEntity;

public class FertilizerItem extends Item {
    public FertilizerItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getStack();

        if (state.getBlock() instanceof CropBlock) return useOnBlock(
                new ItemUsageContext(context.getPlayer(), context.getHand(), new BlockHitResult(context.getHitPos().subtract(0, -1, 0), context.getSide(), pos.down(), false))
        );

        if (state.getBlock() instanceof FertilizerHolderBlock) {
            FertilizerHolderBlockEntity holder = (FertilizerHolderBlockEntity) world.getBlockEntity(pos);
            int decrements = holder.tryFillHolder(player.isSneaking() ? stack.getCount() : 1);
            stack.decrement(decrements);

            return ActionResult.SUCCESS;
        }

        if (state.getBlock() instanceof FarmlandBlock && !(state.getBlock() instanceof FertilizedFarmlandBlock)) {
            stack.decrement(1);

            world.setBlockState(pos, ModBlocks.FERTILIZED_FARMLAND.getDefaultState().with(FarmlandBlock.MOISTURE, state.get(FarmlandBlock.MOISTURE)), 11);

            return ActionResult.SUCCESS;
        }

        return super.useOnBlock(context);
    }
}
