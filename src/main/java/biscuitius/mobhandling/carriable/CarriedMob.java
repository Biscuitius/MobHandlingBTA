package biscuitius.mobhandling.carriable;

import biscuitius.mobhandling.config.MobHandlingConfig;
import com.mojang.nbt.tags.CompoundTag;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.EntityDispatcher;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.ICarriable;
import net.minecraft.core.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CarriedMob implements ICarriable {
    public static final String TYPE = "mob";

    private @Nullable CompoundTag mobTag;

    // Start with an empty carrier so NBT can fill it later.
    public CarriedMob() {
    }

    // Capture the mob data we need to recreate it later.
    public CarriedMob(@NotNull Mob mob) {
        this.mobTag = new CompoundTag();
        mob.save(this.mobTag);
    }

    // Rebuild a carried mob from saved NBT.
    public static CarriedMob createAndLoadCarriedMob(@NotNull CompoundTag tag) {
        CarriedMob carriedMob = new CarriedMob();
        carriedMob.readFromNBT(tag);
        return carriedMob;
    }

    // Spawn a preview copy for rendering in hand.
    public @Nullable Entity createPreviewEntity(World world) {
        return this.createEntity(world, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F);
    }

    @Override
    // Held carried mobs do not need ticking logic here.
    public void heldTick(World world, Entity holder) {
    }

    @Override
    // Try to place the mob in front of the player at the target block.
    public boolean tryPlace(World world, Entity holder, int blockX, int blockY, int blockZ, Side side, double xPlaced, double yPlaced) {
        if (this.mobTag == null) {
            return false;
        }

        if (world.isClientSide) {
            return true;
        }

        double x = (double) blockX + (double) side.getOffsetX() + 0.5D;
        double y = (double) blockY + (double) side.getOffsetY();
        double z = (double) blockZ + (double) side.getOffsetZ() + 0.5D;
        Entity entity = this.createEntity(world, x, y, z, holder.yRot, holder.xRot);
        if (entity == null) {
            return false;
        }

        if (!this.canFitAt(world, entity)) {
            return false;
        }

        return world.entityJoinedWorld(entity);
    }

    @Override
    // Drop the mob near the holder if placement fails.
    public void drop(World world, Entity holder) {
        if (this.mobTag == null || world.isClientSide) {
            return;
        }

        int holderX = MathHelper.floor(holder.x);
        int holderY = MathHelper.floor(holder.y);
        int holderZ = MathHelper.floor(holder.z);

        for (int y = holderY - 1; y <= holderY + 1; ++y) {
            for (int x = holderX - 1; x <= holderX + 1; ++x) {
                for (int z = holderZ - 1; z <= holderZ + 1; ++z) {
                    if (this.spawnAt(world, holder, (double) x + 0.5D, y, (double) z + 0.5D)) {
                        return;
                    }
                }
            }
        }

        for (int yOffset = 0; yOffset <= 2; ++yOffset) {
            if (this.spawnAt(world, holder, holder.x, holder.y + (double) yOffset, holder.z)) {
                return;
            }
        }
    }

    @Override
    // Only mobs that pass the config rules can be carried.
    public boolean canBeCarried(World world, Entity potentialHolder) {
        if (!(potentialHolder instanceof Mob)) {
            return false;
        }

        return MobHandlingConfig.get().canCarry((Mob) potentialHolder);
    }

    @Override
    // Return this carrier instance so the player keeps holding it.
    public ICarriable pickup(World world, Entity holder) {
        return this;
    }

    @Override
    // Save the mob type and its captured entity data.
    public void writeToNBT(CompoundTag tag) {
        tag.putString("type", TYPE);
        if (this.mobTag != null) {
            tag.put("entity", this.mobTag);
        }
    }

    @Override
    // Restore the mob data if the save file contains it.
    public void readFromNBT(CompoundTag tag) {
        if (tag.containsKey("entity")) {
            this.mobTag = tag.getCompound("entity");
        }
    }

    // Recreate the mob and place it at a specific position.
    private boolean spawnAt(World world, Entity holder, double x, double y, double z) {
        Entity entity = this.createEntity(world, x, y, z, holder.yRot, holder.xRot);
        if (entity == null) {
            return false;
        }
        if (!this.canFitAt(world, entity)) {
            return false;
        }

        return world.entityJoinedWorld(entity);
    }

    // Build the mob entity from NBT and move it into place.
    private @Nullable Entity createEntity(World world, double x, double y, double z, float yRot, float xRot) {
        if (this.mobTag == null) {
            return null;
        }

        Entity entity = EntityDispatcher.createEntityFromNBT(this.mobTag, world);
        if (entity != null) {
            entity.absMoveTo(x, y, z, yRot, xRot);
        }

        return entity;
    }

    // Check whether the mob can fit in its current bounding box.
    private boolean canFitAt(World world, Entity entity) {
        return world.checkIfAABBIsClear(entity.bb) && world.getCubes(entity, entity.bb).isEmpty() && !world.getIsAnyLiquid(entity.bb);
    }
}


