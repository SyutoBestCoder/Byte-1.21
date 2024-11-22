package com.syuto.bytes.utils.impl.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.syuto.bytes.utils.impl.C;
import com.syuto.bytes.utils.impl.ChatUtils;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static com.syuto.bytes.Byte.mc;

// sorry 2048! idk kotlin very well and ive NEVER used nanovg!
public class Render3D {
    public static Vec3d magicalSmoothingFunction(Entity entity) {
        float partialTicks = mc.getRenderTickCounter().getTickDelta(false);

        double x = entity.prevX + ((entity.getX() - entity.prevX) * partialTicks);
        double y = entity.prevY + ((entity.getY() - entity.prevY) * partialTicks);
        double z = entity.prevZ + ((entity.getZ() - entity.prevZ) * partialTicks);

        return new Vec3d(x, y, z);
    }


    // lwk made a mess of this sowwy! i assume 2048 will make a render3dutil anyway! i had fun making this so who cares
    public static void drawRect(double x, double y, double z, float w, float h, Color color, MatrixStack matrixStack) {

        BufferBuilder bufferBuilder = getBufferBuilder(matrixStack, VertexFormat.DrawMode.DEBUG_LINE_STRIP);
        setRenderColor(color);
        beginRender3d();

        // offset position by player pos TODO: in f5 it offsets :skull: + when crouching
        Vec3d camPos = C.p().getClientCameraPosVec(mc.getRenderTickCounter().getTickDelta(false));
        x = (x - camPos.x);
        y = (y - camPos.y) + h/2;
        z = (z - camPos.z);

        // translate to position!
        matrixStack.translate((float) x, (float) y, (float) z);

        // rotate to face player!
        matrixStack.multiply(RotationAxis.NEGATIVE_Y.rotation((float) Math.toRadians(C.p().renderYaw)));
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotation((float) Math.toRadians(C.p().renderPitch)));

        Matrix4f matrix = matrixStack.peek().getPositionMatrix();

        bufferBuilder.vertex(matrix, -w/2, -h/2, 0);
        bufferBuilder.vertex(matrix, -w/2, h/2, 0);
        bufferBuilder.vertex(matrix, w/2, h/2, 0);
        bufferBuilder.vertex(matrix, w/2, -h/2, 0);
        bufferBuilder.vertex(matrix, -w/2, -h/2, 0);

        finishRender3d(bufferBuilder, matrixStack);
    }

    // dw.
    public static void drawFilledRect(double x, double y, double z, float w, float h, Color color, MatrixStack matrixStack, float xOffset) {
        BufferBuilder bufferBuilder = getBufferBuilder(matrixStack, VertexFormat.DrawMode.QUADS);
        setRenderColor(color);
        beginRender3d();

        // offset position by player pos TODO: in f5 it offsets :skull: + when crouching
        Vec3d camPos = C.p().getClientCameraPosVec(mc.getRenderTickCounter().getTickDelta(false));
        x = (x - camPos.x);
        y = (y - camPos.y);
        z = (z - camPos.z);

        // translate to position!
        matrixStack.translate((float) x, (float) y, (float) z);

        // rotate to face player!
        matrixStack.multiply(RotationAxis.NEGATIVE_Y.rotation((float) Math.toRadians(C.p().renderYaw)));

        Matrix4f matrix = matrixStack.peek().getPositionMatrix();

        bufferBuilder.vertex(matrix, -w/2 + xOffset   , 0, 0);
        bufferBuilder.vertex(matrix, -w/2 + xOffset   , h, 0);
        bufferBuilder.vertex(matrix, w/2  + xOffset   , h, 0);
        bufferBuilder.vertex(matrix, w/2  + xOffset   , 0, 0);

        finishRender3d(bufferBuilder, matrixStack);
    }

    public static void drawFilledRect(double x, double y, double z, float w, float h, Color color, MatrixStack matrixStack) {
        drawFilledRect(x,y,z,w,h,color,matrixStack,0);
    }


    static void beginRender3d() {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
    }

    // triangle fan for filled box, quads for fillled boxes with 4 vertices! debug lines for unfilled maybe idrk
    static BufferBuilder getBufferBuilder(MatrixStack matrixStack, VertexFormat.DrawMode drawMode) {
        matrixStack.push();

        RenderSystem.setShader(ShaderProgramKeys.POSITION);
        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        return tessellator.begin(drawMode, VertexFormats.POSITION);
    }

    static void finishRender3d(BufferBuilder bufferBuilder, MatrixStack matrixStack) {
        matrixStack.pop();

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.setShaderColor(1,1,1,1);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }


    static void setRenderColor(Color color) {
        RenderSystem.setShaderColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
    }
}
