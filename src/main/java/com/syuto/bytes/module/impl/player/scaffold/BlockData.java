package com.syuto.bytes.module.impl.player.scaffold;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

@Getter
@Setter
public class BlockData {
    BlockPos position;
    Vec3d hit;
    Direction facing;

    public BlockData(BlockPos position, Vec3d hit, Direction facing) {
        this.position = position;
        this.hit = hit;
        this.facing = facing;
    }

}
