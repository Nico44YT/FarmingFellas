package nico.farmingfellas;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

public class FarmingFellasUtil {
    public static int rbgToInt(int red, int green, int blue) {
        int rgb = red;
        rgb = (rgb << 8) + green;
        rgb = (rgb << 8) + blue;

        return rgb;
    }

    public static int rbgToInt(float red, float green, float blue) {
        return rbgToInt((int) red * 255, (int) green * 255, (int) blue * 255);
    }

    public static int[] intToRgb(int rgb) {
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;

        return new int[]{red, green, blue};
    }

    public static float[] intToRgbFloat(int rgb) {
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;

        return new float[]{red / 255f, green / 255f, blue / 255f};
    }

    public static int randomColor(Random random) {
        float hue = random.nextFloat();
        float saturation = MathHelper.nextBetween(random, 0.6f, 0.9f);
        float brightness = MathHelper.nextBetween(random, 0.7f, 1.0f);
        return MathHelper.hsvToRgb(hue, saturation, brightness);
    }
}
