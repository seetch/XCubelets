package me.davidml16.acubelets.animations.animation.animation15;

import me.davidml16.acubelets.Main;
import me.davidml16.acubelets.animations.ASSpawner;
import me.davidml16.acubelets.animations.Animation;
import me.davidml16.acubelets.animations.AnimationSettings;
import me.davidml16.acubelets.api.CubeletOpenEvent;
import me.davidml16.acubelets.enums.CubeletBoxState;
import me.davidml16.acubelets.objects.CubeletBox;
import me.davidml16.acubelets.objects.CubeletType;
import me.davidml16.acubelets.objects.rewards.PermissionReward;
import me.davidml16.acubelets.objects.rewards.Reward;
import me.davidml16.acubelets.utils.*;
import me.davidml16.acubelets.utils.ParticlesAPI.Particles;
import me.davidml16.acubelets.utils.ParticlesAPI.UtilParticles;
import me.davidml16.acubelets.utils.XSeries.ParticleDisplay;
import me.davidml16.acubelets.utils.XSeries.XParticle;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

public class Animation15_Task extends Animation {

	public Animation15_Task(Main main, AnimationSettings animationSettings) {
		super(main, animationSettings);
	}

	private ArmorStand armorStand;
	private Location armorStandLocation;

	private Animation15_Serpent serpent;
	private Animation15_Blocks blocks;
	private Animation15_Spiral spiral;

	private Set<Animation15_RotatingFlame> orbits = new HashSet<>();

	private BukkitTask blackHole;

	@Override
	public void onTick(int time) {

		if (time > 0 && time < 10)
			Objects.requireNonNull(armorStandLocation).add(0, 0.25, 0);
		else if (time > 10 && time < 20)
			Objects.requireNonNull(armorStandLocation).add(0, 0.20, 0);
		else if (time > 20 && time < 30)
			Objects.requireNonNull(armorStandLocation).add(0, 0.15, 0);
		else if (time > 30 && time < 40)
			Objects.requireNonNull(armorStandLocation).add(0, 0.10, 0);
		else if (time > 40 && time < 50)
			Objects.requireNonNull(armorStandLocation).add(0, 0.05, 0);

		if(armorStand != null && time < 152) {

			armorStand.teleport(armorStandLocation);
			Objects.requireNonNull(armorStand).setHeadPose(armorStand.getHeadPose().add(0, 0.26, 0));

		}

		if(time == 45) {

			serpent = new Animation15_Serpent(getCubeletBox().getLocation().clone().add(0, 1, 0).setDirection(new Vector(-1, 0, -1)), false);
			serpent.runTaskTimer(getMain(), 0, 1);

			Location eye = armorStand.getEyeLocation().add(0.0D, 0.4D, 0.0D);
			for (Location location2 : LocationUtils.getCircle(armorStand.getLocation().clone().add(0, 0.75,0), 0.25D, 50)) {
				Vector direction = location2.toVector().subtract(armorStand.getLocation().clone().add(0, 0.75,0).toVector()).normalize();
				UtilParticles.display(Particles.FLAME, direction, eye, 0.3F);
			}

			Sounds.playSound(getCubeletBox().getLocation(), Sounds.MySound.FIREWORK_BLAST, 0.5f, 0);

			try {
				blackHole = XParticle.blackhole(getMain(), 8, 4, 200, 2, 999999, ParticleDisplay.display(getCubeletBox().getLocation().clone().add(0.5, 7, 0.5), Particle.SMOKE_LARGE).withCount(1));
			} catch (NoClassDefFoundError ignore) {}

			Animation15_RotatingFlame orbit1 = new Animation15_RotatingFlame(getMain(), getCubeletBox().getLocation().clone().add(0.5, 6, 0.5), 5f, 120, true, 0);
			orbit1.runTaskTimer(getMain(), 0L, 1L);
			orbits.add(orbit1);

			Animation15_RotatingFlame orbit2 = new Animation15_RotatingFlame(getMain(), getCubeletBox().getLocation().clone().add(0.5, 6, 0.5), 5f, 120, true, 40);
			orbit2.runTaskTimer(getMain(), 0L, 1L);
			orbits.add(orbit2);

			Animation15_RotatingFlame orbit3 = new Animation15_RotatingFlame(getMain(), getCubeletBox().getLocation().clone().add(0.5, 6, 0.5), 5f, 120, true, 80);
			orbit3.runTaskTimer(getMain(), 0L, 1L);
			orbits.add(orbit3);

			spiral.cancel();

		}

		if (time == 150) {

			if(blackHole != null)
				blackHole.cancel();

			try {
				for(Animation15_RotatingFlame orbit : orbits) {
					orbit.cancel();
					if(getMain().getAnimationHandler().getEntities().contains(orbit.getArmorStand())) {
						ArmorStand armorStandOrbit = orbit.getArmorStand();
						if(armorStandOrbit != null) armorStandOrbit.remove();
						getMain().getAnimationHandler().getEntities().remove(armorStandOrbit);
					}
				}
			} catch(IllegalStateException | NullPointerException ignored) {}

			Sounds.playSound(getCubeletBox().getLocation(), Sounds.MySound.IRONGOLEM_DEATH, 0.5f, 0);
			UtilParticles.display(Particles.EXPLOSION_LARGE, armorStand.getLocation().clone().add(0, 1.25, 0), 2);

			armorStand.setGravity(true);
			ASSpawner.setEntityNoclip(armorStand);

		} else if(time > 150 && time < 165) {

			UtilParticles.display(Particles.FLAME, 0.10F, 0.25F, 0.15F, armorStand.getLocation().clone().add(0, 1.5, 0), 5);

		}

		if(time == 163) {

			doPreRewardReveal();

		}

		if(time == 185)
			Sounds.playSound(getCubeletBox().getLocation(), Sounds.MySound.LEVEL_UP, 0.5F, 1F);

	}

