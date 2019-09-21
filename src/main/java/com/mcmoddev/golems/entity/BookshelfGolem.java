package com.mcmoddev.golems.entity;

import com.mcmoddev.golems.entity.base.GolemBase;

import net.minecraft.entity.EntityType;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;

public final class BookshelfGolem extends GolemBase {

	public static final String ALLOW_SPECIAL = "Allow Special: Potion Effects";
	private static final Effect[] goodEffects = { Effects.FIRE_RESISTANCE, Effects.REGENERATION,
			Effects.STRENGTH, Effects.ABSORPTION, Effects.LUCK, Effects.INSTANT_HEALTH,
			Effects.RESISTANCE, Effects.INVISIBILITY, Effects.SPEED,
			Effects.JUMP_BOOST };
	private boolean allowSpecial;

	public BookshelfGolem(final EntityType<? extends GolemBase> entityType, final World world) {
		super(entityType, world);
		this.allowSpecial = this.getConfigBool(ALLOW_SPECIAL);
	}

	/**
	 * Called frequently so the entity can update its state every tick as required. For example,
	 * zombies and skeletons use this to react to sunlight and start to burn.
	 */

	@Override
	public void livingTick() {
		super.livingTick();
		if (allowSpecial && this.getActivePotionEffects().isEmpty()
				&& rand.nextInt(40) == 0) {
			final Effect potion = goodEffects[rand.nextInt(goodEffects.length)];
			final int len = potion.isInstant() ? 1 : 200 + 100 * (1 + rand.nextInt(5));
			this.addPotionEffect(new EffectInstance(potion, len, rand.nextInt(2)));
		}
	}
}
