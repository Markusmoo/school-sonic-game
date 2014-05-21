package game.components;

import game.engine.*;
import java.awt.event.MouseEvent;

/**
 * This class, extending GameObject, provides support for a generic (and
 * simple) click button.
 *
 * @author <A HREF="mailto:P.Hanna@qub.ac.uk">Philip Hanna</A>
 * @version $Revision: 1 $ $Date: 2007/08 $
 */
public class Button extends GameObject {

    ///////////////////////////////////////////////////////////////////////////
    // Class data: Button state                                              //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Possible button states - the default state is the, well,
     * default state. The mouse over state occurs whenever the mouse
     * pointer is over the button (but not pressed down). Finally, the
     * pressed state occurs whenever the mouse has been pressed in but
     * not yet released. A different image can be displayed for each
     * state.
     */
    protected enum ButtonStatus { DEFAULT, MOUSEOVER, PRESSED }

    /**
     * Current button state
     */
    protected ButtonStatus buttonStatus = ButtonStatus.DEFAULT;

    /**
     * Boolean flag used to track if the mouse is currently over the
     * button
     */
    protected boolean mouseOver = false;

    /**
     * Boolean flag which is used to signify if the button has been clicked.
     * This flag is set to true following a click. It will be set to false
     * whenever a true value is returned from the wasButtonClicked()
     * method, i.e. whenever the application picks up the button press.
     */
    protected boolean buttonClicked = false;

    
    ///////////////////////////////////////////////////////////////////////////
    // Class data: Button look and sound                                     //
    ///////////////////////////////////////////////////////////////////////////
        
    /**
     * Name of the graphical asset used to represent the default button state
     */
    protected String defaultImage;

    /**
     * Name of the graphical asset used to represent the mouse over button state
     */
    protected String mouseOverImage;
    
    /**
     * Name of the graphical asset used to represent the pressed button state
     */    
    protected String pressedImage;

    /**
     * Name of the sound asset to be played whenever the mouse moves over 
     * the button
     */    
    protected String mouseOverSound;

    /**
     * Name of the sound asset to be played whenever the button is clicked
     */    
    protected String clickSound;


    ///////////////////////////////////////////////////////////////////////////
    // Constructors                                                          //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Construct a button object with the specified appearance, no sound
     * effects and with the default location and draw order
     *
     * @param  gameLayer GameLayer to which this Button will belong
     * @param  buttonName name to be given to this Button object
     * @param  defaultImage String asset name of the default button image
     * @param  mouseOverImage String asset name of the mouse over image
     * @param  pressedImage String asset name of the pressed button image
     */
    public Button(GameLayer gameLayer, String buttonName, 
            String defaultImage, String mouseOverImage, String pressedImage) {
        this(gameLayer, buttonName, defaultImage, mouseOverImage, pressedImage, 
                null, null, 0.0, 0.0, 0);
    }

    /**
     * Construct a button object with the specified appearance, no sound
     * effects and at the specified location with a default draw order
     *
     * @param  gameLayer GameLayer to which this Button will belong
     * @param  buttonName name to be given to this Button object
     * @param  defaultImage String asset name of the default button image
     * @param  mouseOverImage String asset name of the mouse over image
     * @param  pressedImage String asset name of the pressed button image
     * @param  x double layer x offset of this button
     * @param  y double layer y offset of this button
     */
    public Button(GameLayer gameLayer, String buttonName, 
            String defaultImage, String mouseOverImage, String pressedImage, 
            double x, double y) {
        this(gameLayer, buttonName, defaultImage, mouseOverImage, pressedImage, 
                null, null, x, y, 0);
    }

    /**
     * Construct a button object with the specified appearance, no sound
     * effects and at the specified location with the specified draw order
     *
     * @param  gameLayer GameLayer to which this Button will belong
     * @param  buttonName name to be given to this Button object
     * @param  defaultImage String asset name of the default button image
     * @param  mouseOverImage String asset name of the mouse over image
     * @param  pressedImage String asset name of the pressed button image
     * @param  x double layer x offset of this button
     * @param  y double layer y offset of this button
     * @param  drawOrder integer draw order of this Button
     */
    public Button(GameLayer gameLayer, String buttonName, 
            String defaultImage, String mouseOverImage, String pressedImage, 
            double x, double y, int drawOrder) {
        this(gameLayer, buttonName, defaultImage, 
                mouseOverImage, pressedImage, null, null, x, y, drawOrder);
    }

    /**
     * Construct a button object with the specified appearance and sound
     * effects with a default location and draw order.
     *
     * @param  gameLayer GameLayer to which this Button will belong
     * @param  buttonName name to be given to this Button object
     * @param  defaultImage String asset name of the default button image
     * @param  mouseOverImage String asset name of the mouse over image
     * @param  pressedImage String asset name of the pressed button image
     * @param  mouseOverSound String asset name of the mouse over sound
     * @param  clickSound String asset name of the button click sound
     */
    public Button(GameLayer gameLayer, String buttonName, 
            String defaultImage, String mouseOverImage, String pressedImage, 
            String mouseOverSound, String clickSound) {
        this(gameLayer, buttonName, defaultImage, mouseOverImage, pressedImage, 
                mouseOverSound, clickSound, 0.0, 0.0, 0);
    }

