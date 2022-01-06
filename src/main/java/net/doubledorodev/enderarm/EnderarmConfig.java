package net.doubledorodev.enderarm;

import org.apache.commons.lang3.tuple.Pair;
import net.minecraftforge.common.ForgeConfigSpec;

public class EnderarmConfig
{
    public static final EnderarmConfig.General GENERAL;
    static final ForgeConfigSpec spec;

    static
    {
        final Pair<General, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(EnderarmConfig.General::new);
        spec = specPair.getRight();
        GENERAL = specPair.getLeft();
    }

    public static class General
    {
        public ForgeConfigSpec.IntValue armDurability;
        public ForgeConfigSpec.IntValue durabilityConsumedPerBlock;
        public ForgeConfigSpec.IntValue armReach;

        public ForgeConfigSpec.BooleanValue debug;
        public ForgeConfigSpec.BooleanValue randomActivationDurability;
        public ForgeConfigSpec.BooleanValue ghostSpawnsParticles;
        public ForgeConfigSpec.BooleanValue disableLootEdit;
        public ForgeConfigSpec.BooleanValue dropUseableArms;

        public ForgeConfigSpec.DoubleValue ghostBlockAlpha;
        public ForgeConfigSpec.DoubleValue brokenArmChance;

        General(ForgeConfigSpec.Builder builder)
        {
            builder.comment("General configuration settings")
                    .push("General");

            armDurability = builder
                    .comment("Total arm durability before breaking.")
                    .defineInRange("armDurability", 500, 1, Integer.MAX_VALUE);

            durabilityConsumedPerBlock = builder
                    .comment("How much durability is consumed when a block is converted to one that can be seen through.")
                    .defineInRange("durabilityConsumedPerBlock", 5, 1, Integer.MAX_VALUE);

            armReach = builder
                    .comment("How far will the effects of the arm work. NOTE: THIS DOES NOT ALLOW YOU TO REACH FURTHER! HIGHER VALUES WILL HARM PERFORMANCE!")
                    .defineInRange("armReach", 5, 1, Integer.MAX_VALUE);

            randomActivationDurability = builder
                    .comment("Set if newly activated arms should have randomized durability on them or not.")
                    .define("randomActivationDurability", true);

            ghostBlockAlpha = builder
                    .comment("How visible the base block texture is. Higher = harder to see through. Lower = easier to see through.")
                    .defineInRange("ghostBlockAlpha", 0.5d, 0, 1);

            ghostSpawnsParticles = builder
                    .comment("Should ghost blocks spawn the purple particles when looking through them.")
                    .define("ghostSpawnsParticles", true);

            disableLootEdit = builder
                    .comment("This will disable the loot arm loot table changes done by this mod.")
                    .define("disableLootEdit", false);

            dropUseableArms = builder
                    .comment("This will drop ready to use arms instead of broken ones. (PS. You should use loot tables for better control!)")
                    .define("dropUseableArms", false);

            brokenArmChance = builder
                    .comment("Chance for an arm to drop in percent. (PS. You should use loot tables for better control!)")
                    .defineInRange("brokenArmChance", 0.01d, 0, 1);

            builder.pop();

            builder.push("Debug");
            debug = builder
                    .comment("Enable debug view for blocks. This will make smoke come off of any fake blocks for everyone!")
                    .define("debug", false);
        }
    }
}
