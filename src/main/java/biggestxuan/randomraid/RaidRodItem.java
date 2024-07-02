package biggestxuan.randomraid;

import net.minecraft.entity.monster.AbstractRaiderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.raid.Raid;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.List;

public class RaidRodItem extends Item {
    public RaidRodItem() {
        super(new Properties().durability(7).tab(ItemGroup.TAB_MISC));
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if(!world.isClientSide){
            ServerWorld serverWorld = (ServerWorld) world;
            if(serverWorld.isRaided(player.blockPosition())){
                if(!player.isCreative()){
                    player.getCooldowns().addCooldown(this,1200);
                }
                stack.hurtAndBreak(1,player,(p) -> p.broadcastBreakEvent(hand));
                for(AbstractRaiderEntity entity:getNearRaidEntity(player)){
                    entity.addEffect(new EffectInstance(Effects.GLOWING,1200,0));
                    if(player.isShiftKeyDown()){
                        BlockPos pos = entity.blockPosition();
                        player.displayClientMessage(new TranslationTextComponent("message.raid.pos",entity.getDisplayName().getString(),pos.getX(),pos.getY(),pos.getZ()),false);
                    }
                }
                return ActionResult.consume(stack);
            }else{
                player.displayClientMessage(new TranslationTextComponent("message.raid.no_raid"), false);
                return ActionResult.fail(stack);
            }
        }
        return ActionResult.fail(stack);
    }

    private static List<AbstractRaiderEntity> getNearRaidEntity(PlayerEntity player){
        List<AbstractRaiderEntity> out = new ArrayList<>();
        BlockPos playerPos = new BlockPos(player.position());
        World world = player.level;
        if(world instanceof ServerWorld) {
            ServerWorld serverWorld = player.level.getServer().overworld();
            if (serverWorld.isRaided(playerPos)) {
                Raid raid = serverWorld.getRaidAt(playerPos);
                assert raid != null;
                int raidID = raid.getId();
                BlockPos centerPos = raid.getCenter();
                int x = centerPos.getX();
                int z = centerPos.getZ();
                AxisAlignedBB aabb = new AxisAlignedBB(x - 128, 0, z - 128, x + 128, 256, z + 128);
                for (AbstractRaiderEntity entity : world.getLoadedEntitiesOfClass(AbstractRaiderEntity.class, aabb)) {
                    if (!serverWorld.isRaided(new BlockPos(entity.position()))) {
                        continue;
                    }
                    if (serverWorld.getRaidAt(new BlockPos(entity.position())).getId() == raidID) {
                        out.add(entity);
                    }
                }
            }
        }
        return out;
    }
}
