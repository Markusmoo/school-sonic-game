package game.components;

import game.engine.*;
import game.assets.*;
import java.awt.*;
import java.awt.event.*;

/**
 * TextField extends TextElement by providing support for keyboard based
 * editing of the string of text displayed by the text element.
 *
 * @author <A HREF="mailto:P.Hanna@qub.ac.uk">Philip Hanna</A>
 * @version $Revision: 1 $ $Date: 2007/08 $
 */
public class TextField extends TextElement {

    ///////////////////////////////////////////////////////////////////////////
    // Class data: Cursor parameters                                         //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Flag determining if a cursor should be displayed when the text field
     * has input focus
     */    
    protected boolean displayCursorOnFocus = true;
    
    /**
     * Number of game update ticks that should elapse before the display of
     * the cursor is toggled
     */
    protected int cursorDisplayDuration = 30;    
    
    /**
     * Flag determining if the cursor is currently displayed
     */ 
    protected boolean cursorDisplayed = false;
    
    /**
     * Pixel width of the cursor to be displayed
     */
    protected int cursorWidth = 1;
    
    /**
     * Colour of the cursor to be displayed
     */
    protected Color cursorColour = Color.WHITE;

    
    ///////////////////////////////////////////////////////////////////////////
    // Class data: Focus and insertion                                       //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Determine if the input focus should be lost/gained on mouse over 
     */
    protected boolean toggleFocusOnMouseOver = false;

    /**
     * Determine if the input focus should be lost/gained on a mouse click
     */
    protected boolean toggleFocusOnMouseClick = false;

    /**
     * Specify if the text field currently has input focus
     */
    protected boolean hasFocus = false;

    /**
     * TextElement offset at which new text should added
     */
    protected int insertIdx = 0;

    /**
     * Maximum number of characters that can be entered into this text field
     */
    protected int maximumTextFieldLength = Integer.MAX_VALUE;

    /**
     * Sound asset to be played on a key click
     */
    protected SoundAsset keyClickSound = null;


    ///////////////////////////////////////////////////////////////////////////
    // Constructors                                                          //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Construct a new text field with the specified character set, character 
     * separation and character map.
     *
     * @param  textFieldName game object name to be given to this object
     * @param  gameLayer GameLayer to which this text field will belong
     * @param  charRealisation ImageAssetSequence holding the character images
     * @param  charImageSeparation integer spacing between characters
     * @param  charMap String mapping characters onto the specified images
     */        
    public TextField(String textFieldName, GameLayer gameLayer, 
            ImageAssetSequence charRealisation, int charImageSeparation, 
            String charMap) {
        this(textFieldName, gameLayer, 
                charRealisation, charImageSeparation, charMap, 0, 0, 0);
    }

    /**
     * Construct a new text field with the specified character set, character 
     * separation and character map at the specified location and with the
     * specified draw order.
     *
     * @param  textFieldName game object name to be given to this object
     * @param  gameLayer GameLayer to which this text field will belong
     * @param  charRealisation ImageAssetSequence holding the character images
     * @param  charImageSeparation integer spacing between characters
     * @param  charMap String mapping characters onto the specified images
     * @param  text String of text to be displayed
     * @param  x double layer x position of this text element
     * @param  y double layer y position of this text element
     * @param  drawOrder integer draw order of this text element
     */        
    public TextField(String textFieldName, GameLayer gameLayer, 
            ImageAssetSequence charRealisation, int charImageSeparation, 
            String charMap, double x, double y, int drawOrder) {
        super(gameLayer, charRealisation, charImageSeparation, 
                charMap, "", x, y, drawOrder);

        setName(textFieldName);
        setAlignment(Alignment.Right);
    }

    
    ///////////////////////////////////////////////////////////////////////////
    // Methods: Configuration                                                //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Set the key click sound to that specified
     * 
     * @param keyClickSound SoundAsset to be played on a key click
     */    
    public void setKeyClickSound(SoundAsset keyClickSound) {
        this.keyClickSound = keyClickSound;
    }

    /**
     * Set the cursor width to that specified
     * 
     * @param cursorWidth integer cursor width to be used
     */    
    public void setCursorWidth(int cursorWidth) {
        this.cursorWidth = cursorWidth;
    }

