package com.syuto.bytes.utils.impl.render;

import com.syuto.bytes.module.ModuleManager;
import com.syuto.bytes.module.impl.render.Animations;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.RotationCalculator;
import net.minecraft.util.math.RotationPropertyHelper;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import static com.syuto.bytes.Byte.mc;

public class AnimationUtils {

    @Setter
    @Getter
    public static boolean isBlocking;

    @Setter
    @Getter
    public static ItemStack spoofedItem;

    public static float height = -0.1f;

    public static void animate(MatrixStack matrices, float swingProgress, float f) {
        float sine = (float) Math.sin(MathHelper.sqrt(swingProgress) * Math.PI) ;

        Animations animation = ModuleManager.getModule(Animations.class);
        assert animation != null;

        switch(animation.mode.value) {
            case "Exhibition" -> {
                matrices.translate(0.1, 0, -0.1);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-sine * 50));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-sine * 30));
            }

            case "Vanilla" -> {
                matrices.translate(0.1, 0,-0.1);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45.0f + f * -20.0f));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(sine * -20.0f));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sine * -80.0f));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-45.0f));
            }

            case "Spin" -> {
                float spin = -(System.currentTimeMillis() / 2 % 360);
                matrices.translate(-0.1, 0,-0.2);
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(spin));
            }
        }

    }

}
