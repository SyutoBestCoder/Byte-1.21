package com.syuto.bytes.module.impl.misc;

import com.syuto.bytes.Byte;
import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.PreMotionEvent;
import com.syuto.bytes.eventbus.impl.PreUpdateEvent;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.ModuleManager;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.module.impl.combat.Velocity;
import com.syuto.bytes.utils.impl.client.ChatUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.message.SentMessage;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;

import static com.syuto.bytes.Byte.mc;


public class Test extends Module {

    private Direction facing;
    private BlockHitResult blockHitResult;
    private BlockPos targetBlock, blok;
    private int[] blockInfo;
    private float[] rots;
    private int blockSlot = -1;
    private int airTicks = 0;


    public Test() {
        super("Test", "Module with absolutely 0 purpose.", Category.OTHER);
    }



    private String[] blacklistedBlocks = new String[]{
            "sapling", "torch", "chest", "anvil", "flower", "rail", "pumpkin", "tnt",
            "tripwire"
    };

    @Override
    public void onEnable() {
        super.onEnable();
        blockSlot = getBlockSlot();
    }


    @EventHandler
    public void onPreUpdate(PreUpdateEvent e) {
        blockSlot = getBlockSlot();

        if (blockSlot != -1) {
            mc.player.getInventory().setSelectedSlot(blockSlot);
        }


        if (mc.player.getInventory().getMainHandStack() != null) {
            place();
            place();
            place();

        }
    }

    @EventHandler
    public void onPreMotion(PreMotionEvent event) {
        if (mc.player.isOnGround()) {
            airTicks = 0;
        } else {
            airTicks++;
        }

        if (targetBlock != null) {
            rots = getBlockRotations(targetBlock, facing);
        }

        if (rots != null) {
            float yaw = Math.clamp(rots[0] % 360.0F, -360.0F, 360.0F);
            float pitch = Math.clamp(rots[1] % 360.0F, -90.0F, 90.0F);

            event.yaw = yaw;
            event.pitch = pitch;
            if (facing == Direction.UP) {
                event.yaw = yaw;
                event.pitch = pitch;
            }

            //float spin = -(System.currentTimeMillis() / 2 % 360);
            mc.player.bodyYaw = yaw;
            mc.player.headYaw = yaw;
        }


        Vec3d motion = mc.player.getVelocity();
        int simpleY = (int) Math.round((event.posY % 1.0D) * 100.0D);

        ChatUtils.print(simpleY + " " + airTicks);
        if (mc.options.jumpKey.isPressed()) {
            switch(simpleY) {
                case 0 -> {
                    mc.player.setVelocity(motion.x, 0.42f, motion.z);
                    if (airTicks == 6) {
                        event.posY += 1E-14;
                        mc.player.setVelocity(motion.x, 0, motion.z);
                        airTicks = -1;
                        ChatUtils.print("bye " + airTicks);
                    }
                }
                case 42 -> {
                    mc.player.setVelocity(motion.x, 0.33f, motion.z);
                }
                case 75 -> {
                    mc.player.setVelocity(motion.x, 0.25f, motion.z);
                }

            }
        }

        targetBlock = null;
    }



    public void place() {
        blockInfo = findBlocks();

        if (blockInfo == null) {
            return;
        }

        int blockX = blockInfo[0];
        int blockY = blockInfo[1];
        int blockZ = blockInfo[2];
        int blockFacing = blockInfo[3];

        facing = Direction.byId(blockFacing);

        //if (facing == Direction.UP) return;


        targetBlock = new BlockPos(blockX, blockY, blockZ);

        BlockPos blockPos = targetBlock;

        double hitX = blockX + 0.5 + getCoord(blockFacing, "x") * 0.5;
        double hitY = blockY + 0.5 + getCoord(blockFacing, "y") * 0.5;
        double hitZ = blockZ + 0.5 + getCoord(blockFacing, "z") * 0.5;

        //ChatUtils.print("Face: " + facing.asString() + " HitY: " + hitY);

        //BlockPos.findClosest()
        Vec3d hitVec = new Vec3d(hitX, hitY, hitZ);

        blockHitResult = new BlockHitResult(hitVec, facing, blockPos, false);
        MinecraftClient client = MinecraftClient.getInstance();
        client.interactionManager.interactBlock(client.player, client.player.getActiveHand(), blockHitResult);
        client.player.swingHand(mc.player.getActiveHand());

    }

    double getCoord(int facing, String axis) {
        return switch (axis) {
            case "x" -> (facing == 4) ? -0.5 : (facing == 5) ? 0.5 : 0;
            case "y" -> (facing == 0) ? -0.5 : (facing == 1) ? 0.5 : 0;
            case "z" -> (facing == 2) ? -0.5 : (facing == 3) ? 0.5 : 0;
            default -> 0;
        };
    }




