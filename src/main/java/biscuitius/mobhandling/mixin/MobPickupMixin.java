package biscuitius.mobhandling.mixin;

import biscuitius.mobhandling.carriable.CarriedMob;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings({"unused", "ConstantConditions"})
@Mixin(Mob.class)
public class MobPickupMixin {
    @Inject(method = "interact(Lnet/minecraft/core/entity/player/Player;)Z", at = @At("HEAD"), cancellable = true, remap = false)
    // Intercept mob interaction so sneaking players can pick mobs up.
    private void mobhandling$pickupMob(Player player, CallbackInfoReturnable<Boolean> cir) {
        Mob mob = (Mob) (Object) this;
        World world = mob.world;
        if (world == null || player == null) {
            return;
        }

        ItemStack heldItem = player.getCurrentEquippedItem();
        if (!player.isSneaking() || heldItem != null || player.getHeldObject() != null) {
            return;
        }
        if (!mob.isAlive()) {
            cir.setReturnValue(false);
            return;
        }

        CarriedMob carriedMob = new CarriedMob(mob);
        if (!carriedMob.canBeCarried(world, mob)) {
            cir.setReturnValue(false);
            return;
        }

        player.setHeldObject(carriedMob);
        world.setEntityDead(mob);
        cir.setReturnValue(true);
    }
}



