package com.syuto.bytes.mixin;

import com.mojang.authlib.GameProfile;
import com.syuto.bytes.Byte;
import com.syuto.bytes.eventbus.impl.PreMotionEvent;
import com.syuto.bytes.eventbus.impl.PreUpdateEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.ClientPlayerTickable;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.util.Cooldown;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import java.util.List;

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

    @Shadow @Final private Cooldown itemDropCooldown;

    @Shadow protected abstract void sendSneakingPacket();

    @Shadow private PlayerInput lastPlayerInput;

    @Shadow public Input input;

    @Shadow @Final private List<ClientPlayerTickable> tickables;

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void tick() {
        this.itemDropCooldown.tick();
        Byte.INSTANCE.eventBus.post(new PreUpdateEvent());
        super.tick();
        this.sendSneakingPacket();
        if (!this.lastPlayerInput.equals(this.input.playerInput)) {
            this.networkHandler.sendPacket(new PlayerInputC2SPacket(this.input.playerInput));
            this.lastPlayerInput = this.input.playerInput;
        }

        if (this.hasVehicle()) {
            this.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(this.getYaw(), this.getPitch(), this.isOnGround(), this.horizontalCollision));
            Entity entity = this.getRootVehicle();
            if (entity != this && entity.isLogicalSideForUpdatingMovement()) {
                this.networkHandler.sendPacket(new VehicleMoveC2SPacket(entity));
                this.sendSprintingPacket();
            }
        } else {
            this.sendMovementPackets();
        }

        Iterator var3 = this.tickables.iterator();

        while(var3.hasNext()) {
            ClientPlayerTickable clientPlayerTickable = (ClientPlayerTickable)var3.next();
            clientPlayerTickable.tick();
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    private void sendMovementPackets() {
        this.sendSprintingPacket();
        if (this.isCamera()) {
            PreMotionEvent event = new PreMotionEvent(
                    this.getX(),
                    this.getBoundingBox().minY,
                    this.getZ(),
                    this.getYaw(),
                    this.getPitch(),
                    this.isOnGround(),
                    this.isSneaking(),
                    this.isSprinting(),
                    this.horizontalCollision
            );

            Byte.INSTANCE.eventBus.post(event);

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
}
