package nico.farmingfellas.common.item.custom;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import nico.farmingfellas.common.entity.base.FellaGolemEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public class GolemItem extends Item {

    private final Function<World, FellaGolemEntity> factory;

    public GolemItem(Settings settings, Function<World, FellaGolemEntity> factory) {
        super(settings);

        this.factory = factory;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);

        if(stack.getOrCreateNbt().contains("Inventory")) {
            tooltip.add(Text.translatable("item.farming_fellas.golem_generic.has_inventory").formatted(Formatting.DARK_PURPLE));
        }

        if(stack.getOrCreateNbt().contains("zone_id")) {
            tooltip.add(Text.translatable("item.farming_fellas.golem_generic.has_zone").formatted(Formatting.DARK_PURPLE));
        }
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        BlockPos pos = context.getBlockPos();
        World world = context.getWorld();
        ItemStack stack = context.getStack();

        HitResult hitResult = context.getPlayer().raycast(6, 0, false);
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            assert player != null;
            FellaGolemEntity golem = factory.apply(world);

            golem.setPosition(hitResult.getPos());

            if (stack.hasCustomName()) golem.setCustomName(stack.getName());
            if (stack.getOrCreateNbt().contains("Inventory")) {
                assert stack.getNbt() != null;
                Inventories.readNbt(stack.getOrCreateSubNbt("Inventory"), golem.getInventory().stacks);
            }
            if (stack.getOrCreateNbt().contains("zone_id")) {
                assert stack.getNbt() != null;
                golem.setZone(stack.getNbt().getUuid("zone_id"));
            }
            world.spawnEntity(golem);
            stack.decrement(1);
            context.getPlayer().swingHand(context.getHand());
            context.getPlayer().getItemCooldownManager().set(this, 1);
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    public FellaGolemEntity createGolem(@Nullable World world) {
        return factory.apply(world);
    }

    @Override
    public Text getName(ItemStack stack) {
        return Text.translatable(super.getName(stack).getString().replace("item.", "entity."));
    }
}
