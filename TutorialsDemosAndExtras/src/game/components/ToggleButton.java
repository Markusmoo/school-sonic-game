package game.components;

import game.engine.*;
import game.assets.*;
import java.awt.event.MouseEvent;

/**
 * This class, extending GameObject, provides support for a generic (and
 * simple) toggle button.
 *
 * @author <A HREF="mailto:P.Hanna@qub.ac.uk">Philip Hanna</A>
 * @version $Revision: 1 $ $Date: 2007/08 $
 */
public class ToggleButton extends GameObject {

    ///////////////////////////////////////////////////////////////////////////
    // Class data: Button state                                              //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Possible button states - unselected is when the button is, unsurprisingly,
     * unselected.
     */
    protected enum ButtonStatus { UNSELECTED, SELECTED }
    
    /**
     * Current button state
     */    
    protected ButtonStatus buttonStatus = ButtonStatus.UNSELECTED;

    /**
     * Boolean flag used to track if the mouse is currently over the
     * button
     */
    protected boolean mouseOver = false;

    /**
     * Boolean flag used to track if the button is currently selected
     */
    protected boolean clicked = false;

    
    ///////////////////////////////////////////////////////////////////////////
    // Class data: Button look and sound                                     //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Name of the graphical asset used to represent the default button state
     */    
    protected String unselectedImage;

    /**
     * Name of the graphical asset used to represent the mouse over button state
     */    
    protected String mouseOverImage;

    /**
     * Name of the graphical asset used to represent the selected button state
     */    
    protected String selectedImage;

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
     * Construct a toggle button object with the specified appearance and no sound
     * effects at the default location and with the default draw order.
     *
     * @param  gameLayer GameLayer to which this Button will belong
     * @param  buttonName name to be given to this Button object
     * @param  unselectedImage String asset name of the unselected button image
     * @param  mouseOverImage String asset name of the mouse over image
     * @param  selectedImage String asset name of the selected button image
     */    
    public ToggleButton(GameLayer gameLayer, String buttonName, 
            String unselectedImage, String mouseOverImage, String selectedImage) {
        this(gameLayer, buttonName, unselectedImage, mouseOverImage, selectedImage, 
                null, null, 0.0, 0.0, 0);
    }

    /**
     * Construct a toggle button object with the specified appearance and no sound
     * effects at the specified location with the default draw order.
     *
     * @param  gameLayer GameLayer to which this Button will belong
     * @param  buttonName name to be given to this Button object
     * @param  unselectedImage String asset name of the unselected button image
     * @param  mouseOverImage String asset name of the mouse over image
     * @param  selectedImage String asset name of the selected button image
     * @param  x double layer x offset of this button
     * @param  y double layer y offset of this button
     */    
    public ToggleButton(GameLayer gameLayer, String buttonName, 
            String unselectedImage, String mouseOverImage, String selectedImage, 
            double x, double y) {
        this(gameLayer, buttonName, 
                unselectedImage, mouseOverImage, selectedImage, null, null, x, y, 0);
    }

    /**
     * Construct a toggle button object with the specified appearance and no sound
     * effects at the specified location and with the specified draw order.
     *
     * @param  gameLayer GameLayer to which this Button will belong
     * @param  buttonName name to be given to this Button object
     * @param  unselectedImage String asset name of the unselected button image
     * @param  mouseOverImage String asset name of the mouse over image
     * @param  selectedImage String asset name of the selected button image
     * @param  x double layer x offset of this button
     * @param  y double layer y offset of this button
     * @param  drawOrder integer draw order of this Button
     */    
    public ToggleButton(GameLayer gameLayer, String buttonName, 
            String unselectedImage, String mouseOverImage, String selectedImage, 
            double x, double y, int drawOrder) {
        this(gameLayer, buttonName, 
                unselectedImage, mouseOverImage, selectedImage, 
                null, null, x, y, drawOrder);
    }

    /**
     * Construct a toggle button object with the specified appearance and sound
     * effects with the default location and draw order.
     *
     * @param  gameLayer GameLayer to which this Button will belong
     * @param  buttonName name to be given to this Button object
     * @param  unselectedImage String asset name of the unselected button image
     * @param  mouseOverImage String asset name of the mouse over image
     * @param  selectedImage String asset name of the selected button image
     * @param  mouseOverSound String asset name of the mouse over sound
     * @param  clickSound String asset name of the button click sound
     */    
    public ToggleButton(GameLayer gameLayer, String buttonName, 
            String unselectedImage, String mouseOverImage, String selectedImage, 
            String mouseOverSound, String clickSound) {
        this(gameLayer, buttonName, 
                unselectedImage, mouseOverImage, selectedImage, 
                mouseOverSound, clickSound, 0.0, 0.0, 0);
    }

