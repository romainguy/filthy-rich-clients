package org.progx.artemis.graphics;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.progx.artemis.image.FastBlurFilter;

public final class Reflection {
    private Reflection() {
    }

    public static BufferedImage createReflection(BufferedImage image) {
        BufferedImage mask = createGradientMask(image.getWidth(),
                                                image.getHeight());
        return createReflectedPicture(image, mask);
    }

    public static BufferedImage createReflectedPicture(BufferedImage avatar,
                                                       BufferedImage alphaMask) {
        int avatarWidth = avatar.getWidth() + 6;
        int avatarHeight = avatar.getHeight();

        BufferedImage buffer = createReflection(avatar,
                                                avatarWidth, avatarHeight);

        applyAlphaMask(buffer, alphaMask, avatarHeight);

        return buffer;/*.getSubimage(0, 0, avatarWidth, avatarHeight * 3 / 2)*/
    }

    private static void applyAlphaMask(BufferedImage buffer,
                                       BufferedImage alphaMask,
                                       int avatarHeight) {

        Graphics2D g2 = buffer.createGraphics();
        g2.setComposite(AlphaComposite.DstOut);
        g2.drawImage(alphaMask, null, 0, avatarHeight);
        g2.dispose();
    }

    private static BufferedImage createReflection(BufferedImage avatar,
                                                  int avatarWidth,
                                                  int avatarHeight) {

        BufferedImage buffer = new BufferedImage(avatarWidth, avatarHeight * 5 / 3,
                                                 BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = buffer.createGraphics();

        g.drawImage(avatar, null, null);
        g.translate(0, avatarHeight * 2);

        g.scale(1.0, -1.0);
        FastBlurFilter filter = new FastBlurFilter(3);
        g.drawImage(avatar, filter, 0, 0);

        g.dispose();

        return buffer;
    }

    public static BufferedImage createGradientMask(int avatarWidth,
                                                   int avatarHeight) {
        return createGradientMask(avatarWidth, avatarHeight, 0.7f, 1.0f);
    }

    public static BufferedImage createGradientMask(int avatarWidth,
                                                   int avatarHeight,
                                                   float opacityStart,
                                                   float opacityEnd) {
        BufferedImage gradient = new BufferedImage(avatarWidth, avatarHeight,
                                                   BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = gradient.createGraphics();
        GradientPaint painter = new GradientPaint(0.0f, 0.0f,
                                                  new Color(1.0f, 1.0f, 1.0f, opacityStart),
                                                  0.0f, avatarHeight / 2.0f,
                                                  new Color(1.0f, 1.0f, 1.0f, opacityEnd));
        g.setPaint(painter);
        g.fill(new Rectangle2D.Double(0, 0, avatarWidth, avatarHeight));

        g.dispose();

        return gradient;
    }
}
