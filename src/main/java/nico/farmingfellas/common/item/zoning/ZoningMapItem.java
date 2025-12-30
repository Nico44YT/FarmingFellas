package nico.farmingfellas.common.item.zoning;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import nico.farmingfellas.common.entity.base.FellaGolemEntity;
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


        if(isZonePosSet(TAG_BLOCKPOS_1, stack)) {
            setZonePos(TAG_BLOCKPOS_2, stack, pos);
            player.sendMessage(Text.translatable("item.farming_fellas.zoning_map.set_position", "2", pos.toShortString()), true);
            return ActionResult.SUCCESS;
        }

        setZonePos(TAG_BLOCKPOS_1, stack, pos);
        player.sendMessage(Text.translatable("item.farming_fellas.zoning_map.set_position", "1", pos.toShortString()), true);
        return ActionResult.SUCCESS;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if(world.isClient()) return TypedActionResult.success(stack);
        if(!player.isSneaking()) return TypedActionResult.pass(stack);

        setZonePos(TAG_BLOCKPOS_1, stack, null);
        setZonePos(TAG_BLOCKPOS_2, stack, null);
        player.sendMessage(Text.translatable("item.farming_fellas.zoning_map.clear"), true);

        return super.use(world, player, hand);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand) {
        if(entity instanceof FellaGolemEntity golem && isZoneSet(stack) && !player.getWorld().isClient()) {
            golem.setZone(getZonePos(TAG_BLOCKPOS_1, stack).get(), getZonePos(TAG_BLOCKPOS_2, stack).get());
            player.sendMessage(Text.translatable("item.farming_fellas.zoning_map.assigned_position"));
            return ActionResult.SUCCESS;
        }

        return super.useOnEntity(stack, player, entity, hand);
    }

    //region // * Helper * //
    public static Optional<BlockPos> getZonePos(String key, ItemStack stack) {
        if(stack.hasNbt() && stack.getNbt() != null && stack.getNbt().contains(key)) {
            return Optional.of(BlockPos.fromLong(stack.getNbt().getLong(key)));
        }

        return Optional.empty();
    }

    public static void setZonePos(String key, ItemStack stack, BlockPos pos) {
        if(pos == null) {
            stack.getOrCreateNbt().remove(key);
            return;
        }
        stack.getOrCreateNbt().putLong(key, pos.asLong());
    }

    public static boolean isZonePosSet(String key, ItemStack stack) {
        return stack.hasNbt() && stack.getNbt() != null && stack.getNbt().contains(key);
    }

    public static boolean isZoneSet(ItemStack stack) {
        return isZonePosSet(TAG_BLOCKPOS_1, stack) &&  isZonePosSet(TAG_BLOCKPOS_2, stack);
    }

    public static boolean isAnyPosSet(ItemStack stack) {
        return isZonePosSet(TAG_BLOCKPOS_1, stack) || isZonePosSet(TAG_BLOCKPOS_2, stack);
    }

    public static boolean isSameZone(ItemStack stack, BlockPos pos1, BlockPos pos2) {
        if(!isZoneSet(stack)) return false;

        BlockPos thisPos1 = getZonePos(TAG_BLOCKPOS_1, stack).get();
        BlockPos thisPos2 = getZonePos(TAG_BLOCKPOS_2, stack).get();

        return (thisPos1.equals(pos1) && thisPos2.equals(pos2)) || (thisPos1.equals(pos2) && thisPos2.equals(pos1));
    }
    //endregion


    @Override
    public boolean hasGlint(ItemStack stack) {
        return isAnyPosSet(stack);
    }
}
