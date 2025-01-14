package com.syuto.bytes.utils.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.syuto.bytes.eventbus.impl.RenderTickEvent;
import com.syuto.bytes.eventbus.impl.RenderWorldEvent;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.impl.renderer.RendererManager;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static com.syuto.bytes.Byte.mc;

public class RenderUtils {
    private static final Color black = Color.black;
    private static final Color gray = Color.darkGray;
    private static final Color green = Color.green;
    private static final Color yellow = Color.yellow;
    private static final Color red = Color.red;
    private static final Color orange = Color.orange;
    private static final Color white = Color.white;

    public static final int[] lastViewport = new int[4];
    public static final Matrix4f lastProjMat = new Matrix4f();
    public static final Matrix4f lastModMat = new Matrix4f();
    public static final Matrix4f lastWorldSpaceMatrix = new Matrix4f();


    public static void renderBlock(BlockPos pos, RenderWorldEvent event, float delta) {
        Box box = new Box(
                pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1
        );

        float cameraX = (float) mc.gameRenderer.getCamera().getPos().x;
        float cameraY = (float) mc.gameRenderer.getCamera().getPos().y;
        float cameraZ = (float) mc.gameRenderer.getCamera().getPos().z;

        float minX = (float) box.minX - cameraX;
        float maxX = (float) box.maxX - cameraX;
        float minY = (float) box.minY - cameraY;
        float maxY = (float) box.maxY - cameraY;
        float minZ = (float) box.minZ - cameraZ;
        float maxZ = (float) box.maxZ - cameraZ;

        BufferBuilder vb = getBufferBuilder(event.matrixStack, VertexFormat.DrawMode.QUADS);
        preRender();

        MatrixStack matrixStack = event.matrixStack;
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();

        int r = 255, g = 255, b = 255, a = 75;

        vb.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);

