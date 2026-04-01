package biscuitius.mobhandling.mixin;

import biscuitius.mobhandling.carriable.CarriableSupport;
import com.mojang.nbt.tags.CompoundTag;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.world.ICarriable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings("unused")
@Mixin(Player.class)
public class PlayerCarriableLoadMixin {
    @Redirect(
        method = "readAdditionalSaveData",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/core/world/ICarriable;createAndLoadCarriable(Lnet/minecraft/core/entity/Entity;Lcom/mojang/nbt/tags/CompoundTag;)Lnet/minecraft/core/world/ICarriable;"
        ),
        remap = false
    )
    // Restore carried mobs from the player's saved NBT.
    private ICarriable mobhandling$loadMobCarriable(Entity holder, CompoundTag tag) {
        return CarriableSupport.createAndLoadCarriable(holder, tag);
    }
}


