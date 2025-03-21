package com.syuto.bytes.mixin;

import com.syuto.bytes.Byte;
import com.syuto.bytes.eventbus.impl.ChatEvent;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ChatMixin {
    @Inject(at = @At("HEAD"), method = "sendChatMessage", cancellable = true)
    public void sendChatMessage(String content, CallbackInfo ci) {
        ChatEvent e = new ChatEvent(content);

        content = e.getMessage();

        Byte.INSTANCE.eventBus.post(e);
        if (e.isCanceled()) ci.cancel();

        Byte.LOGGER.info("Sent message content: {} ", content);
    }
}
