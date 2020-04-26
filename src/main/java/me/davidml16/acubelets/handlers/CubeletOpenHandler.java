package me.davidml16.acubelets.handlers;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import me.davidml16.acubelets.Main;
import me.davidml16.acubelets.animations.Animation;
import me.davidml16.acubelets.objects.CubeletBox;
import me.davidml16.acubelets.enums.CubeletBoxState;
import me.davidml16.acubelets.objects.CubeletType;
import org.bukkit.entity.Player;

public class CubeletOpenHandler {

    private Main main;
    public CubeletOpenHandler(Main main) {
        this.main = main;
    }

    public void openAnimation(Player p, CubeletBox box, CubeletType type) {
        if(box.getState() == CubeletBoxState.EMPTY) {
            box.setPlayerOpening(p);

            for (Hologram hologram : box.getHolograms().values()) {
                hologram.clearLines();
            }

            Animation animation = main.getAnimationHandler().getAnimation(type.getAnimation());
            animation.start(box, type);
        } else {
            if(box.getPlayerOpening().getUniqueId() == p.getUniqueId()) {
                p.sendMessage(main.getLanguageHandler().getMessage("Cubelet.BoxInUse.Me"));
            } else {
                p.sendMessage(main.getLanguageHandler().getMessage("Cubelet.BoxInUse.Other")
                        .replaceAll("%player%", box.getPlayerOpening().getName()));
            }
        }
    }

}