package nico.farmingfellas.common.item.zoning;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import nico.farmingfellas.common.item.SimpleItemModel;

import java.util.Optional;

public class ZoningMapItem extends Item implements SimpleItemModel {
    public static final String TAG_BLOCKPOS_1 = "blockpos1";
    public static final String TAG_BLOCKPOS_2 = "blockpos2";

    public ZoningMapItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        ItemStack stack = context.getStack();
        BlockPos pos = context.getBlockPos();
        PlayerEntity player = context.getPlayer();

        if(world.isClient()) return ActionResult.SUCCESS;

        if(player.isSneaking()) {
            setBlockPos(TAG_BLOCKPOS_2, stack, pos);
            player.sendMessage(Text.translatable("item.farming_fellas.zoning_map.set_position", "2", pos.toShortString()), true);
            return ActionResult.SUCCESS;
        }

        setBlockPos(TAG_BLOCKPOS_1, stack, pos);
        player.sendMessage(Text.translatable("item.farming_fellas.zoning_map.set_position", "1", pos.toShortString()), true);
        return ActionResult.SUCCESS;
    }

    //region // * Helper * //
    public Optional<BlockPos> getBlockPos(String key, ItemStack stack) {
        if(stack.hasNbt() && stack.getNbt() != null && stack.getNbt().contains(key)) {
            return Optional.of(BlockPos.fromLong(stack.getNbt().getLong(key)));
        }

        return Optional.empty();
    }

    public void setBlockPos(String key, ItemStack stack, BlockPos pos) {
        stack.getOrCreateNbt().putLong(key, pos.asLong());
    }

    public boolean isBlockPosSet(String key, ItemStack stack) {
        return stack.hasNbt() && stack.getNbt() != null && stack.getNbt().contains(key);
    }
    //endregion


    @Override
    public boolean hasGlint(ItemStack stack) {
        return isBlockPosSet(TAG_BLOCKPOS_1, stack) || isBlockPosSet(TAG_BLOCKPOS_2, stack);
    }
}
