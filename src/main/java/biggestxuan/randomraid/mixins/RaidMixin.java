package biggestxuan.randomraid.mixins;

import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.monster.AbstractRaiderEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.raid.Raid;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(Raid.class)
public abstract class RaidMixin {
    @Mutable
    @Shadow
    @Final
    private final ServerWorld level;

    protected RaidMixin(ServerWorld level) {
        this.level = level;
    }

    @Shadow
    @Nullable
    protected abstract BlockPos findRandomSpawnPos(int p_221298_1_, int p_221298_2_);

    @Inject(method = "joinRaid",at = @At(value = "INVOKE",target = "Lnet/minecraft/entity/monster/AbstractRaiderEntity;setPos(DDD)V"),cancellable = true)
    public void spawn(int p_221317_1_, AbstractRaiderEntity p_221317_2_, BlockPos p_221317_3_, boolean p_221317_4_, CallbackInfo ci){
        BlockPos pos = findRandomSpawnPos(0,20);
        p_221317_2_.setPos((double)pos.getX() + 0.5D, (double)pos.getY() + 1.0D, (double)pos.getZ() + 0.5D);
        p_221317_2_.finalizeSpawn(this.level, this.level.getCurrentDifficultyAt(p_221317_3_), SpawnReason.EVENT, (ILivingEntityData)null, (CompoundNBT)null);
        p_221317_2_.applyRaidBuffs(p_221317_1_, false);
        p_221317_2_.setOnGround(true);
        this.level.addFreshEntity(p_221317_2_);
        ci.cancel();
    }
}
