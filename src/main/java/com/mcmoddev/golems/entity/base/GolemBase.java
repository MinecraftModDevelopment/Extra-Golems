package com.mcmoddev.golems.entity.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.mcmoddev.golems.blocks.BlockUtilityGlow;
import com.mcmoddev.golems.blocks.BlockUtilityPower;
import com.mcmoddev.golems.entity.ai.GoToWaterGoal;
import com.mcmoddev.golems.entity.ai.PlaceUtilityBlockGoal;
import com.mcmoddev.golems.entity.ai.SwimUpGoal;
import com.mcmoddev.golems.items.ItemBedrockGolem;
import com.mcmoddev.golems.main.ExtraGolems;
import com.mcmoddev.golems.main.GolemItems;
import com.mcmoddev.golems.util.GolemContainer;
import com.mcmoddev.golems.util.GolemContainer.SwimMode;
import com.mcmoddev.golems.util.GolemRegistrar;
import com.mcmoddev.golems.util.config.ExtraGolemsConfig;
import com.mcmoddev.golems.util.config.special.GolemSpecialContainer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BannerItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.SwimmerPathNavigator;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.network.NetworkHooks;

/**
 * Base class for all golems in this mod.
 **/
public abstract class GolemBase extends IronGolemEntity {

  protected static final DataParameter<Boolean> CHILD = EntityDataManager.createKey(GolemBase.class, DataSerializers.BOOLEAN);
  protected static final String KEY_CHILD = "isChild";
  protected static final String KEY_BANNER = "Banner";
  
  public static final String ALLOW_LIGHT = "Allow Special: Light";
  public static final String ALLOW_POWER = "Allow Special: Power";

  private final GolemContainer container;

  // swimming helpers
  protected final SwimmerPathNavigator waterNavigator;
  protected final GroundPathNavigator groundNavigator;
  protected boolean swimmingUp;
  
  public GolemBase(EntityType<? extends GolemBase> type, World world) {
    super(type, world);
    this.container = GolemRegistrar.getContainer(type);
    // the following will be unused if swimming is not enabled
    this.waterNavigator = new SwimmerPathNavigator(this, world);
    this.groundNavigator = new GroundPathNavigator(this, world);
    // define behavior for the given swimming ability
    switch (container.getSwimMode()) {
    case FLOAT:
      // basic swimming AI
      this.goalSelector.addGoal(0, new SwimGoal(this));
      break;
    case SWIM:
      // advanced swimming AI
      this.stepHeight = 1.0F;
      this.moveController = new SwimmingMovementController(this);
      this.setPathPriority(PathNodeType.WATER, 0.0F);
      this.goalSelector.addGoal(1, new GoToWaterGoal(this, 14, 1.0D));
      this.goalSelector.addGoal(4, new RandomSwimmingGoal(this, 0.8F, 200));
      this.goalSelector.addGoal(5, new SwimUpGoal(this, 1.0D, this.world.getSeaLevel()));
      break;
    case SINK:
    default:
      // no swimming AI
      break;
    }
  }

  /**
   * Called after construction when a golem is built by a player
   * 
   * @param body
   * @param legs
   * @param arm1
   * @param arm2
   */
  public void onBuilt(final BlockState body, final BlockState legs, final BlockState arm1, final BlockState arm2) {
    // do nothing
  }

  @Override
  protected void registerData() {
    super.registerData();
    this.getDataManager().register(CHILD, Boolean.valueOf(false));
  }
  
  @Override
  protected void registerGoals() {
    super.registerGoals();
    final GolemContainer cont = this.getGolemContainer();
    // register light level AI if enabled
    if(cont.getLightLevel() > 0 && getConfigBool(ALLOW_LIGHT)) {
      int lightInt = cont.getLightLevel();
      final BlockState state = GolemItems.UTILITY_LIGHT.getDefaultState().with(BlockUtilityGlow.LIGHT_LEVEL, lightInt);
      this.goalSelector.addGoal(9, new PlaceUtilityBlockGoal(this, state, BlockUtilityGlow.UPDATE_TICKS, 
          true, null));
    }
    // register power level AI if enabled
    if(cont.getPowerLevel() > 0 && getConfigBool(ALLOW_POWER)) {
      int powerInt = cont.getPowerLevel();
      final BlockState state = GolemItems.UTILITY_POWER.getDefaultState().with(BlockUtilityPower.POWER_LEVEL, powerInt);
      final int freq = BlockUtilityPower.UPDATE_TICKS;
      this.goalSelector.addGoal(9, new PlaceUtilityBlockGoal(this, state, freq));
    }
  }