    /**
     * Construct a toggle button object with the specified appearance and sound
     * effects at the specified location with the default draw order.
     *
     * @param  gameLayer GameLayer to which this Button will belong
     * @param  buttonName name to be given to this Button object
     * @param  unselectedImage String asset name of the unselected button image
     * @param  mouseOverImage String asset name of the mouse over image
     * @param  selectedImage String asset name of the selected button image
     * @param  mouseOverSound String asset name of the mouse over sound
     * @param  clickSound String asset name of the button click sound
     * @param  x double layer x offset of this button
     * @param  y double layer y offset of this button
     */        
    public ToggleButton(GameLayer gameLayer, String buttonName, 
            String unselectedImage, String mouseOverImage, String selectedImage, 
            String mouseOverSound, String clickSound, double x, double y) {
        this(gameLayer, buttonName, unselectedImage, 
                mouseOverImage, selectedImage, mouseOverSound, clickSound, x, y, 0);
    }

    /**
     * Construct a toggle button object with the specified appearance and sound
     * effects at the specified location and with the specified draw order.
     *
     * @param  gameLayer GameLayer to which this Button will belong
     * @param  buttonName name to be given to this Button object
     * @param  unselectedImage String asset name of the unselected button image
     * @param  mouseOverImage String asset name of the mouse over image
     * @param  selectedImage String asset name of the selected button image
     * @param  mouseOverSound String asset name of the mouse over sound
     * @param  clickSound String asset name of the button click sound
     * @param  x double layer x offset of this button
     * @param  y double layer y offset of this button
     * @param  drawOrder integer draw order of this Button
     */    
    public ToggleButton(GameLayer gameLayer, String buttonName, 
            String unselectedImage, String mouseOverImage, String selectedImage, 
            String mouseOverSound, String clickSound, 
            double x, double y, int drawOrder) {
        super(gameLayer, x, y, drawOrder);

        if (buttonName != null) {
            setName(buttonName);
        }
        
        // Retrieve and store selected and unselected images
        this.unselectedImage = unselectedImage;
        this.selectedImage = selectedImage;

        this.setRealisationAndGeometry(unselectedImage);

        // If a mouse over button image has been specified, then retrieve and
        // store this button image. If no mouse over button has been specified
        // then use the default unselected button image.
        if (mouseOverImage != null) {
            this.mouseOverImage = mouseOverImage;
        } else {
            this.mouseOverImage = unselectedImage;
        }
        
        // Store the specified mouse click and mouse over sound clips
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
            // Check if the button is not pressed and the previous status
            // was unselected. If so, then change the mouse status and realisation
            // to mouse over, playing a sound if needed.
            if (mouseOver == false) {
                mouseOver = true;

                setRealisationAndGeometry(mouseOverImage);

                if (mouseOverSound != null) {
                    assetManager.retrieveSoundAsset( mouseOverSound ).play();
                }
            }

            // Check if the mouse button has been pressed and the current mouse
            // status is mouse over. If so, then change the status and realisation
            // to selected, playing a sound if needed.
            if (inputEvent.mouseClicked(MouseEvent.BUTTON1) 
                    && mouseOver == true && clicked == false) {
                clicked = true;

                if (buttonStatus == ButtonStatus.UNSELECTED) {
                    buttonStatus = ButtonStatus.SELECTED;
                    setRealisationAndGeometry(selectedImage);
                } else {
                    buttonStatus = ButtonStatus.UNSELECTED;
                    setRealisationAndGeometry(unselectedImage);
                }
                
                if (clickSound != null) {
                    assetManager.retrieveSoundAsset(clickSound).play();
                }                
            }
        } else {
            mouseOver = false;
            clicked = false;

            // Revert to the image for the button state whenever the mouse
            // is not over the button
            if (buttonStatus == ButtonStatus.SELECTED) {
                setRealisationAndGeometry(selectedImage);
            } else {
                setRealisationAndGeometry(unselectedImage);
            }
        }
    }

    /**
     * Set the status of the button to that specified
     * 
     * @param selected boolean determining if this button should be selected
     */            
    public void setSelected(boolean selected) {
        if (selected == true) {
            buttonStatus = buttonStatus.SELECTED;
        } else {
            buttonStatus = buttonStatus.UNSELECTED;
        }
    }

    /**
     * Return true if the button is currently selected
     * 
     * @return Boolean true if this button is currently selected, otherwise false
     */            
    public boolean isSelected() {
        if (buttonStatus == buttonStatus.SELECTED) {
            return true;
        } else {
            return false;
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
