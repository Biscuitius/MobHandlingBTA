package biscuitius.mobhandling;

import biscuitius.mobhandling.config.MobHandlingConfig;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MobHandlingMod implements ModInitializer {
    public static final String MOD_ID = "mobhandling";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    // Boot the mod and load the JSON config before any gameplay hooks run.
    public void onInitialize() {
        MobHandlingConfig.load();
        LOGGER.info("Mob Handling has been initialized!");
    }
}