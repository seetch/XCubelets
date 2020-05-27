package me.davidml16.acubelets.handlers;

import me.davidml16.acubelets.Main;
import me.davidml16.acubelets.enums.CraftType;
import me.davidml16.acubelets.objects.CraftIngredient;
import me.davidml16.acubelets.objects.CraftParent;
import me.davidml16.acubelets.objects.CubeletType;
import me.davidml16.acubelets.utils.ColorUtil;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class CubeletCraftingHandler {

    private File file;
    private YamlConfiguration config;

    private List<CraftParent> crafts;

    private int inventorySize;

    private Main main;

    public CubeletCraftingHandler(Main main) {
        this.main = main;
        this.crafts = new ArrayList<>();
    }

    public File getFile() {
        return file;
    }

    public YamlConfiguration getConfig() {
        return config;
    }

    public List<CraftParent> getCrafts() {
        return crafts;
    }

    public CraftParent getCraftById(String id) {
        for (CraftParent craftParent : crafts) {
            if(craftParent.getCubeletType().equalsIgnoreCase(id))
                return craftParent;
        }
        return null;
    }

    public void saveConfig() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadCrafting() {
        crafts.clear();

        file = new File(main.getDataFolder(), "cubelet-crafting.yml");
        config = YamlConfiguration.loadConfiguration(file);
        if(!file.exists()) {
            try {
                file.createNewFile();
                config.set("cubelet-crafting.settings.rows", 4);
                config.set("cubelet-crafting.crafts", new ArrayList<>());
                saveConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(!config.contains("cubelet-crafting.settings.rows"))
            config.set("cubelet-crafting.settings.rows", 4);

        if(!config.contains("cubelet-crafting.crafts"))
            config.set("cubelet-crafting.crafts", new ArrayList<>());

        saveConfig();

        inventorySize = config.getInt("cubelet-crafting.settings.rows");

        Main.log.sendMessage(ColorUtil.translate("  &eLoading crafts:"));

        if(config.contains("cubelet-crafting.crafts")) {
            if (config.getConfigurationSection("cubelet-crafting.crafts") != null) {
                for (String cubeletType : config.getConfigurationSection("cubelet-crafting.crafts").getKeys(false)) {
                    if(!main.getCubeletTypesHandler().getTypes().containsKey(cubeletType)) continue;

                    int slot = config.getInt("cubelet-crafting.crafts." + cubeletType + ".slot");
                    if(slot > (getInventorySize() - 10)) continue;

                    List<CraftIngredient> ingredients = new ArrayList<>();
                    if(config.contains("cubelet-crafting.crafts." + cubeletType + ".ingredients")) {
                        if (config.getConfigurationSection("cubelet-crafting.crafts." + cubeletType + ".ingredients") != null) {
                            for (int i = 1; i <= config.getConfigurationSection("cubelet-crafting.crafts." + cubeletType + ".ingredients").getKeys(false).size(); i++) {
                                String type = config.getString("cubelet-crafting.crafts." + cubeletType + ".ingredients." + i + ".type");
                                if (!type.equalsIgnoreCase("money") && !type.equalsIgnoreCase("cubelet")) continue;

                                int amount = config.getInt("cubelet-crafting.crafts." + cubeletType + ".ingredients." + i + ".amount");

                                if (type.equalsIgnoreCase("money")) {
                                    if (main.getEconomyHandler().isLoaded())
                                        ingredients.add(new CraftIngredient(cubeletType, CraftType.MONEY, amount));
                                } else if (type.equalsIgnoreCase("cubelet")) {
                                    String name = config.getString("cubelet-crafting.crafts." + cubeletType + ".ingredients." + i + ".name");
                                    if (name == null) continue;
                                    if (!main.getCubeletTypesHandler().getTypes().containsKey(name)) continue;

                                    ingredients.add(new CraftIngredient(cubeletType, CraftType.CUBELET, name, amount));
                                }
                            }
                        }
                    }

                    crafts.add(new CraftParent(cubeletType, slot, ingredients));
                }
            }
        }

        if(crafts.size() == 0)
            Main.log.sendMessage(ColorUtil.translate("    &cNo Cubelet crafts has been loaded!"));
        else
            Main.log.sendMessage(ColorUtil.translate("    &b" + crafts.size() + " &aCubelet crafts loaded!"));
        Main.log.sendMessage(ColorUtil.translate(""));

    }

    public int getInventorySize() {
        int size = this.inventorySize;
        if(size >= 2 && size <= 6)
            return size * 9;
        return 4 * 9;
    }

    public boolean haveIngredient(Player player, CraftIngredient ingredient) {
        if(ingredient.getCraftType() == CraftType.CUBELET) {
            long amount = main.getPlayerDataHandler()
                    .getData(Objects.requireNonNull(player.getPlayer()))
                    .getCubelets().stream().filter(cubelet -> cubelet.getType().equalsIgnoreCase(ingredient.getName())).count();

            return amount >= ingredient.getAmount();
        } else if(ingredient.getCraftType() == CraftType.MONEY) {
            return main.getEconomyHandler().getBalance(player) >= ingredient.getAmount();
        }

        return false;
    }

    public boolean haveIngredients(Player player, CraftParent craft) {
        for(CraftIngredient ingredient : craft.getIngrediens()) {
            if(!haveIngredient(player, ingredient)) return false;
        }
        return true;
    }

    public void removeIngredients(Player player, CraftParent craft) {
        for(CraftIngredient ingredient : craft.getIngrediens()) {
            if(ingredient.getCraftType() == CraftType.CUBELET) {
                try {
                    main.getCubeletTypesHandler().removeCubelet(player.getUniqueId(), ingredient.getName(), ingredient.getAmount());
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            } else if(ingredient.getCraftType() == CraftType.MONEY) {
                main.getEconomyHandler().removeBalance(player, ingredient.getAmount());
            }
        }
    }

    public boolean containsEconomyIngredient(List<CraftParent> crafts) {
        if(crafts.size() == 0) return false;
        for(CraftParent craft : crafts) {
            for(CraftIngredient ingredient : craft.getIngrediens()) {
                if(ingredient.getCraftType() == CraftType.MONEY) return true;
            }
        }
        return false;
    }

}