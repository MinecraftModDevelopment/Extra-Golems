package com.mcmoddev.golems.entity.base;

import java.util.Map;

import com.mcmoddev.golems.util.GolemTextureBytes;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public abstract class GolemMultiTextured extends GolemBase implements IMultiTexturedGolem<ResourceLocation> {

  /**
   * The DataParameter that stores which texture this golem is using. Max value is
   * 128
   **/
  protected static final DataParameter<Byte> DATA_TEXTURE = EntityDataManager.<Byte>createKey(GolemMultiTextured.class, DataSerializers.BYTE);
  protected static final String KEY_TEXTURE = "GolemTextureData";

  /**
   * ResourceLocation array of textures to loop through when the player interacts
   * with this golem. Max size is Byte.MAX_VALUE
   **/
  protected final ResourceLocation[] textures;

  /**
   * Loot Table array to match texture array. If you don't want this, override
   * {@link getLootTable}
   **/
  protected final ResourceLocation[] lootTables;

  /**
   * This is a base class for golems that change texture when player interacts.
   * Pass Strings that will be used to construct a ResourceLocation array of
   * textures as well as loot tables
   * <p>
   * <b>Example call to this constructor:</b>
   * <p>
   * <code>
   * String[] NAMES = new String[] {"one","two","three"};
   * <br><br>public EntityExampleGolem(EntityType entityType, World world) {
   * <br>&nbsp;&nbsp;super(entityType, world, "golems", NAMES);
   * <br>}</code>
   * <p>
   * If the golem was registered with name <code>"golem_example"</code>, then the
   * following textures will be initialized: <br>
   * <code>golems/textures/entity/golem_example/one.png</code> <br>
   * <code>golems/textures/entity/golem_example/two.png</code> <br>
   * <code>golems/textures/entity/golem_example/three.png</code> <br>
   * as well as loot tables for the same names with the JSON suffix
   **/
  public GolemMultiTextured(final EntityType<? extends GolemBase> entityType, final World world, final String textureModId, 
      final String[] textureNames, final String lootTableModId, final String[] lootTableNames) {
    super(entityType, world);
    this.textures = new ResourceLocation[textureNames.length];
    this.lootTables = new ResourceLocation[lootTableNames.length];
    for (int n = 0, len = textureNames.length; n < len; n++) {
      // initialize textures
      this.textures[n] = new ResourceLocation(textureModId, "textures/block/" + textureNames[n] + ".png");
      // initialize loot tables
      this.lootTables[n] = new ResourceLocation(lootTableModId, "entities/" + this.getGolemContainer().getName() + "/" + lootTableNames[n]);
    }
  }

  @Override
  protected void registerData() {
    super.registerData();
    this.getDataManager().register(DATA_TEXTURE, (byte) 0);
  }

  @Override
  public ActionResultType getEntityInteractionResult(final PlayerEntity player, final Hand hand) {
    // change texture when player clicks (if enabled)
    if (!player.isCrouching() && this.canInteractChangeTexture()) {
      return handlePlayerInteract(player, hand);
    } else {
      return super.getEntityInteractionResult(player, hand);
    }
  }

  @Override
  public void onBuilt(final BlockState body, final BlockState legs, final BlockState arm1, final BlockState arm2) {
    final Map<Block, Byte> map = this.getTextureBytes();
    if (map != null && !map.isEmpty()) {
      byte textureNum = GolemTextureBytes.getByBlock(map, body.getBlock());
      this.setTextureNum(textureNum);
    }
  }

  @Override
  public void notifyDataManagerChange(DataParameter<?> key) {
    super.notifyDataManagerChange(key);
    // attempt to sync texture from client -> server -> other clients
    if (DATA_TEXTURE.equals(key)) {
      this.setTextureNum((byte) this.getTextureNum());
    }
  }

  @Override
  public void writeAdditional(final CompoundNBT nbt) {
    super.writeAdditional(nbt);
    nbt.putByte(KEY_TEXTURE, (byte) this.getTextureNum());
  }

  @Override
  public void readAdditional(final CompoundNBT nbt) {
    super.readAdditional(nbt);
    this.setTextureNum(nbt.getByte(KEY_TEXTURE));
  }

  @Override
  protected ResourceLocation getLootTable() {
    return this.lootTables[this.getTextureNum() % this.lootTables.length];
  }

  @Override
  public ItemStack getPickedResult(final RayTraceResult target) {
    return getCreativeReturn(target);
  }

  @Override
  public void setTextureNum(final byte toSet) {
    if (toSet != this.getDataManager().get(DATA_TEXTURE).byteValue()) {
      this.getDataManager().set(DATA_TEXTURE, Byte.valueOf(toSet));
    }
  }

  @Override
  public int getTextureNum() {
    return this.getDataManager().get(DATA_TEXTURE).byteValue();
  }

  @Override
  public int getNumTextures() {
    return this.textures != null ? this.textures.length : null;
  }

  @Override
  public ResourceLocation[] getTextureArray() {
    return this.textures;
  }

  public ResourceLocation getTextureFromArray(final int index) {
    return getTextureArray()[index % this.textures.length];
  }

  public ResourceLocation getTexture() {
    return getTextureFromArray(getTextureNum());
  }
  
  @Override
  public ResourceLocation[] getLootTableArray() {
    return this.lootTables;
  }
}
