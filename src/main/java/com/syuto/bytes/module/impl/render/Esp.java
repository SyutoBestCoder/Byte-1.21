package com.syuto.bytes.module.impl.render;

import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.RenderWorldEvent;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.utils.impl.client.ChatUtils;
import com.syuto.bytes.utils.impl.render.RenderUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.*;

public class Esp extends Module {
    public Esp() {
        super("Esp", "Shows players through walls", Category.RENDER);
    }



    @EventHandler
    public void onRenderWorld(RenderWorldEvent event) {
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof PlayerEntity en && entity.isAlive() && entity != mc.player) {
                float delta = mc.getRenderTickCounter().getTickDelta(true);
                RenderUtils.renderHealth(
                        en,
                        event,
                        en.getHealth(),
                        en.getMaxHealth() + en.getAbsorptionAmount(),
                        (en.getHealth() / en.getMaxHealth() + en.getAbsorptionAmount()),
                        delta
                );
            }
        }

    }




    public boolean isEntityInView(Entity entity) {
        Vec3d playerLook = mc.player.getRotationVec(1.0F);
        Vec3d toEntity = entity.getPos().subtract(mc.player.getPos()).normalize();
        double angle = Math.acos(playerLook.dotProduct(toEntity));
        return Math.toDegrees(angle) < mc.options.getFov().getValue() / 1.5f;
    }



    /*
                    if (!isEntityInView(en)) continue;
                float delta = mc.getRenderTickCounter().getTickDelta(true);

                double interpolatedX = en.prevX + (en.getX() - en.prevX) * delta;
                double interpolatedY = en.prevY + (en.getY() - en.prevY) * delta;
                double interpolatedZ = en.prevZ + (en.getZ() - en.prevZ) * delta;

                Box interpolatedBox = en.getBoundingBox().expand(0.15f).offset(
                        interpolatedX - en.getX(),
                        interpolatedY - en.getY(),
                        interpolatedZ - en.getZ()
                );

                Vec3d[] corners = {
                        new Vec3d(interpolatedBox.minX, interpolatedBox.minY, interpolatedBox.minZ),
                        new Vec3d(interpolatedBox.maxX, interpolatedBox.minY, interpolatedBox.minZ),
                        new Vec3d(interpolatedBox.minX, interpolatedBox.maxY, interpolatedBox.minZ),
                        new Vec3d(interpolatedBox.maxX, interpolatedBox.maxY, interpolatedBox.minZ),
                        new Vec3d(interpolatedBox.minX, interpolatedBox.minY, interpolatedBox.maxZ),
                        new Vec3d(interpolatedBox.maxX, interpolatedBox.minY, interpolatedBox.maxZ),
                        new Vec3d(interpolatedBox.minX, interpolatedBox.maxY, interpolatedBox.maxZ),
                        new Vec3d(interpolatedBox.maxX, interpolatedBox.maxY, interpolatedBox.maxZ),
                };

                float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
                float maxX = Float.MIN_VALUE, maxY = Float.MIN_VALUE;

                for (Vec3d corner : corners) {
                    Vec3d screenPos = RenderUtils.worldToScreen(corner);
                    if (screenPos != null) {
                        minX = Math.min(minX, (float) screenPos.x);
                        minY = Math.min(minY, (float) screenPos.y);
                        maxX = Math.max(maxX, (float) screenPos.x);
                        maxY = Math.max(maxY, (float) screenPos.y);
                    }
                }

                if (minX < maxX && minY < maxY) {
                    RenderUtils.drawRectOutline(event, minX, minY, maxX, maxY, 0xFFFFFFFF);
                    RenderUtils.drawRect(event, minX + 0.1F, minY + 0.1F, maxX - 0.1F, maxY - 0.1F, 0x80000000);
                }
     */


}


