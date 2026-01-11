package nico.farmingfellas.common.entity.base.goal;

import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import nico.farmingfellas.common.entity.base.FellaGolemEntity;
import nico.farmingfellas.common.entity.base.GolemAnimationState;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

// TODO Cleanup everything / refactor
public class EmptyInventoryGoal extends Goal {
    private FellaGolemEntity golem;
    private BlockPos targetChest;

    private Inventory chestInventory;
    private int transferSlot = 0;

    public EmptyInventoryGoal(FellaGolemEntity golem) {
        this.golem = golem;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        return golem.isFull() && !golem.hasCookie();
    }

    @Override
    public boolean canStop() {
        return golem.isEmpty();
    }

    @Override
    public boolean shouldContinue() {
        return chestInventory != null || !golem.isEmpty();
    }

    @Override
    public void tick() {
        if (targetChest != null) {
            this.targetChest = null;
            this.chestInventory = null;
            return;
        }

        if (golem.isEmpty()) {
            stop();
            return;
        }

        if (targetChest == null || !(golem.getWorld().getBlockState(targetChest).getBlock() instanceof ChestBlock)) {
            targetChest = findChest();
            chestInventory = null;
        }

        if (!isAtChest()) {
            moveToChest();
            return;
        }

        if (chestInventory == null) {
            startChestTransfer(targetChest);
        }

        transferOneItemPerTick(); // or transferOneStackPerTick()
    }

    private boolean isAtChest() {
        if (targetChest == null) return false;

        return golem.squaredDistanceTo(
                targetChest.getX() + 0.5,
                targetChest.getY() + 0.5,
                targetChest.getZ() + 0.5
        ) < 2.5;
    }

    @Override
    public void stop() {
        super.stop();

        this.golem.getNavigation().stop();
        this.golem.setState(GolemAnimationState.IDLE);
    }

    private void moveToChest() {
        if (targetChest == null) return;

        golem.getNavigation().startMovingTo(
                targetChest.getX() + 0.5,
                targetChest.getY(),
                targetChest.getZ() + 0.5,
                1.0
        );
    }


    private void transferOneItemPerTick() {
        if (chestInventory == null) return;

        Inventory golemInv = golem;

        for (int i = transferSlot; i < golemInv.size(); i++) {
            ItemStack stack = golemInv.getStack(i);
            if (stack.isEmpty()) continue;

            ItemStack single = stack.copy();
            single.setCount(1);

            ItemStack remainder = insertIntoInventory(chestInventory, single);

            if (remainder.isEmpty()) {
                stack.decrement(1);
                if (stack.isEmpty()) {
                    golemInv.setStack(i, ItemStack.EMPTY);
                    transferSlot = i + 1;
                }
            }

            return;
        }

        chestInventory.markDirty();
        chestInventory = null;
    }

    private void startChestTransfer(BlockPos chestPos) {
        World world = golem.getWorld();
        BlockState state = world.getBlockState(chestPos);

        if (!(state.getBlock() instanceof ChestBlock chest)) {
            return;
        }

        this.chestInventory = ChestBlock.getInventory(
                chest,
                state,
                world,
                chestPos,
                true
        );
        this.transferSlot = 0;

        this.golem.setState(GolemAnimationState.INTERACT_CHEST);
    }

    private ItemStack insertIntoInventory(Inventory inv, ItemStack stack) {
        ItemStack remainder = stack.copy();

        // Merge first
        for (int i = 0; i < inv.size(); i++) {
            ItemStack slot = inv.getStack(i);

            if (!slot.isEmpty() && ItemStack.canCombine(slot, remainder)) {
                int max = Math.min(inv.getMaxCountPerStack(), slot.getMaxCount());
                int space = max - slot.getCount();

                if (space > 0) {
                    int move = Math.min(space, remainder.getCount());
                    slot.increment(move);
                    remainder.decrement(move);

                    if (remainder.isEmpty()) return ItemStack.EMPTY;
                }
            }
        }

        // Empty slots
        for (int i = 0; i < inv.size(); i++) {
            if (inv.getStack(i).isEmpty()) {
                ItemStack moved = remainder.copy();
                moved.setCount(Math.min(remainder.getCount(), moved.getMaxCount()));
                inv.setStack(i, moved);
                remainder.decrement(moved.getCount());

                if (remainder.isEmpty()) return ItemStack.EMPTY;
            }
        }

        return remainder;
    }

    private BlockPos findChest() {
        BlockPos origin = golem.getBlockPos();
        World world = golem.getWorld();
        int radius = 16;

        // Map of chest position -> squared distance
        Map<BlockPos, Double> chestDistances = new HashMap<>();

        golem.getZone().ifPresent(zone -> {
            zone.getChests().forEach(chest -> {
                ChestBlockEntity be = (ChestBlockEntity) world.getBlockEntity(chest);
                // Filter out full chests
                if (!isChestFull(be)) {
                    double distSq = origin.getSquaredDistance(
                            chest.getX(), chest.getY(), chest.getZ()
                    );

                    chestDistances.put(chest, distSq);
                }
            });
        });

        // Choose nearest chest
        return chestDistances.entrySet()
                .stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private boolean isChestFull(ChestBlockEntity chest) {
        for (int i = 0; i < chest.size(); i++) {
            if (chest.getStack(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
