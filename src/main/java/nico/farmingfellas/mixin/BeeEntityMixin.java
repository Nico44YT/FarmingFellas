package nico.farmingfellas.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.BeeEntity;
import nico.farmingfellas.common.entity.beekeeper.BeekeeperFellaEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BeeEntity.class)
public abstract class BeeEntityMixin {
    @Inject(method = "tryAttack", at = @At("HEAD"), cancellable = true)
    public void farming_fellas$tryAttack(Entity target, CallbackInfoReturnable<Boolean> cir) {
        if(target instanceof BeekeeperFellaEntity) cir.setReturnValue(false);
    }
}
