package com.mcmoddev.golems.entity;

import com.mcmoddev.golems.entity.base.GolemBase;
import com.mcmoddev.golems.main.ExtraGolems;
import com.mcmoddev.golems.util.GolemNames;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public final class EntityPrismarineGolem extends GolemBase {

	public EntityPrismarineGolem(final World world) {
		super(EntityPrismarineGolem.class, world);
		this.setLootTableLoc(GolemNames.PRISMARINE_GOLEM);
		this.getAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.2D);
	}

	@Override
	protected ResourceLocation applyTexture() {
		return makeTexture(ExtraGolems.MODID, GolemNames.PRISMARINE_GOLEM);
	}

	@Override
	protected float getWaterSlowDown() {
		return 1.1F;
	}

	@Override
	public SoundEvent getGolemSound() {
		return SoundEvents.BLOCK_STONE_STEP;
	}
}
