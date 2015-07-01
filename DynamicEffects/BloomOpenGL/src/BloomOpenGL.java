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

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLJPanel;
import javax.media.opengl.glu.GLU;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import com.sun.opengl.util.BufferUtil;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;

/**
 * THIS DEMO REQUIRES THE JOGL LIBRARY TO COMPILE AND EXECUTE !
 *
 * JOGL can be found at http://jogl.dev.java.net for your OS.
 *
 * /!\ The rendering happens in FBOs so that you can get the result back into
 *     a Java 2D image without displaying it on screen through a GLJPanel. This
 *     implementation does not offer the conversion from FBO to a BufferedImage
 *     but you can do it by reading the texture data from frameBufferTexture2.
 *
 * @author Romain Guy <romain.guy@mac.com>
 */
public class BloomOpenGL extends GLJPanel implements GLEventListener {
    private int frameBufferObject1 = -1;
    private int frameBufferTexture1 = -1;

    private int frameBufferObject2 = -1;
    private int frameBufferTexture2 = -1;

    private Texture texture;
    private BufferedImage image;

    private GLU glu = new GLU();

    private String blurShaderSource =
        "const int MAX_KERNEL_SIZE = 25;" +
        "uniform sampler2D baseImage;" +
        "uniform vec2 offsets[MAX_KERNEL_SIZE];" +
        "uniform float kernelVals[MAX_KERNEL_SIZE];" +
        "" +
        "void main(void) {" +
        "    int i;" +
        "    vec4 sum = vec4(0.0);" +
        "" +
        "    for (i = 0; i < MAX_KERNEL_SIZE; i++) {" +
        "        vec4 tmp = texture2D(baseImage," +
        "                             gl_TexCoord[0].st + offsets[i]);" +
        "        sum += tmp * kernelVals[i];" +
        "    }" +
        "" +
        "    gl_FragColor = sum;" +
        "}";
    private int blurShader;

    private String brightPassShaderSource =
        "uniform sampler2D baseImage;" +
        "uniform float brightPassThreshold;" +
        "" +
        "void main(void) {" +
        "    vec3 luminanceVector = vec3(0.2125, 0.7154, 0.0721);" +
        "    vec4 sample = texture2D(baseImage, gl_TexCoord[0].st);" +
        "" +
        "    float luminance = dot(luminanceVector, sample.rgb);" +
	    "    luminance = max(0.0, luminance - brightPassThreshold);" +
	    "    sample.rgb *= sign(luminance);" +
	    "    sample.a = 1.0;" +
        "" +
        "    gl_FragColor = sample;" +
        "}";
    private int brightPassShader;

    private float threshold = 0.3f;

