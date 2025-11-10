package com.syuto.bytes.mixin;

import com.syuto.bytes.Byte;
import com.syuto.bytes.eventbus.impl.TickEvent;
import com.syuto.bytes.module.ModuleManager;
import com.syuto.bytes.module.impl.combat.AimAssist;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.ThreadLocalRandom;

@Mixin(MinecraftClient.class)
public class MinecraftMixin {
    @Inject(at = @At("HEAD"), method = "tick")
    private void onTick(CallbackInfo ci) {
        TickEvent tick = new TickEvent();
        Byte.INSTANCE.eventBus.post(tick);
    }

    @Inject(at = @At("HEAD"), method = "handleBlockBreaking", cancellable = true)
    private void onHandleBlockBreaking(CallbackInfo ci) {
        AimAssist a = ModuleManager.getModule(AimAssist.class);

        if (a != null && a.isEnabled()) {
            //ci.cancel();
        }

    }
}