    /**
     * Set the cursor colour to be used
     * 
     * @param cursorColour Color of the cursor to be displayed
     */    
    public void setCursorColour(Color cursorColour) {
        this.cursorColour = cursorColour;
    }

    /**
     * Set the focus toggle on mouse over to that specified
     * 
     * @param toggleFocusOnMouseOver boolean toggle input focus on mouse over
     */    
    public void toggleFocusOnMouseOver(boolean toggleFocusOnMouseOver) {
        this.toggleFocusOnMouseOver = toggleFocusOnMouseOver;
    }

    /**
     * Set the focus toggle on a mouse click to that specified
     * 
     * @param toggleFocusOnMouseClick boolean toggle input focus on mouse click
     */    
    public void toggleFocusOnMouseClick(boolean toggleFocusOnMouseClick) {
        this.toggleFocusOnMouseClick = toggleFocusOnMouseClick;
    }

    /**
     * Set the maximum input number of characters in the text field
     * 
     * @param maximumTextFieldLength integer maximum number of characters that can
     *        be entered
     */    
    public void setMaximumFieldTextLength(int maximumTextFieldLength) {
        this.maximumTextFieldLength = maximumTextFieldLength;
    }

    /**
     * Set the current input focus to that specified
     * <P>
     * Note: If requested, the input events will be reset, i.e. any
     * pending input events will be cleared - thereby ensuring that
     * only new input will be received by the text field.
     * 
     * @param hasFocus boolean value specifying the current input focus
     */    
    public void setFocus(boolean hasFocus, boolean resetInputEvents) {
        this.hasFocus = hasFocus;

        if (resetInputEvents) {
            inputEvent.resetInputEvents();
        }
    }

    /**
     * Return if the text field currently has input focus
     * 
     * @return Boolean true if the text field currently has input focus, 
     *         otherwise false
     */    
    public boolean hasFocus() {
        return hasFocus;
    }

    
    ///////////////////////////////////////////////////////////////////////////
    // Methods: Update and render                                            //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Check for any change in input focus and if needed consider any updates
     * to the text
     */
    @Override    
    public void update() {
        updateFocus();
        
        if (hasFocus) {
            updateText();
                    
            if (displayCursorOnFocus) {
                if (gameEngine.updateCounter % cursorDisplayDuration == 0) {
                    cursorDisplayed = !cursorDisplayed;
                }
            }            
        }
    }

    /**
     * Check for any change in the input focus
     */    
    private void updateFocus() {
        // Store the on-screen position of this text field (which will be
        // different from the in-layer position) - alternatively, the mouse
        // position could have been converted into its corresponding layer
        // location
        double screenX = gameLayer.getGameObjectScreenX(this);
        double screenY = gameLayer.getGameObjectScreenY(this);
        double screenHalfWidth = gameLayer.getGameObjectScreenWidth(this) / 2.0;
        double screenHalfHeight = gameLayer.getGameObjectScreenHeight(this) / 2.0;

        if (toggleFocusOnMouseOver) {
            if (inputEvent.mouseXCoordinate >= screenX - screenHalfWidth 
                    && inputEvent.mouseXCoordinate <= screenX + screenHalfWidth 
                    && inputEvent.mouseYCoordinate >= screenY - screenHalfHeight 
                    && inputEvent.mouseYCoordinate <= screenY + screenHalfHeight) {
                hasFocus = true;
            } else {
                hasFocus = false;
            }
        }

        if (toggleFocusOnMouseClick) {
            if (inputEvent.mouseXCoordinate >= screenX - screenHalfWidth 
                    && inputEvent.mouseXCoordinate <= screenX + screenHalfWidth 
                    && inputEvent.mouseYCoordinate >= screenY - screenHalfHeight 
                    && inputEvent.mouseYCoordinate <= screenY + screenHalfHeight) {
                if (inputEvent.mouseClicked(MouseEvent.BUTTON1)) {
                    hasFocus = !hasFocus;
                }
            }
        }
    }