    public BloomOpenGL() {
        super(new GLCapabilities());
        addGLEventListener(this);

        try {
            image = ImageIO.read(getClass().getResource("/images/screen.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void init(GLAutoDrawable glAutoDrawable) {
        GL gl = glAutoDrawable.getGL();

        if (texture == null) {
            texture = TextureIO.newTexture(image, false);
        }

        // create the blur shader
        blurShader = createFragmentProgram(gl, new String[] { blurShaderSource });
        gl.glUseProgramObjectARB(blurShader);
        int loc = gl.glGetUniformLocationARB(blurShader, "baseImage");
        gl.glUniform1iARB(loc, 0);
        gl.glUseProgramObjectARB(0);

        // create the bright-pass shader
        brightPassShader = createFragmentProgram(gl, new String[] { brightPassShaderSource });
        gl.glUseProgramObjectARB(brightPassShader);
        loc = gl.glGetUniformLocationARB(brightPassShader, "baseImage");
        gl.glUniform1iARB(loc, 0);
        gl.glUseProgramObjectARB(0);

        // create the FBOs
        if (gl.isExtensionAvailable("GL_EXT_framebuffer_object")) {
            int[] fboId = new int[1];
            int[] texId = new int[1];

            createFrameBufferObject(gl, fboId, texId,
                                    image.getWidth(), image.getHeight());
            frameBufferObject1 = fboId[0];
            frameBufferTexture1 = texId[0];

            createFrameBufferObject(gl, fboId, texId,
                                    image.getWidth(), image.getHeight());
            frameBufferObject2 = fboId[0];
            frameBufferTexture2 = texId[0];
        }
    }

    private static void createFrameBufferObject(GL gl, int[] frameBuffer,
                                                int[] colorBuffer, int width,
                                                int height) {
        gl.glGenFramebuffersEXT(1, frameBuffer, 0);
        gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, frameBuffer[0]);

        gl.glGenTextures(1, colorBuffer, 0);
        gl.glBindTexture(GL.GL_TEXTURE_2D, colorBuffer[0]);
        gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA,
                        width, height,
                        0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE,
                        BufferUtil.newByteBuffer(width * height * 4));
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT,
                                     GL.GL_COLOR_ATTACHMENT0_EXT,
                                     GL.GL_TEXTURE_2D, colorBuffer[0], 0);
        gl.glBindTexture(GL.GL_TEXTURE_2D, 0);

        int status = gl.glCheckFramebufferStatusEXT(GL.GL_FRAMEBUFFER_EXT);
        if (status == GL.GL_FRAMEBUFFER_COMPLETE_EXT) {
            gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, 0);
        } else {
            throw new IllegalStateException("Frame Buffer Oject not created.");
        }
    }

    private static void viewOrtho(GL gl, int width, int height) {
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glOrtho(0, width, height, 0, -1, 1);
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();
    }

    private static void renderTexturedQuad(GL gl, float width, float height,
                                           boolean flip) {
        gl.glBegin(GL.GL_QUADS);
            gl.glTexCoord2f(0.0f, flip ? 1.0f : 0.0f);
            gl.glVertex2f(0.0f, 0.0f);

            gl.glTexCoord2f(1.0f, flip ? 1.0f : 0.0f);
            gl.glVertex2f(width, 0.0f);

            gl.glTexCoord2f(1.0f, flip ? 0.0f : 1.0f);
            gl.glVertex2f(width, height);

            gl.glTexCoord2f(0.0f, flip ? 0.0f : 1.0f);
            gl.glVertex2f(0.0f, height);
        gl.glEnd();
    }

    private static int createFragmentProgram(GL gl, String[] fragmentShaderSource) {
        int fragmentShader, fragmentProgram;
        int[] success = new int[1];

        // create the shader object and compile the shader source code
        fragmentShader = gl.glCreateShaderObjectARB(GL.GL_FRAGMENT_SHADER_ARB);
        gl.glShaderSourceARB(fragmentShader, 1, fragmentShaderSource, null);
        gl.glCompileShaderARB(fragmentShader);
        gl.glGetObjectParameterivARB(fragmentShader,
                                      GL.GL_OBJECT_COMPILE_STATUS_ARB,
                                      success, 0);

        // print the compiler messages, if necessary
        int[] infoLogLength = new int[1];
        int[] length = new int[1];
        gl.glGetObjectParameterivARB(fragmentShader,
                                     GL.GL_OBJECT_INFO_LOG_LENGTH_ARB,
                                     infoLogLength, 0);
        if (infoLogLength[0] > 1) {
            byte[] b = new byte[1024];
            gl.glGetInfoLogARB(fragmentShader, 1024, length, 0, b, 0);
            System.out.println("Fragment compile phase = " + new String(b, 0, length[0]));
        }

        if (success[0] == 0) {
            gl.glDeleteObjectARB(fragmentShader);
            return -1;
        }

        // create the program object and attach it to the shader
        fragmentProgram = gl.glCreateProgramObjectARB();
        gl.glAttachObjectARB(fragmentProgram, fragmentShader);

        // it is now safe to delete the shader object
        gl.glDeleteObjectARB(fragmentShader);

        // link the program
        gl.glLinkProgramARB(fragmentProgram);
        gl.glGetObjectParameterivARB(fragmentProgram,
                                     GL.GL_OBJECT_LINK_STATUS_ARB,
                                     success, 0);

        gl.glGetObjectParameterivARB(fragmentShader,
                                     GL.GL_OBJECT_INFO_LOG_LENGTH_ARB,
                                     infoLogLength, 0);
        if (infoLogLength[0] > 1) {
            byte[] b = new byte[1024];
            gl.glGetInfoLogARB(fragmentShader, 1024, length, 0, b, 0);
            System.out.println("Fragment link phase = " + new String(b, 0, length[0]));
        }

        if (success[0] == 0) {
            gl.glDeleteObjectARB(fragmentProgram);
            return -1;
        }

        return fragmentProgram;
    }

    private static void enableBlurFragmentProgram(GL gl, int program,
                                                  float textureWidth,
                                                  float textureHeight) {
        gl.glUseProgramObjectARB(program);

        int kernelWidth = 5;
        int kernelHeight = 5;

        float xoff = 1.0f / textureWidth;
        float yoff = 1.0f / textureHeight;

        float[] offsets = new float[kernelWidth * kernelHeight * 2];
        int offsetIndex = 0;

        for (int i = -kernelHeight / 2; i < kernelHeight / 2 + 1; i++) {
            for (int j = -kernelWidth / 2; j < kernelWidth / 2 + 1; j++) {
                offsets[offsetIndex++] = j * xoff;
                offsets[offsetIndex++] = i * yoff;
            }
        }

        int loc = gl.glGetUniformLocationARB(program, "offsets");
        gl.glUniform2fv(loc, offsets.length, offsets, 0);

        float[] values = createGaussianBlurFilter(2);

        loc = gl.glGetUniformLocationARB(program, "kernelVals");
        gl.glUniform1fvARB(loc, values.length, values, 0);
    }

    private static float[] createGaussianBlurFilter(int radius) {
        if (radius < 1) {
            throw new IllegalArgumentException("Radius must be >= 1");
        }

        int size = radius * 2 + 1;
        float[] data = new float[size * size];

        float sigma = radius / 3.0f;
        float twoSigmaSquare = 2.0f * sigma * sigma;
        float sigmaRoot = (float) Math.sqrt(twoSigmaSquare * Math.PI);
        float total = 0.0f;

        int index = 0;
        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                float distance = x * x + y * y;
                data[index] = (float) Math.exp(-distance / twoSigmaSquare) / sigmaRoot;
                total += data[index];
                index++;
            }
        }

        for (int i = 0; i < data.length; i++) {
            data[i] /= total;
        }

        return data;
    }

    private static void enableBrightPassFragmentProgram(GL gl, int program,
                                                        float threshold) {
        gl.glUseProgramObjectARB(program);

        int loc = gl.glGetUniformLocationARB(program, "brightPassThreshold");
        gl.glUniform1fARB(loc, threshold);
    }

    private static void disableFragmentProgram(GL gl) {
        gl.glUseProgramObjectARB(0);
    }

    public void display(GLAutoDrawable glAutoDrawable) {
        GL gl = glAutoDrawable.getGL();
        gl.glLoadIdentity(); 
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        viewOrtho(gl, image.getWidth(), image.getHeight());
        gl.glEnable(GL.GL_TEXTURE_2D);

        int width = image.getWidth();
        int height = image.getHeight();

        // Source Image/bright pass on FBO1
        renderBrightPass(gl, width, height);
        // Source image on FBO2
        renderImage(gl, width, height);
        // On screen
        renderTextureOnScreen(gl, width, height);

        //render5x5(gl, width, height);
        render11x11(gl, width, height);
        render21x21(gl, width, height);
        render41x41(gl, width, height);

        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glFlush();
    }

    private void render41x41(GL gl, int width, int height) {
        // FBO1/blur on FBO2
        renderBlur(gl, width / 8.0f, height / 8.0f);
        // Add on screen
        gl.glPushMatrix();
        gl.glTranslatef(0.0f, -height * 7.0f, 0.0f);
        renderAddTextureOnScreen(gl, width * 8.0f, height * 8.0f);
        gl.glPopMatrix();
    }

    private void render21x21(GL gl, int width, int height) {
        // FBO1/blur on FBO2
        renderBlur(gl, width / 4.0f, height / 4.0f);
        // Add on screen
        gl.glPushMatrix();
        gl.glTranslatef(0.0f, -height * 3.0f, 0.0f);
        renderAddTextureOnScreen(gl, width * 4.0f, height * 4.0f);
        gl.glPopMatrix();
    }

    private void render11x11(GL gl, int width, int height) {
        // FBO1/blur on FBO2
        renderBlur(gl, width / 2.0f, height / 2.0f);
        // Add on screen
        gl.glPushMatrix();
        gl.glTranslatef(0.0f, -height, 0.0f);
        renderAddTextureOnScreen(gl, width * 2.0f, height * 2.0f);
        gl.glPopMatrix();
    }

    private void render5x5(GL gl, int width, int height) {
        // FBO1/blur on FBO2
        renderBlur(gl, width, height);
        // Add on screen
        renderAddTextureOnScreen(gl, width, height);
    }

    private void renderAddTextureOnScreen(GL gl, float width, float height) {
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_ONE, GL.GL_ONE);
        renderTextureOnScreen(gl, width, height);
        gl.glDisable(GL.GL_BLEND);
    }

    private void renderTextureOnScreen(GL gl, float width, float height) {
        // Draw the texture on a quad
        gl.glBindTexture(GL.GL_TEXTURE_2D, frameBufferTexture2);
        renderTexturedQuad(gl, width, height, false);
        gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
    }

    private void renderBrightPass(GL gl, float width, float height) {
        // Draw into the FBO
        gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, frameBufferObject1);
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        enableBrightPassFragmentProgram(gl, brightPassShader, threshold);
        gl.glBindTexture(GL.GL_TEXTURE_2D, texture.getTextureObject());

        renderTexturedQuad(gl, width, height, texture.getMustFlipVertically());

        gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
        disableFragmentProgram(gl);

        gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, 0);
    }

    private void renderImage(GL gl, float width, float height) {
        // Draw into the FBO
        gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, frameBufferObject2);
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

        gl.glBindTexture(GL.GL_TEXTURE_2D, texture.getTextureObject());
        renderTexturedQuad(gl, width, height, texture.getMustFlipVertically());
        gl.glBindTexture(GL.GL_TEXTURE_2D, 0);

        gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, 0);
    }

    private void renderBlur(GL gl, float width, float height) {
        // Draw into the FBO
        gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, frameBufferObject2);
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

        enableBlurFragmentProgram(gl, blurShader, width, height);
        gl.glBindTexture(GL.GL_TEXTURE_2D, frameBufferTexture1);

        renderTexturedQuad(gl, width, height, texture.getMustFlipVertically());

        gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
        disableFragmentProgram(gl);

        gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, 0);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(image.getWidth(), image.getHeight());
    }

    public void reshape(GLAutoDrawable glAutoDrawable, int x, int y,
                        int width, int height) {
        GL gl = glAutoDrawable.getGL();

        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();

        glu.gluPerspective(50, (float) width / height, 5, 2000);
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    public void displayChanged(GLAutoDrawable glAutoDrawable, boolean modeChanged,
                               boolean deviceChanged) {
    }

    public float getThreshold() {
        return threshold;
    }

    public void setThreshold(float threshold) {
        this.threshold = threshold;
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                final BloomOpenGL bloom;
                final JSlider slider;

                JFrame f = new JFrame("Bloom OpenGL");
                f.add(bloom = new BloomOpenGL());

                JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEADING));
                controls.add(new JLabel("Bloom: 0.0"));
                controls.add(slider = new JSlider(0, 100, 30));
                slider.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                        JSlider slider = (JSlider) e.getSource();
                        float threshold = slider.getValue() / 100.0f;
                        bloom.setThreshold(threshold);
                    }
                });
                controls.add(new JLabel("1.0"));
                f.add(controls, BorderLayout.SOUTH);

                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                f.pack();
                f.setLocationRelativeTo(null);
                f.setResizable(false);
                f.setVisible(true);
            }
        });
    }
}