  /////////////// GOLEM UTILITY METHODS //////////////////

  /**
   * Whether right-clicking on this entity triggers a texture change.
   *
   * @return True if this is a {@link IMultiTexturedGolem} AND the config option
   *         is enabled.
   **/
  public boolean canInteractChangeTexture() {
    return ExtraGolemsConfig.enableTextureInteract() && this instanceof IMultiTexturedGolem;
  }

  /**
   * Whether this golem provides light (by placing light source blocks). Does not
   * change any behavior, but is used in the Light Block code to determine if it
   * can stay (called AFTER light is placed).
   *
   * @see com.mcmoddev.golems.blocks.BlockUtilityGlow
   **/
  public boolean isProvidingLight() {
    return this.getGolemContainer().getLightLevel() > 0;
  }

  /**
   * Whether this golem provides power (by placing power source blocks). Does not
   * change any behavior, but is used in the Power Block code to determine if it
   * can stay.
   *
   * @see com.mcmoddev.golems.blocks.BlockUtilityPower
   **/
  public boolean isProvidingPower() {
    return this.getGolemContainer().getPowerLevel() > 0;
  }

  /** @return the Golem Container **/
  public GolemContainer getGolemContainer() {
    return container != null ? container : GolemRegistrar.getContainer(this.getType().getRegistryName());
  }

  /**
   * @param i the ItemStack being used to heal the golem
   * @return the amount by which this item should heal the golem, in half-hearts.
   *         Defaults to 25% of max health or 32.0, whichever is smaller
   **/
  public float getHealAmount(final ItemStack i) {
    float amount = (float) (this.getMaxHealth() * this.getGolemContainer().getHealAmount(i.getItem()));
    if(this.isChild()) {
      amount *= 1.75F;
    }
    // max heal amount is 64, for no reason at all
    return Math.min(amount, 64.0F);
  }

  public BlockPos getBlockBelow() {
//    int i = MathHelper.floor(this.getPosX());
//    int j = MathHelper.floor(this.getPosY() - 0.2D);
//    int k = MathHelper.floor(this.getPosZ());
//    return new BlockPos(i, j, k);
    return getPositionUnderneath();
  }
  
  public ItemStack getBanner() { return this.getItemStackFromSlot(EquipmentSlotType.CHEST); }
  
  public void setBanner(final ItemStack bannerItem) { 
    this.setItemStackToSlot(EquipmentSlotType.CHEST, bannerItem);
    if(bannerItem.getItem() instanceof BannerItem) {
      this.setDropChance(EquipmentSlotType.CHEST, 1.0F);
    }
  }

  /////////////// CONFIG HELPERS //////////////////

  /**
   * @param name the name of the config value
   * @return the config value, or null if none is found
   **/
  public ForgeConfigSpec.ConfigValue getConfigValue(final String name) {
    final GolemContainer cont = this.getGolemContainer();
    final GolemSpecialContainer special = cont.getSpecialContainer(name);
    if(null == special) {
      ExtraGolems.LOGGER.error("Tried to access config value '" + name + "' in golem '" 
          + cont.getName() + "' but no config container was found!");
      return null;
    } else if(!ExtraGolemsConfig.GOLEM_CONFIG.specials.containsKey(special)) {
      ExtraGolems.LOGGER.error("Tried to access config value '" + name + "' in golem '"
          + cont.getName() + "' but the config value was not registered!");
      return null;
    }
    return (ExtraGolemsConfig.GOLEM_CONFIG.specials.get(special)).value;
  }
  
  /**
   * @param name the name of the config value
   * @return the config value, or false if none is found
   **/
  public boolean getConfigBool(final String name) {
    ForgeConfigSpec.ConfigValue v = getConfigValue(name);
    if(null == v) {
      return false;
    }
    return (Boolean) v.get();
  }

