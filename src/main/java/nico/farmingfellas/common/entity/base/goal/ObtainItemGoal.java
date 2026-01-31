package nico.farmingfellas.common.entity.base.goal;

import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import nico.farmingfellas.common.entity.base.FellaGolemEntity;
import nico.farmingfellas.common.item.ModItems;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class ObtainItemGoal<T extends FellaGolemEntity> extends Goal {
    private final T golem;
    private final int maxItemCount;
    private final Predicate<ItemStack> itemStackPredicate;

    private BlockPos chestPos;

    public ObtainItemGoal(T golem, int maxItemCount, Predicate<ItemStack> itemStackPredicate) {
        this.golem = golem;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));

        this.maxItemCount = maxItemCount - 1; // Just please don't ask idk why
        this.itemStackPredicate = itemStackPredicate;
    }

    @Override
    public boolean shouldContinue() {
        return chestPos != null && golem.getWorld().getBlockEntity(chestPos) instanceof ChestBlockEntity chestBlockEntity && count(itemStackPredicate, chestBlockEntity.getInvStackList()) > 0;
    }

    @Override
    public void tick() {
        super.tick();

        chestPos = findChestBlock();

        if (chestPos != null) {
            tryObtainFertilizer(chestPos);
        }
    }

    @Override
    public void stop() {
        super.stop();

        chestPos = null;
    }

    private void tryObtainFertilizer(BlockPos pos) {
        World world = golem.getWorld();

        if (pos.getSquaredDistance(golem.getPos()) > 2.25f) {
            Vec3d $ = pos.toCenterPos().add(0, -0.5, 0);
            golem.getNavigation().startMovingTo($.x, $.y, $.z, 1);
            return;
        }

        if (world.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
            int count = count(itemStackPredicate, chest.getInvStackList());
            if (count == 0) stop();
            if (count(itemStackPredicate, golem.getInventory().stacks) >= maxItemCount) stop();

            ItemStack stack = chest.getInvStackList().stream().filter(itemStackPredicate).findAny().get().copy();
            decrement(itemStackPredicate, chest.getInvStackList(), 1);
            stack.setCount(1);
            golem.insertStack(stack);
        }
    }

    @Override
    public boolean canStart() {
        return !golem.hasCookie() && !golem.containsAny(itemStackPredicate) && findChestBlock() != null;
    }

    private BlockPos findChestBlock() {
        BlockPos origin = golem.getBlockPos();
        World world = golem.getWorld();

        Map<BlockPos, Double> chestBlockPos = new HashMap<>();

        golem.getZone().ifPresent(zone -> {
            zone.getChests(world).forEach(blockPos -> {
                ChestBlockEntity blockEntity = (ChestBlockEntity) world.getBlockEntity(blockPos);

                if (count(itemStackPredicate, blockEntity.getInvStackList()) > 0) {
                    chestBlockPos.put(blockPos, origin.getSquaredDistance(blockPos));
                }
            });
        });

        return chestBlockPos.entrySet()
                .stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private static void decrement(Predicate<ItemStack> predicate, DefaultedList<ItemStack> list, int amount) {
        for (ItemStack stack : list) {
            if (amount <= 0) return;
            if (!predicate.test(stack)) continue;

            int removed = Math.min(stack.getCount(), amount);
            stack.decrement(removed);
            amount -= removed;
        }
    }

    private static int count(Predicate<ItemStack> itemStackPredicate, DefaultedList<ItemStack> list) {
        int i = 0;

        for (ItemStack itemStack : list) {
            if (itemStackPredicate.test(itemStack)) {
                i += itemStack.getCount();
            }
        }

        return i;
    }
}