    /**
     * Construct a button object with the specified appearance and sound
     * effects at the specified location with a default draw order.
     *
     * @param  gameLayer GameLayer to which this Button will belong
     * @param  buttonName name to be given to this Button object
     * @param  defaultImage String asset name of the default button image
     * @param  mouseOverImage String asset name of the mouse over image
     * @param  pressedImage String asset name of the pressed button image
     * @param  mouseOverSound String asset name of the mouse over sound
     * @param  clickSound String asset name of the button click sound
     * @param  x double layer x offset of this button
     * @param  y double layer y offset of this button
     */
    public Button(GameLayer gameLayer, String buttonName, 
            String defaultImage, String mouseOverImage, String pressedImage, 
            String mouseOverSound, String clickSound, double x, double y) {
        this(gameLayer, buttonName, defaultImage, mouseOverImage, pressedImage, 
                mouseOverSound, clickSound, x, y, 0);
    }
    
    /**
     * Construct a button object with the specified appearance and sound
     * effects at the specified location and with the specified draw order.
     *
     * @param  gameLayer GameLayer to which this Button will belong
     * @param  buttonName name to be given to this Button object
     * @param  defaultImage String asset name of the default button image
     * @param  mouseOverImage String asset name of the mouse over image
     * @param  pressedImage String asset name of the pressed button image
     * @param  mouseOverSound String asset name of the mouse over sound
     * @param  clickSound String asset name of the button click sound
     * @param  x double layer x offset of this button
     * @param  y double layer y offset of this button
     * @param  drawOrder integer draw order of this Button
     */
    public Button(GameLayer gameLayer, String buttonName, 
            String defaultImage, String mouseOverImage, String pressedImage, 
            String mouseOverSound, String clickSound, 
            double x, double y, int drawOrder) {
        super(gameLayer, x, y, drawOrder);

        if (buttonName != null) {
            setName(buttonName);
        }
        
        // Retrieve and store the default button image
        this.defaultImage = defaultImage;
        setRealisationAndGeometry(defaultImage);

        // If a mouse over button image has been specified, then retrieve and
        // store this button image. If no mouse over button has been specified
        // then use the default button image.
        if (mouseOverImage != null) {
            this.mouseOverImage = mouseOverImage;
        } else {
            this.mouseOverImage = defaultImage;
        }
        if (pressedImage != null) {
            this.pressedImage = pressedImage;
        } else {
            this.pressedImage = defaultImage;
        }
        
        // Store the specified mouse click sound clip
        this.mouseOverSound = mouseOverSound;
        this.clickSound = clickSound;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Methods:                                                              //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Update the state of the button depending upon mouse interaction
     */        
    @Override    
    public void update() {
        // Check if the mouse is currently within the bounds of the button
        double layerXClick, layerYClick;
        layerXClick = gameLayer.getLayerPositionFromScreenX(inputEvent.mouseXCoordinate);
        layerYClick = gameLayer.getLayerPositionFromScreenY(inputEvent.mouseYCoordinate);
        if (layerXClick > x - width / 2 && layerXClick < x + width / 2 
                && layerYClick > y - height / 2 && layerYClick < y + height / 2) {
            mouseOver = true;

            // Check if the button is not pressed and the previous status
            // is default. If so, then change the mouse status and realisation
            // to mouse over.
            if (!inputEvent.mouseButton1Pressed 
                    && buttonStatus == ButtonStatus.DEFAULT) {
                buttonStatus = ButtonStatus.MOUSEOVER;
                setRealisationAndGeometry(mouseOverImage);

                if (mouseOverSound != null) {
                    assetManager.retrieveSoundAsset( mouseOverSound ).play();
                }
            }

            // Check if the mouse button has been pressed and the current mouse
            // status is mouse over. If so, then change the status and realisation
            // to mouse pressed.
            if (inputEvent.mouseButton1Pressed 
                    && buttonStatus == ButtonStatus.MOUSEOVER) {
                buttonStatus = ButtonStatus.PRESSED;
                setRealisationAndGeometry(pressedImage);
            }

            // Check if the mouse has been released and the current mouse
            // status is pressed. In which case, change the button to the default
            // status, play a button clicked sound and set the clicked flag to true.
            if (inputEvent.mouseClicked(MouseEvent.BUTTON1) 
                    && buttonStatus == ButtonStatus.PRESSED) {
                buttonStatus = ButtonStatus.DEFAULT;
                setRealisationAndGeometry(defaultImage);

                if (clickSound != null) {
                    assetManager.retrieveSoundAsset(clickSound).play();
                }

                buttonClicked = true;
            }
        } else {
            mouseOver = false;

            // If the mouse is not within the bounds of the button, then ensure
            // we are in the default mouse state
            buttonStatus = ButtonStatus.DEFAULT;
            setRealisationAndGeometry(defaultImage);
        }
    }

    /**
     * Return true if the button has been clicked since this method was
     * last called.
     * <P>
     * Note: Once called, if this method returns true then subsequent calls
     * will return false until the button is next clicked, i.e. in effect
     * the click event is assumed consumed.
     * 
     * @return Boolean true if this button was clicked since the last check,
     *         otherwise false
     */            
    public boolean buttonClicked() {
        if (buttonClicked == false) {
            return false;
        } else {
            buttonClicked = false;
            return true;
        }
    }

    /**
     * Return true if the button is currently in the mouse over state
     * 
     * @return Boolean true if the button is in the mouse over state,
     *         otherwise false
     */                
    public boolean mouseOver() {
        return mouseOver;
    }
}
