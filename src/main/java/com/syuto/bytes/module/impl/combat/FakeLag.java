package com.syuto.bytes.module.impl.combat;

import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.PacketSentEvent;
import com.syuto.bytes.eventbus.impl.PreUpdateEvent;
import com.syuto.bytes.eventbus.impl.RenderWorldEvent;
import com.syuto.bytes.mixin.SendPacketMixinAccessor;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.setting.impl.NumberSetting;
import com.syuto.bytes.utils.impl.client.ChatUtils;
import com.syuto.bytes.utils.impl.client.ClientUtil;
import com.syuto.bytes.utils.impl.render.RenderUtils;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Iterator;

public class FakeLag extends Module {
    private final NumberSetting delaySetting = new NumberSetting(
            "Delay",
            this,
            200,
            1,
            1000,
            5
    );

    private final ArrayList<Packet<?>> packetList = new ArrayList<>();
    private EntityHitResult entityHit;

    private boolean delay;

    private long lastDelayStart;
    private long lastFlushTime;

    private Vec3d pos, p;

    public FakeLag() {
        super("FakeLag", "Simulates lag / delayed packets", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        flushAll();
    }

    @EventHandler
    public void onPacketSent(PacketSentEvent event) {
        if (ClientUtil.nullCheck() && event.getPacket() instanceof PlayerInteractEntityC2SPacket)
            flushAll();

        if (ClientUtil.nullCheck() && delay) {
            synchronized (packetList) {
                packetList.add(event.getPacket());
                event.setCanceled(true);
            }
        }
    }

    @EventHandler
    public void onPreUpdate(PreUpdateEvent event) {


        long now = System.currentTimeMillis();

        if (!delay) {
            delay = true;
            lastDelayStart = now;
            lastFlushTime = now;
            pos = mc.player.getPos();
            ChatUtils.print("Delay started");
        } else {
            long elapsed = now - lastDelayStart;

            var s = delaySetting.getValue().longValue();
            if (!packetList.isEmpty() && now - lastFlushTime >= s) {
                flush();
                lastFlushTime = now;
            }

            if (elapsed >= delaySetting.getValue().longValue()) {
                flushAll();
                p = mc.player.getPos();
            }
        }
    }

    private void flush() {
        if (packetList.isEmpty()) return;
        SendPacketMixinAccessor silent = (SendPacketMixinAccessor) mc.getNetworkHandler();

        synchronized (packetList) {
            for (Packet<?> packet : packetList) {
                silent.getConnection().send(packet);
            }
            packetList.clear();
        }
        ChatUtils.print("Flushed");
    }

    private void flushAll() {
        SendPacketMixinAccessor silent = (SendPacketMixinAccessor) mc.getNetworkHandler();
        p = mc.player.getPos();
        synchronized (packetList) {
            for (Packet<?> packet : packetList) {
                silent.getConnection().send(packet);
            }
            packetList.clear();
            delay = false;
            lastDelayStart = System.currentTimeMillis();
            ChatUtils.print("Flushed all packets");
        }
    }

    @EventHandler
    public void onRenderWorld(RenderWorldEvent e) {
        if (pos != null && p != null) {
            long now = System.currentTimeMillis();
            long elapsed = now - lastDelayStart;
            long duration = delaySetting.getValue().longValue();

            float progress = Math.min(1.0f, (float) elapsed / (float) duration);
            double interpX = pos.x + (p.x - pos.x) * progress;
            double interpY = pos.y + (p.y - pos.y) * progress;
            double interpZ = pos.z + (p.z - pos.z) * progress;

            Vec3d interpolatedPos = new Vec3d(interpX, interpY, interpZ);

            RenderUtils.renderp(pos, interpolatedPos, e, e.partialTicks);

            if (progress >= 1.0f) {
                pos = p;
                lastDelayStart = now;
                p = mc.player.getPos();
            }
        }
    }
}
