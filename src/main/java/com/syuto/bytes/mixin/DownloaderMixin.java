package com.syuto.bytes.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.syuto.bytes.Byte;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Downloader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.nio.file.Path;
import java.util.UUID;

@Mixin(Downloader.class)
public class DownloaderMixin {

    @Shadow
    @Final
    private Path directory;

    //pasted from liquid bounce Credit: @1zun4
    @ModifyExpressionValue(
            method = "method_55485",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/nio/file/Path;resolve(Ljava/lang/String;)Ljava/nio/file/Path;"
            )
    )
    private Path hookResolve(Path original, @Local(argsOnly = true) UUID id) {
        var accountId = MinecraftClient.getInstance().getSession().getUuidOrNull();
        if (accountId == null) {
            Byte.LOGGER.warn("Failed to change download directory, because account id is null.");
            return original;
        }
        return directory.resolve(accountId.toString()).resolve(String.valueOf(id));
    }
}
