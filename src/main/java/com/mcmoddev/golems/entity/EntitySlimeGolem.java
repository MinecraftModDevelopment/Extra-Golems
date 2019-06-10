package com.mcmoddev.golems.entity;

import com.mcmoddev.golems.entity.base.GolemBase;
import com.mcmoddev.golems.main.ExtraGolems;
import com.mcmoddev.golems.util.GolemNames;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public final class EntitySlimeGolem extends GolemBase {

	public static final String ALLOW_SPECIAL = "Allow Special: Extra Knockback";
	public static final String ALLOW_SPLITTING = "Allow Special: Split";
	public static final String KNOCKBACK = "Knockback Factor";
	
	public EntitySlimeGolem(final World world) {
		this(world, false);
	}
	
	public EntitySlimeGolem(final World world, final boolean isBaby) {
		super(EntitySlimeGolem.class, world);
		this.setChild(isBaby);
		this.setCanSwim(true);
	}

	@Override
	protected ResourceLocation applyTexture() {
		return makeTexture(ExtraGolems.MODID, GolemNames.SLIME_GOLEM);
	}

	@Override
	public boolean attackEntityAsMob(final Entity entity) {
		if (super.attackEntityAsMob(entity)) {
			// knocks back the entity it's attacking (if it's adult and not attacking a slime)
			if (this.getConfigBool(ALLOW_SPECIAL) && !(entity instanceof EntitySlime) && !this.isChild()) {
				knockbackTarget(entity, this.getConfigDouble(KNOCKBACK));
			}
			return true;
		}
		return false;
	}

	@Override
	protected void damageEntity(final DamageSource source, final float amount) {
		if (!this.isInvulnerableTo(source)) {
			super.damageEntity(source, amount);
			// knocks back the entity that is attacking it
			if (!this.isChild() && source.getImmediateSource() != null && this.getConfigBool(ALLOW_SPECIAL)) {
				knockbackTarget(source.getImmediateSource(), this.getConfigDouble(KNOCKBACK));
			}
		}
	}

	/**
	 * Adds extra velocity to the golem's knockback attack. 
	 **/
	protected void knockbackTarget(final Entity entity, final double knockbackFactor) {
		final double dX = Math.signum(entity.posX - this.posX) * knockbackFactor;
		final double dZ = Math.signum(entity.posZ - this.posZ) * knockbackFactor;
		entity.addVelocity(dX, knockbackFactor / 4, dZ);
		entity.velocityChanged = true;
	}
	
	@Override
	public void remove() {
		if(!this.world.isRemote && !this.isChild() && this.getConfigBool(ALLOW_SPLITTING)) {
			GolemBase slime1 = new EntitySlimeGolem(this.world, true);
			GolemBase slime2 = new EntitySlimeGolem(this.world, true);
			// copy attack target info
			if(this.getAttackTarget() != null) {
				slime1.setAttackTarget(this.getAttackTarget());
				slime2.setAttackTarget(this.getAttackTarget());
			}
			// set location
			slime1.setLocationAndAngles(this.posX + rand.nextDouble() - 0.5D, this.posY, 
					 this.posZ + rand.nextDouble() - 0.5D, this.rotationYaw + rand.nextInt(20) - 10, 0);
			slime2.setLocationAndAngles(this.posX + rand.nextDouble() - 0.5D, this.posY, 
					 this.posZ + rand.nextDouble() - 0.5D, this.rotationYaw + rand.nextInt(20) - 10, 0);
			// spawn the entities
			this.getEntityWorld().spawnEntity(slime1);
			this.getEntityWorld().spawnEntity(slime2);
		}

		super.remove();
	}
	
	@Override
	public void notifyDataManagerChange(DataParameter<?> key) {
		super.notifyDataManagerChange(key);
		if(BABY.equals(key)) {
			if(this.isChild()) {
				this.setSize(0.7F, 1.45F);
				this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(container.getHealth() / 3);
				this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(container.getAttack() * 0.6F);
				this.getAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.0D);
			} else {
				this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(container.getHealth());
				this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(container.getAttack());
				this.getAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.35D);
			}
		}
	}

	@Override
	public SoundEvent getGolemSound() {
		return SoundEvents.BLOCK_SLIME_BLOCK_STEP;
	}
}