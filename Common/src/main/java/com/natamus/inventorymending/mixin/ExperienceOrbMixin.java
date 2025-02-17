package com.natamus.inventorymending.mixin;

import com.google.common.collect.Lists;
import com.natamus.inventorymending.config.ConfigHandler;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Mixin(value = ExperienceOrb.class, priority = 1001)
public abstract class ExperienceOrbMixin {
    @Shadow protected abstract int xpToDurability(int i);
    @Shadow protected abstract int durabilityToXp(int i);
    @Shadow protected abstract int repairPlayerItems(Player player, int i);
    @Shadow private int value;

    @Unique private static final Predicate<ItemStack> isDamagedPredicate = ItemStack::isDamaged;

    @Inject(method = "repairPlayerItems(Lnet/minecraft/world/entity/player/Player;I)I", at = @At(value = "RETURN"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    public void repairPlayerItems_equipmentSlotItemStackEntry(Player player, int a, CallbackInfoReturnable<Integer> cir, Map.Entry<EquipmentSlot, ItemStack> equipmentSlotItemStackEntry) {
       if (equipmentSlotItemStackEntry == null) {
            List<Map.Entry<EquipmentSlot, ItemStack>> list = Lists.newArrayList();

            Inventory playerInventory = player.getInventory();
            for (int i = 0; i < playerInventory.getContainerSize(); i++) {
                if (ConfigHandler.mendToolbarOnly && i > 8) {
                    break;
                }

                ItemStack slotStack = playerInventory.getItem(i);
                if (isDamagedPredicate.test(slotStack)) {
                    if (!slotStack.isEmpty() && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MENDING, slotStack) > 0) {
                        list.add(Map.entry(EquipmentSlot.MAINHAND, slotStack));
                    }
                }
            }

            if (!list.isEmpty()) {
                equipmentSlotItemStackEntry = list.get(player.getRandom().nextInt(list.size()));

                ItemStack newStack = (ItemStack)equipmentSlotItemStackEntry.getValue();
                int b = Math.min(this.xpToDurability(this.value), newStack.getDamageValue());
                newStack.setDamageValue(newStack.getDamageValue() - a);
                int c = a - this.durabilityToXp(b);
                cir.setReturnValue(c > 0 ? this.repairPlayerItems(player, c) : 0);
            }
        }
    }
}