  /**
   * @param name the name of the config value
   * @return the config value, or 0 if none is found
   **/
  public int getConfigInt(final String name) {
    ForgeConfigSpec.ConfigValue v = getConfigValue(name);
    if(null == v) {
      return 0;
    }
    return (Integer) v.get();
  }

  /**
   * @param name the name of the config value
   * @return the config value, or 0 if none is found
   **/
  public double getConfigDouble(final String name) {
    ForgeConfigSpec.ConfigValue v = getConfigValue(name);
    if(null == v) {
      return 0.0D;
    }
    return (Double) v.get();
  }

  /////////////// OVERRIDEN BEHAVIOR //////////////////

  // fall(float, float)
  @Override
  public boolean onLivingFall(float distance, float damageMultiplier) {
    if (!container.takesFallDamage()) {
      return false;
    }

    float[] ret = net.minecraftforge.common.ForgeHooks.onLivingFall(this, distance, damageMultiplier);
    if (ret == null) return false;
    distance = ret[0];
    damageMultiplier = ret[1];

    boolean flag = super.onLivingFall(distance, damageMultiplier);
    int i = this.calculateFallDamage(distance, damageMultiplier);
    if (i > 0) {
       this.playSound(this.getFallSound(i), 1.0F, 1.0F);
       this.playFallSound();
       this.attackEntityFrom(DamageSource.FALL, (float)i);
       return true;
    } else {
       return flag;
    }
  }
  
  @Override
  public boolean isImmuneToExplosions() {
    return this.getGolemContainer().isImmuneToExplosions();
  }

  @Override
  public boolean canAttack(final EntityType<?> type) {
    if (type == EntityType.PLAYER && this.isPlayerCreated()) {
      return ExtraGolemsConfig.enableFriendlyFire();
    }
    if (type == EntityType.VILLAGER || type.getRegistryName().toString().contains("golem")) {
      return false;
    }
    return super.canAttack(type);
  }

  @Override
  public ItemStack getPickedResult(final RayTraceResult ray) {
    final Block block = container.getPrimaryBuildingBlock();
    return block != null ? new ItemStack(block) : ItemStack.EMPTY;
  }

  @Override
  protected ActionResultType getEntityInteractionResult(final PlayerEntity player, final Hand hand) {
    ItemStack stack = player.getHeldItem(hand);
    // Attempt to remove banner from the golem
    if(!this.getBanner().isEmpty() && stack.getItem() instanceof ShearsItem) {
      this.entityDropItem(this.getBanner(), this.isChild() ? 0.9F : 1.4F);
      this.setBanner(ItemStack.EMPTY);
    }
    // Attempt to place a banner on the golem
    if(stack.getItem() instanceof BannerItem && processInteractBanner(player, hand, stack)) {
      return ActionResultType.CONSUME;
    }
    // Attempt to heal the golem
    final float healAmount = getHealAmount(stack);
    if (healAmount > 0 && processInteractHeal(player, hand, stack, healAmount)) {
      return ActionResultType.CONSUME;
    }
    return super.getEntityInteractionResult(player, hand);
  }
  
  /**
   * Called when the player uses an item that might be a banner
   * @param player the player using the item
   * @param hand the player hand
   * @param stack the item being used
   * @param healAmount the amount of health this item will restore
   * @return true if the item was consumed
   */
  protected boolean processInteractBanner(final PlayerEntity player, final Hand hand, final ItemStack stack) {
    if(!this.getBanner().isEmpty()) {
      this.entityDropItem(this.getBanner(), this.isChild() ? 0.9F : 1.4F);
    }
    setBanner(stack.split(1));
    return true;
  }
  
