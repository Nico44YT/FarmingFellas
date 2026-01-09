package nico.farmingfellas.common.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import nico.farmingfellas.common.zone.ZoneSaveData;

public class ZoneItem extends Item {
    public ZoneItem(Settings settings) {
        super(settings);
    }


    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if(user.getWorld() instanceof ServerWorld serverWorld) {
            ZoneSaveData.getZones(serverWorld);
        }

        return super.useOnEntity(stack, user, entity, hand);
    }
}
