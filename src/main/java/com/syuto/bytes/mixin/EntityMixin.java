package com.syuto.bytes.mixin;


import com.syuto.bytes.module.ModuleManager;
import com.syuto.bytes.module.impl.combat.Velocity;
import com.syuto.bytes.module.impl.render.RenderingTest;
import com.syuto.bytes.utils.impl.rotation.RotationUtils;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static com.syuto.bytes.Byte.mc;


@Mixin(Entity.class)
public abstract class EntityMixin {


    @Shadow public abstract float getYaw();

    @Shadow public abstract void setVelocity(Vec3d velocity);

    @Shadow public abstract Vec3d getVelocity();

    @ModifyArgs(method = "pushAwayFrom(Lnet/minecraft/entity/Entity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;addVelocity(DDD)V"))
    private void onPushAwayFrom(Args args, Entity entity) {
        Velocity test = ModuleManager.getModule(Velocity.class);
        if (test != null && test.isEnabled()) {
            if ((Object) this == mc.player) {
                args.set(0, (double) args.get(0) * 0);
                args.set(2, (double) args.get(2) * 0);
            }
        }
    }



    @ModifyArgs(
            method = "updateVelocity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;movementInputToVelocity(Lnet/minecraft/util/math/Vec3d;FF)Lnet/minecraft/util/math/Vec3d;"
            )
    )
    private void mf(Args args) { //movefix this doesnt work in air for some reaosn
        RenderingTest test = ModuleManager.getModule(RenderingTest.class);
        if (test != null && test.isEnabled()) {
            float customYaw = RotationUtils.getRotationYaw();
            //args.set(2, customYaw);
        }
    }
}
