package com.syuto.bytes.module.impl.combat;

import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.*;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.setting.impl.BooleanSetting;
import com.syuto.bytes.setting.impl.NumberSetting;
import com.syuto.bytes.utils.impl.player.PlayerUtil;
import com.syuto.bytes.utils.impl.render.RenderUtils;
import com.syuto.bytes.utils.impl.rotation.RotationUtils;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AimAssist extends Module {

    public AimAssist() {
        super("AimAssist", "Automatically aims at nearby players.", Category.COMBAT);
    }
    private final NumberSetting reach = new NumberSetting("Reach", this, 3, 1, 6, 0.5);
    private final NumberSetting cps = new NumberSetting("CPS", this, 10, 0, 40, 1);
    private final BooleanSetting rotation = new BooleanSetting("Rotations", this,true);

    private long lastAttackTime = 0;
    private final List<PlayerEntity> targets = new ArrayList<>();
    public static PlayerEntity target;
    private float[] rotations, lastRotation;

    @Override
    public void onDisable() {
        this.lastRotation = null;
    }

    @EventHandler
    public void onRotation(RotationEvent event) {
        if (rotation.getValue()) {
            if (lastRotation == null)
                lastRotation = new float[]{RotationUtils.getLastRotationYaw(), RotationUtils.getLastRotationPitch()};

            if (target != null) {
                rotations = RotationUtils.getRotations(
                        lastRotation,
                        mc.player.getEyePos(),
                        target
                );


                rotations = RotationUtils.getFixedRotation(rotations, lastRotation);

                if (
                        mc.options.attackKey.isPressed() &&
                                PlayerUtil.isHoldingWeapon() &&
                                PlayerUtil.getBiblicallyAccurateDistanceToEntity(target) <= reach.getValue().doubleValue()
                ) {
                    event.setYaw(rotations[0]);
                    event.setPitch(rotations[1]);

                    this.lastRotation = new float[]{RotationUtils.getLastRotationYaw(), RotationUtils.getLastRotationPitch()};
                }
            }
        }
    }

    @EventHandler
    public void onPreUpdate(PreUpdateEvent event) {


        targets.clear();
        targets.addAll(mc.world.getPlayers().stream()
                .filter(ent -> ent != mc.player)
                .sorted(Comparator.comparingDouble(PlayerUtil::getBiblicallyAccurateDistanceToEntity))
                .limit(1)
                .toList());

        target = null;
        for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
            if (player != mc.player && PlayerUtil.getBiblicallyAccurateDistanceToEntity(player) <= reach.getValue().doubleValue()) {
                target = player;
                break;
            }
        }

        if (target == null || rotations == null) return;
        if (!mc.options.attackKey.isPressed() || !PlayerUtil.isHoldingWeapon()) return;


        EntityHitResult result = (EntityHitResult) PlayerUtil.raycast(
                RotationUtils.getRotationYaw(),
                RotationUtils.getRotationPitch(),
                reach.getValue().doubleValue(),
                delta,
                false
        );

        if (result != null && result.getEntity().equals(target)) {
            double cpsValue = cps.getValue().doubleValue();
            long delay = (long) (1000.0 / cpsValue);

            long currentTime = System.currentTimeMillis();
            if (cpsValue == 0) {
                if (mc.player.getAttackCooldownProgress(0.5f) >= 1.0) {
                    mc.interactionManager.attackEntity(mc.player, target);
                    mc.player.swingHand(mc.player.getActiveHand());
                }
            } else if (currentTime - lastAttackTime >= delay) {
                mc.interactionManager.attackEntity(mc.player, target);
                mc.player.swingHand(Hand.MAIN_HAND);
                lastAttackTime = currentTime;
            }
        }

    }

    @EventHandler
    public void onRenderWorld(RenderWorldEvent e) {
        if (target != null) {

            RenderUtils.renderBox(target, e, e.partialTicks);
        }
    }
}
