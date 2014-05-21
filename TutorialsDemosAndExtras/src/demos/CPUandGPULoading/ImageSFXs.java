///////////////////////////////////////////////////////////////////////////////
// Note: The code in this file is that available from Davison's Killer Games //
// Programming in Java. I do strongly recommended that you read Chapter 6 to //
// gain more insight into how the various transforms operation.              //
///////////////////////////////////////////////////////////////////////////////

// ImageSFXs.java
// Andrew Davison, April 2005, ad@fivedots.coe.psu.ac.th

/* Various image operations used as special effects.
 
   These methods are called from ImagesTests, usually from methods
   which implement a series of images changes (an animation effect)
   spread over several 'ticks' of the update/redraw animation
   cycle.
 
   The types of SFXs and their public methods:
 
   drawImage() based effects
 * draw a resized image: drawResizedImage()
 * return a flipped image: getFlippedImage()
 * draw a flipped image: drawVerticalFlip(), drawHorizFlip()
 
   Alpha composite effect
 * draw a faded image: drawFadedImage();
 
   Affine transform effect
 * return a rotated image: getRotatedImage()
 
   Convolution effect
 * draw a blurred image: drawBlurredImage();
 * draw a blurred image with a specified blur size: drawBlurredImage()
 
   LookupOp effect
 * draw a redder image: drawRedderImage()
                -- there are LookupOp and RescaleOp versions
 
   RescaleOp effects
 * draw a brighter image: drawBrighterImage()
 * draw a negated image: drawNegatedImage()
 
   BandCombineOp effect
 * draw the image with mixed up colours: drawMixedColouredImage()
 
   Pixel effects
 * make some of the image's pixels transparent: eraseImageParts()
 * change some of the image's pixels to red or yellow:
                                                     zapImageParts()
 */


package demos.CPUandGPULoading;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.color.*;
import java.util.*;


public class ImageSFXs {
    // constants used to specify the type of image flipping required
    public static final int VERTICAL_FLIP = 0;
    public static final int HORIZONTAL_FLIP = 1;
    public static final int DOUBLE_FLIP = 2;   // flip horizontally and vertically
    
    private GraphicsConfiguration gc;
    
    // pre-defined image operations
    private RescaleOp negOp, negOpTrans;   // image negation
    private ConvolveOp blurOp;    // image blurring
    
    
    
    /** Creates a new instance of ImageSFXs */
    public ImageSFXs() {    // get the GraphicsConfiguration so images can be copied easily and
        // efficiently
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
        
        initEffects();
    }  // end of ImageSFXs()
    
    private void initEffects()
    // Create pre-defined image operations for image negation and blurring.
    {
        // image negative.  Multiply each colour value by -1.0 and add 255
        negOp = new RescaleOp(-1.0f, 255f, null);
        
        // image negative for images with transparency
        float[] negFactors = {-1.0f, -1.0f, -1.0f, 1.0f};  // don't change the alpha
        float[] offsets = {255f, 255f, 255f, 0.0f};
        negOpTrans = new RescaleOp(negFactors, offsets, null);
        
        
        // blur by convolving the image with a matrix
        float ninth = 1.0f / 9.0f;
        
        float[] blurKernel = {      // the 'hello world' of Image Ops :)
            ninth, ninth, ninth,
            ninth, ninth, ninth,
            ninth, ninth, ninth
        };
        
        blurOp = new ConvolveOp(
                new Kernel(3, 3, blurKernel), ConvolveOp.EDGE_NO_OP, null);
        
    }  // end of initEffects()
    
    
    
    // ------------------ drawImage() based effects -----------------
    // resizing, flipping
    
    
    public void drawResizedImage(Graphics2D g2d, BufferedImage im, int x, int y,
            double widthChange, double heightChange)
            // draw a resized image by multiplying by widthChange and heightChange
    {
        if (im == null) {
            System.out.println("drawResizedImage: input image is null");
            return;
        }
        
        if (widthChange <= 0) {
            System.out.println("width change cannot <= 0");
            widthChange = 1.0;
        }
        if (heightChange <= 0) {
            System.out.println("height change cannot <= 0");
            heightChange = 1.0;
        }
        
        int destWidth = (int) (im.getWidth() * widthChange);
        int destHeight = (int) (im.getHeight() * heightChange);
        
        // adjust top-left (x,y) coord of resized image so it remains centered
        int destX = x + im.getWidth()/2 - destWidth/2;
        int destY = y + im.getHeight()/2 - destHeight/2;
        
        g2d.drawImage(im, destX, destY, destWidth, destHeight, null);
    } // end of drawResizedImage()
    
    
    
