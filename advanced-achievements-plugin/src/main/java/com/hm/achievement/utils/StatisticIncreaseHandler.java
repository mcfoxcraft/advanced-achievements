package com.hm.achievement.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.hm.achievement.category.Category;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.domain.Achievement;
import com.hm.achievement.lifecycle.Reloadable;

/**
 * Abstract class in charge of factoring out common functionality for classes which track statistic increases (such as
 * listeners or runnables).
 * 
 * @author Pyves
 */
@Singleton
public class StatisticIncreaseHandler implements Reloadable {

	protected final YamlConfiguration mainConfig;
	protected final int serverVersion;
	protected final AchievementMap achievementMap;
	protected final CacheManager cacheManager;
	protected final RewardParser rewardParser;

	private boolean configRestrictCreative;
	private boolean configRestrictSpectator;
	private boolean configRestrictAdventure;
	private Set<String> configExcludedWorlds;

	@Inject
	public StatisticIncreaseHandler(@Named("main") YamlConfiguration mainConfig, int serverVersion,
			AchievementMap achievementMap, CacheManager cacheManager, RewardParser rewardParser) {
		this.mainConfig = mainConfig;
		this.serverVersion = serverVersion;
		this.achievementMap = achievementMap;
		this.cacheManager = cacheManager;
		this.rewardParser = rewardParser;
	}

	@Override
	public void extractConfigurationParameters() {
		configRestrictCreative = mainConfig.getBoolean("RestrictCreative");
		configRestrictSpectator = mainConfig.getBoolean("RestrictSpectator");
		configRestrictAdventure = mainConfig.getBoolean("RestrictAdventure");
		// Spectator mode introduced in Minecraft 1.8. Automatically relevant parameter for older versions.
		if (configRestrictSpectator && serverVersion < 8) {
			configRestrictSpectator = false;
		}
		configExcludedWorlds = new HashSet<>(mainConfig.getStringList("ExcludedWorlds"));
	}

	/**
	 * Compares the current value to the achievement thresholds. If a threshold is reached, awards the achievement if it
	 * wasn't previously received.
	 * 
	 * @param player
	 * @param category
	 * @param currentValue
	 */
	public void checkThresholdsAndAchievements(Player player, Category category, long currentValue) {
		checkThresholdsAndAchievements(player, achievementMap.getForCategory(category), currentValue);
	}

	/**
	 * Compares the current value to the achievement thresholds in the same subcategory. If a threshold is reached,
	 * awards the achievement if it wasn't previously received.
	 * 
	 * @param player
	 * @param category
	 * @param subcategory
	 * @param currentValue
	 */
	public void checkThresholdsAndAchievements(Player player, Category category, String subcategory,
			long currentValue) {
		checkThresholdsAndAchievements(player, achievementMap.getForCategoryAndSubcategory(category, subcategory),
				currentValue);
	}

	private void checkThresholdsAndAchievements(Player player, List<Achievement> achievements, long currentValue) {
		for (Achievement achievement : achievements) {
			// Check whether player has met the threshold.
			if (currentValue >= achievement.getThreshold()) {
				// Check whether player has received the achievement and has permission to do so.
				if (!cacheManager.hasPlayerAchievement(player.getUniqueId(), achievement.getName())
						&& player.hasPermission("achievement." + achievement.getName())) {
					Bukkit.getPluginManager().callEvent(new PlayerAdvancedAchievementEvent(player, achievement));
				}
			} else {
				// Entries in List sorted in increasing order, all subsequent thresholds will fail the condition.
				return;
			}
		}
	}

	/**
	 * Determines whether the statistic increase should be taken into account.
	 * 
	 * @param player
	 * @param category
	 * @return true if the increase should be taken into account, false otherwise
	 */
	protected boolean shouldIncreaseBeTakenIntoAccount(Player player, Category category) {
		GameMode gameMode = player.getGameMode();
		return !player.hasMetadata("NPC")
				&& player.hasPermission(category.toPermName())
				&& (!configRestrictCreative || gameMode != GameMode.CREATIVE)
				&& (!configRestrictSpectator || gameMode != GameMode.SPECTATOR)
				&& (!configRestrictAdventure || gameMode != GameMode.ADVENTURE)
				&& !configExcludedWorlds.contains(player.getWorld().getName());
	}

}
