package com.syuto.bytes.mixin;

import com.syuto.bytes.Byte;
import com.syuto.bytes.eventbus.impl.AttackEntityEvent;
import com.syuto.bytes.eventbus.impl.AttackEntityEvent.Mode;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {


    @Inject(
            method = "attackEntity",
            at = @At("HEAD"),
            cancellable = true
    )
    private void attackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
        AttackEntityEvent event = new AttackEntityEvent(target, Mode.Pre);
        Byte.INSTANCE.eventBus.post(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(
            method = "attackEntity",
            at = @At("TAIL"),
            cancellable = false
    )
    private void attackEntityPost(PlayerEntity player, Entity target, CallbackInfo ci) {
        AttackEntityEvent post = new AttackEntityEvent(target, Mode.Post);
        Byte.INSTANCE.eventBus.post(post);
    }

}