  /**
   * Called when the player uses an item that can heal this golem
   * @param player the player using the item
   * @param hand the player hand
   * @param stack the item being used
   * @param healAmount the amount of health this item will restore
   * @return true if the item was consumed
   */
  protected boolean processInteractHeal(final PlayerEntity player, final Hand hand, final ItemStack stack, final float healAmount) {
    if (ExtraGolemsConfig.enableHealGolems() && this.getHealth() < this.getMaxHealth()) {
      heal(healAmount);
      // update stack size/item
      if(!player.isCreative()) {
        if (stack.getCount() > 1) {
          stack.shrink(1);
        } else {
          // update the player's held item
          player.setHeldItem(hand, stack.getContainerItem());
        }
      }
      // if currently attacking this player, stop
      if (this.getAttackTarget() == player) {
        this.setRevengeTarget(null);
        this.setAttackTarget(null);
      }
      // spawn particles and play sound
      final Vector3d pos = this.getPositionVec();
      ItemBedrockGolem.spawnParticles(this.world, pos.x, pos.y + this.getHeight() / 2.0D, pos.z, 0.15D, ParticleTypes.INSTANT_EFFECT, 30);
      this.playSound(SoundEvents.BLOCK_STONE_PLACE, 0.85F, 1.1F + rand.nextFloat() * 0.2F);
      return true;
    }
    return false;
  }
  
  @Override
  public float getBrightness() {
    return this.isProvidingLight() || this.isProvidingPower() ? 1.0F : super.getBrightness();
  }
  
  ///////////////// CHILD LOGIC ///////////////////

  @Override
  public boolean isChild() {
    return this.getDataManager().get(CHILD).booleanValue();
  }

  /** Update whether this entity is 'child' and recalculate size **/
  public void setChild(final boolean isChild) {
    if (this.getDataManager().get(CHILD).booleanValue() != isChild) {
      this.getDataManager().set(CHILD, Boolean.valueOf(isChild));
      this.recalculateSize();
    }
  }
  
  @Override
  public void notifyDataManagerChange(final DataParameter<?> key) {
    super.notifyDataManagerChange(key);
    if (CHILD.equals(key)) {
      if (this.isChild()) {
        // truncate these values to one decimal place after reducing them from base values
        double childHealth = (Math.floor(getGolemContainer().getHealth() * 0.3D * 10D)) / 10D;
        double childAttack = (Math.floor(getGolemContainer().getAttack() * 0.6D * 10D)) / 10D;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(childHealth);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(childAttack);
        this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(0.0D);
      } else {
        // use full values for non-child golem
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(getGolemContainer().getHealth());
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(getGolemContainer().getAttack());
        this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(getGolemContainer().getKnockbackResist());
      }
      // recalculate size
      this.recalculateSize();
    }
  }
  
  /**
   * Attempts to spawn the given number of "mini" golems
   * @param count the number of children to spawn
   * @return a collection containing the entities that were spawned
   **/
  protected Collection<GolemBase> trySpawnChildren(final int count) {
    final List<GolemBase> children = new ArrayList<>();
    if(!this.world.isRemote && !this.isChild()) {
      for(int i = 0; i < count; i++) {
        GolemBase child = this.getGolemContainer().getEntityType().create(this.world);
        child.setChild(true);
        if (this.getAttackTarget() != null) {
          child.setAttackTarget(this.getAttackTarget());
        }
        // set location
        child.copyLocationAndAnglesFrom(this);
        // spawn the entity
        this.getEntityWorld().addEntity(child);
        // add to the list
        children.add(child);
      }
    }
    return children;
  }
  
  //////////////// NBT /////////////////

  @Override
  public void readAdditional(final CompoundNBT tag) {
    super.readAdditional(tag);
    this.setChild(tag.getBoolean(KEY_CHILD));
  }

  @Override
  public void writeAdditional(final CompoundNBT tag) {
    super.writeAdditional(tag);
    tag.putBoolean(KEY_CHILD, this.isChild());
  }

  @Override
  public IPacket<?> createSpawnPacket() {
    return NetworkHooks.getEntitySpawningPacket(this);
  }

  ///////////////////// SOUND OVERRIDES ////////////////////

  @Override
  protected SoundEvent getAmbientSound() {
    return getGolemSound();
  }

  @Override
  protected SoundEvent getHurtSound(final DamageSource ignored) {
    return getGolemSound() == SoundEvents.BLOCK_GLASS_STEP ? SoundEvents.BLOCK_GLASS_HIT : getGolemSound();
  }

  @Override
  protected SoundEvent getDeathSound() {
    return getGolemSound() == SoundEvents.BLOCK_GLASS_STEP ? SoundEvents.BLOCK_GLASS_BREAK : getGolemSound();
  }

