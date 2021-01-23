package com.hm.achievement.listener.statistics;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ProjectileHitEvent;

import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.RewardParser;

@Singleton
public class TargetsShotListener extends AbstractListener {

	@Inject
	public TargetsShotListener(@Named("main") YamlConfiguration mainConfig, int serverVersion, AchievementMap achievementMap,
			CacheManager cacheManager, RewardParser rewardParser) {
		super(MultipleAchievements.TARGETSSHOT, mainConfig, serverVersion, achievementMap, cacheManager, rewardParser);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onProjectileHit(ProjectileHitEvent event) {
		if (!(event.getEntity().getShooter() instanceof Player)) {
			return;
		}

		String targetName;
		if (event.getHitEntity() != null) {
			targetName = event.getHitEntity().getType().name().toLowerCase();
		} else if (event.getHitBlock() != null) {
			targetName = event.getHitBlock().getType().name().toLowerCase();
		} else {
			return;
		}

		Player player = (Player) event.getEntity().getShooter();
		if (!player.hasPermission(category.toChildPermName(targetName))) {
			return;
		}

		Set<String> foundAchievements = findAchievementsByCategoryAndName(targetName);
		updateStatisticAndAwardAchievementsIfAvailable(player, foundAchievements, 1);
	}
}
