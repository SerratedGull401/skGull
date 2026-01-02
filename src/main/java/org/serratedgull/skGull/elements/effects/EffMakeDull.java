package org.serratedgull.skGull.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class EffMakeDull extends Effect {

    static {
        Skript.registerEffect(EffMakeDull.class, "make %itemstacks% [look] (dull|unshiny)");
    }

    private Expression<ItemStack> itemExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        itemExpr = (Expression<ItemStack>) exprs[0];
        return true;
    }

    @Override
    protected void execute(Event e) {
        ItemStack[] items = itemExpr.getArray(e);
        if (items == null) return;

        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (item == null || item.getType().isAir()) continue;

            ItemStack dullItem = item.clone();
            ItemMeta meta = dullItem.getItemMeta();

            if (meta != null) {
                // 1. Remove 1.21+ Glint Override (set to false)
                try {
                    meta.setEnchantmentGlintOverride(false);
                } catch (NoSuchMethodError | AbstractMethodError ignored) {}

                // 2. Remove Fallback Enchantment (Luck of the Sea)
                if (meta.hasEnchant(Enchantment.LUCK_OF_THE_SEA)) {
                    meta.removeEnchant(Enchantment.LUCK_OF_THE_SEA);
                }

                // 3. Remove the Hide Enchants flag so real enchants show up again
                meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);

                dullItem.setItemMeta(meta);
            }

            // Sync the change back to the player's inventory or the variable
            if (itemExpr.acceptChange(Changer.ChangeMode.SET) != null) {
                itemExpr.change(e, new Object[]{dullItem}, Changer.ChangeMode.SET);
            } else {
                items[i] = dullItem;
            }
        }
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "make " + itemExpr.toString(e, debug) + " dull";
    }
}