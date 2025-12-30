package nico.farmingfellas.screen.custom;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import nico.farmingfellas.screen.ModHandledScreens;

public class Generic3x2ContainerScreenHandler extends ScreenHandler {
	private static final int CONTAINER_SIZE = 6;

	private static final int CONTAINER_START  = 0;
	private static final int CONTAINER_END = CONTAINER_SIZE;

	private static final int PLAYER_INV_START = 6;
	private static final int PLAYER_INV_END = 33;

	private static final int HOTBAR_START = 33;
	private static final int HOTBAR_END = 42;

	private final Inventory inventory;

	public Generic3x2ContainerScreenHandler(int syncId, PlayerInventory playerInventory) {
		this(syncId, playerInventory, new SimpleInventory(CONTAINER_SIZE));
	}

	public Generic3x2ContainerScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
		super(ModHandledScreens.GENERIC_3X2, syncId);
		checkSize(inventory, CONTAINER_SIZE);
		this.inventory = inventory;
		inventory.onOpen(playerInventory.player);

		int y;
		int x;
		for(y = 0; y < 2; ++y) {
			for(x = 0; x < 3; ++x) {
				this.addSlot(new Slot(inventory, x + y * 3, 62 + x * 18, 17 + y * 18));
			}
		}

		for(y = 0; y < 3; ++y) {
			for(x = 0; x < 9; ++x) {
				this.addSlot(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 84 + y * 18));

			}
		}

		for (x = 0; x < 9; ++x) {
			this.addSlot(new Slot(playerInventory, x, 8 + x * 18, 142));
		}

	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return this.inventory.canPlayerUse(player);
	}

	@Override
	public ItemStack quickMove(PlayerEntity player, int slotIndex) {
		ItemStack original = ItemStack.EMPTY;
		Slot slot = this.slots.get(slotIndex);

		if (!slot.hasStack()) {
			return ItemStack.EMPTY;
		}

		ItemStack stack = slot.getStack();
		original = stack.copy();

		// From container → player inventory + hotbar
		if (slotIndex < CONTAINER_END) {
			if (!this.insertItem(stack, PLAYER_INV_START, HOTBAR_END, true)) {
				return ItemStack.EMPTY;
			}
		}
		// From player inventory
		else if (slotIndex < HOTBAR_START) {
			if (!this.insertItem(stack, HOTBAR_START, HOTBAR_END, false)) {
				return ItemStack.EMPTY;
			}
		}
		// From hotbar
		else {
			if (!this.insertItem(stack, PLAYER_INV_START, HOTBAR_START, false)) {
				return ItemStack.EMPTY;
			}
		}

		if (stack.isEmpty()) {
			slot.setStack(ItemStack.EMPTY);
		} else {
			slot.markDirty();
		}

		slot.onTakeItem(player, stack);
		return original;
	}

	public void onClosed(PlayerEntity player) {
		super.onClosed(player);
		this.inventory.onClose(player);
	}
}
