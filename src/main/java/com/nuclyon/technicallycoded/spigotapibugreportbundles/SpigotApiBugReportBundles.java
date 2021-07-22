package com.nuclyon.technicallycoded.spigotapibugreportbundles;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public final class SpigotApiBugReportBundles extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    /**
     * Handle saving the player's inventory on death.
     * COPIED FROM InventoryRollbackPlus Code
     * @param event Bukkit damage event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    private void playerDeath(EntityDamageEvent event) {
        // Sanity checks to prevent unwanted saves
        if (!(event.getEntity() instanceof Player)) return;
        if (event.isCancelled()) return;

        Player p = (Player) event.getEntity();

        byte[] invByteArr = convertToByteArray(p.getInventory().getContents());
        if (invByteArr == null) {
            p.sendMessage(ChatColor.RED + "Please run /kill with a non-empty bundle in your inventory to trigger error");
            return;
        }
        reCreateItems(invByteArr);
    }

    /**
     * COPIED FROM InventoryRollbackPlus Code
     * @param contents
     * @return
     */
    public static byte[] convertToByteArray(ItemStack[] contents) {
        boolean convert = false;

        // Check if there are non-null items to convert
        for (ItemStack item : contents) {
            if (item != null) {
                convert = true;
                break;
            }
        }

        // If so, create save
        if (convert) {
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

                dataOutput.writeInt(contents.length);

                for (ItemStack stack : contents) {
                    dataOutput.writeObject(stack);
                }
                dataOutput.close();
                return outputStream.toByteArray();

            } catch (Exception e) {
                throw new IllegalStateException("Unable to save item stacks.", e);
            }
        }
        return null;
    }

    /**
     * COPIED FROM InventoryRollbackPlus Code
     * @param byteArr
     */
    public static void reCreateItems(byte[] byteArr) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArr);
        BukkitObjectInputStream dataInput = null;

        // We don't care about using the output.. this is just a test, therefore this is never used
        ItemStack[] stacks = null;

        try {
            dataInput = new BukkitObjectInputStream(inputStream);
            stacks = new ItemStack[dataInput.readInt()];
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        for (int i = 0; i < stacks.length; i++) {
            try {
                Object objectRead = dataInput.readObject();
                stacks[i] = (ItemStack) objectRead;
            } catch (IOException | ClassNotFoundException | NullPointerException e) {
                e.printStackTrace();
            }
        }
        try { dataInput.close(); } catch (IOException ignored) {}
    }
}