  /**
   * @return A SoundEvent to play when the golem is attacking, walking, hurt, and
   *         on death
   **/
  public final SoundEvent getGolemSound() {
    return container.getSound();
  }

  ///////////////////// SWIMMING BEHAVIOR ////////////////////////

  @Override
  public void travel(final Vector3d vec) {
    if (isServerWorld() && isInWater() && isSwimmingUp()) {
      moveRelative(0.01F, vec);
      move(MoverType.SELF, getMotion());
      setMotion(getMotion().scale(0.9D));
    } else {
      super.travel(vec);
    }
  }

  @Override
  public void updateSwimming() {
    if (container.getSwimMode() != SwimMode.SWIM) {
      super.updateSwimming();
      return;
    }
    if (!this.world.isRemote) {
      if (isServerWorld() && isInWater() && isSwimmingUp()) {
        this.navigator = this.waterNavigator;
        setSwimming(true);
      } else {
        this.navigator = this.groundNavigator;
        setSwimming(false);
      }
    }
  }

  @Override
  protected float getWaterSlowDown() {
    return container.getSwimMode() == SwimMode.SWIM ? 0.88F : super.getWaterSlowDown();
  }

  @Override
  public boolean isPushedByWater() {
    return !isSwimming();
  }

  public void setSwimmingUp(boolean isSwimmingUp) {
    this.swimmingUp = (isSwimmingUp && container.getSwimMode() == SwimMode.SWIM);
  }

  public boolean isSwimmingUp() {
    if (container.getSwimMode() != SwimMode.SWIM) {
      return false;
    }
    if (this.swimmingUp) {
      return true;
    }
    LivingEntity e = getAttackTarget();
    return e != null && e.isInWater();
  }

  public static boolean isSwimmingUp(final GolemBase golem) {
    return golem.swimmingUp;
  }

  /**
   * Referenced from {@link GoToWaterGoal}.
   * 
   * @param target a location representing a water block
   * @return true if the golem should move towards the water
   **/
  public boolean shouldMoveToWater(final Vector3d target) {
    return container.getSwimMode() == SwimMode.SWIM;
  }
  
  static class SwimmingMovementController extends MovementController {
    private final GolemBase golem;

    public SwimmingMovementController(GolemBase golem) {
      super(golem);
      this.golem = golem;
    }

    @Override
    public void tick() {
      // All of this is copied from DrownedEntity#MoveHelperController
      LivingEntity target = this.golem.getAttackTarget();
      if (this.golem.isSwimmingUp() && this.golem.isInWater()) {
        if ((target != null && target.getPosY() > this.golem.getPosY()) || this.golem.swimmingUp) {
          this.golem.setMotion(this.golem.getMotion().add(0.0D, 0.002D, 0.0D));
        }

        if (this.action != MovementController.Action.MOVE_TO || this.golem.getNavigator().noPath()) {
          this.golem.setAIMoveSpeed(0.0F);
          return;
        }
        double dX = this.posX - this.golem.getPosX();
        double dY = this.posY - this.golem.getPosY();
        double dZ = this.posZ - this.golem.getPosZ();
        double dTotal = MathHelper.sqrt(dX * dX + dY * dY + dZ * dZ);
        dY /= dTotal;

        float rot = (float) (MathHelper.atan2(dZ, dX) * 57.2957763671875D) - 90.0F;
        this.golem.rotationYaw = limitAngle(this.golem.rotationYaw, rot, 90.0F);
        this.golem.renderYawOffset = this.golem.rotationYaw;

        float moveSpeed = (float) (this.speed * this.golem.getAttributeValue(Attributes.MOVEMENT_SPEED));
        float moveSpeedAdjusted = MathHelper.lerp(0.125F, this.golem.getAIMoveSpeed(), moveSpeed);
        this.golem.setAIMoveSpeed(moveSpeedAdjusted);
        this.golem.setMotion(this.golem.getMotion().add(moveSpeedAdjusted * dX * 0.005D, moveSpeedAdjusted * dY * 0.1D,
            moveSpeedAdjusted * dZ * 0.005D));
      } else {
        if (!this.golem.onGround) {
          this.golem.setMotion(this.golem.getMotion().add(0.0D, -0.008D, 0.0D));
        }
        super.tick();
      }
    }
  }
}
