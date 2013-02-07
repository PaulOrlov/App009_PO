/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package app008_po;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.IntBuffer;
import javax.imageio.ImageIO;
import static java.lang.System.*;
import static com.jogamp.common.nio.Buffers.*;
import com.jogamp.opencl.*;
import static com.jogamp.opencl.CLImageFormat.ChannelOrder.*;
import static com.jogamp.opencl.CLImageFormat.ChannelType.*;
import java.nio.FloatBuffer;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/**
 *
 * @author coglab
 */
public class App008_PO {

    private static int[] pixels;
    static int width, height;

    public static void init() throws IOException {
        BufferedImage bi = ImageIO.read(App008_PO.class.getResourceAsStream("butterfly1.jpg"));
        width = bi.getWidth();
        height = bi.getHeight();
        pixels = new int[width*height*4];
        bi.getData().getPixels(0, 0, width, height, pixels);
    }

    public CLDevice getCompatibleDevice() {

        CLPlatform[] platforms = CLPlatform.listCLPlatforms();
        for (CLPlatform platform : platforms) {
            CLDevice[] devices = platform.listCLDevices();

            for (CLDevice device : devices) {
                if(device.isImageSupportAvailable()) {
                    return device;
                }
            }
        }

        return null;
    }


    public void supportedImageFormatsTest() {
        CLDevice device = getCompatibleDevice();
        if(device == null) {
            out.println("WARNING: can not test image api.");
            return;
        }
        CLContext context = CLContext.create(device);

        try{
            CLImageFormat[] formats = context.getSupportedImage2dFormats();
//            out.println("sample image format: "+formats[0]);
            for (CLImageFormat format : formats) {
                out.println(format);
            }
        }finally{
            context.release();
        }

    }

    public void image2dKernelCopyTest() throws IOException {

        CLDevice device = getCompatibleDevice();
        if(device == null) {
            out.println("WARNING: can not test image api.");
            return;
        }
        CLContext context = CLContext.create(device);
        CLProgram program = context.createProgram(App008_PO.class.getResourceAsStream("Kernel_2.cl"));
        program.build();
        CLKernel kernel = program.createCLKernel("convolve");

        CLCommandQueue queue = device.createCommandQueue();

        try{

            CLImageFormat format = new CLImageFormat(RGBA, UNSIGNED_INT32);

            CLImage2d<IntBuffer> imageA = context.createImage2d(newDirectIntBuffer(pixels), width, height, format);
            CLImage2d<IntBuffer> imageB = context.createImage2d(newDirectIntBuffer(pixels.length), width, height, format);
            
            kernel.putArgs(imageA, imageB);
            queue.putWriteImage(imageA, false)
                 .put2DRangeKernel(kernel, 0, 0, width, height, 0, 0)
                 .putReadImage(imageB, true);
            
//            while(bufferA.hasRemaining()) {
//                assertEquals(bufferA.get(), bufferB.get());
//            }
            
            show(createImage(width, height, imageA)); 
            show(createImage(width, height, imageB)); 

        }finally{
            context.release();
        }

    }
    
    private static void show(final BufferedImage image) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame frame = new JFrame("CopyCL");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.add(new JLabel(new ImageIcon(image)));
                frame.pack();
                frame.setLocation(100, 100);
                frame.setVisible(true);
            }
        });
    }
    private static BufferedImage createImage(int width, int height, CLImage2d<IntBuffer> buffer) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int[] localPixels = new int[buffer.getBuffer().capacity()];
        buffer.getBuffer().get(localPixels).rewind();
        image.getRaster().setPixels(0, 0, width, height, localPixels);
        return image;
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        App008_PO.init();
        App008_PO app = new App008_PO();
        //app.supportedImageFormatsTest();
        app.image2dKernelCopyTest();
    }
}