    // ------------------ flipping -----------------------
    
    public BufferedImage getFlippedImage(BufferedImage im, int flipKind)
  /* Return a new image which has been flipped horizontally, vertically,
     or both. */
    {
        if (im == null) {
            System.out.println("getFlippedImage: input image is null");
            return null;
        }
        
        int imWidth = im.getWidth();
        int imHeight = im.getHeight();
        int transparency = im.getColorModel().getTransparency();
        
        BufferedImage copy =  gc.createCompatibleImage(imWidth, imHeight, transparency);
        Graphics2D g2d = copy.createGraphics();
        // g2d.setComposite(AlphaComposite.Src);
        
        // draw in the flipped image
        renderFlip(g2d, im, imWidth, imHeight, flipKind);
        g2d.dispose();
        
        return copy;
    } // end of getFlippedImage()
    
    
    private void renderFlip(Graphics2D g2d, BufferedImage im,
            int imWidth, int imHeight, int flipKind)
            // the flipping is achieved by supplying suitable coords to drawImage()
    {
        if (flipKind == VERTICAL_FLIP)
            g2d.drawImage(im, imWidth, 0,  0, imHeight,
                    0, 0,  imWidth, imHeight, null);
        else if (flipKind == HORIZONTAL_FLIP)
            g2d.drawImage(im, 0, imHeight,  imWidth, 0,
                    0, 0,  imWidth, imHeight, null);
        else    // assume DOUBLE_FLIP
            g2d.drawImage(im, imWidth, imHeight,  0, 0,
                    0, 0,  imWidth, imHeight, null);
        
    }  // end of renderFlip()
    
    
    public void drawVerticalFlip(Graphics2D g2d, BufferedImage im, int x, int y)
    // Draw a vertically flipped image without creating a new
    // BufferedImage object.
    {
        if (im == null) {
            System.out.println("drawVerticalFlip: input image is null");
            return;
        }
        int imWidth = im.getWidth();
        int imHeight = im.getHeight();
        g2d.drawImage(im, x+imWidth, y,  x, y+imHeight,
                0, 0,  imWidth, imHeight, null);
    } // end of drawVerticalFlip()
    
    
    public void drawHorizFlip(Graphics2D g2d, BufferedImage im, int x, int y)
    // Draw a horizontally flipped image without creating a new
    // BufferedImage object.
    {
        if (im == null) {
            System.out.println("drawHorizFlip: input image is null");
            return;
        }
        int imWidth = im.getWidth();
        int imHeight = im.getHeight();
        g2d.drawImage(im, x, y+imHeight,  x+imWidth, y,
                0, 0,  imWidth, imHeight, null);
    } // end of drawHorizFlip()
    
    
    
    // --------------- alpha composite effect: fading  -----------------
    
    
    public void drawFadedImage(Graphics2D g2d, BufferedImage im,
            int x, int y, float alpha)
  /* The degree of fading is specified with the alpha value.
     alpha == 1 means fully visible, 0 mean invisible. */
    {
        if (im == null) {
            System.out.println("drawFadedImage: input image is null");
            return;
        }
        
        if (alpha < 0.0f) {
            System.out.println("Alpha must be >= 0.0f; setting to 0.0f");
            alpha = 0.0f;
        } else if (alpha > 1.0f) {
            System.out.println("Alpha must be <= 1.0f; setting to 1.0f");
            alpha = 1.0f;
        }
        
        Composite c = g2d.getComposite();  // backup the old composite
        
        g2d.setComposite( AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, alpha));
        g2d.drawImage(im, x, y, null);
        
