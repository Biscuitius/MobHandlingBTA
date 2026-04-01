package biscuitius.mobhandling.carriable;

import com.mojang.nbt.tags.CompoundTag;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.world.ICarriable;

public final class CarriableSupport {
    // Keep this utility class from being instantiated.
    private CarriableSupport() {
    }

    // Choose the right carriable type when the game loads saved data.
    public static ICarriable createAndLoadCarriable(Entity holder, CompoundTag tag) {
        if (tag == null) {
            return null;
        }

        if (CarriedMob.TYPE.equalsIgnoreCase(tag.getString("type"))) {
            return CarriedMob.createAndLoadCarriedMob(tag);
        }

        return ICarriable.createAndLoadCarriable(holder, tag);
    }
}



