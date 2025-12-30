package nico.farmingfellas.common.item.zoning;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import nico.farmingfellas.common.entity.base.FellaGolemEntity;
import nico.farmingfellas.common.item.SimpleItemModel;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class ZoningMapItem extends Item implements SimpleItemModel {
    public static final String TAG_BLOCKPOS_1 = "blockpos1";
    public static final String TAG_BLOCKPOS_2 = "blockpos2";
    public static final String TAG_COLOR = "color";

    public ZoningMapItem(Settings settings) {
        super(settings);
    }

    @Override
    public void onCraft(ItemStack stack, World world, PlayerEntity player) {
        super.onCraft(stack, world, player);

    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        ItemStack stack = context.getStack();
        BlockPos pos = context.getBlockPos();
        PlayerEntity player = context.getPlayer();

        if (world.isClient()) return ActionResult.SUCCESS;

        assignRandomColor(world, stack);

        if (isZonePosSet(TAG_BLOCKPOS_1, stack)) {
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
        if (world.isClient()) return TypedActionResult.success(stack);

        assignRandomColor(player.getWorld(), stack);

        if (!player.isSneaking()) return TypedActionResult.pass(stack);

        setZonePos(TAG_BLOCKPOS_1, stack, null);
        setZonePos(TAG_BLOCKPOS_2, stack, null);
        player.sendMessage(Text.translatable("item.farming_fellas.zoning_map.clear"), true);

        return super.use(world, player, hand);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand) {
        if (player.getWorld() instanceof ServerWorld serverWorld) assignRandomColor(serverWorld, stack);

        if (entity instanceof FellaGolemEntity golem && !player.getWorld().isClient()) {
            if (isZoneSet(stack)) {
                golem.setZone(getZonePos(TAG_BLOCKPOS_1, stack).get(), getZonePos(TAG_BLOCKPOS_2, stack).get());
                player.sendMessage(Text.translatable("item.farming_fellas.zoning_map.assigned_position"));
                return ActionResult.SUCCESS;

            }

            Pair<BlockPos, BlockPos> corners = golem.getZone().get();
            setZonePos(TAG_BLOCKPOS_1, stack, corners.getLeft());
            setZonePos(TAG_BLOCKPOS_2, stack, corners.getRight());
            player.sendMessage(Text.translatable("item.farming_fellas.zoning_map.zone_copy"));
            return ActionResult.SUCCESS;
        }

        return super.useOnEntity(stack, player, entity, hand);
    }


    //region // * Helper * //
    public void assignRandomColor(World world, ItemStack stack) {
        if (stack.getOrCreateNbt().contains(TAG_COLOR)) return;

        Random random = world.random;
        float hue = random.nextFloat();                 // 0.0–1.0
        float saturation = MathHelper.nextBetween(random, 0.6f, 0.9f);
        float brightness = MathHelper.nextBetween(random, 0.7f, 1.0f);

        int color = MathHelper.hsvToRgb(hue, saturation, brightness);
        stack.getOrCreateNbt().putInt(TAG_COLOR, color);
    }

    public static int getColor(ItemStack stack) {
        if(!stack.getOrCreateNbt().contains(TAG_COLOR)) return 0xFF_FF_FF;
        return stack.getOrCreateNbt().getInt(TAG_COLOR);
    }

    public static Optional<BlockPos> getZonePos(String key, ItemStack stack) {
        if (stack.hasNbt() && stack.getNbt() != null && stack.getNbt().contains(key)) {
            return Optional.of(BlockPos.fromLong(stack.getNbt().getLong(key)));
        }

        return Optional.empty();
    }

    public static void setZonePos(String key, ItemStack stack, BlockPos pos) {
        if (pos == null) {
            stack.getOrCreateNbt().remove(key);
            return;
        }
        stack.getOrCreateNbt().putLong(key, pos.asLong());
    }

    public static boolean isZonePosSet(String key, ItemStack stack) {
        return stack.hasNbt() && stack.getNbt() != null && stack.getNbt().contains(key);
    }

    public static boolean isZoneSet(ItemStack stack) {
        return isZonePosSet(TAG_BLOCKPOS_1, stack) && isZonePosSet(TAG_BLOCKPOS_2, stack);
    }

    public static boolean isAnyPosSet(ItemStack stack) {
        return isZonePosSet(TAG_BLOCKPOS_1, stack) || isZonePosSet(TAG_BLOCKPOS_2, stack);
    }

    public static boolean isSameZone(ItemStack stack, BlockPos pos1, BlockPos pos2) {
        if (!isZoneSet(stack)) return false;

        BlockPos thisPos1 = getZonePos(TAG_BLOCKPOS_1, stack).get();
        BlockPos thisPos2 = getZonePos(TAG_BLOCKPOS_2, stack).get();

        return (thisPos1.equals(pos1) && thisPos2.equals(pos2)) || (thisPos1.equals(pos2) && thisPos2.equals(pos1));
    }
    //endregion


    @Override
    public Text getName(ItemStack stack) {
        return super.getName(stack).copy().styled(style -> style.withColor(getColor(stack)));
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);

        getZonePos(TAG_BLOCKPOS_1, stack).ifPresent(blockPos -> {
            tooltip.add(Text.translatable("item.farming_fellas.zoning_map.from", blockPos.toShortString()));
        });

        getZonePos(TAG_BLOCKPOS_2, stack).ifPresent(blockPos -> {
            tooltip.add(Text.translatable("item.farming_fellas.zoning_map.to", blockPos.toShortString()));
        });
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return isAnyPosSet(stack);
    }
}