        g2d.setComposite(c);
        // restore the old composite so it doesn't mess up future rendering
    }  // end of drawFadedImage()
    
    
    
    // --------------- affine transform effect: rotation  -----------------
    
    
    public BufferedImage getRotatedImage(BufferedImage src, int angle)
  /* Create a new BufferedImage which is the input image, rotated
     angle degrees clockwise.
   
     An issue is edge clipping. The simplest solution is to design the
     image with plenty of (transparent) border.
   */
    {
        if (src == null) {
            System.out.println("getRotatedImage: input image is null");
            return null;
        }
        
        int transparency = src.getColorModel().getTransparency();
        BufferedImage dest =  gc.createCompatibleImage(
                src.getWidth(), src.getHeight(), transparency );
        Graphics2D g2d = dest.createGraphics();
        
        AffineTransform origAT = g2d.getTransform(); // save original transform
        
        // rotate the coord. system of the dest. image around its center
        AffineTransform rot = new AffineTransform();
        rot.rotate( Math.toRadians(angle), src.getWidth()/2, src.getHeight()/2);
        g2d.transform(rot);
        
        g2d.drawImage(src, 0, 0, null);   // copy in the image
        
        g2d.setTransform(origAT);    // restore original transform
        g2d.dispose();
        
        return dest;
    } // end of getRotatedImage()
    
    
    
    // --------------- convolution effect: blurring  -----------------
    
    
    public void drawBlurredImage(Graphics2D g2d,
            BufferedImage im, int x, int y)
            // blurring with a fixed convolution kernel
    { if (im == null) {
          System.out.println("getBlurredImage: input image is null");
          return;
      }
      g2d.drawImage(im, blurOp, x, y);   // use the predefined ConvolveOp
    }  // end of drawBlurredImage()
    
    
    public void drawBlurredImage(Graphics2D g2d,
            BufferedImage im, int x, int y, int size)
  /*
     The size argument is used to specify a size*size blur kernel,
     filled with 1/(size*size) values.
   
     Size should be odd so that the center cell of the kernel
     corresponds to the coordinate being blurred.
   
     The larger the size value, the larger the kernel, and more blurry
     the resulting image.
   
     An issue is the edge effects, which will produce a nasty black
     border or a border with no bluriness, depending on which
     ConvolveOp EDGE constant is used.
   */
    {
        if (im == null) {
            System.out.println("getBlurredImage: input image is null");
            return;
        }
        int imWidth = im.getWidth();
        int imHeight = im.getHeight();
        int maxSize = (imWidth > imHeight) ? imWidth : imHeight;
        
        if ((maxSize%2) == 0)  // if even
            maxSize--;  // make it odd
        
        if ((size%2) == 0) {  // if even
            size++;   // make it odd
            System.out.println("Blur size must be odd; adding 1 to make size = " + size);
        }
        
        if (size < 3) {
            System.out.println("Minimum blur size is 3");
            size = 3;
        } else if (size > maxSize) {
            System.out.println("Maximum blur size is " + maxSize);
            size = maxSize;
        }
        
        // create the blur kernel
        int numCoords = size * size;
        float blurFactor = 1.0f / (float) numCoords;
        
        float[] blurKernel = new float[numCoords];
        for (int i=0; i < numCoords; i++)
            blurKernel[i] = blurFactor;
        
        ConvolveOp blurringOp = new ConvolveOp(
                new Kernel(size, size, blurKernel),
                ConvolveOp.EDGE_NO_OP, null);   // leaves edges unaffected
        // ConvolveOp.EDGE_ZERO_FILL, null);
        // edges are filled with black
        
        g2d.drawImage(im, blurringOp, x, y);
    }  // end of drawBlurredImage() with size argument
    
    
    
    // --------------- LookupOp effect: redden  -----------------
    
  /* There are two versions of drawRedderImage() here: one using
     a LookupOp, the other a RescaleOp. There is functionally no
     difference between them.
   */
    
    
    public void drawRedderImage(Graphics2D g2d, BufferedImage im,
            int x, int y, float brightness)
            // using LookupOp
  /* Draw the image with its redness is increased, and its greenness
     and blueness decreased. Any alpha channel is left unchanged.
   */
    { if (im == null) {
          System.out.println("drawRedderImage: input image is null");
          return;
      }
      
      if (brightness < 0.0f) {
          System.out.println("Brightness must be >= 0.0f; setting to 0.0f");
          brightness = 0.0f;
      }
      // brightness may be less than 1.0 to make the image less red
      
      short[] brighten = new short[256];    // for red channel
      short[] lessen = new short[256];      // for green and blue channels
      short[] noChange = new short[256];    // for the alpha channel
      
      for(int i=0; i < 256; i++) {
          float brightVal = 64.0f + (brightness * i);
          if (brightVal > 255.0f)
              brightVal = 255.0f;
          brighten[i] = (short) brightVal;
          lessen[i] = (short) ((float)i / brightness);
          noChange[i] = (short) i;
      }
      
      short[][] brightenRed;
      if (hasAlpha(im)) {
          brightenRed = new short[4][];
          brightenRed[0] = brighten;
          brightenRed[1] = lessen;
          brightenRed[2] = lessen;
          brightenRed[3] = noChange;    // without this the LookupOp fails
          // which is a bug (?)
      } else {  // not transparent
          brightenRed = new short[3][];
          brightenRed[0] = brighten;
          brightenRed[1] = lessen;
          brightenRed[2] = lessen;
      }
      LookupTable table = new ShortLookupTable(0, brightenRed);
      LookupOp brightenRedOp = new LookupOp(table, null);
      
      g2d.drawImage(im, brightenRedOp, x, y);
    }  // end of drawRedderImage() using LookupOp
    
    
