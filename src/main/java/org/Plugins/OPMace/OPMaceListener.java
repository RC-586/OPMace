package org.Plugins.OPMace;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class OPMaceListener implements Listener {

    private final OPMace plugin;

    public OPMaceListener(OPMace plugin) {
        this.plugin = plugin;
    }

    // 1. PREVENT PREVIEW
    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (event.getRecipe() == null) return;

        if (event.getRecipe().getResult().getType() == Material.MACE) {
            if (plugin.getConfig().getBoolean("mace.mace-crafted", false)) {
                event.getInventory().setResult(new ItemStack(Material.AIR));
            }
        }
    }

    // 2. CRAFT ITEM EVENT
    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (event.getRecipe() == null) return;

        ItemStack result = event.getRecipe().getResult();

        if (result.getType() == Material.MACE) {
            // Block second craft attempt
            if (plugin.getConfig().getBoolean("mace.mace-crafted", false)) {
                event.setCancelled(true);
                if (event.getWhoClicked() instanceof Player player) {
                    player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&cOP Mace has already been crafted on this server"));
                }
                return;
            }

            Player player = (Player) event.getWhoClicked();

            // Lock craft state and save owner
            plugin.getConfig().set("mace.mace-crafted", true);
            plugin.getConfig().set("mace.mace-owner", player.getName());
            plugin.saveConfig();

            // Create OP Mace
            ItemStack opMace = new ItemStack(Material.MACE);
            ItemMeta meta = opMace.getItemMeta();

            if (meta != null) {
                // Unbreakable
                meta.setUnbreakable(true);

                // Multi-version safe enchant registry
                addEnchantSafe(meta, "density", 6);
                addEnchantSafe(meta, "breach", 4);
                addEnchantSafe(meta, "wind_burst", 2);

                opMace.setItemMeta(meta);
            }

            event.setCurrentItem(opMace);

            // Broadcast message from config
            String rawMessage = plugin.getConfig().getString("messages.crafted", "&b[OPMace] &eOP Mace has been crafted!");
            Component broadcastMsg = LegacyComponentSerializer.legacyAmpersand().deserialize(rawMessage);

            plugin.getServer().broadcast(broadcastMsg);
        }
    }

    // 3. INDESTRUCTIBLE ITEM
    @EventHandler
    public void onItemDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Item itemEntity) {
            if (itemEntity.getItemStack().getType() == Material.MACE) {
                event.setCancelled(true);
            }
        }
    }

    // Helper method to apply enchantments safely across sub-versions
    private void addEnchantSafe(ItemMeta meta, String keyName, int level) {
        Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(keyName));
        if (enchantment != null) {
            meta.addEnchant(enchantment, level, true);
        }
    }
}