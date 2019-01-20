package com.golems.events;

import com.golems.entity.GolemBase;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Fired when an EntityRedstoneGolem is about to place a BlockPowerProvider. This event exists for
 * other mods or addons to handle and modify the Redstone Golem's behavior. It is not handled in
 * Extra Golems.
 */
@Event.HasResult
@Cancelable
public final class RedstoneGolemPowerEvent extends Event {

    public final GolemBase golem;
    public final BlockPos posToAffect;

    protected int powerLevel;
    public int updateFlag = 3;

    public RedstoneGolemPowerEvent(final GolemBase golemBase, final BlockPos toAffect, final int defPower) {
        this.setResult(Result.ALLOW);
        this.golem = golemBase;
        this.posToAffect = toAffect;
        this.powerLevel = defPower;
    }

    public void setPowerLevel(final int toSet) {
        //TODO: Ensure this works. nested ternary operations are super confusing.
        if (toSet > 15) {
            this.powerLevel = 15;
        } else {
            this.powerLevel = toSet < 0 ? 0 : toSet;
        }
        //this.powerLevel = toSet > 15 ? 15 : ();
    }

    public int getPowerLevel() {
        return this.powerLevel;
    }
}
