package nico.farmingfellas.common.block.fertilizer;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import nico.farmingfellas.common.block.ModBlocks;

public class FertilizerHolderBlockEntity extends BlockEntity {

    public static final int MAX_FERTILIZERS = 64;
    public int currentFertilizers = 0;

    public FertilizerHolderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.FERTILIZED_HOLDER_TYPE, pos, state);
    }

    public int tryFillHolder(int amount) {
        if (currentFertilizers >= MAX_FERTILIZERS) return 0;

        currentFertilizers += amount;
        int remained = currentFertilizers % MAX_FERTILIZERS;

        return amount - remained;
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        nbt.putInt("fertilizers", currentFertilizers);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        this.currentFertilizers = nbt.getInt("fertilizers");
    }
}
