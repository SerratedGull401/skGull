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

public class EffMakeShiny extends Effect {

    static {
        Skript.registerEffect(EffMakeShiny.class, "make %itemstacks% [look] shiny");
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

            // Clone the item to ensure we aren't messing with Skript's internal references directly
            ItemStack shinyItem = item.clone();
            ItemMeta meta = shinyItem.getItemMeta();

            if (meta != null) {
                // 1.21+ Method
                try {
                    meta.setEnchantmentGlintOverride(true);
                } catch (NoSuchMethodError | AbstractMethodError err) {
                    // Fallback
                    meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }
                shinyItem.setItemMeta(meta);
            }

            // This is the magic part for "tool of player" etc.
            // If the expression allows setting (like a player's tool), we push the change back.
            if (itemExpr.acceptChange(Changer.ChangeMode.SET) != null) {
                itemExpr.change(e, new Object[]{shinyItem}, Changer.ChangeMode.SET);
            } else {
                // If it's just a local variable {_item}, we modify the object directly
                items[i] = shinyItem;
            }
        }
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "make " + itemExpr.toString(e, debug) + " shiny";
    }
}