package dev.starfall.starfallnuke.item;

import dev.starfall.starfallnuke.entity.StarfallProjectile;
import dev.starfall.starfallnuke.registry.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StarfallCoreItem extends Item {
    public StarfallCoreItem(Properties properties) {
        super(properties.rarity(Rarity.EPIC).fireResistant());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            StarfallProjectile projectile = new StarfallProjectile(
                    ModEntities.STARFALL_PROJECTILE.get(), level);
            projectile.setOwner(player);
            projectile.setItem(stack);

            if (player.isShiftKeyDown()) {
                Vec3 eyePos = player.getEyePosition();
                Vec3 lookVec = player.getLookAngle();
                Vec3 endPos = eyePos.add(lookVec.scale(120.0));
                BlockHitResult hit = level.clip(new ClipContext(
                        eyePos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
                Vec3 target = hit.getType() != HitResult.Type.MISS ? hit.getLocation() : endPos;

                projectile.setPos(target.x + (level.random.nextDouble() - 0.5) * 12.0,
                        Math.min(level.getMaxBuildHeight() + 40, target.y + 75.0),
                        target.z + (level.random.nextDouble() - 0.5) * 12.0);
                Vec3 dir = target.subtract(projectile.position()).normalize();
                projectile.shoot(dir.x, dir.y, dir.z, 2.4F, 0.2F);

                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.5F, 0.6F);
            } else {
                projectile.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
                projectile.shootFromRotation(player, player.getXRot(), player.getYRot(),
                        -3.0F, 1.65F, 0.4F);

                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 1.2F, 0.65F);
            }

            level.addFreshEntity(projectile);

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            player.getCooldowns().addCooldown(this, 60);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.starfallnuke.starfall_core.line1").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.starfallnuke.starfall_core.line2").withStyle(ChatFormatting.AQUA));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
