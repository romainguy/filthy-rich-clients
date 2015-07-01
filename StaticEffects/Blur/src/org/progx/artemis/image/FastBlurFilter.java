/*
 * $Id: FastBlurFilter.java,v 1.1 2007/01/15 16:12:02 gfx Exp $
 *
 * Dual-licensed under LGPL (Sun and Romain Guy) and BSD (Romain Guy).
 *
 * Copyright 2006 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * Copyright (c) 2006 Romain Guy <romain.guy@mac.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.progx.artemis.image;

import java.awt.image.BufferedImage;

import org.progx.artemis.graphics.GraphicsUtilities;

/**
 *
 * @author Romain Guy <romain.guy@mac.com>
 */
public class FastBlurFilter extends AbstractFilter {
    private final int radius;

    public FastBlurFilter() {
        this(3);
    }

    public FastBlurFilter(int radius) {
        if (radius < 1) {
            radius = 1;
        }

        this.radius = radius;
    }

    public int getRadius() {
        return radius;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BufferedImage filter(BufferedImage src, BufferedImage dst) {
        int width = src.getWidth();
        int height = src.getHeight();

        if (dst == null) {
            dst = createCompatibleDestImage(src, null);
        }

        int[] srcPixels = new int[width * height];
        int[] dstPixels = new int[width * height];

        GraphicsUtilities.getPixels(src, 0, 0, width, height, srcPixels);
        // horizontal pass
        blur(srcPixels, dstPixels, width, height, radius);
        // vertical pass
        blur(dstPixels, srcPixels, height, width, radius);
        // the result is now stored in srcPixels due to the 2nd pass
        GraphicsUtilities.setPixels(dst, 0, 0, width, height, srcPixels);

        return dst;
    }

    private static void blur(int[] srcPixels, int[] dstPixels,
                             int width, int height, int radius) {
        int windowSize = radius * 2 + 1;

        int sumAlpha;
        int sumRed;
        int sumGreen;
        int sumBlue;

        int srcIndex = 0;
        int dstIndex;
        int pixel;

        for (int y = 0; y < height; y++) {
            sumAlpha = sumRed = sumGreen = sumBlue = 0;
            dstIndex = y;

            pixel = srcPixels[srcIndex];
            sumAlpha += (radius + 1) * ((pixel >> 24) & 0xFF);
            sumRed   += (radius + 1) * ((pixel >> 16) & 0xFF);
            sumGreen += (radius + 1) * ((pixel >>  8) & 0xFF);
            sumBlue  += (radius + 1) * ( pixel        & 0xFF);

            for (int i = 1; i <= radius; i++) {
                pixel = srcPixels[srcIndex + (i <= width - 1 ? i : width - 1)];
                sumAlpha += (pixel >> 24) & 0xFF;
                sumRed   += (pixel >> 16) & 0xFF;
                sumGreen += (pixel >>  8) & 0xFF;
                sumBlue  +=  pixel        & 0xFF;
            }

            for  (int x = 0; x < width; x++) {
                dstPixels[dstIndex] = sumAlpha / windowSize << 24 |
                                      sumRed   / windowSize << 16 |
                                      sumGreen / windowSize <<  8 |
                                      sumBlue  / windowSize;
                dstIndex += height;

                int nextPixelIndex = x + radius + 1;
                if (nextPixelIndex >= width) {
                    nextPixelIndex = width - 1;
                }

                int previousPixelIndex = x - radius;
                if (previousPixelIndex < 0) {
                    previousPixelIndex = 0;
                }

                int nextPixel = srcPixels[srcIndex + nextPixelIndex];
                int previousPixel = srcPixels[srcIndex + previousPixelIndex];

                sumAlpha += (nextPixel     >> 24) & 0xFF;
                sumAlpha -= (previousPixel >> 24) & 0xFF;

                sumRed += (nextPixel     >> 16) & 0xFF;
                sumRed -= (previousPixel >> 16) & 0xFF;

                sumGreen += (nextPixel     >> 8) & 0xFF;
                sumGreen -= (previousPixel >> 8) & 0xFF;

                sumBlue += nextPixel & 0xFF;
                sumBlue -= previousPixel & 0xFF;
            }

            srcIndex += width;
        }
    }
}
