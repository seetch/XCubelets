package me.davidml16.acubelets.handlers;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.VisibilityManager;
import com.gmail.filoghost.holographicdisplays.api.line.ItemLine;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import me.davidml16.acubelets.Main;
import me.davidml16.acubelets.objects.CubeletBox;
import me.davidml16.acubelets.enums.CubeletBoxState;
import me.davidml16.acubelets.objects.Reward;
import me.davidml16.acubelets.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class HologramHandler {

    private Main main;

    private boolean red;

    public HologramHandler(Main main) {
        this.main = main;
        this.red = false;
    }

    public void loadHolograms() {
        for(Player p : Bukkit.getOnlinePlayers()) {
            loadHolograms(p);
        }
    }

    public void loadHolograms(CubeletBox box) {
        for(Player p : Bukkit.getOnlinePlayers()) {
            loadHolograms(p, box);
        }
    }

    public void removeHolograms() {
        for (Hologram hologram : HologramsAPI.getHolograms(main)) {
            hologram.delete();
        }
    }

    public void removeHolograms(CubeletBox box) {
        for(Player p : Bukkit.getOnlinePlayers()) {
            removeHolograms(p, box);
        }
    }

    public void reloadHolograms() {
        for(Player p : Bukkit.getOnlinePlayers()) {
            reloadHolograms(p);
        }
        this.red = !this.red;
    }

    public void loadHolograms(Player p) {
        for(CubeletBox box : main.getCubeletBoxHandler().getBoxes().values()) {
            Hologram hologram = HologramsAPI.createHologram(main, box.getLocation().clone().add(0.5, 2, 0.5));
            VisibilityManager visibilityManager = hologram.getVisibilityManager();

            visibilityManager.showTo(p);
            visibilityManager.setVisibleByDefault(false);

            if(box.getState() == CubeletBoxState.EMPTY) {
                for (String line : getLines(p)) {
                    hologram.appendTextLine(line);
                }
            } else if(box.getState() == CubeletBoxState.REWARD) {
                hologram.teleport(box.getLocation().clone().add(0.5, (4 * 0.33) + 1, 0.5));
                for (String line : getLinesReward(p, box.getPlayerOpening(), box.getLastReward())) {
                    hologram.appendTextLine(line);
                }
                hologram.appendItemLine(box.getLastReward().getIcon().getItem());
            }

            box.getHolograms().put(p.getUniqueId(), hologram);
        }
    }

    public void loadHolograms(Player p, CubeletBox box) {
        Hologram hologram = HologramsAPI.createHologram(main, box.getLocation().clone().add(0.5, 2, 0.5));
        VisibilityManager visibilityManager = hologram.getVisibilityManager();

        visibilityManager.showTo(p);
        visibilityManager.setVisibleByDefault(false);

        if(box.getState() == CubeletBoxState.EMPTY) {
            for (String line : getLines(p)) {
                hologram.appendTextLine(line);
            }
        } else if(box.getState() == CubeletBoxState.REWARD) {
            hologram.teleport(box.getLocation().clone().add(0.5, (4 * 0.33) + 1, 0.5));
            for (String line : getLinesReward(p, box.getPlayerOpening(), box.getLastReward())) {
                hologram.appendTextLine(line);
            }
            hologram.appendItemLine(box.getLastReward().getIcon().getItem());
        }

        box.getHolograms().put(p.getUniqueId(), hologram);
    }

    public void reloadHolograms(Player p) {
        for(CubeletBox box : main.getCubeletBoxHandler().getBoxes().values()) {
            reloadHologram(p, box);
        }
    }

    public void reloadHologram(CubeletBox box) {
        if(box.getState() == CubeletBoxState.EMPTY) {
            for(Player p : Bukkit.getOnlinePlayers()) {
                if (box.getHolograms().containsKey(p.getUniqueId())) {
                    Hologram hologram = box.getHolograms().get(p.getUniqueId());

                    hologram.teleport(box.getLocation().clone().add(0.5, (3 * 0.33) + 1, 0.5));

                    for (String line : getLines(p)) {
                        hologram.appendTextLine(line);
                    }
                }
            }
        } else if(box.getState() == CubeletBoxState.REWARD) {
            for(Player p : Bukkit.getOnlinePlayers()) {
                if (box.getHolograms().containsKey(p.getUniqueId())) {
                    Hologram hologram = box.getHolograms().get(p.getUniqueId());
                    hologram.teleport(box.getLocation().clone().add(0.5, (4 * 0.33) + 1, 0.5));

                    List<String> lines = getLinesReward(p, box.getPlayerOpening(), box.getLastReward());
                    for (int i = 0; i < lines.size(); i++) {
                        ((TextLine) hologram.getLine(i)).setText(lines.get(i));
                    }
                    ((ItemLine) hologram.getLine(3)).setItemStack(box.getLastReward().getIcon().getItem());
                }
            }
        }
    }

    public void rewardHologram(CubeletBox box, Reward reward) {
        for(Player p : Bukkit.getOnlinePlayers()) {
            if (box.getHolograms().containsKey(p.getUniqueId())) {
                Hologram hologram = box.getHolograms().get(p.getUniqueId());
                hologram.clearLines();

                hologram.teleport(box.getLocation().clone().add(0.5, (4 * 0.33) + 1, 0.5));

                for (String line : getLinesReward(p, box.getPlayerOpening(), reward)) {
                    hologram.appendTextLine(line);
                }
                hologram.appendItemLine(reward.getIcon().getItem());
            }
        }
    }

    public void reloadHologram(Player p, CubeletBox box) {
        if(box.getState() == CubeletBoxState.EMPTY) {
            if(box.getHolograms().containsKey(p.getUniqueId())) {
                List<String> lines = getLines(p);
                Hologram hologram = box.getHolograms().get(p.getUniqueId());

                hologram.teleport(box.getLocation().clone().add(0.5, (3 * 0.33) + 1, 0.5));

                if(hologram.size() > lines.size()) {
                    hologram.getLine(2).removeLine();
                } else if(hologram.size() < lines.size()) {
                    hologram.appendTextLine(lines.get(2));
                }

                for (int i = 0; i < lines.size(); i++) {
                    ((TextLine) hologram.getLine(i)).setText(lines.get(i));
                }
            }
        } else if(box.getState() == CubeletBoxState.REWARD) {
            if (box.getHolograms().containsKey(p.getUniqueId())) {
                Hologram hologram = box.getHolograms().get(p.getUniqueId());
                hologram.teleport(box.getLocation().clone().add(0.5, (4 * 0.33) + 1, 0.5));

                List<String> lines = getLinesReward(p, box.getPlayerOpening(), box.getLastReward());
                for (int i = 0; i < lines.size(); i++) {
                    ((TextLine) hologram.getLine(i)).setText(lines.get(i));
                }
                ((ItemLine) hologram.getLine(3)).setItemStack(box.getLastReward().getIcon().getItem());
            }
        }
    }

    public void removeHolograms(Player p) {
        for(CubeletBox box : main.getCubeletBoxHandler().getBoxes().values()) {
            if(box.getHolograms().containsKey(p.getUniqueId())) {
                box.getHolograms().get(p.getUniqueId()).delete();
                box.getHolograms().remove(p.getUniqueId());
            }
        }
    }

    public void removeHolograms(Player p, CubeletBox box) {
        if(box.getHolograms().containsKey(p.getUniqueId())) {
            box.getHolograms().get(p.getUniqueId()).delete();
            box.getHolograms().remove(p.getUniqueId());
        }
    }

    public List<String> getLines(Player p) {
        List<String> lines = new ArrayList<String>();
        int available = main.getPlayerDataHandler().getData(p).getCubelets().size();
        if(available > 0) {
            lines.add(ColorUtil.translate(main.getLanguageHandler().getMessage("Holograms.CubeletAvailable.Line1").replaceAll("%cubelets_available%", String.valueOf(available))));
            lines.add(ColorUtil.translate(main.getLanguageHandler().getMessage("Holograms.CubeletAvailable.Line2").replaceAll("%cubelets_available%", String.valueOf(available))));
            lines.add(ColorUtil.translate(this.red ? "&c" : "&f") + main.getLanguageHandler().getMessage("Holograms.CubeletAvailable.Line3").replaceAll("%cubelets_available%", String.valueOf(available)));
        } else {
            lines.add(ColorUtil.translate(main.getLanguageHandler().getMessage("Holograms.CubeletAvailable.Line1")));
            lines.add(ColorUtil.translate(main.getLanguageHandler().getMessage("Holograms.CubeletAvailable.Line2")));
        };
        return lines;
    }

    public List<String> getLinesReward(Player p, Player opening, Reward reward) {
        List<String> lines = new ArrayList<String>();

        if (p.getUniqueId().toString().equalsIgnoreCase(opening.getUniqueId().toString())) {
            lines.add(ColorUtil.translate(main.getLanguageHandler().getMessage("Holograms.Reward.Line1.You")));
        }else {
            lines.add(ColorUtil.translate(main.getLanguageHandler().getMessage("Holograms.Reward.Line1.Other").replaceAll("%player%", opening.getName())));
        }
        lines.add(ColorUtil.translate(main.getLanguageHandler().getMessage("Holograms.Reward.Line2")
                .replaceAll("%reward_name%", reward.getName())
                .replaceAll("%reward_rarity%", reward.getRarity().getName())));
        lines.add(ColorUtil.translate(main.getLanguageHandler().getMessage("Holograms.Reward.Line3")
                .replaceAll("%reward_name%", reward.getName())
                .replaceAll("%reward_rarity%", reward.getRarity().getName())));
        return lines;
    }

}