    public static int[] findBlocks() {
        MinecraftClient client = MinecraftClient.getInstance();
        Entity player = client.player;
        World world = client.world;

        int[] enumFacings = new int[]{0, 1, 2, 3, 4, 5};

        Vec3d playerPos = player.getPos();
        int x = (int) Math.floor(playerPos.x);
        int y = (int) Math.floor(playerPos.y);
        int z = (int) Math.floor(playerPos.z);

        BlockPos belowPlayer = new BlockPos(x, y - 1, z);
        if (world.getBlockState(belowPlayer).isAir()) {
            for (int enumFacing : enumFacings) {
                if (enumFacing != 1) {
                    BlockPos offsetPos = offsetPosition(belowPlayer, enumFacing);
                    if (!world.getBlockState(offsetPos).isAir()) {
                        return new int[]{offsetPos.getX(), offsetPos.getY(), offsetPos.getZ(), Direction.byId(enumFacing).getOpposite().getId()};
                    }
                }
            }

            for (int offsetY = -1; offsetY <= 2; offsetY++) {
                BlockPos belowPlayerOffset = belowPlayer.offset(Direction.Axis.Y, -offsetY);
                for (int enumFacing : enumFacings) {
                    if (enumFacing != 1) {
                        BlockPos offsetPos = offsetPosition(belowPlayerOffset, enumFacing);
                        if (world.getBlockState(offsetPos).isAir()) {
                            for (int enumFacing2 : enumFacings) {
                                if (enumFacing2 != 1) {
                                    BlockPos offsetPos2 = offsetPosition(offsetPos, enumFacing2);
                                    if (!world.getBlockState(offsetPos2).isAir()) {
                                        return new int[]{offsetPos2.getX(), offsetPos2.getY(), offsetPos2.getZ(), Direction.byId(enumFacing2).getOpposite().getId()};
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    public static BlockPos offsetPosition(BlockPos p, int facing) {
        switch (facing) {
            case 0: return new BlockPos(p.getX(), p.getY() - 1, p.getZ());
            case 1: return new BlockPos(p.getX(), p.getY() + 1, p.getZ());
            case 2: return new BlockPos(p.getX(), p.getY(), p.getZ() - 1);
            case 3: return new BlockPos(p.getX(), p.getY(), p.getZ() + 1);
            case 4: return new BlockPos(p.getX() - 1, p.getY(), p.getZ());
            case 5: return new BlockPos(p.getX() + 1, p.getY(), p.getZ());
            default: return new BlockPos(p.getX(), p.getY(), p.getZ());
        }
    }


    float getDirection() {
        float yaw = mc.player.getYaw();
        float forward = (mc.player.forwardSpeed > 0 ? 0.5F : mc.player.forwardSpeed < 0 ? -0.5F : 1);
        if (mc.player.forwardSpeed < 0) yaw += 180;
        if (mc.player.sidewaysSpeed > 0) yaw -= 90 * forward;
        if (mc.player.sidewaysSpeed < 0) yaw += 90 * forward;
        return yaw;
    }

    public float[] getBlockRotations(BlockPos blockPos, Direction facing) {
        double d = blockPos.getX() + 0.5 - mc.player.getX() + facing.getOffsetX() * 0.5;
        double d2 = blockPos.getZ() + 0.5 - mc.player.getZ() + facing.getOffsetZ() * 0.5;
        double d3 = mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()) - blockPos.getY() - facing.getOffsetY() * 0.5;
        double d4 = Math.sqrt(d * d + d2 * d2);
        float yaw = (float) (Math.atan2(d2, d) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float) (Math.atan2(d3, d4) * 180.0 / Math.PI);
        if (facing == Direction.UP || facing == Direction.DOWN) {
            yaw = getDirection() + 180;
        }
        yaw = MathHelper.wrapDegrees(yaw);
        return new float[]{yaw, pitch};
    }


    private int getBlockSlot() {
        int selectedSlot = -1;
        int largestStackSize = 0;

        for (int slot = 0; slot < 9; slot++) {
            ItemStack itemStack = mc.player.getInventory().getStack(slot);

            if (!itemStack.isEmpty() && itemStack.getItem() instanceof BlockItem) {
                int stackSize = itemStack.getCount();
                if (stackSize > largestStackSize && canBePlaced(itemStack)) {
                    largestStackSize = stackSize;
                    selectedSlot = slot;
                }
            }
        }
        return selectedSlot;
    }

    private boolean canBePlaced(ItemStack itemStack) {
        BlockItem blockItem = (BlockItem) itemStack.getItem();
        String blockName = blockItem.getBlock().getTranslationKey(); // Get the block's translation key

        //ChatUtils.print(blockName);
        for (String blacklisted : blacklistedBlocks) {
            if (blockName.contains(blacklisted)) {
                //ChatUtils.print(blockName);
                return false;
            }
        }
        return true;
    }


}
