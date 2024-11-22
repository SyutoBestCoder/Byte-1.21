package com.syuto.bytes.mixin;

import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.syuto.bytes.Byte;
import com.syuto.bytes.eventbus.impl.RenderWorldEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.Pool;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class RenderWorldMixin {


    @Shadow private int ticks;

    @Shadow @Final private LightmapTextureManager lightmapTextureManager;

    @Shadow @Final private MinecraftClient client;

    @Shadow public abstract void updateCrosshairTarget(float tickDelta);

    @Shadow protected abstract boolean shouldRenderBlockOutline();

    @Shadow @Final private Camera camera;

    @Shadow private float viewDistance;

    @Shadow protected abstract float getFov(Camera camera, float tickDelta, boolean changingFov);

    @Shadow public abstract Matrix4f getBasicProjectionMatrix(float fovDegrees);

    @Shadow protected abstract void tiltViewWhenHurt(MatrixStack matrices, float tickDelta);

    @Shadow protected abstract void bobView(MatrixStack matrices, float tickDelta);

    @Shadow private boolean renderHand;

    @Shadow protected abstract void renderHand(Camera camera, float tickDelta, Matrix4f matrix4f);

    @Shadow @Final private Pool pool;

    /**
     * @author scale
     * @reason sorry for overwrite!
     */
    @Overwrite
    public void renderWorld(RenderTickCounter renderTickCounter) {
        float f = renderTickCounter.getTickDelta(true);
        this.lightmapTextureManager.update(f);
        if (this.client.getCameraEntity() == null) {
            this.client.setCameraEntity(this.client.player);
        }

        this.updateCrosshairTarget(f);
        Profiler profiler = Profilers.get();
        profiler.push("center");
        boolean bl = this.shouldRenderBlockOutline();
        profiler.swap("camera");
        Camera camera = this.camera;
        Entity entity = this.client.getCameraEntity() == null ? this.client.player : this.client.getCameraEntity();
        float g = this.client.world.getTickManager().shouldSkipTick((Entity)entity) ? 1.0F : f;
        camera.update(this.client.world, (Entity)entity, !this.client.options.getPerspective().isFirstPerson(), this.client.options.getPerspective().isFrontView(), g);
        this.viewDistance = (float)(this.client.options.getClampedViewDistance() * 16);
        float h = this.getFov(camera, f, true);
        Matrix4f matrix4f = this.getBasicProjectionMatrix(h);
        MatrixStack matrixStack = new MatrixStack();
        this.tiltViewWhenHurt(matrixStack, camera.getLastTickDelta());
        if ((Boolean)this.client.options.getBobView().getValue()) {
            this.bobView(matrixStack, camera.getLastTickDelta());
        }

        matrix4f.mul(matrixStack.peek().getPositionMatrix());
        float i = ((Double)this.client.options.getDistortionEffectScale().getValue()).floatValue();
        float j = MathHelper.lerp(f, this.client.player.prevNauseaIntensity, this.client.player.nauseaIntensity) * i * i;
        if (j > 0.0F) {
            int k = this.client.player.hasStatusEffect(StatusEffects.NAUSEA) ? 7 : 20;
            float l = 5.0F / (j * j + 5.0F) - j * 0.04F;
            l *= l;
            Vector3f vector3f = new Vector3f(0.0F, MathHelper.SQUARE_ROOT_OF_TWO / 2.0F, MathHelper.SQUARE_ROOT_OF_TWO / 2.0F);
            float m = ((float)this.ticks + f) * (float)k * 0.017453292F;
            matrix4f.rotate(m, vector3f);
            matrix4f.scale(1.0F / l, 1.0F, 1.0F);
            matrix4f.rotate(-m, vector3f);
        }

        float n = Math.max(h, (float)(Integer)this.client.options.getFov().getValue());
        Matrix4f matrix4f2 = this.getBasicProjectionMatrix(n);
        RenderSystem.setProjectionMatrix(matrix4f, ProjectionType.PERSPECTIVE);
        Quaternionf quaternionf = camera.getRotation().conjugate(new Quaternionf());
        Matrix4f matrix4f3 = (new Matrix4f()).rotation(quaternionf);
        this.client.worldRenderer.setupFrustum(camera.getPos(), matrix4f3, matrix4f2);
        this.client.getFramebuffer().beginWrite(true);
        this.client.worldRenderer.render(this.pool, renderTickCounter, bl, camera, this.client.gameRenderer, this.lightmapTextureManager, matrix4f3, matrix4f);
        profiler.swap("hand");
        if (this.renderHand) {
            RenderSystem.clear(256);

            // RENDER WORLD EVENT

            // code the renderHand event uses to get a matrix stack
            MatrixStack matrixStack2 = new MatrixStack();
            matrixStack2.multiplyPositionMatrix(matrix4f3);
            Byte.INSTANCE.eventBus.post(new RenderWorldEvent(matrixStack2));


            // ---------------------------------------------------------------

            this.renderHand(camera, f, matrix4f3);
        }

        profiler.pop();
    }


}
