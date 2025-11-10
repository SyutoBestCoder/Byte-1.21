package com.syuto.bytes.utils.impl.rotation;

import com.syuto.bytes.eventbus.impl.PreMotionEvent;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Unique;

import static com.syuto.bytes.Byte.mc;

public class MixinUtils {

    public static float getLerpedPitch(float tickDelta, LivingEntity entity) {
        if(RotationUtils.pitchChanged) {
            return tickDelta == 1.0F ? RotationUtils.getRotationPitch() : MathHelper.lerp(tickDelta, RotationUtils.getLastRotationPitch(), RotationUtils.getRotationPitch());
        } else {
            return entity.getLerpedPitch(tickDelta);
        }
    }

    public static void turnHead(float yaw, LivingEntityRenderState state) {
        float f = MathHelper.wrapDegrees(yaw - state.bodyYaw);
        state.bodyYaw += f * 0.3f;

        float h = 80.0f;
        if (Math.abs(f) > h) {
            state.bodyYaw += f - (float)MathHelper.sign((double)f) * h;
        }

        float headRotation = MathHelper.wrapDegrees(yaw - state.bodyYaw);
        state.yawDegrees = headRotation;

    }



}
