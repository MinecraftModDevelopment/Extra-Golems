package com.mcmoddev.golems.entity;

import com.mcmoddev.golems.entity.base.GolemBase;

import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

public final class GenericGolem extends GolemBase {

  public GenericGolem(final EntityType<? extends GolemBase> entityType, final World world) {
    super(entityType, world);
  }
}
