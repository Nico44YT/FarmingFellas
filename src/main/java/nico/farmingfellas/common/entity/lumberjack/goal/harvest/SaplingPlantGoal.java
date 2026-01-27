package nico.farmingfellas.common.entity.lumberjack.goal.harvest;

import net.minecraft.block.SaplingBlock;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

public class SaplingPlantGoal extends Goal {
    @Override
    public boolean canStart() {
        return false;
    }

    private static ItemStack findSapling(Inventory inv) {
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.isEmpty()) continue;

            if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof SaplingBlock) {
                return stack;
            }
        }
        return null;
    }
}
