package com.syuto.bytes.mixin;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Entity.class)
public class EntityMixin {
    /**
     * @author scale
     * @reason lol xd!!!
     */
    @Overwrite
    public boolean isGlowing() {
        return true;
    }
}
