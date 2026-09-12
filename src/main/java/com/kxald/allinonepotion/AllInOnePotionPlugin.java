package com.kxald.allinonepotion;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private static final char N = 'N';
    private static final char W = 'W';
    private static final char S = 'S';
    private static final String[] DEFAULT_SHAPE = {"NW", "S "};
    private static final Material DEFAULT_N = Material.NETHER_WART;
    private static final Material DEFAULT_S = Material.DIAMOND_SWORD;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        registerRecipe();
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("All-in-One Splash Potion recipe registered.");
    }

    private void registerRecipe() {
        String[] shape = buildShape();

        ShapedRecipe recipe = new ShapedRecipe(
                new NamespacedKey(this, "all_in_one_splash"),
                createPotion()
        );

        recipe.shape(shape);
        recipe.setGroup("all_in_one_potion");

        Set<Character> chars = new HashSet<>();
        for (String line : shape) {
            for (char c : line.toCharArray()) {
                chars.add(c);
            }
        }
        for (char c : chars) {
            if (c == ' ') continue;
            RecipeChoice choice = ingredientFor(c);
            if (choice != null) {
                recipe.setIngredient(c, choice);
            } else {
                getLogger().warning("Unknown slot marker '" + c + "' in potion-recipe shape, ignoring it.");
            }
        }

        getServer().addRecipe(recipe);
    }

    private String[] buildShape() {
        List<String> lines = getConfig().getStringList("potion-recipe.shape");
        if (lines.isEmpty() || lines.size() > 3) return DEFAULT_SHAPE;

        int width = lines.get(0).length();
        for (String line : lines) {
            if (line.length() != width || width < 1 || width > 3) return DEFAULT_SHAPE;
            for (char c : line.toCharArray()) {
                if (c != ' ' && c != N && c != W && c != S) return DEFAULT_SHAPE;
            }
        }
        return lines.toArray(new String[0]);
    }

    private RecipeChoice ingredientFor(char c) {
        String value = getConfig().getString("potion-recipe.ingredients." + c);
        switch (c) {
            case N -> {
                return new RecipeChoice.MaterialChoice(materialFor(value, DEFAULT_N));
            }
            case W -> {
                return new RecipeChoice.ExactChoice(createWaterBottle());
            }
            case S -> {
                return new RecipeChoice.MaterialChoice(materialFor(value, DEFAULT_S));
            }
            default -> {
                return null;
            }
        }
    }

    private Material materialFor(String name, Material fallback) {
        if (name == null) return fallback;
        Material material = Material.matchMaterial(name);
        return material != null ? material : fallback;
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