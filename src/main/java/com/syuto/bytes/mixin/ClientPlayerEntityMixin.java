package com.syuto.bytes.mixin;

import com.mojang.authlib.GameProfile;
import com.syuto.bytes.Byte;
import com.syuto.bytes.eventbus.impl.PreMotionEvent;
import com.syuto.bytes.eventbus.impl.PreUpdateEvent;
import com.syuto.bytes.eventbus.impl.SlowDownEvent;
import com.syuto.bytes.module.ModuleManager;
import com.syuto.bytes.module.impl.movement.NoSlow;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {

    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Shadow protected abstract void sendSprintingPacket();

    @Shadow protected abstract boolean isCamera();

    @Shadow private double lastX;

    @Shadow private double lastBaseY;

    @Shadow private double lastZ;

    @Shadow private float lastYaw;

    @Shadow private float lastPitch;

    @Shadow public abstract boolean isSneaking();

    @Shadow private int ticksSinceLastPositionPacketSent;

    @Shadow @Final public ClientPlayNetworkHandler networkHandler;

    @Shadow private boolean lastOnGround;

    @Shadow private boolean lastHorizontalCollision;

    @Shadow private boolean autoJumpEnabled;

    @Shadow @Final protected MinecraftClient client;


    @Shadow protected abstract boolean isBlind();

    @Shadow public abstract boolean shouldSlowDown();

    @Shadow protected abstract boolean isRidingCamel();

    @Shadow public abstract boolean isUsingItem();

    @Shadow public abstract boolean isSubmergedInWater();

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;tick()V"), method = "tick")
    public void onPreUpdate(CallbackInfo ci) {
        Byte.INSTANCE.eventBus.post(new PreUpdateEvent());
    }


    /**
     * @author
     * @reason
     */
    @Overwrite
    private void sendMovementPackets() {
        PreMotionEvent event = new PreMotionEvent(
                this.getX(),
                this.getBoundingBox().minY,
                this.getZ(),
                this.getYaw(),
                this.getPitch(),
                this.lastYaw,
                this.lastPitch,
                this.isOnGround(),
                this.isSneaking(),
                this.isSprinting(),
                this.horizontalCollision
        );

        this.sendSprintingPacket();
        if (this.isCamera()) {

            Byte.INSTANCE.eventBus.post(event);
            PreMotionEvent.pitchChanged = PreMotionEvent.pitch != getPitch();

            double d = event.posX - this.lastX;
            double e = event.posY - this.lastBaseY;
            double f = event.posZ - this.lastZ;
            double g = (double)(event.yaw - this.lastYaw);
            double h = (double)(event.pitch - this.lastPitch);
            ++this.ticksSinceLastPositionPacketSent;
            boolean bl = MathHelper.squaredMagnitude(d, e, f) > MathHelper.square(2.0E-4) || this.ticksSinceLastPositionPacketSent >= 20;
            boolean bl2 = g != 0.0 || h != 0.0;
            if (bl && bl2) {
                this.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(event.posX, event.posY, event.posZ, event.yaw, event.pitch, event.onGround, event.horizontalCollision));
            } else if (bl) {
                this.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(event.posX, event.posY, event.posZ, event.onGround, event.horizontalCollision));
            } else if (bl2) {
                this.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(event.yaw, event.pitch, event.onGround, event.horizontalCollision));
            } else if (this.lastOnGround != event.onGround ||  this.lastHorizontalCollision != event.horizontalCollision) {
                this.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(event.onGround, event.horizontalCollision));
            }

            if (bl) {
                this.lastX = event.posX;
                this.lastBaseY = event.posY;
                this.lastZ = event.posZ;
                this.ticksSinceLastPositionPacketSent = 0;
            }

            if (bl2) {
                this.lastYaw = event.yaw;
                this.lastPitch = event.pitch;
            }

            this.lastOnGround = event.onGround;
            this.lastHorizontalCollision = event.horizontalCollision;
            this.autoJumpEnabled = (Boolean)this.client.options.getAutoJump().getValue();
        }
    }

    /**
     * @author
     * @reason
     */

    @Overwrite
    private boolean shouldStopSprinting() {
        NoSlow noslow = ModuleManager.getModule(NoSlow.class);
        return this.isGliding() || this.isBlind() || this.shouldSlowDown() || this.hasVehicle() && !this.isRidingCamel() || (noslow != null && !noslow.isEnabled() && this.isUsingItem() && !this.hasVehicle() && !this.isSubmergedInWater());
    }


    @Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"))
    private boolean noSlow(ClientPlayerEntity instance) {
        SlowDownEvent event = new SlowDownEvent(SlowDownEvent.Mode.Item);

        Byte.INSTANCE.eventBus.post(event);

        NoSlow noslow = ModuleManager.getModule(NoSlow.class);

        if (noslow != null) {
            if (noslow.isEnabled() && event.isCanceled()) {
                return false;
            }
        }
        return instance.isUsingItem();
    }


}
