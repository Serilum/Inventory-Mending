package com.natamus.inventorymending.mixin;

import com.natamus.inventorymending.config.ConfigHandler;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Mixin(value = ExperienceOrb.class, priority = 1001)
public class ExperienceOrbMixin {
    @Unique private static final Predicate<ItemStack> isDamagedPredicate = ItemStack::isDamaged;

    @ModifyVariable(method = "repairPlayerItems(Lnet/minecraft/server/level/ServerPlayer;I)I", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getRandomItemWith(Lnet/minecraft/core/component/DataComponentType;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Predicate;)Ljava/util/Optional;"))
    public Optional<EnchantedItemInUse> repairPlayerItems_optionalEnchantedItemInUse(Optional<EnchantedItemInUse> optionalEnchantedItemInUse, ServerPlayer serverPlayer, int n) {
        if (optionalEnchantedItemInUse.isEmpty()) {
            List<EnchantedItemInUse> list = new ArrayList<>();

            Inventory playerInventory = serverPlayer.getInventory();
            for (int i = 0; i < playerInventory.getContainerSize(); i++) {
                if (ConfigHandler.mendToolbarOnly && i > 8) {
                    break;
                }

                ItemStack slotStack = playerInventory.getItem(i);
                if (isDamagedPredicate.test(slotStack)) {
                    ItemEnchantments itemEnchantments = slotStack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

                    for (Object2IntMap.Entry<Holder<Enchantment>> entry : itemEnchantments.entrySet()) {
                        Holder<Enchantment> holder = entry.getKey();
                        if ((holder.value()).effects().has(EnchantmentEffectComponents.REPAIR_WITH_XP)) {
                            list.add(new EnchantedItemInUse(slotStack, EquipmentSlot.MAINHAND, serverPlayer));
                        }
                    }
                }
            }

            if (!list.isEmpty()) {
                return Util.getRandomSafe(list, serverPlayer.getRandom());
            }
        }

        return optionalEnchantedItemInUse;
    }
}
