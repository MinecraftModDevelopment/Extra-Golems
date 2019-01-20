package com.golems.entity;

import com.golems.util.GolemConfigSet;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public final class EntityMagmaGolem extends GolemBase {

    public static final String ALLOW_FIRE_SPECIAL = "Allow Special: Burn Enemies";
    public static final String ALLOW_LAVA_SPECIAL = "Allow Special: Melt Cobblestone";
    public static final String MELT_DELAY = "Melting Delay";

    /**
     * Golem should stand in one spot for number of ticks before affecting the block below it.
     */
    private int ticksStandingStill;

    public EntityMagmaGolem(final World world) {
        super(world);
        this.setImmuneToFire(true);
        this.ticksStandingStill = 0;
        this.stepHeight = 1.0F;
        this.tasks.addTask(0, this.swimmingAI);
        this.setLootTableLoc("golem_magma");
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.28D);
    }

    @Override
    protected ResourceLocation applyTexture() {
        return makeGolemTexture("magma");
    }

    /**
     * Attack by lighting on fire as well.
     */
    @Override
    public boolean attackEntityAsMob(final Entity entity) {
        if (super.attackEntityAsMob(entity)) {
            GolemConfigSet cfg = getConfig(this);
            if (cfg.getBoolean(ALLOW_FIRE_SPECIAL)) {
                entity.setFire(2 + rand.nextInt(5));
            }
            return true;
        }
        return false;
    }

    /**
     * Called frequently so the entity can update its state every tick as required. For example,
     * zombies and skeletons use this to react to sunlight and start to burn.
     */
    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        GolemConfigSet cfg = getConfig(this);
        if (cfg.getBoolean(ALLOW_LAVA_SPECIAL)) {
            final int x = MathHelper.floor(this.posX);
            final int y = MathHelper.floor(this.posY - 0.20000000298023224D);
            final int z = MathHelper.floor(this.posZ);
            final BlockPos below = new BlockPos(x, y, z);
            final Block b1 = this.world.getBlockState(below).getBlock();
            // debug:
            // System.out.println("below=" + below + "; lastPos = " + new
            // BlockPos(MathHelper.floor_double(this.lastTickPosX), this.lastTickPosY,
            // MathHelper.floor_double(this.lastTickPosZ)));
            // System.out.println("block on= " + b1.getUnlocalizedName() + "; ticksStandingStill=" +
            // ticksStandingStill);

            if (x == MathHelper.floor(this.lastTickPosX)
                    && z == MathHelper.floor(this.lastTickPosZ)) {
                if (++this.ticksStandingStill >= cfg.getInt(MELT_DELAY)
                        && b1 == Blocks.COBBLESTONE && rand.nextInt(16) == 0) {
                    this.world.setBlockState(below, Blocks.LAVA.getDefaultState(), 3);
                    this.ticksStandingStill = 0;
                }
            } else {
                this.ticksStandingStill = 0;
            }
        }
    }

    @Override
    public SoundEvent getGolemSound() {
        return SoundEvents.BLOCK_STONE_STEP;
    }

    @Override
    public List<String> addSpecialDesc(final List<String> list) {
        GolemConfigSet cfg = getConfig(this);
        if (cfg.getBoolean(EntityMagmaGolem.ALLOW_LAVA_SPECIAL))
            list.add(TextFormatting.RED + trans("entitytip.slowly_melts", trans("tile.stonebrick.name")));
        if (cfg.getBoolean(EntityMagmaGolem.ALLOW_FIRE_SPECIAL))
            list.add(TextFormatting.RED + trans("entitytip.lights_mobs_on_fire"));
        return list;
    }
}
