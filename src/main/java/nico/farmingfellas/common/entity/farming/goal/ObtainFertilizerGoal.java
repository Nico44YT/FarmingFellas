package nico.farmingfellas.common.entity.farming.goal;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import nico.farmingfellas.common.block.fertilizer_holder.FertilizerHolderBlockEntity;
import nico.farmingfellas.common.entity.farming.FarmingFellaEntity;
import nico.farmingfellas.common.item.ModItems;
import nico.farmingfellas.common.item.custom.FertilizerItem;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class ObtainFertilizerGoal extends Goal {
    private FarmingFellaEntity golem;
    private final int maxFertilizerCount;

    private BlockPos holderPos;

    public ObtainFertilizerGoal(FarmingFellaEntity golem, int maxFertilizerCount) {
        this.golem = golem;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));

        this.maxFertilizerCount = maxFertilizerCount;
    }

    @Override
    public boolean shouldContinue() {
        return holderPos != null && golem.getWorld().getBlockEntity(holderPos) instanceof FertilizerHolderBlockEntity holderBlockEntity && holderBlockEntity.currentFertilizers > 0;
    }

    @Override
    public void tick() {
        super.tick();

        holderPos = findHolderBlock();

        if (holderPos != null) {
            tryObtainFertilizer(holderPos);
        }
    }

    @Override
    public void stop() {
        super.stop();

        holderPos = null;
    }

    private void tryObtainFertilizer(BlockPos pos) {
        World world = golem.getWorld();

        if (pos.getSquaredDistance(golem.getPos()) > 2.25f) {
            Vec3d $ = pos.toCenterPos().add(0, -0.5, 0);
            golem.getNavigation().startMovingTo($.x, $.y, $.z, 1);
            return;
        }

        if (world.getBlockEntity(pos) instanceof FertilizerHolderBlockEntity holder) {
            if (holder.currentFertilizers == 0) stop();
            if (golem.getInventory().count(ModItems.FERTILIZER_ITEM) >= maxFertilizerCount) stop();


            holder.currentFertilizers--;
            holder.updateBlockState();

            golem.getInventory().addStack(new ItemStack(ModItems.FERTILIZER_ITEM, 1));
        }
    }

    @Override
    public boolean canStart() {
        return !golem.hasCookie() && !golem.containsAny(stack -> stack.getItem() instanceof FertilizerItem) && findHolderBlock() != null;
    }

    private BlockPos findHolderBlock() {
        BlockPos origin = golem.getBlockPos();
        World world = golem.getWorld();

        // Map of crop position -> squared distance
        Map<BlockPos, Double> fertilizerHolderDistances = new HashMap<>();

        golem.getZone().ifPresent(zone -> {
            zone.getFertilizerHolder(world).forEach(blockPos -> {
                FertilizerHolderBlockEntity blockEntity = (FertilizerHolderBlockEntity) world.getBlockEntity(blockPos);

                if (blockEntity.currentFertilizers > 0) {
                    fertilizerHolderDistances.put(blockPos, origin.getSquaredDistance(blockPos));
                }
            });
        });

        // Choose nearest valid crop
        return fertilizerHolderDistances.entrySet()
                .stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}
