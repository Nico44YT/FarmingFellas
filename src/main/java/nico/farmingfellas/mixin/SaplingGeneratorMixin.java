package nico.farmingfellas.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.sapling.SaplingGenerator;
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

@Mixin(SaplingGenerator.class)
public abstract class SaplingGeneratorMixin {

    @Unique
    private BlockState farming_fellas$belowState;

    @Inject(method = "generate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/feature/ConfiguredFeature;generate(Lnet/minecraft/world/StructureWorldAccess;Lnet/minecraft/world/gen/chunk/ChunkGenerator;Lnet/minecraft/util/math/random/Random;Lnet/minecraft/util/math/BlockPos;)Z", shift = At.Shift.BEFORE))
    public void farming_fellas$beforeGenerate(ServerWorld world, ChunkGenerator chunkGenerator, BlockPos pos, BlockState state, Random random, CallbackInfoReturnable<Boolean> cir) {
        final BlockState belowState = world.getBlockState(pos.down());
        if (belowState.isOf(ModBlocks.SAPLING_HOLDER)) farming_fellas$belowState = belowState;
    }

    @Inject(method = "generate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/feature/ConfiguredFeature;generate(Lnet/minecraft/world/StructureWorldAccess;Lnet/minecraft/world/gen/chunk/ChunkGenerator;Lnet/minecraft/util/math/random/Random;Lnet/minecraft/util/math/BlockPos;)Z", shift = At.Shift.AFTER))
    public void farming_fellas$afterGenerate(ServerWorld world, ChunkGenerator chunkGenerator, BlockPos pos, BlockState state, Random random, CallbackInfoReturnable<Boolean> cir) {
        if (farming_fellas$belowState != null) world.setBlockState(pos.down(), farming_fellas$belowState, Block.NO_REDRAW);
        farming_fellas$belowState = null;
    }
}
