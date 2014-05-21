package game.assets;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

/**
 * DrawnAssetMessage provides a means of rendering a textual message.
 * <P>
 * Note: This class does not provide character stroke and fill support.
 * As such, it provides more of a rough and ready means of displaying
 * textual messages, but without the precision and accuracy that would
 * be afforded from the inclusion of stroke and fill support.
 * <P>
 * Note: as currently developed this class invalidates the notion that
 * game object state should only be changed within an update tick (
 * in this class the draw tick can change the state of the object)
 * Ideally, the class should be changed to require that an update 
 * be invoked that will determine when the message is to be removed.
 * 
 * @author <A HREF="mailto:P.Hanna@qub.ac.uk">Philip Hanna</A>
 * @version $Revision: 1.1 $ $Date: 2007/08 $
 */
public class DrawnAssetMessage extends GraphicalAsset {

    ///////////////////////////////////////////////////////////////////////////
    // Class data                                                            //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * String of text to be drawn
     */
    private String messageText;

    /*
     * Font and Color in which the message is to be drawn
     */
    private Font messageFont;
    private Color messageColor;

    /*
     * Border width and colour to be added around the drawn message
     */            
    private int borderWidth;
    private Color borderColor;

    /**
     * BufferedImage copy of the rendered text, i.e. whenever the message
     * is set or changed the rendered text is stored within this instances
     * to avoid the need to continually regenerate the text (which can be
     * a computationally expensive operation if done 60 times a second...)
     */
    private BufferedImage renderedText;

    /**
     * Variables used to control the timing and duration of the textual
     * display. In particular, messageDisplayDuration controls how long
     * the message should be displayed, whilst messageDisplayInitialPause
     * determines the period that should elapse (from the first draw
     * attempt). By default, a timed message is not assumed.
     */
    private boolean useMessageDisplayTime = false;
    private long messageDisplayDuration; // in ms
    private long messageDisplayInitialPause; // in ms
    private long messageDisplayStartTime;


    ///////////////////////////////////////////////////////////////////////////
    // Constructors                                                          //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Create a DrawnAssetMessage instance using the specified font and colour
     * (setMessage can be used to specify/change the message as necessary).
     *
     * @param  assetName the name of this asset
     * @param  messageFont Font to be used to display the message
     * @param  messageColor Color in which the message should be displayed
     */
    public DrawnAssetMessage(String assetName, Font messageFont, Color messageColor) {
        super(assetName);

        setFont(messageFont);
        setColor(messageColor);
        setBorder(0, Color.WHITE);
        setMessage("");
    }

    /**
     * Create a DrawnAssetMessage instance using the specified message, font
     * and colour.
     *
     * @param  assetName the name of this asset
     * @param  messageText String textual message
     * @param  messageFont Font to be used to display the message
     * @param  messageColor Color in which the message should be displayed
     */
    public DrawnAssetMessage(String assetName, String messageText, 
            Font messageFont, Color messageColor) {
        super(assetName);

        setFont(messageFont);
        setColor(messageColor);
        setBorder(0, Color.WHITE);
        setMessage(messageText);
    }

    /**
     * Create a DrawnAssetMessage instance using the specified message, font
     * colour and border information.
     *
     * @param  assetName the name of this asset
     * @param  messageText String textual message
     * @param  messageFont Font to be used to display the message
     * @param  messageColor Color in which the message should be displayed
     * @param  borderWidth integer pixel width of the border
     * @param  messageColor Color of the border
     */
    public DrawnAssetMessage(String assetName, String messageText, 
            Font messageFont, Color messageColor,
            int borderWidth, Color borderColor) {
        super(assetName);

        setFont(messageFont);
        setColor(messageColor);
        setBorder(borderWidth, borderColor);
        setMessage(messageText);
    }


