package biscuitius.mobhandling.mixin;

import biscuitius.mobhandling.carriable.CarriedMob;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.MobRendererPlayer;
import net.minecraft.client.render.tessellator.Tessellator;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.entity.animal.MobPig;
import net.minecraft.core.entity.animal.MobCow;
import net.minecraft.core.entity.animal.MobSheep;
import net.minecraft.core.entity.animal.MobSquid;
import net.minecraft.core.entity.animal.MobWolf;
import net.minecraft.core.entity.monster.MobScorpion;
import net.minecraft.core.entity.monster.MobSlime;
import net.minecraft.core.entity.monster.MobSpider;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.world.ICarriable;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(MobRendererPlayer.class)
public class MobRendererPlayerCarriedMobMixin {
    @Inject(method = "drawHeldObject(Lnet/minecraft/core/entity/player/Player;F)V", at = @At("HEAD"), cancellable = true, remap = false)
    // Replace the default held-object render with a mob preview.
    private void mobhandling$renderCarriedMob(Player player, float partialTick, CallbackInfo ci) {
        ICarriable heldObject = player.getHeldObject();
        if (!(heldObject instanceof CarriedMob)) {
            return;
        }

        Entity preview = ((CarriedMob) heldObject).createPreviewEntity(player.world);
        if (preview == null) {
            return;
        }

        if (preview instanceof Mob) {
            Mob mob = (Mob) preview;
            mob.hurtTime = 0;
            mob.deathTime = 0;
        }

        if (preview instanceof MobWolf) {
            MobWolf wolf = (MobWolf) preview;
            wolf.setWolfSitting(false);
        }

        EntityRenderDispatcher dispatcher = EntityRenderDispatcher.instance;
        Minecraft mc = Minecraft.getMinecraft();
        net.minecraft.client.render.TextureManager previousTextureManager = dispatcher.textureManager;
        try {
            if (mc != null && mc.textureManager != null) {
                dispatcher.textureManager = mc.textureManager;
            }

            GL11.glPushMatrix();
            try {
                if (preview instanceof MobSheep || preview instanceof MobCow) {
                    GL11.glScalef(1.0F, -1.0F, -1.0F);
                    GL11.glTranslated(0.0D, -1.2D, 1.1D);
                    GL11.glRotated(-15F, 1.0F, 0.0F, 0.0F);
                } else if (preview instanceof MobPig) {
                    GL11.glScalef(1.0F, -1.0F, -1.0F);
                    GL11.glTranslated(0.0D, -0.8D, 1.2D);
                    GL11.glRotated(-75F, 1.0F, 0.0F, 0.0F);
                } else if (preview instanceof MobScorpion) {
                    GL11.glScalef(1.0F, -1.0F, -1.0F);
                    GL11.glTranslated(0.0D, -0.6D, 0.15D);
                    GL11.glRotated(80F, 1.0F, 0.0F, 0.0F);
                } else if (preview instanceof MobSpider) {
                    GL11.glTranslated(0.0D, 0.5D, -1.2D);
                    GL11.glRotated(-255F, 1.0F, 0.0F, 0.0F);
                } else if (preview instanceof MobSquid) {
                    GL11.glScalef(1.0F, -1.0F, -1.0F);
                    GL11.glTranslated(0.0D, -2.0D, 0.5D);
                } else if (preview instanceof MobSlime) {
                    GL11.glScalef(1.0F, -1.0F, -1.0F);
                    GL11.glTranslated(0.0D, -0.9D, 0.5D);
                    GL11.glRotated(-10F, 1.0F, 0.0F, 0.0F);
                } else if (preview instanceof MobWolf) {
                    GL11.glScalef(1.0F, -1.0F, -1.0F);
                    GL11.glTranslated(0.06D, -0.7D, 1.2D);
                    GL11.glRotated(-75F, 1.0F, 0.0F, 0.0F);
                } else {
                    GL11.glScalef(1.0F, -1.0F, -1.0F);
                    GL11.glTranslated(0.0D, -1.0D, 0.5D);
                }
                preview.setWorld(player.world);
                preview.moveTo(0.0D, 0.0D, 0.0D, 0.0F, 0.0F);
                dispatcher.renderEntityPreviewWithPosYaw(Tessellator.instance, preview, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
            } finally {
                GL11.glPopMatrix();
            }
        } finally {
            dispatcher.textureManager = previousTextureManager;
        }

        ci.cancel();
    }

}



