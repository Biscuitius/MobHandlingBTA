package biscuitius.mobhandling.mixin;

import biscuitius.mobhandling.carriable.CarriableSupport;
import com.mojang.nbt.tags.CompoundTag;
import net.minecraft.client.net.handler.PacketHandlerClient;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.world.ICarriable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings("unused")
@Mixin(PacketHandlerClient.class)
public class PacketHandlerClientCarriableMixin {
    @Redirect(
        method = "handleNamedEntitySpawn",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/core/world/ICarriable;createAndLoadCarriable(Lnet/minecraft/core/entity/Entity;Lcom/mojang/nbt/tags/CompoundTag;)Lnet/minecraft/core/world/ICarriable;"
        ),
        remap = false
    )
    // Rebuild carried mobs when they appear in spawn packets.
    private ICarriable mobhandling$loadMobCarriableFromSpawn(Entity holder, CompoundTag tag) {
        return CarriableSupport.createAndLoadCarriable(holder, tag);
    }

    @Redirect(
        method = "handlePlayerHeldObject",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/core/world/ICarriable;createAndLoadCarriable(Lnet/minecraft/core/entity/Entity;Lcom/mojang/nbt/tags/CompoundTag;)Lnet/minecraft/core/world/ICarriable;"
        ),
        remap = false
    )
    // Rebuild carried mobs when the server updates a held object.
    private ICarriable mobhandling$loadMobCarriableFromUpdate(Entity holder, CompoundTag tag) {
        return CarriableSupport.createAndLoadCarriable(holder, tag);
    }
}