    ///////////////////////////////////////////////////////////////////////////
    // Methods: Timed display                                                //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Enable the timing options for message display. In particular, the message
     * will only be displayed after the initial pause and for the specified
     * duration. The timing is relative to the time where an initial call has
     * been made to the draw method (and not from object creation, etc.).
     *
     * @param  messageDisplayInitialPause initial pause (in ms) before displaying 
     *         the message
     * @param  messageDisplayDuration period (in ms) that the message should be 
     *         displayed for
     */
    public void enableMessageDisplayTime(
            long messageDisplayInitialPause, long messageDisplayDuration) {
        useMessageDisplayTime = true;
        this.messageDisplayDuration = messageDisplayDuration;
        this.messageDisplayInitialPause = messageDisplayInitialPause;

        // A value of -1 is used to signify that the timing start period
        // has yet to be defined.
        messageDisplayStartTime = -1;
    }
    
    /**
     * Return if this message uses a timed display
     *
     * @return  boolean true if the message has an associated display
     *          time, otherwise false
     */    
    public boolean getIsMessageDisplayTimeEnabled() {
        return useMessageDisplayTime;
    }

    /**
     * Return the message display duration
     *
     * @return  long containing the message display duration (in ms)
     */    
    public long getMessageDisplayDuration() {
        return messageDisplayDuration;
    }

    /**
     * Return the message initial display pause duration
     *
     * @return  long containing the initial message display pause duration (in ms)
     */    
    public long getMessageDisplayInitialPause() {
        return messageDisplayInitialPause;
    }

    /**
     * Disable the any message display timings (i.e. if rendered the
     * DrawnAssetMessage will always be displayed).
     */
    public void disableMessageDisplayTime() {
        useMessageDisplayTime = false;
    }

    
    ///////////////////////////////////////////////////////////////////////////
    // Methods: Message display options                                      //
    ///////////////////////////////////////////////////////////////////////////
   
    /**
     * Set/change the displayed message text to that specified (any changes
     * will be realised during the next render).
     *
     * @param  messageText String textual message
     */
    public void setMessage(String messageText) {
        this.messageText = messageText;
        renderMessage();
    }

    /**
     * Return the String message rendered by this DrawnAssetMessage
     *
     * @return  String containing the message text
     */
    public String getMessage() {
        return messageText;
    }

    /**
     * Set the Font to be used to render the message text
     *
     * @param  messageFont Font to be used to render the message text
     */
    public void setFont(Font messageFont) {
        this.messageFont = messageFont;
        renderMessage();
    }

    /**
     * Return the Font used to render the message text
     *
     * @return  Font used to render the message text
     */
    public Font getFont() {
        return messageFont;
    }

    /**
     * Set the Color to be used to render the message text
     *
     * @param  messageColor Color to be used to render the message text
     */
    public void setColor(Color messageColor) {
        this.messageColor = messageColor;
        renderMessage();
    }

    /**
     * Return the Color used to render the message text
     *
     * @return  Color used to render the message text
     */
    public Color getColor() {
        return messageColor;
    }

    /**
     * Set the border colour and width to be used to render this message
     *
     * @param  borderWidth integer pixel border width to use
     * @param  bordereColor Color to be used to render the border
     */
    public void setBorder(int borderWidth, Color borderColor) {
        this.borderWidth = borderWidth;
        this.borderColor = borderColor;

        renderMessage();
    }

    /**
     * Return the width of the current border
     *
     * @return  int pixel width of the border
     */
    public int getBorderWidth() {
        return borderWidth;
    }

    /**
     * Return the Color used to render the message border
     *
     * @return  Color used to render the message border
     */
    public Color getBorderColor() {
        return borderColor;
    }

    
    ///////////////////////////////////////////////////////////////////////////
    // Methods: Render and cloning                                           //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Return a BufferedImage realisation of the DrawnAssetMessage
     *
     * @return  BufferedImage containing the rendered message text
     *
     * @exception IllegalStateException if called without a message text having 
     *            been set
     */
    public BufferedImage getImageRealisation() {
        return renderedText;
    }

