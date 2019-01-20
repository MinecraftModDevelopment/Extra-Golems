package com.golems.entity;

import com.golems.main.ExtraGolems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;

public abstract class GolemMultiTextured extends GolemBase {

	/**
	 * The DataParameter that stores which texture this golem is using. Max value is 128
	 **/
	protected static final DataParameter<Byte> DATA_TEXTURE = EntityDataManager
		.createKey(GolemMultiTextured.class, DataSerializers.BYTE);
	protected static final String NBT_TEXTURE = "GolemTextureData";

	/**
	 * ResourceLocation array of textures to loop through when the player interacts with this golem.
	 * Max size is 128
	 **/
	public final ResourceLocation[] textures;

	/**
	 * Loot Table array to match texture array. If you don't want this, override {@link getLootTable}
	 **/
	public final ResourceLocation[] lootTables;

	/**
	 * This is a base class for golems that change texture when player interacts. Pass Strings that
	 * will be used to construct a ResourceLocation array of textures as well as loot tables<br/>
	 * <b>Example call to this constructor:</b><br/>
	 * <br/>
	 * <code>
	 * public EntityExampleGolem(World world) {<br/>
	 * super(world, 1.0F, Blocks.AIR, "example", new String[] {"one","two","three"});<br/>
	 * }</code><br/>
	 * This will initialize textures for <code>golem_example_one.png</code>,
	 * <code>golem_example_two.png</code> and <code>golem_example_three.png</code>,
	 * as well as loot tables for the same names with the JSON suffix
	 **/
	public GolemMultiTextured(final World world, final String prefix,
				  final String[] textureNames) {
		super(world);
		this.textures = new ResourceLocation[textureNames.length];
		this.lootTables = new ResourceLocation[textureNames.length];
		for (int n = 0, len = textureNames.length; n < len; n++) {
			// initialize textures
			final String s = textureNames[n];
			this.textures[n] = GolemBase.makeGolemTexture(getModId(), prefix + "_" + s);
			// initialize loot tables
			this.lootTables[n] = new ResourceLocation(getModId(), "entities/golem_" + prefix + "/" + s);
		}
	}

	@Override
	protected ResourceLocation applyTexture() {
		// apply TEMPORARY texture to avoid NPE. Actual texture is first applied in onLivingUpdate
		return makeGolemTexture("clay");
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.getDataManager().register(DATA_TEXTURE, (byte) 0);
	}

	@Override
	public boolean processInteract(final EntityPlayer player, final EnumHand hand) {
		final ItemStack stack = player.getHeldItem(hand);
		// only change texture when player has empty hand
		if (!stack.isEmpty()) {
			return super.processInteract(player, hand);
		} else {
			final int incremented = (this.getTextureNum() + 1) % this.textures.length;
			this.setTextureNum((byte) incremented);
			// this.writeEntityToNBT(this.getEntityData());
			player.swingArm(hand);
			return true;
		}
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		// since textureNum is correct, update texture AFTER loading from NBT and init
		if (this.ticksExisted == 2) {
			this.setTextureType(this.getTextureFromArray(this.getTextureNum()));
		}
	}

	@Override
	public void writeEntityToNBT(final NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		nbt.setByte(NBT_TEXTURE, (byte) this.getTextureNum());
	}

	@Override
	public void readEntityFromNBT(final NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		this.setTextureNum(nbt.getByte(NBT_TEXTURE));
	}

	@Override
	public boolean doesInteractChangeTexture() {
		return true;
	}

	/**
	 * Calls {@link #setTextureNum(byte, boolean)} with <b>toSet</b> and <b>true</b>.
	 **/
	public void setTextureNum(final byte toSet) {
		setTextureNum(toSet, true);
	}

	/**
	 * Update the texture data. If <b>updateInstantly</b> is true, call
	 * {@link #setTextureType(ResourceLocation)} based on {@link #getTextureFromArray(int)} and
	 * {@link #getTextureNum()}
	 **/
	public void setTextureNum(final byte toSet, final boolean updateInstantly) {
		this.getDataManager().set(DATA_TEXTURE, toSet);
		if (updateInstantly) {
			this.setTextureType(this.getTextureFromArray(this.getTextureNum()));
		}
	}

	public int getTextureNum() {
		return this.getDataManager().get(DATA_TEXTURE);
	}

	public int getNumTextures() {
		//TODO: Inspect possible NPE
		return this.textures != null ? this.textures.length : null;
	}

	public int getMaxTextureNum() {
		return getNumTextures() - 1;
	}

	public ResourceLocation[] getTextureArray() {
		return this.textures;
	}

	/**
	 * @deprecated automatically handled in {@link #setTextureNum(byte, boolean)}
	 **/
	@Deprecated
	public void updateTexture() {
		this.setTextureType(this.getTextureFromArray(this.getTextureNum()));
	}

	public ResourceLocation getTextureFromArray(final int index) {
		return this.textures[index % this.textures.length];
	}

	@Override
	protected ResourceLocation getLootTable() {
		ExtraGolems.LOGGER.log(Level.DEBUG, "[GMT] getting loot table: " + this.lootTables[this.getTextureNum() % this.lootTables.length]);
		return this.lootTables[this.getTextureNum() % this.lootTables.length];
	}

	public abstract String getModId();
}
