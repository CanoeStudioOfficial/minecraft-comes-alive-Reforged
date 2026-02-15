package mca.core;

import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Config implements Serializable {
    private transient final Configuration config;

    public String villagerSpawnMode;
    public double mcaVillagerSpawnRatio;
    public boolean enableDiminishingReturns;
    public boolean enableInfection;
    public int infectionChance;
    public boolean allowGrimReaper;
    public int guardSpawnRate;
    public boolean guardPatrolEnabled;
    public double guardPatrolSpeed;
    public int guardPatrolRadius;
    public int guardPatrolWaitTime;
    public boolean guardsTargetMonsters;
    public int guardAttackInterval;
    public int guardBowAttackInterval;
    public int guardBowAttackRange;
    public double guardRetreatHealthThreshold;
    public String[] guardTargetPriorities;
    public int guardSensorUpdateInterval;
    public int guardBanditPriority;
    public int guardAttackerPriority;
    public int guardUnknownMonsterPriority;
    private Map<String, Integer> parsedTargetPriorities;
    public int chanceToHaveTwins;
    public int marriageHeartsRequirement;
    public int babyGrowUpTime;
    public int childGrowUpTime;
    public int villagerSpawnerCap;
    public int villagerSpawnerRateMinutes;
    public int villagerMaxHealth;
    public boolean allowTrading;
    public boolean logVillagerDeaths;
    public boolean enableRevivals;
    public String villagerChatPrefix;
    public boolean allowPlayerMarriage;
    public boolean enableAdminCommands;
    public boolean allowRoseGoldGeneration;

    public Config(FMLPreInitializationEvent event) {
        config = new Configuration(event.getSuggestedConfigurationFile());
        addConfigValues();
    }

    private void addConfigValues() {
        villagerSpawnMode = config.get("General", "Villager Spawn Mode", "replace", "Modes: 'replace' - all vanilla villagers become MCA villagers; 'coexist' - both types spawn together; 'vanilla' - keep original villagers only.").getString();
        mcaVillagerSpawnRatio = config.get("General", "MCA Villager Spawn Ratio", 0.5, "In 'coexist' mode, chance that a spawning villager is MCA type (0.0-1.0). Default 0.5 (50%).").getDouble();
        enableDiminishingReturns = config.get("General", "Enable Interaction Fatigue?", true, "Should interactions yield diminishing returns over time?").getBoolean();
        enableInfection = config.get("General", "Enable Zombie Infection?", true, "Should zombies be able to infect villagers?").getBoolean();
        infectionChance = config.get("General", "Chance of Infection", 5, "Chance that a villager will be infected on hit from a zombie. Default is 5 for 5%.").getInt();
        allowGrimReaper = config.get("General", "Allow Grim Reaper?", true, "Should the Grim Reaper boss be enabled?").getBoolean();
        guardSpawnRate = config.get("General", "Guard Spawn Rate", 6, "How many villagers that should be in a village before a guard spawns.").getInt();
        guardPatrolEnabled = config.get("General", "Guard Patrol Enabled", true, "Enable guards to patrol around the village.").getBoolean();
        guardPatrolSpeed = config.get("General", "Guard Patrol Speed", 0.5, "Movement speed of guards while patrolling.").getDouble();
        guardPatrolRadius = config.get("General", "Guard Patrol Radius", 64, "Radius within which guards will search for their village.").getInt();
        guardPatrolWaitTime = config.get("General", "Guard Patrol Wait Time", 120, "Time in ticks a guard will wait at each patrol point before moving to the next.").getInt();
        guardsTargetMonsters = config.get("General", "Guards Target Monsters", false, "If true, guards will attack all monsters including modded ones. May cause guards to attack neutral mobs.").getBoolean();
        guardAttackInterval = config.get("General", "Guard Attack Interval", 20, "Ticks between guard melee attacks.").getInt();
        guardBowAttackInterval = config.get("General", "Guard Bow Attack Interval", 20, "Ticks between guard bow shots.").getInt();
        guardBowAttackRange = config.get("General", "Guard Bow Attack Range", 15, "Maximum range for guard bow attacks.").getInt();
        guardRetreatHealthThreshold = config.get("General", "Guard Retreat Health Threshold", 0.25, "Health fraction below which guards will retreat (0.0-1.0). Default 0.25 (25%).").getDouble();
        guardTargetPriorities = config.get("Guard Targets", "Target Priorities", new String[]{
                "minecraft:zombie=2",
                "minecraft:drowned=2",
                "minecraft:husk=2",
                "minecraft:evoker=3",
                "minecraft:vindicator=3",
                "minecraft:vex=2",
                "minecraft:spider=1",
                "minecraft:cave_spider=1",
                "minecraft:skeleton=2",
                "minecraft:stray=2",
                "minecraft:witch=2",
                "minecraft:enderman=1",
                "minecraft:creeper=-1"
        }, "Target priorities for guards. Format: modid:entity=priority. Higher priority = more important. Negative = ignore.").getStringList();
        guardSensorUpdateInterval = config.get("Guard Targets", "Sensor Update Interval", 20, "Ticks between guard sensor updates. Lower = faster response but more CPU.").getInt();
        guardBanditPriority = config.get("Guard Targets", "Bandit Priority", 10, "Priority for targeting bandits (MCA bandit villagers).").getInt();
        guardAttackerPriority = config.get("Guard Targets", "Attacker Priority", 9, "Priority for targeting entities attacking the guard.").getInt();
        guardUnknownMonsterPriority = config.get("Guard Targets", "Unknown Monster Priority", 3, "Priority for unknown monsters when guardsTargetMonsters is true.").getInt();
        parsedTargetPriorities = parseTargetPriorities(guardTargetPriorities);
        chanceToHaveTwins = config.get("General", "Chance to Have Twins", 2, "Chance that you will have twins. Default is 2 for 2%.").getInt();
        marriageHeartsRequirement = config.get("General", "Marriage Hearts Requirement", 100, "Number of hearts required to get married.").getInt();
        babyGrowUpTime = config.get("General", "Baby Grow Up Time (Minutes)", 30, "Minutes it takes for a baby to be ready to grow up.").getInt();
        childGrowUpTime = config.get("General", "Child Grow Up Time (Minutes)", 60, "Minutes it takes for a child to grow into an adult.").getInt();
        villagerSpawnerCap = config.get("General", "Villager Spawner Cap", 5, "Maximum number of villagers that a spawner will create in the area before it stops.").getInt();
        villagerSpawnerRateMinutes = config.get("General", "Villager Spawner Rate", 30, "The spawner will spawn 1 villager per this many minutes.").getInt();
        allowTrading = config.get("General", "Enable Trading?", true, "Is trading with villagers enabled?").getBoolean();
        logVillagerDeaths = config.get("General", "Log Villager Deaths?", true, "Should villager deaths be logged?").getBoolean();
        enableRevivals = config.get("General", "Enable Revivals?", true, "Should reviving dead villagers be enabled?").getBoolean();
        villagerChatPrefix = config.get("General", "Villager Chat Prefix", "", "Formatting prefix used for all chat with villagers.").getString();
        allowPlayerMarriage = config.get("General", "Allow Player Marriage?", true, "Enables or disables player marriage.").getBoolean();
        enableAdminCommands = config.get("General", "Enable Admin Commands?", true, "Enables or disables MCA admin commands for ops.").getBoolean();
        allowRoseGoldGeneration = config.get("General", "Allow Rose Gold World Generation", true, "If enabled, generates rose gold in your world. If disabled, generates stone instead.").getBoolean();
        villagerMaxHealth = config.get("General", "Villager Max Health", 20, "Each villager's maximum health. 1 point equals 1 heart.").getInt();
        config.save();
    }

    public Configuration getInstance() {
        return config;
    }

    public List<IConfigElement> getCategories() {
        List<IConfigElement> elements = new ArrayList<>();

        for (String s : config.getCategoryNames()) {
            if (s.equals("server")) continue;

            IConfigElement element = new ConfigElement(config.getCategory(s));
            elements.addAll(element.getChildElements());
        }

        return elements;
    }

    private Map<String, Integer> parseTargetPriorities(String[] priorities) {
        Map<String, Integer> result = new HashMap<>();
        for (String entry : priorities) {
            String[] parts = entry.split("=");
            if (parts.length == 2) {
                try {
                    String entityId = parts[0].trim();
                    int priority = Integer.parseInt(parts[1].trim());
                    result.put(entityId, priority);
                } catch (NumberFormatException e) {
                }
            }
        }
        return result;
    }

    public int getTargetPriority(String entityId) {
        if (parsedTargetPriorities == null) {
            parsedTargetPriorities = parseTargetPriorities(guardTargetPriorities);
        }
        return parsedTargetPriorities.getOrDefault(entityId, Integer.MIN_VALUE);
    }

    public boolean hasTargetPriority(String entityId) {
        if (parsedTargetPriorities == null) {
            parsedTargetPriorities = parseTargetPriorities(guardTargetPriorities);
        }
        return parsedTargetPriorities.containsKey(entityId);
    }
}