/*
  public void drawRedderImage(Graphics2D g2d, BufferedImage im,
                                    int x, int y, float brightness)
  // using RescaleOp
  // Draw the image with its redness is increased, and its greenness
  // and blueness decreased. Any alpha channel is left unchanged.
  {
    if (im == null) {
      System.out.println("drawRedderImage: input image is null");
      return;
    }
 
    if (brightness < 0.0f) {
      System.out.println("Brightness must be >= 0.0f; setting to 0.0f");
      brightness = 0.0f;
    }
    // brightness may be less than 1.0 to make the image less red
 
    RescaleOp brigherOp;
    if (hasAlpha(im)) {
      float[] scaleFactors = {brightness, 1.0f/brightness, 1.0f/brightness, 1.0f};
                // don't change alpha
                // without the 1.0f the RescaleOp fails, which is a bug (?)
      float[] offsets = {64.0f, 0.0f, 0.0f, 0.0f};
      brigherOp = new RescaleOp(scaleFactors, offsets, null);
    }
    else {  // not transparent
      float[] scaleFactors = {brightness, 1.0f/brightness, 1.0f/brightness};
      float[] offsets = {64.0f, 0.0f, 0.0f};
      brigherOp = new RescaleOp(scaleFactors, offsets, null);
    }
    g2d.drawImage(im, brigherOp, x, y);
  }  // end of drawRedderImage() using RescaleOp
 */
    
    
    
    // --------------- RescaleOp effects: brighten, negate  ---------------
    
    
    public void drawBrighterImage(Graphics2D g2d, BufferedImage im,
            int x, int y, float brightness)
  /* Draw the image with changed brightness, by using a RescaleOp.
     Any alpha channel is unaffected. */
    {
        if (im == null) {
            System.out.println("drawBrighterImage: input image is null");
            return;
        }
        
        if (brightness < 0.0f) {
            System.out.println("Brightness must be >= 0.0f; setting to 0.5f");
            brightness = 0.5f;
        }
        // brightness may be less than 1.0 to make the image dimmer
        
        RescaleOp brigherOp;
        if (hasAlpha(im)) {
            float[] scaleFactors = {brightness, brightness, brightness, 1.0f};
            // don't change alpha
            // without the 1.0f the RescaleOp fails, which is a bug (?)
            float[] offsets = {0.0f, 0.0f, 0.0f, 0.0f};
            brigherOp = new RescaleOp(scaleFactors, offsets, null);
        } else   // not transparent
            brigherOp = new RescaleOp(brightness, 0, null);
        
        g2d.drawImage(im, brigherOp, x, y);
    }  // end of drawBrighterImage()
    
    
    public void drawNegatedImage(Graphics2D g2d, BufferedImage im, int x, int y)
  /* Draw the image with 255-<colour value> applied to its RGB components.
     Any alpha channel is unaffected. */
    {
        if (im == null) {
            System.out.println("drawNegatedImage: input image is null");
            return;
        }
        
        if (hasAlpha(im))
            g2d.drawImage(im, negOpTrans, x, y);  // use predefined RescaleOp
        else
            g2d.drawImage(im, negOp, x, y);
    }  // end of drawNegatedImage()
    
    
    
    // ------------------- BandCombineOp effect  -------------------
    
    
    public void drawMixedColouredImage(Graphics2D g2d, BufferedImage im, int x, int y)
    /* Mix up the colours in the green and blue bands of the image. */
    {
        if (im == null) {
            System.out.println("drawMixedColouredImage: input image is null");
            return;
        }
        
        BandCombineOp changeColoursOp;
        Random r = new Random();
        
        if (hasAlpha(im)) {
            float[][] colourMatrix = {
                { 1.0f, 0.0f, 0.0f, 0.0f },           // for new red band, unchanged
                { r.nextFloat(), r.nextFloat(), r.nextFloat(), 0.0f },    // new green band
                { r.nextFloat(), r.nextFloat(), r.nextFloat(), 0.0f },    // new blue band
                { 0.0f, 0.0f, 0.0f, 1.0f} };   // unchanged alpha
            changeColoursOp =  new BandCombineOp(colourMatrix, null);
        } else {    // not transparent
            float[][] colourMatrix = {
                { 1.0f, 0.0f, 0.0f },           // for new red band, unchanged
                { r.nextFloat(), r.nextFloat(), r.nextFloat() },    // new green band
                { r.nextFloat(), r.nextFloat(), r.nextFloat() }};   // new blue band
            
            changeColoursOp =  new BandCombineOp(colourMatrix, null);
        }
        
        // create the Raster used by the filter
        Raster sourceRaster = im.getRaster();
        
        WritableRaster destRaster = changeColoursOp.filter(sourceRaster, null);
        
        // convert the destination Raster into a BufferedImage
        BufferedImage newIm = new BufferedImage(im.getColorModel(),
                destRaster, false, null);
        
        g2d.drawImage(newIm, x, y, null);
    }  // end of drawMixedColouredImage()
    
    
    
    // ------------------- Pixel effects -------------------
    // erasing, zapping
    
    
    public void eraseImageParts(BufferedImage im, int spacing)
  /* Change some of the image's pixels to 0's, which will become
     transparent in an image with an alpha channel, black otherwise.
   */
    {
        if (im == null) {
            System.out.println("eraseImageParts: input image is null");
            return;
        }
        
        int imWidth = im.getWidth();
        int imHeight = im.getHeight();
        
        int [] pixels = new int[imWidth * imHeight];
        im.getRGB(0, 0, imWidth, imHeight, pixels, 0, imWidth);
        
        int i = 0;
        while (i < pixels.length) {
            pixels[i] = 0;    // make transparent (or black if no alpha)
            i = i + spacing;
        }
        
        im.setRGB(0, 0, imWidth, imHeight, pixels, 0, imWidth);
    }  // end of eraseImageParts()
    
    
    
    public void zapImageParts(BufferedImage im, double likelihood)
    // change some of the image's pixels to red or yellow
    {
        if (im == null) {
            System.out.println("zapImageParts: input image is null");
            return;
        }
        
        if ((likelihood < 0) || (likelihood > 1)) {
            System.out.println("likelihood must be in the range 0 to 1");
            likelihood = 0.5;
        }
        
        int redCol = 0xf90000;      // nearly full-on red
        int yellowCol = 0xf9fd00;   // a mix of red and green
        
        int imWidth = im.getWidth();
        int imHeight = im.getHeight();
        
        int [] pixels = new int[imWidth * imHeight];
        im.getRGB(0, 0, imWidth, imHeight, pixels, 0, imWidth);
        
        double rnd;
        for(int i=0; i < pixels.length; i++) {
            rnd = Math.random();
            if (rnd <= likelihood) {
                if (rnd <=  15*likelihood/16 )    // make red more likely then yellow
                    pixels[i] = pixels[i] | redCol;
                else
                    pixels[i] = pixels[i] | yellowCol;
            }
        }
        
        im.setRGB(0, 0, imWidth, imHeight, pixels, 0, imWidth);
    }  // end of eraseImageParts()
    
    
    
    // --------- useful support methods --------------
    
    public boolean hasAlpha(BufferedImage im)
    // does im have an alpha channel?
    {
        if (im == null)
            return false;
        
        int transparency = im.getColorModel().getTransparency();
        
        if ((transparency == Transparency.BITMASK) ||
                (transparency == Transparency.TRANSLUCENT))
            return true;
        else
            return false;
    } // end of hasAlpha()
    
    
    public BufferedImage copyImage(BufferedImage src)
    // make a copy of the BufferedImage src
    {
        if (src == null) {
            System.out.println("copyImage: input image is null");
            return null;
        }
        
        int transparency = src.getColorModel().getTransparency();
        BufferedImage copy =  gc.createCompatibleImage(
                src.getWidth(), src.getHeight(), transparency );
        Graphics2D g2d = copy.createGraphics();
        
        // copy image
        g2d.drawImage(src, 0, 0, null);
        g2d.dispose();
        
        return copy;
    } // end of copyImage()
    
    
    
    public BufferedImage makeTransImage(BufferedImage src)
  /* Make a copy of src, but in a BufferedImage object
     with an alpha channel. */
    {
        if (src == null) {
            System.out.println("makeTransImage: input image is null");
            return null;
        }
        
        BufferedImage dest = new BufferedImage( src.getWidth(), src.getHeight(),
                BufferedImage.TYPE_INT_ARGB);  // alpha channel
        Graphics2D g2d = dest.createGraphics();
        
        // copy image
        g2d.drawImage(src, 0, 0, null);
        g2d.dispose();
        
        return dest;
    } // end of makeTransImage()
    
}
