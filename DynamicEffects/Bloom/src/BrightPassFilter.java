/*
 * Copyright (c) 2007, Romain Guy
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided
 *     with the distribution.
 *   * Neither the name of the TimingFramework project nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;

/**
 * @author Romain Guy
 */
public class BrightPassFilter extends AbstractFilter {
    private float brightnessThreshold;

    public BrightPassFilter() {
        this(0.7f);
    }

    public BrightPassFilter(float brightnessThreshold) {
        this.brightnessThreshold = brightnessThreshold;
    }

    @Override
    public BufferedImage filter(BufferedImage src, BufferedImage dst) {
        if (dst == null) {
            DirectColorModel directCM = new DirectColorModel(32,
                    0x00FF0000, 0x0000FF00, 0x000000FF, 0xFF000000);
            dst = createCompatibleDestImage(src, directCM);
        }

        int width = src.getWidth();
        int height = src.getHeight();

        int[] pixels = new int[width * height];
        GraphicsUtilities.getPixels(src, 0, 0, width, height, pixels);
        brightPass(pixels, width, height);
        GraphicsUtilities.setPixels(dst, 0, 0, width, height, pixels);

        return dst;
    }

    private void brightPass(int[] pixels, int width, int height) {
        int threshold = (int) (brightnessThreshold * 255);
        
        int r;
        int g;
        int b;

        int luminance;
        int[] luminanceData = new int[3 * 256];
        
        for (int i = 0; i < luminanceData.length; i += 3) {
            luminanceData[i    ] = (int) (i * 0.2125f);
            luminanceData[i + 1] = (int) (i * 0.7154f);
            luminanceData[i + 2] = (int) (i * 0.0721f);
        }
        
        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = pixels[index];

                // unpack the pixel's components
                r = pixel >> 16 & 0xFF;
                g = pixel >> 8  & 0xFF;
                b = pixel       & 0xFF;
                
                // compute the luminance
                luminance = luminanceData[r * 3] + luminanceData[g * 3 + 1] +
                        luminanceData[b * 3 + 2];
                
                // apply the treshold to select the brightest pixels
                luminance = Math.max(0, luminance - threshold);
                
                int sign = (int) Math.signum(luminance);

                // pack the components in a single pixel
                pixels[index] = 0xFF000000 | (r * sign) << 16 |
                        (g * sign) << 8 | (b * sign);

                index++;
            }
        }
    }
}
