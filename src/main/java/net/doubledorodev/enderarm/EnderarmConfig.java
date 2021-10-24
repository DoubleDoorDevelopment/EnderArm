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

            builder.pop();

            builder.push("Debug");
            debug = builder
                    .comment("Enable debug view for blocks. This will make them render as end gates regardless for everyone.")
                    .define("debug", false);
        }
    }
}
