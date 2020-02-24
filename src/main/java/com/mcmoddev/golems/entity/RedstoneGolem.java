package com.mcmoddev.golems.entity;

import com.mcmoddev.golems.blocks.BlockUtilityPower;
import com.mcmoddev.golems.entity.ai.PlaceUtilityBlockGoal;
import com.mcmoddev.golems.entity.base.GolemBase;
import com.mcmoddev.golems.main.GolemItems;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public final class RedstoneGolem extends GolemBase {

  public static final String ALLOW_SPECIAL = "Allow Special: Redstone Power";

  public RedstoneGolem(final EntityType<? extends GolemBase> entityType, final World world) {
    super(entityType, world);
    final BlockState state = GolemItems.UTILITY_POWER.getDefaultState().with(BlockUtilityPower.POWER_LEVEL, 15);
    final int freq = BlockUtilityPower.UPDATE_TICKS;
    final boolean allow = this.getConfigBool(ALLOW_SPECIAL);
    this.goalSelector.addGoal(9, new PlaceUtilityBlockGoal(this, state, freq, allow));
  }

  @Override
  public boolean isProvidingPower() {
    return true;
  }

  @Override
  @OnlyIn(Dist.CLIENT)
  public int getBrightnessForRender() {
    return super.getBrightnessForRender() + 64;
  }
}
