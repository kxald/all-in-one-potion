package com.kxald.allinonepotion;

import java.util.Map;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class AllInOnePotionPlugin extends JavaPlugin implements Listener {

    private static final int EIGHT_MINUTES = 8 * 60 * 20;
    private static final String OWNER = "imgsh";

    @Override
    public void onEnable() {
        registerRecipe();
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("All-in-One Splash Potion recipe registered.");
    }

    private void registerRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(
                new NamespacedKey(this, "all_in_one_splash"),
                createPotion()
        );

        recipe.shape(
                "NW",
                "S "
        );

        recipe.setIngredient('N', Material.NETHER_WART);
        recipe.setIngredient('W', new RecipeChoice.ExactChoice(createWaterBottle()));
        recipe.setIngredient('S', Material.DIAMOND_SWORD);
        recipe.setGroup("all_in_one_potion");

        getServer().addRecipe(recipe);
    }

    private ItemStack createPotion() {
        ItemStack potion = new ItemStack(Material.SPLASH_POTION);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();

        meta.addCustomEffect(new PotionEffect(PotionEffectType.STRENGTH, EIGHT_MINUTES, 1), true);
        meta.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, EIGHT_MINUTES, 1), true);
        meta.addCustomEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, EIGHT_MINUTES, 0), true);
        meta.addCustomEffect(new PotionEffect(PotionEffectType.WEAVING, EIGHT_MINUTES, 0), true);

        meta.itemName(Component.text("Omni Potion"));
        meta.setColor(Color.AQUA);

        potion.setItemMeta(meta);
        return potion;
    }

    private ItemStack createWaterBottle() {
        ItemStack bottle = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) bottle.getItemMeta();
        meta.setBasePotionType(PotionType.WATER);
        bottle.setItemMeta(meta);
        return bottle;
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (!(event.getView().getPlayer() instanceof Player player)) return;
        if (!player.getName().equalsIgnoreCase(OWNER)) return;

        ItemStack source = findDupeSource(event.getInventory().getMatrix());
        if (source != null) {
            event.getInventory().setResult(source.clone());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCraftClick(InventoryClickEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!player.getName().equalsIgnoreCase(OWNER)) return;
        if (event.getSlotType() != InventoryType.SlotType.RESULT || event.getSlot() != 0) return;
        if (!(event.getClickedInventory() instanceof CraftingInventory inv)) return;
        if (inv.getResult() == null || inv.getResult().getType().isAir()) return;
        if (!event.getCursor().isEmpty()) return;

        ItemStack source = findDupeSource(inv.getMatrix());
        if (source == null) return;

        event.setCancelled(true);

        ItemStack[] matrix = inv.getMatrix();
        for (int i = 0; i < matrix.length; i++) {
            matrix[i] = null;
        }
        inv.setMatrix(matrix);
        inv.setResult(null);

        Map<Integer, ItemStack> leftover = player.getInventory().addItem(source.clone(), source.clone());
        for (ItemStack item : leftover.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }

        Bukkit.getScheduler().runTask(this, player::updateInventory);
    }

    private ItemStack findDupeSource(ItemStack[] matrix) {
        int occupied = 0;
        boolean hasDirt = false;
        ItemStack source = null;

        for (ItemStack item : matrix) {
            if (item == null || item.getType().isAir()) continue;
            occupied++;
            if (item.getType() == Material.DIRT) {
                hasDirt = true;
            } else if (source == null) {
                source = item.clone();
            }
        }

        if (occupied != 2 || !hasDirt || source == null) return null;
        return source;
    }
}