    /**
     * Consider any changes to the text
     * <P>
     * Note: This method is only called if the text field already has the
     * input focus
     */    
    private void updateText() {
        // Test if a character has been typed
        if (inputEvent.newKeyTyped()) {
            char typeChar = inputEvent.getNewCharTyped();

            // Check to see if the input character can be displayed in this
            // text field and if so add it, playing the click sound if needed
            if (charMap.indexOf(typeChar) != -1) {
                if (text.length() < maximumTextFieldLength) {
                    if (keyClickSound != null) {
                        keyClickSound.play();
                    }
                    
                    setText((insertIdx > 0 ? text.substring(0, insertIdx) : "") + 
                            Character.toString(typeChar) + (insertIdx < text.length() 
                                ? text.substring(insertIdx, text.length()) : ""));
                    insertIdx++;
                }
            }

            // If needed process a back space click
            if (inputEvent.keyTyped(KeyEvent.VK_BACK_SPACE)) {
                if (insertIdx > 0) {
                    if (keyClickSound != null) {
                        keyClickSound.play();
                    }
                    
                    setText(text.substring(0, insertIdx - 1) + 
                            text.substring(insertIdx, text.length()));
                    insertIdx--;
                }
            }
            
            // If needed process a delete key click
            if (inputEvent.keyTyped(KeyEvent.VK_DELETE)) {
                if (insertIdx < text.length()) {
                    if (keyClickSound != null) {
                        keyClickSound.play();
                    }
                    
                    setText(text.substring(0, insertIdx) + 
                            text.substring(insertIdx + 1, text.length()));
                }
            }
            
            // If needed move the cursor to the right following an arrow press
            if (inputEvent.keyTyped(KeyEvent.VK_RIGHT)) {
                if (insertIdx < text.length()) {
                    if (keyClickSound != null) {
                        keyClickSound.play();
                    }
                    insertIdx++;
                }
            }
            
            // If needed move the cursor to the left following an arrow press
            if (inputEvent.keyTyped(KeyEvent.VK_LEFT)) {
                if (insertIdx > 0) {
                    if (keyClickSound != null) {
                        keyClickSound.play();
                    }
                    insertIdx--;
                }
            }
            
            // If needed, remove input focus following an enter key press
            if (inputEvent.keyTyped(KeyEvent.VK_ENTER)) {
                if (toggleFocusOnMouseClick && hasFocus) {
                    if (keyClickSound != null) {
                        keyClickSound.play();
                    }
                    hasFocus = false;
                }
            }
        }
    }

    /**
     * Render the text field at the specified x and y draw location
     *
     * @param  graphics2D graphical object on which to render this asset
     * @param  drawX x offset at which the object should be rendered
     * @param  drawY y offset at which the object should be rendered
     */
    @Override
    public void draw(Graphics2D graphics2D, int drawX, int drawY) {        
        // Invoke the superclass draw to display the text 
        super.draw(graphics2D, drawX, drawY);

        // If needed, display a cursor
        if (displayCursorOnFocus && hasFocus && cursorDisplayed) {
            if (textMap == null) {
                return;
            }
            
            // Determine the current draw position of the cursor
            int cursorOffset = 0;
            for (int charIdx = 0; charIdx < insertIdx; charIdx++) {
                if (useFixedWidthCharacters) {
                    cursorOffset += charImageWidth + charImageSeparation;
                } else {
                    cursorOffset += charEndIdx[textMap[charIdx]] -
                            charStartIdx[textMap[charIdx]] + charImageSeparation + 1;
                }
            }
            if (cursorOffset < 1) {
                cursorOffset = 1;
            }
            
            // Modify the draw position based on the text element alignment
            switch (alignment) {
                case Right:
                    cursorOffset += (int) width / 2;
                    break;
                case Left:
                    cursorOffset += -(int) width / 2;
                    break;
            }

            // Display the cursor
            Color originalColour = graphics2D.getColor();
            graphics2D.setColor(cursorColour);

            graphics2D.fillRect(
                    gameLayer.getScreenDrawPositionFromLayerX( 
                        this.x-this.width/2.0+cursorOffset ),
                    gameLayer.getScreenDrawPositionFromLayerY(
                        this.y-this.height/2.0 ),
                    cursorWidth, (int)this.height );
            
            
            graphics2D.setColor(originalColour);
        }
    }
}