	@Override
	public void onStart() {

		blocks = new Animation15_Blocks(getCubeletBox().getLocation());
		blocks.runTaskTimer(getMain(), 0L, 6L);

		setColors(Arrays.asList(Color.BLACK, Color.ORANGE));

		armorStand = ASSpawner.spawn(
				getMain(),
				getCubeletBox().getLocation().clone().add(0.5, -0.3, 0.5),
				SkullCreator.itemFromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTlhNWExZTY5YjRmODEwNTYyNTc1MmJjZWUyNTM0MDY2NGIwODlmYTFiMmY1MjdmYTkxNDNkOTA2NmE3YWFkMiJ9fX0="),
				false,
				false,
				true
		);
		getMain().getAnimationHandler().getEntities().add(armorStand);

		armorStandLocation = armorStand.getLocation();
		Sounds.playSound(getCubeletBox().getLocation(), Sounds.MySound.FIREWORK_LAUNCH, 0.5f, 0);

		spiral = new Animation15_Spiral(armorStand);
		spiral.runTaskTimer(getMain(), 0L, 1L);

	}

	@Override
	public void onStop() {

		if(serpent != null) serpent.cancel();
		if(blackHole != null) blackHole.cancel();

		try {
			for(Animation15_RotatingFlame orbit : orbits) {
				orbit.cancel();
				if(getMain().getAnimationHandler().getEntities().contains(orbit.getArmorStand())) {
					ArmorStand armorStandOrbit = orbit.getArmorStand();
					if(armorStandOrbit != null) armorStandOrbit.remove();
					getMain().getAnimationHandler().getEntities().remove(armorStandOrbit);
				}
			}
		} catch(IllegalStateException | NullPointerException ignored) {}

		spiral.cancel();

		blocks.cancel();
		if(blocks != null) blocks.restore();

		if(getMain().getAnimationHandler().getEntities().contains(armorStand)) {
			if(armorStand != null) armorStand.remove();
			getMain().getAnimationHandler().getEntities().remove(armorStand);
		}

	}

	@Override
	public void onPreRewardReveal() {

		Sounds.playSound(armorStand.getLocation(), Sounds.MySound.EXPLODE, 0.5F, 1F);

		getMain().getFireworkUtil().spawn(
				getCubeletBox().getLocation().clone().add(0.5, 1.50, 0.5),
				FireworkEffect.Type.BURST,
				getColors().get(0),
				getColors().get(1)
		);

	}

	@Override
	public void onRewardReveal() {

		armorStand.remove();
		armorStand = null;

	}

}