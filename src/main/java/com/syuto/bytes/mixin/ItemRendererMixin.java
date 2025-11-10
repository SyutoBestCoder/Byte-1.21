package com.syuto.bytes.mixin;

import com.syuto.bytes.module.ModuleManager;
import com.syuto.bytes.module.impl.render.Animations;
import com.syuto.bytes.utils.impl.client.ChatUtils;
import com.syuto.bytes.utils.impl.render.AnimationUtils;
import com.syuto.bytes.utils.impl.render.RenderUtils;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.consume.UseAction;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.syuto.bytes.Byte.mc;

@Mixin(HeldItemRenderer.class)
public abstract class ItemRendererMixin {
    //i stole most of these injects from liquidbounce

    @Shadow
    void renderFirstPersonItem(AbstractClientPlayerEntity player, float tickDelta, float pitch,
                               Hand hand, float swingProgress, ItemStack item, float equipProgress,
                               MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light)
    {}

    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"), cancellable = true)
    private void hideShield(AbstractClientPlayerEntity player, float tickDelta, float pitch,
                            Hand hand, float swingProgress, ItemStack item, float equipProgress,
                            MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                            CallbackInfo ci) {
        if (hand == Hand.OFF_HAND && AnimationUtils.isBlocking()) {
            ci.cancel();
        }

        if (hand == Hand.MAIN_HAND && AnimationUtils.getSpoofedItem() != null && AnimationUtils.getSpoofedItem() != item) {
            ItemStack spoofed = AnimationUtils.getSpoofedItem();
            ci.cancel();

            this.renderFirstPersonItem(
                    player,
                    tickDelta,
                    pitch,
                    hand,
                    swingProgress,
                    spoofed,
                    equipProgress,
                    matrices,
                    vertexConsumers,
                    light
            );

        }
    }


    @Redirect(
            method = "renderFirstPersonItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;isUsingItem()Z",
                    ordinal = 1
            )
    )
    private boolean hookIsUseItem(AbstractClientPlayerEntity instance) {
        var item = instance.getMainHandStack().getItem();
        if (AnimationUtils.isBlocking() && item instanceof SwordItem) { //work
            //ChatUtils.print("Blocking returning true");
            return true;
        }
        return instance.isUsingItem();
    }

    @Redirect(method = "renderFirstPersonItem", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;getActiveHand()Lnet/minecraft/util/Hand;",
            ordinal = 1
    ))
    private Hand hookActiveHand(AbstractClientPlayerEntity instance) {
        var itemStack = instance.getMainHandStack();

        if (itemStack.isIn(ItemTags.SWORDS) && AnimationUtils.isBlocking()) {
            return Hand.MAIN_HAND;
        }

        return instance.getActiveHand();
    }

    @Redirect(method = "renderFirstPersonItem", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;getItemUseTimeLeft()I",
            ordinal = 2
    ))
    private int hookItemUseItem(AbstractClientPlayerEntity instance) {
        var item = instance.getMainHandStack().getItem();
        if (AnimationUtils.getSpoofedItem() != null || (AnimationUtils.isBlocking() & item instanceof SwordItem) ) {
            //ChatUtils.print("Updated item use");
            return 7200;
        }

        return instance.getItemUseTimeLeft();
    }

    @ModifyArg(
            method = "renderFirstPersonItem",
            at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/render/item/HeldItemRenderer;applyEquipOffset(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/Arm;F)V",
                ordinal = 3
            ),
            index = 2)
    private float injectIgnoreBlocking(float equipProgress) {
        if (AnimationUtils.isBlocking() || AnimationUtils.getSpoofedItem() != null) {
            return 0.0F;
        }

        return equipProgress;
    }

    @Redirect(
            method = "renderFirstPersonItem",
            at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/item/ItemStack;getUseAction()Lnet/minecraft/item/consume/UseAction;"
            )
    )
    private UseAction hookUseAction(ItemStack instance) {
        if (AnimationUtils.isBlocking() && instance.isIn(ItemTags.SWORDS)) { //works only while holding
            //ChatUtils.print("Sword found set use action");
            return UseAction.BLOCK;
        }
        return instance.getUseAction();
    }

    @Inject(
            method = "renderFirstPersonItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V",
                    ordinal = 4,
                    shift = At.Shift.BEFORE
            )
    )
    public void doAnimation(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        float f =  MathHelper.sin((float) (swingProgress * swingProgress * Math.PI));
        Animations animation = ModuleManager.getModule(Animations.class);

        if (animation != null && animation.isEnabled()) {
            AnimationUtils.animate(matrices, player.getHandSwingProgress(tickDelta), f);
        }
    }

    @Inject(method = "resetEquipProgress", at = @At("HEAD"), cancellable = true)
    private void injectIgnorePlace(Hand hand, CallbackInfo ci) {
        if (AnimationUtils.isBlocking() || AnimationUtils.getSpoofedItem() != null) {
            ci.cancel();
        }
    }
}
