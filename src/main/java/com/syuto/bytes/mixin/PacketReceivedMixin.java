package com.syuto.bytes.mixin;


import com.syuto.bytes.Byte;
import com.syuto.bytes.eventbus.impl.PacketReceivedEvent;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ClientConnection.class)
public class PacketReceivedMixin {


    @Inject(
            at = @At("HEAD"),
            method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V",
            cancellable = true
    )
    public void handlePacket(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
        PacketReceivedEvent e = new PacketReceivedEvent(packet);
        Byte.INSTANCE.eventBus.post(e);

        if (e.isCanceled()) {
            ci.cancel();
        }
    }

    /*@Inject(at = @At("HEAD"), method = "send", cancellable = true)
    public void handlePacket(Packet<?> packet, PacketCallbacks callbacks, CallbackInfo ci) {
        Byte.LOGGER.info("Packet " + packet.getPacketId().toString());
        PacketReceivedEvent e = new PacketReceivedEvent(packet);
        Byte.INSTANCE.eventBus.post(e);

        if (e.isCanceled()) {
            ci.cancel();
        }
    }*/
}
