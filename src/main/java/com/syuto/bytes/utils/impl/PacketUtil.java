package com.syuto.bytes.utils.impl;

import net.minecraft.network.packet.Packet;

import static com.syuto.bytes.Byte.mc;

// packet event! maybe need a sendPacketNoEvent function!
// easier to type PacketUtil.SendPacket(packet) than mc.getNetworkHandler().sendPacket(packet)
public class PacketUtil {
    public static void sendPacket(Packet<?> packet) {
        mc.getNetworkHandler().sendPacket(packet);
    }
}