        vb.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);

        vb.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);
        vb.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);

        vb.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);

        vb.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
        vb.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);

        vb.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);

        postRender(vb, matrixStack);
    }


    public static void renderBox(Entity e, RenderWorldEvent event, float delta) {
        float interpolatedX = (float) (e.lastRenderX + (e.getX() - e.lastRenderX) * delta - mc.gameRenderer.getCamera().getPos().x);
        float interpolatedY = (float) (e.lastRenderY + (e.getY() - e.lastRenderY) * delta - mc.gameRenderer.getCamera().getPos().y);
        float interpolatedZ = (float) (e.lastRenderZ + (e.getZ() - e.lastRenderZ) * delta - mc.gameRenderer.getCamera().getPos().z);

        Box box = e.getBoundingBox();

        float minX = (float) (box.minX - e.getX()) - 0.12f;
        float maxX = (float) (box.maxX - e.getX()) + 0.12f;
        float minY = (float) (box.minY - e.getY()) - 0.12f;
        float maxY = (float) (box.maxY - e.getY()) + 0.12f;
        float minZ = (float) (box.minZ - e.getZ()) - 0.12f;
        float maxZ = (float) (box.maxZ - e.getZ()) + 0.12f;

        BufferBuilder vb = getBufferBuilder(event.matrixStack, VertexFormat.DrawMode.QUADS);
        preRender();
        MatrixStack matrixStack = event.matrixStack;
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();

        matrixStack.translate(interpolatedX, interpolatedY, interpolatedZ);

        int r = 255, g = 255, b = 255, a = 75;

        vb.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);

        vb.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);

        vb.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);
        vb.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);

        vb.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);

        vb.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
        vb.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);

        vb.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);


        postRender(vb, matrixStack);
    }


    public static void renderHealth(Entity e, RenderWorldEvent event, float currentHealth, float maxHealth, float targetHealthRatio, float delta) {
        float h = (currentHealth / maxHealth);
        int barHeight = (int) (74.0D * h);
        Color healthColor = h < 0.3D ? red : (h < 0.5D ? orange : (h < 0.7D ? yellow : green));

        float x = (float) (e.lastRenderX + (e.getX() - e.lastRenderX) * delta - mc.gameRenderer.getCamera().getPos().x);
        float y = (float) (e.lastRenderY + (e.getY() - e.lastRenderY) * delta - mc.gameRenderer.getCamera().getPos().y);
        float z = (float) (e.lastRenderZ + (e.getZ() - e.lastRenderZ) * delta - mc.gameRenderer.getCamera().getPos().z);

        BufferBuilder bufferBuilder = getBufferBuilder(event.matrixStack, VertexFormat.DrawMode.QUADS);

        preRender();

        MatrixStack matrixStack = event.matrixStack;

        matrixStack.translate(x, y - 0.2D, z);
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-mc.getCameraEntity().getYaw()));
        matrixStack.scale(0.03F, 0.03F, 0.03F);

        int barX = 21;
        int barWidth = 4;
        int fullBarHeight = 75;

        Matrix4f matrix = matrixStack.peek().getPositionMatrix();

        bufferBuilder.vertex(matrix, barX, -1, 0).color(black.getRGB());
        bufferBuilder.vertex(matrix, barX + barWidth, -1, 0).color(black.getRGB());
        bufferBuilder.vertex(matrix, barX + barWidth, fullBarHeight, 0).color(black.getRGB());
        bufferBuilder.vertex(matrix, barX, fullBarHeight, 0).color(black.getRGB());

        bufferBuilder.vertex(matrix, barX + 1, (float) barHeight, 0).color(gray.getRGB());
        bufferBuilder.vertex(matrix, barX + barWidth - 1, (float) barHeight, 0).color(gray.getRGB());
        bufferBuilder.vertex(matrix, barX + barWidth - 1, fullBarHeight - 1, 0).color(gray.getRGB());
        bufferBuilder.vertex(matrix, barX + 1, fullBarHeight - 1, 0).color(gray.getRGB());

        bufferBuilder.vertex(matrix, barX + 1, 0, 0).color(healthColor.getRGB());
        bufferBuilder.vertex(matrix, barX + barWidth - 1, 0, 0).color(healthColor.getRGB());
        bufferBuilder.vertex(matrix, barX + barWidth - 1, (float) barHeight, 0).color(healthColor.getRGB());
        bufferBuilder.vertex(matrix, barX + 1, (float) barHeight, 0).color(healthColor.getRGB());
        postRender(bufferBuilder, event.matrixStack);
    }

    public static void drawTextWithBackground(Matrix4f matrix, String text, float x, float y, int color, RenderTickEvent event) {
        int textWidth = mc.textRenderer.getWidth(text);
        int textHeight = mc.textRenderer.fontHeight;

        float padding = 1.0f;

        float adjustedX = x + padding;
        float adjustedY = y + padding;

        float left = x - padding;
        float top = y - padding;
        float right = x + textWidth + padding * 2;
        float bottom = y + textHeight + padding * 2;

        drawRect(event, left, top, right, bottom, 0x80000000);

        mc.textRenderer.draw(
                text,
                adjustedX,
                adjustedY,
                color,
                false,
                matrix,
                mc.getBufferBuilders().getEntityVertexConsumers(),
                TextRenderer.TextLayerType.NORMAL,
                0,
                255
        );
    }

    public static void drawRect(RenderTickEvent event, float left, float top, float right, float bottom, int color) {
        float f3 = (color >> 24 & 255) / 255.0F;
        float f = (color >> 16 & 255) / 255.0F;
        float f1 = (color >> 8 & 255) / 255.0F;
        float f2 = (color & 255) / 255.0F;
        MatrixStack matrixStack = event.context.getMatrices();

        BufferBuilder bufferBuilder = getBufferBuilder(matrixStack, VertexFormat.DrawMode.QUADS);

        preRender();

        Matrix4f matrix = matrixStack.peek().getPositionMatrix();

        bufferBuilder.vertex(matrix, left, bottom,0.0f).color(f, f1, f2, f3);
        bufferBuilder.vertex(matrix, right, bottom,0.0f).color(f, f1, f2, f3);
        bufferBuilder.vertex(matrix, right, top,0.0f).color(f, f1, f2, f3);
        bufferBuilder.vertex(matrix, left, top,0.0f).color(f, f1, f2, f3);
        bufferBuilder.vertex(matrix, left, bottom,0.0f).color(f, f1, f2, f3);

        postRender(bufferBuilder, matrixStack);
    }


    public static void drawRectOutline(RenderTickEvent event, float left, float top, float right, float bottom, int color) {
        float f3 = (color >> 24 & 255) / 255.0F;
        float f = (color >> 16 & 255) / 255.0F;
        float f1 = (color >> 8 & 255) / 255.0F;
        float f2 = (color & 255) / 255.0F;
        MatrixStack matrixStack = event.context.getMatrices();

        BufferBuilder bufferBuilder = getBufferBuilder(matrixStack, VertexFormat.DrawMode.DEBUG_LINE_STRIP);

        preRender();

        Matrix4f matrix = matrixStack.peek().getPositionMatrix();

        bufferBuilder.vertex(matrix, left, bottom,0.0f).color(f, f1, f2, f3);
        bufferBuilder.vertex(matrix, right, bottom,0.0f).color(f, f1, f2, f3);
        bufferBuilder.vertex(matrix, right, top,0.0f).color(f, f1, f2, f3);
        bufferBuilder.vertex(matrix, left, top,0.0f).color(f, f1, f2, f3);
        bufferBuilder.vertex(matrix, left, bottom,0.0f).color(f, f1, f2, f3);

        postRender(bufferBuilder, matrixStack);
    }




    public static void drawRect(DrawContext event, float left, float top, float right, float bottom, int color) {
        float f3 = (color >> 24 & 255) / 255.0F;
        float f = (color >> 16 & 255) / 255.0F;
        float f1 = (color >> 8 & 255) / 255.0F;
        float f2 = (color & 255) / 255.0F;
        MatrixStack matrixStack = event.getMatrices();

        BufferBuilder bufferBuilder = getBufferBuilder(matrixStack, VertexFormat.DrawMode.QUADS);
        preRender();

        Matrix4f matrix = matrixStack.peek().getPositionMatrix();

        bufferBuilder.vertex(matrix, left, bottom,0.0f).color(f, f1, f2, f3);
        bufferBuilder.vertex(matrix, right, bottom,0.0f).color(f, f1, f2, f3);
        bufferBuilder.vertex(matrix, right, top,0.0f).color(f, f1, f2, f3);
        bufferBuilder.vertex(matrix, left, top,0.0f).color(f, f1, f2, f3);

        postRender(bufferBuilder, matrixStack);
    }

    public static Vec3d worldToScreen(Vec3d pos) {
        Camera camera = mc.getEntityRenderDispatcher().camera;
        int displayHeight = mc.getWindow().getHeight();
        Vector3f target = new Vector3f();

        double deltaX = pos.x - camera.getPos().x;
        double deltaY = pos.y - camera.getPos().y;
        double deltaZ = pos.z - camera.getPos().z;


        Vector4f transformedCoordinates = new Vector4f((float) deltaX, (float) deltaY, (float) deltaZ, 1.0f).mul(lastWorldSpaceMatrix);

        Matrix4f matrixProj = new Matrix4f(lastProjMat);
        Matrix4f matrixModel = new Matrix4f(lastModMat);

        matrixProj.mul(matrixModel).project(transformedCoordinates.x(), transformedCoordinates.y(), transformedCoordinates.z(), lastViewport, target);

        return new Vec3d(target.x / mc.getWindow().getScaleFactor(), (displayHeight - target.y) / mc.getWindow().getScaleFactor(), target.z);
    }

    public static void drawCircle(DrawContext event, float centerX, float centerY, float radius, int color) {
        float alpha = (color >> 24 & 255) / 255.0F;
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;

        MatrixStack matrixStack = event.getMatrices();
        BufferBuilder bufferBuilder = getBufferBuilder(matrixStack, VertexFormat.DrawMode.TRIANGLE_FAN);
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();

        preRender();

        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glEnable(GL11.GL_BLEND);

        //bufferBuilder.vertex(matrix, centerX, centerY, 0.0f).color(red, green, blue, alpha);

        final float step = 0.05f;
        final int segments = (int) (2 * Math.PI / step);

        for (int i = 0; i < segments; i++) {
            float angle = i * step;
            float x = centerX + MathHelper.cos(angle) * radius;
            float y = centerY + MathHelper.sin(angle) * radius;

            bufferBuilder.vertex(matrix, x, y, 0.0f).color(red, green, blue, alpha);
            bufferBuilder.vertex(matrix, x, y, 0.0f).color(red, green, blue, alpha);
        }

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_BLEND);

        postRender(bufferBuilder, matrixStack);
    }




    public static int interpolateColor(int startColor, int endColor, float ratio) {
        int sr = (startColor >> 16) & 0xFF, sg = (startColor >> 8) & 0xFF, sb = startColor & 0xFF;
        int er = (endColor >> 16) & 0xFF, eg = (endColor >> 8) & 0xFF, eb = endColor & 0xFF;
        int r = (int) (sr + ratio * (er - sr)), g = (int) (sg + ratio * (eg - sg)), b = (int) (sb + ratio * (eb - sb));
        return (0xFF << 24) | (r << 16) | (g << 8) | b;
    }

    public static void preRender() {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
    }

    public static BufferBuilder getBufferBuilder(MatrixStack matrixStack, VertexFormat.DrawMode drawMode) {
        matrixStack.push();

        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        return tessellator.begin(drawMode, VertexFormats.POSITION_COLOR);
    }

    public static void postRender(BufferBuilder bufferBuilder, MatrixStack matrixStack) {
        matrixStack.pop();

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

        RenderSystem.setShaderColor(1,1,1,1);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }
}