    /**
     * Render the message text to the specified graphics context at the specified
     * offset location.
     *
     * Note: If message timing is enabled, then this method will:
     * <P>
     * If a start reference time has yet to be set, set the start reference time to
     * the current time.
     * <P>
     * If the messageDisplayInitialPause has yet to elapse or the
     * messageDisplayDuration has been exceeded then the message will not be
     * displayed.
     * <P>
     * Note: By default all rendered text is displayed antialiased. This does
     * considerably improve the readability of the text but can have a heavy
     * computational overhead (certainly given a high target number of messages).
     *
     * @param  graphics2D Graphics context on which to render the asset
     * @param  drawX x draw offset
     * @param  drawY y draw offset
     *
     * @exception IllegalStateException if called without a message text having 
     *            been set
     */
    public void draw(Graphics2D graphics2D, int drawX, int drawY) {
        if (messageText == null) {
            throw new IllegalStateException("DrawnAssetMessage.getImageRealisation: No message has been set.");
        }
        
        // Determine if we need to display the message if message timing is enabled
        if (useMessageDisplayTime) {
            if (messageDisplayDuration == -1) {
                return;
            } else {
                long currentTime = System.nanoTime() / 1000000;

                if (messageDisplayStartTime == -1) {
                    messageDisplayStartTime = currentTime;
                } else {
                    if (currentTime < messageDisplayStartTime 
                            + messageDisplayInitialPause) {
                        return;
                    } else if (currentTime > messageDisplayStartTime 
                            + messageDisplayInitialPause + messageDisplayDuration) {
                        // Notify any observing objects that the message display 
                        // time is now complete
                        setChanged();
                        notifyObservers("MessageDisplayTimeCompleted");

                        messageDisplayDuration = -1;
                        return;
                    }
                }
            }
        }

        graphics2D.drawImage(renderedText, drawX, drawY, null);
    }
    
    /**
     * Based on the specified message, font, colour and border generate
     * an image of the text and store this to be rendered as needed.
     */
    private void renderMessage() {
        if (messageText == null) {
            return;
        }
        
        /**
         * Create a testImage (which is compatible with the current
         * graphics configuration on which to determine the width and
         * height of the rendered textual message, i.e. this will enable
         * the inherited width and height fields of GraphicalAsset to
         * maintain valid information. A FontRenderContent and TextLayout
         * object are used to determine the width and height of the
         * rendered text.
         */
        GraphicsConfiguration graphicsConfiguration 
                = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice().getDefaultConfiguration();

        BufferedImage testImage = graphicsConfiguration.createCompatibleImage(
                1, 1, ColorModel.getRGBdefault().getTransparency());
        Graphics2D graphics2D = testImage.createGraphics();

        FontRenderContext fontRenderContext = graphics2D.getFontRenderContext();
        TextLayout messageLayout 
                = new TextLayout(messageText, messageFont, fontRenderContext);

        float descent = messageLayout.getDescent();        
        float ascent = messageLayout.getAscent();
        width = (int) (messageLayout.getAdvance()) + 2 * borderWidth;
        height = (int) (ascent + descent) + 2 * borderWidth;

        renderedText = graphicsConfiguration.createCompatibleImage(
                width, height, ColorModel.getRGBdefault().getTransparency());
        graphics2D = renderedText.createGraphics();

        // Draw the message text
        graphics2D.setFont(messageFont);
        graphics2D.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (borderWidth > 0) {
            graphics2D.setColor(borderColor);
            graphics2D.drawString(messageText, 0, height - descent);
            graphics2D.drawString(messageText, 0, height - descent - 2 * borderWidth);
            graphics2D.drawString(messageText, height - descent - 2 * borderWidth, 0);
            graphics2D.drawString(messageText, 2 * borderWidth, 
                    height - descent - 2 * borderWidth);
        }

        graphics2D.setColor(messageColor);
        graphics2D.drawString(messageText, borderWidth, height - descent - borderWidth);

        graphics2D.dispose();
    }    

    
    /**
     * Return a clone of this DrawnAssetMessage.
     *
     * Note: As this asset does not depend upon any external image or sound assets
     * there is no functional difference between the shallow and deep clone methods.
     *
     * @return  Asset containing a clone of this instance
     */
    public Asset shallowClone() {
        DrawnAssetMessage clone = new DrawnAssetMessage(
                assetName, messageText, messageFont, messageColor);

        return clone;
    }

    /**
     * Return a clone of this DrawnAssetMessage.
     *
     * Note: As this asset does not depend upon any external image or sound assets
     * there is no functional difference between the shallow and deep clone methods.
     *
     * @return  Asset containing a clone of this instance
     */
    public Asset deepClone() {
        return shallowClone();
    }
}
