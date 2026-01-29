package nico.farmingfellas.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.sapling.LargeTreeSaplingGenerator;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import nico.farmingfellas.common.block.ModBlocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

@Mixin(LargeTreeSaplingGenerator.class)
public abstract class LargeTreeSaplingGeneratorMixin {

    @Unique
    private final BlockState[] farming_fellas$belowState = new BlockState[4 * 4];

    @Inject(method = "generateLargeTree", at = @At(value = "HEAD"))
    public void farming_fellas$beforeGenerate(ServerWorld world, ChunkGenerator chunkGenerator, BlockPos pos, BlockState state, Random random, int x, int z, CallbackInfoReturnable<Boolean> cir) {
        System.out.println("HEAD");
        System.out.println(pos);

        int index = 0;
        for(int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                var belowState = world.getBlockState(pos.add(i, -1, j));
                System.out.println(belowState);
                if (belowState.isOf(ModBlocks.SAPLING_HOLDER)) farming_fellas$belowState[index] = belowState;
                index++;
            }
        }

    }

    @Inject(method = "generateLargeTree", at = @At(value = "RETURN"))
    public void farming_fellas$afterGenerate(ServerWorld world, ChunkGenerator chunkGenerator, BlockPos pos, BlockState state, Random random, int x, int z, CallbackInfoReturnable<Boolean> cir) {
        if(!cir.getReturnValue()) return;

        System.out.println("TAIL");

        int index = 0;
        for(int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                if(farming_fellas$belowState[index] != null) {
                    System.out.println(farming_fellas$belowState[index]);
                    world.setBlockState(pos.add(i, -1, j), farming_fellas$belowState[index], Block.NO_REDRAW);
                }
                farming_fellas$belowState[index] = null;
                index++;
            }
        }
    }
}
