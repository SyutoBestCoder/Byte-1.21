package com.syuto.bytes.eventbus.impl;

import com.syuto.bytes.eventbus.Event;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.Entity;

public class AttackEntityEvent implements Event {
    public enum Mode { Pre, Post }

    @Getter
    private final Entity target;

    @Getter
    private final Mode mode;

    @Getter
    @Setter
    private boolean cancelled;

    public AttackEntityEvent(Entity target) {
        this(target, Mode.Pre);
    }

    public AttackEntityEvent(Entity target, Mode mode) {
        this.target = target;
        this.mode = mode;
    }
}
