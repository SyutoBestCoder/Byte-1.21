package com.syuto.bytes.module.impl.combat;

import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.PreUpdateEvent;
import com.syuto.bytes.eventbus.impl.RenderTickEvent;
import com.syuto.bytes.eventbus.impl.RenderWorldEvent;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.setting.impl.BooleanSetting;
import com.syuto.bytes.setting.impl.ModeSetting;
import com.syuto.bytes.setting.impl.NumberSetting;
import com.syuto.bytes.utils.impl.player.PlayerUtil;
import com.syuto.bytes.utils.impl.render.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.MaceItem;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

import static com.syuto.bytes.Byte.mc;


public class Triggerbot extends Module {

    private ModeSetting mode = new ModeSetting("Attack Mode", this, "1.8", "1.9");
    public NumberSetting aps = new NumberSetting("APS",this, 10,1,20, 1);
    private BooleanSetting highlight = new BooleanSetting("Target Esp", this, true);

    public Triggerbot() {
        super("Triggerbot", "Attacks for you while over a tagret", Category.COMBAT);
    }

    private EntityHitResult entityHit;
    private long delay, lastTime;
    private boolean shouldAttack;


    @EventHandler
    void onPreUpdate(PreUpdateEvent event) {

        HitResult hit = mc.crosshairTarget;
        if (hit.getType() == HitResult.Type.ENTITY) {
            entityHit = (EntityHitResult) hit;
            if (canAttack(entityHit)) {
                switch(mode.getValue()) {
                    case "1.8" -> {
                        if (shouldAttack) {
                            mc.interactionManager.cancelBlockBreaking();
                            if (!mc.options.useKey.isPressed() && mc.options.attackKey.isPressed()) {
                                mc.interactionManager.attackEntity(mc.player, entityHit.getEntity());
                                mc.player.swingHand(Hand.MAIN_HAND);
                            }
                            shouldAttack = false;
                        }
                    }



                    case "1.9" -> {
                        if (mc.player.getAttackCooldownProgress(0.5f) >= 1.0) {
                            if (!mc.options.useKey.isPressed()) {
                                mc.interactionManager.attackEntity(mc.player, entityHit.getEntity());
                                mc.player.swingHand(Hand.MAIN_HAND);
                                delay = System.currentTimeMillis() + 300;
                            }
                        }
                    }


                }
            }
        } else {
            entityHit = null;
        }

    }

    @EventHandler
    void onRenderTick(RenderTickEvent e) {
        if (System.currentTimeMillis() - this.lastTime >= delay && canAttack(entityHit)) {
            this.lastTime = System.currentTimeMillis();
            updateAttackDelay();
            if (canAttack(entityHit)) {
                shouldAttack = true;
            }
        }
    }

    @EventHandler
    public void onRenderWorld(RenderWorldEvent e) {
        if (highlight.getValue()) {
            if (canAttack(entityHit)) {
                RenderUtils.renderBox(entityHit.getEntity(), e, e.partialTicks);
            }
        }
    }

    private void updateAttackDelay() {
        delay = (long)(1000.0 / aps.value.longValue());
    }

    private boolean canAttack(EntityHitResult hit) {
        return entityHit != null && entityHit.getEntity().isAlive() && PlayerUtil.isHoldingWeapon() && entityHit.getEntity() instanceof LivingEntity;
    }
}
