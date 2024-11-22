package com.syuto.bytes.module.impl.misc;

import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.PreMotionEvent;
import com.syuto.bytes.eventbus.impl.PreUpdateEvent;
import com.syuto.bytes.eventbus.impl.RenderTickEvent;
import com.syuto.bytes.eventbus.impl.RenderWorldEvent;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.utils.impl.C;
import com.syuto.bytes.utils.impl.ChatUtils;
import com.syuto.bytes.utils.impl.render.Render3D;
import com.syuto.bytes.utils.impl.rotation.RotationUtils;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.Arrays;

import static com.syuto.bytes.Byte.mc;

public class Test extends Module {

    public Test() {
        super("Test", "Module with absolutely 0 purpose.", Category.OTHER);
    }

    private boolean rot;
    public static float[] rots;
    Entity target;
    Color black = Color.BLACK;
    Color white = Color.WHITE;


    @EventHandler
    public void onRenderTick(RenderTickEvent event) {
        if (target != null) {
         //   rots = RotationUtils.getRotations(target);
        }
        VertexConsumerProvider vertexConsumers = mc.getBufferBuilders().getEntityVertexConsumers();
        Matrix4f matrix = new Matrix4f();
        OrderedText orderedText = Text.literal("</byte>").asOrderedText();

        mc.textRenderer.drawWithOutline(
                orderedText,
                5,
                10,
                white.getRGB(),
                black.getRGB(),
                matrix,
                vertexConsumers,
                255
        );
    }

    @EventHandler
    void onPreUpdate(PreUpdateEvent event) {
        /*
        Entity closestEntity = null;

        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof LivingEntity && entity != mc.player) {
                double distance = mc.player.distanceTo(entity);
                if (distance <= 6.0 && entity.isAlive()) {
                    closestEntity = entity;
                }
            }
        }

        if (closestEntity != null) {
            this.target = closestEntity;
            rots = RotationUtils.getRotations(target);
            if (mc.player.getAttackCooldownProgress(0.5f) >= 1.0 && target.isAlive()) {
                mc.interactionManager.attackEntity(mc.player, target);
                mc.player.swingHand(Hand.MAIN_HAND);
            }
        } else {
            this.target = null;
            rots = null;
        }

         */
    }

    @EventHandler
    void onPreMotion(PreMotionEvent event) {
        if (target != null && rots != null) {
            event.yaw = rots[0];
            event.pitch = rots[1];
            //ChatUtils.print("Applying rotations: " + Arrays.toString(rots));
        }
    }


    @EventHandler
    public void onRender3d(RenderWorldEvent event) {
        C.w().getEntities().forEach(e -> {
            if (e != C.p() && e instanceof LivingEntity entity) {
                float healthPercent = entity.getHealth() / entity.getMaxHealth();
                healthPercent = Math.min(1, healthPercent);


                // what the FUCK.
                int maxNumber = 200;
                int healthRed = Math.max(Math.min((int) ((0.5f - (healthPercent - 0.5f)) * maxNumber * 2), maxNumber), 0);
                int healthGreen = Math.max(Math.min((int) (((healthPercent)) * maxNumber * 2), maxNumber), 0);

                Vec3d posEntity = Render3D.magicalSmoothingFunction(e);

                Render3D.drawRect(posEntity.x, posEntity.y - 0.3f, posEntity.z, entity.getWidth() * 2, entity.getHeight() + 0.3f, new Color(255, 255, 255), event.matrixStack);

                Render3D.drawFilledRect(posEntity.x, posEntity.y, posEntity.z, 0.1f, entity.getHeight(), new Color(22, 22, 22), event.matrixStack, 0.8f);
                Render3D.drawFilledRect(posEntity.x, posEntity.y + 0.02f, posEntity.z, 0.05f, entity.getHeight()-0.04f * healthPercent, new Color(healthRed, healthGreen, 0), event.matrixStack, 0.8f);
            }
        });
    }
}
