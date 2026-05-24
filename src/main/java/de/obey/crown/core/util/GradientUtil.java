package de.obey.crown.core.util;


/*
    Author: Obey
    Date: 22.05.2026
    Time: 10:19
    Project: CrownCore
*/

import de.obey.crown.core.noobf.CrownCore;
import lombok.experimental.UtilityClass;

import java.util.Arrays;

@UtilityClass
public class GradientUtil {

    public String interpolateGradient(final String[] colors, final int index, final int totalFilledBars, final String text) {
        CrownCore.log.debug("interpolateGradient called");
        CrownCore.log.debug(" - colors: " + Arrays.toString(colors));
        CrownCore.log.debug(" - text: " + text);

        if (colors.length == 1)
            return colors[0];

        final double progress = (double) index / Math.max(1, totalFilledBars - 1);

        final double scaled = progress * (colors.length - 1);
        final int lowerIndex = (int) Math.floor(scaled);
        final int upperIndex = Math.min(lowerIndex + 1, colors.length - 1);

        final double localProgress = scaled - lowerIndex;

        return interpolateHex(colors[lowerIndex], colors[upperIndex], localProgress, text);
    }

    public String interpolateHex(final String startHex, final String endHex, final double progress, final String text) {

        final int start = Integer.parseInt(startHex.replace("#", ""), 16);
        final int end = Integer.parseInt(endHex.replace("#", ""), 16);

        final int r1 = (start >> 16) & 0xFF;
        final int g1 = (start >> 8) & 0xFF;
        final int b1 = start & 0xFF;

        final int r2 = (end >> 16) & 0xFF;
        final int g2 = (end >> 8) & 0xFF;
        final int b2 = end & 0xFF;

        final int r = (int) (r1 + (r2 - r1) * progress);
        final int g = (int) (g1 + (g2 - g1) * progress);
        final int b = (int) (b1 + (b2 - b1) * progress);

        return "<color:" + String.format("#%02X%02X%02X", r, g, b) + ">" + text + "</color>";
    }

}
