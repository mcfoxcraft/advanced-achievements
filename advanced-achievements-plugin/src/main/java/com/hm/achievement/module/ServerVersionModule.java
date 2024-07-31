package com.hm.achievement.module;

import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;

import dagger.Module;
import dagger.Provides;

@Module
public class ServerVersionModule {

	@Provides
	@Singleton
	int provideServerVersion() {
		// Simple parsing of game version. Might need to be updated in the future depending on how the Minecraft
		// versions change in the future.
		String versionIdentifier = Bukkit.getServer().getMinecraftVersion();
		return Integer.parseInt(versionIdentifier.split("\\.")[1]);
	}

}
