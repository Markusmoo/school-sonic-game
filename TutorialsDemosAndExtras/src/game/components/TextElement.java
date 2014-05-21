package game.components;

import game.engine.*;
import game.assets.*;
import java.awt.*;
import java.awt.image.*;
import game.geometry.*;
import java.awt.geom.*;

/**
 * This class provide a means of displaying textual strings by mapping
 * a specified input string onto corresponding individual character
 * graphics.
 * <P>
 * Note: It is intended that a reference text element object is created,
 * fully specifying the text to image mapping, and that subsequent text
 * element can be derived from the initial reference object. See the 
 * constructor description or the getMatchingText method for more information.
 * <P>
 * There is a known issue with this method in that the '£' unicode 
 * character is converted into a sequence of two characters. It is strongly
 * recommended that any input strings (and indeed character mappings avoid
 * using the '£' character). This warning may also apply to other less common
 * characters!
 *
 * @author <A HREF="mailto:P.Hanna@qub.ac.uk">Philip Hanna</A>
 * @version $Revision: 1 $ $Date: 2007/08 $
 */
public class TextElement extends GameObject {

    ///////////////////////////////////////////////////////////////////////////
    // Class data                                                            //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Text alignment relative to the object x and y. A center
     * alignment will ensure that the text is always centered around the
     * object's center. The left and right alignments will allign the 
     * text to the left or right of the object center respectively.
     */
    public enum Alignment { Left, Centre, Right }

    /**
     * Current text alignment of this text element
     */
    protected Alignment alignment = Alignment.Centre;
        
    /**
     * Image asset sequence used to hold the individual character images 
     */
    protected ImageAssetSequence charRealisation;

    /**
     * Fixed width of each graphical character realisation
     */
    protected int charImageWidth;

    /**
     * Fixed height of each graphical character realisation
     */
    protected int charImageHeight;

    /**
     * Definable pixel separation between characters. A negative
     * separation is permitted. This value will determine how much space
     * is introduced between the characters when they are displayed.
     */ 
    protected int charImageSeparation;
        
    /**
     * Character start x offset relative to the character images. This value
     * denotes how far into the fixed wide character image the character starts
     * (e.g. and w character is likely to have a lower start offset than the 
     * less wide i character). This offset will be used if non-fixed width
     * character display is enabled.
     */
    protected int[] charStartIdx;

    /**
     * Character end y offset relative to the character images. See the 
     * description of charStartIdx for more information.
     */
    protected int[] charEndIdx;

    /**
     * String character map, e.g. "abcdefgh..." where the location of a
     * particular character (case sensitive) within the charMap will determine
     * the offset of the graphical representation within the image sequence.
     */ 
    protected String charMap;
    
    /**
     * Specify if fixed width characters are to be used 
     */
    protected boolean useFixedWidthCharacters = false;    
    
    /**
     * Current string of text to be rendered
     */     
    protected String text;
    
    /**
     * Width of the rendered text. This value is calculated whenever the
     * text String is set and is used to construct the geometry for the object
     */
    protected int textWidth;
    
    /**
     * This is generated whenever text String is set and is used to provide
     * a quick link onto the individual character images (i.e. the images do
     * not need to be looked up during the draw tick)
     */
    protected int[] textMap;

    
    ///////////////////////////////////////////////////////////////////////////
    // Constructors                                                          //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Construct a text element with a default position and draw order.
     * <P>
     * Note: Unless set, the default string displayed by the text element
     * is "NULL". This will cause problems unless the specified set of 
     * character images contain the N, U and L characters. An alterative
     * approach would have been to set a null string.
     *
     * @param  gameLayer GameLayer to which this text element will belong
     * @param  charRealisation ImageAssetSequence holding the character images
     * @param  charImageSeparation integer spacing between characters
     * @param  charMap String mapping characters onto the specified images
     */    
    public TextElement( GameLayer gameLayer, ImageAssetSequence charRealisation, 
            int charImageSeparation, String charMap ) {
        this( gameLayer, charRealisation, charImageSeparation, charMap, 
                "NULL", 0, 0, 0 );        
    }
    
    /**
     * Construct a text element with the specified text string and a default 
     * position and draw order.
     *
     * @param  gameLayer GameLayer to which this text element will belong
     * @param  charRealisation ImageAssetSequence holding the character images
     * @param  charImageSeparation integer spacing between characters
     * @param  charMap String mapping characters onto the specified images
     * @param  text String of text to be displayed
     */        
    public TextElement( GameLayer gameLayer, ImageAssetSequence charRealisation, 
            int charImageSeparation, String charMap, String text ) {
        this( gameLayer, charRealisation, charImageSeparation, charMap, 
                text, 0, 0, 0 );        
    }
    
    /**
     * Construct a text element with the specified text string at the 
     * specified position and draw order.
     *
     * @param  gameLayer GameLayer to which this text element will belong
     * @param  charRealisation ImageAssetSequence holding the character images
     * @param  charImageSeparation integer spacing between characters
     * @param  charMap String mapping characters onto the specified images
     * @param  text String of text to be displayed
     * @param  x double layer x position of this text element
     * @param  y double layer y position of this text element
     * @param  drawOrder integer draw order of this text element
     */        
    public TextElement( GameLayer gameLayer, ImageAssetSequence charRealisation, 
            int charImageSeparation, String charMap, String text,
            double x, double y, int drawOrder ) {        
        super( gameLayer, x, y, drawOrder );

        // Store the character separator and build up mapping information
        // on the specified array of character images
        setCharacterSeparation( charImageSeparation );
        setCharacterRealisationAndMapping( charRealisation, charMap );    
        
        // Set the text to be displayed to that specified
        setText( text );
    }
    
    /**
     * Derive a new text element based on the parameters assigned to a
     * reference text element and using the specified text string.
     * <P>
     * Note: This method is intended to be used to derived a new
     * text element from an existing text element, i.e. there is no
     * need to respecify the character mapping, spacing, alignment, etc.
     *
     * @param  baseElement TextElement object from which to derive the
     *         settings of this text element.
     * @param  text String of text to be displayed
     */        
    public TextElement( TextElement baseElement, String text ) {        
        super( baseElement.gameLayer, 
                baseElement.x, baseElement.y, baseElement.drawOrder );

        this.charRealisation = baseElement.charRealisation;
        this.charStartIdx = baseElement.charStartIdx;
        this.charEndIdx = baseElement.charEndIdx;
        this.charMap = baseElement.charMap;    
        this.charImageWidth = baseElement.charImageWidth;
        this.charImageHeight = baseElement.charImageHeight;
        this.charImageSeparation = baseElement.charImageSeparation;    
        this.textWidth = baseElement.textWidth;
        this.textMap = baseElement.textMap;
        this.alignment = baseElement.alignment;
        this.useFixedWidthCharacters = baseElement.useFixedWidthCharacters;
        
        setText( text );
    }


    ///////////////////////////////////////////////////////////////////////////
    // Methods:                                                              //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Based on the current configuration of this text element, returning a
     * matching text element which displays the specified String of text.
     * <P>
     * Note: It is intended that this method be used to as a convenient means
     * of deriving new text elements from an existing element.
     *
     * @param  text String to be displayed by the returned text element
     * @return TextElement matching the configuration of this text element but
     *         displaying the specified text
    */           
    public TextElement getMatchingElement( String text ) {
        TextElement matchingElement= new TextElement( this, text );
        
        return matchingElement;
    }
    
    /**
     * Set the character images and character map to be used within this
     * text element.
     *
     * @param  charRealisation ImageAssetSequence holding the character images
     * @param  charMap String mapping characters onto the specified images
     * 
     * @exception IllegalArgumentException if the mapping cannot be performed
    */           
    public void setCharacterRealisationAndMapping( 
            ImageAssetSequence charRealisation, String charMap ) {
        if( charRealisation.getNumberOfImages() != charMap.length() )
            throw new IllegalArgumentException( 
                    "TextElement.setCharacterRealisationAndMapping: " +
                    "Character realisation length does not match character map length: " +
                    "Num Images = " + charRealisation.getNumberOfImages() + " " +
                    "Num Chars = " + charMap.length() );
        
        // Store the default width of each character image
        charImageWidth = charRealisation.getImage(0).getWidth();
        charImageHeight = charRealisation.getImage(0).getHeight();
        
        // Check that each character has the same size
        for( int charIdx = 1; 
                charIdx < charRealisation.getNumberOfImages(); charIdx++ )
            if( charRealisation.getImage(charIdx).getWidth() != charImageWidth
                || charRealisation.getImage(charIdx).getHeight() != charImageHeight )
            throw new IllegalArgumentException( 
                    "TextElement.setCharacterRealisationAndMapping: " +
                    "Input character realisation do not all have the same size." );

        // Store the character images and map
        this.charRealisation = charRealisation;
        this.charMap = charMap;

        // Determine the start and end offset of each character within the image
        // In order to do this pixels strips are compared within each character
        // to determine the columns where non-zero pixels are first and last found.
        charStartIdx = new int[charMap.length()];
        charEndIdx = new int[charMap.length()];
        for( int charIdx = 0; charIdx < charStartIdx.length; charIdx++ ) {
            BufferedImage charImage = charRealisation.getImage(charIdx);
            
            charStartIdx[charIdx] = 0;
            boolean charStartFound = false;
            while( !charStartFound && charStartIdx[charIdx] < charImage.getWidth()-1 ) {
                for( int pixelIdx = 0; pixelIdx < charImage.getHeight(); pixelIdx++ )
                    if( (charImage.getRGB( 
                            charStartIdx[charIdx], pixelIdx ) & 0x000000FF ) != 0 )
                        charStartFound = true;                        
                
                charStartIdx[charIdx] += 1;
            }

            charEndIdx[charIdx] = charImage.getWidth()-1;
            boolean charEndFound = false;
            while( !charEndFound && charEndIdx[charIdx] > charStartIdx[charIdx] ) {
                for( int pixelIdx = 0; pixelIdx < charImage.getHeight(); pixelIdx++ )
                    if( (charImage.getRGB( 
                            charEndIdx[charIdx], pixelIdx ) & 0x000000FF) != 0 )
                        charEndFound = true;                        
                
                charEndIdx[charIdx] -= 1;
            }
            
            // If an entirely blank character was found, i.e. a space, then
            // set the width equal to a third of the blank character width
            // (this is the assumed width of the space character).
            if( charStartIdx[charIdx] == charEndIdx[charIdx] ) {
                charStartIdx[charIdx] = 0;
                charEndIdx[charIdx] = charImage.getWidth()/3;
            }            
        }
    }
    
    /**
     * Set the text to be displayed to that specified.
     * <P>
     * Note: There is a known issue with this method in that the '£' unicode 
     * character is converted into a sequence of two characters. It is strongly
     * recommended that any input strings (and indeed character mappings avoid
     * using the '£' character). This warning may also apply to other less common
     * characters!
     * 
     * @param  text String of text to be displayed
     * 
     * @exception IllegalArgumentException if the input string contains unmapped
     *            characters
    */           
    public void setText( String text ) {
        this.text = text;

        textMap = new int[text.length()];
        for( int charIdx = 0; charIdx < text.length(); charIdx++ ) {
            char letter = text.charAt(charIdx);

            int charMapIdx = charMap.indexOf( letter );
            if( charMapIdx != -1 )
                textMap[charIdx] = charMapIdx;
            else throw new IllegalArgumentException( "TextElement.setText: " 
                    + "Input text contains unmapped character [" + letter + "]" );                      
        }

        if( text.length() > 0 )
            defineTextElementGeometry();
    }
    
    /**
     * Based on the specified text to be displayed and the specified text
     * alignment define the geometry of this text element.
    */               
    private void defineTextElementGeometry() {
        textWidth = 0;

        if( textMap == null )
            return;

        // Build up the text width of the specified string
        for( int charIdx = 0; charIdx < textMap.length; charIdx++ ) 
            if( useFixedWidthCharacters )
                textWidth += charImageWidth + charImageSeparation;
            else
                textWidth += charEndIdx[textMap[charIdx]] - charStartIdx[textMap[charIdx]] +charImageSeparation +1;

        // Do not permit negative text widths, e.g. if a large negative 
        // charImageSeparation has been specified
        if( textWidth < 1 ) textWidth = 1; 

        // Create an appropriate geometry size based on the current alignment
        switch( alignment ) {
            case Centre: 
                setGeometry( new Box( 0.0, 0.0, textWidth, charImageHeight ) ); 
                break;
            case Right: 
                setGeometry( new Box( textWidth/2, 0.0, textWidth, charImageHeight ) ); 
                break;
            case Left: 
                setGeometry( new Box( -textWidth/2, 0.0, textWidth, charImageHeight ) ); 
                break;                
        }        
    }
    
    /**
     * Return the current text String displayed by this text element
     * 
     * @return String containing the current text 
     */                
    public String getText() {
        return text;
    }
    
    /**
     * Set the text alignment to that specified
     * 
     * @param alignment Alignment to be used
     */                
    public void setAlignment( Alignment alignment ) {
        this.alignment = alignment;
        defineTextElementGeometry();
    }
    
    /**
     * Return the current text alignment used by this text element
     * 
     * @return Alignment currently used by this text element
     */                    
    public Alignment getAlignmnet() {
        return alignment;
    }

    /**
     * Set the character separation to that specified
     * 
     * @param int character separation to use
     */                
    public void setCharacterSeparation( int charImageSeparation ) {
        this.charImageSeparation = charImageSeparation;
        defineTextElementGeometry();
    }
    
    /**
     * Return the current character separation
     * 
     * @return integer current character separation
     */                    
    public int getCharacterSeparation() {
        return charImageSeparation;
    }
    
    /**
     * Set if fixed width characters are to be used
     * 
     * @param useFixedWidthCharacters boolean true if fixed wide characters
     *        are to be used
     */                
    public void setUseFixedWidthCharacters( boolean useFixedWidthCharacters ) {
        this.useFixedWidthCharacters = useFixedWidthCharacters;
        defineTextElementGeometry();
    }
    
    /**
     * Return if fixed width characters are currently used
     * 
     * @return Boolean true if fixed width characters are currently used,
     *         otherwise false
     */                    
    public boolean useFixedWidthCharacters() {
        return useFixedWidthCharacters;
    }
        
    /**
     * Render the text element at the specified x and y draw location
     *
     * @param  graphics2D graphical object on which to render this asset
     * @param  drawX x offset at which the object should be rendered
     * @param  drawY y offset at which the object should be rendered
     */
    @Override
    public void draw( Graphics2D graphics2D, int drawX, int drawY )
    {
        AffineTransform originalAffineTransofrm = graphics2D.getTransform(); 

        // Create a new affine rotational transform centered on the 
        // middle of the object and use this for the graphics object
        AffineTransform rotationAffineTransform = new AffineTransform();
        rotationAffineTransform.rotate( rotation, drawX, drawY );
        graphics2D.transform( rotationAffineTransform );
        
        int charDrawX = 0, charDrawY = drawY - charImageHeight/2;
        
        switch( alignment ) {
            case Centre: charDrawX = drawX-textWidth/2; break;
            case Left: charDrawX = drawX-textWidth; break;
            case Right: charDrawX = drawX; break;            
        }
        
        for( int charIdx = 0; charIdx < textMap.length; charIdx++ ) {            
            int charWidth = charEndIdx[textMap[charIdx]] 
                    - charStartIdx[textMap[charIdx]] +1;
            
            if( useFixedWidthCharacters ) {
                graphics2D.drawImage( charRealisation.getImage(textMap[charIdx]), 
                    charDrawX, charDrawY, null );
                charDrawX += charImageWidth + charImageSeparation;
            }
            else {
                graphics2D.drawImage( charRealisation.getImage(textMap[charIdx]), 
                    charDrawX, charDrawY, 
                    charDrawX+charWidth, charDrawY+charImageHeight,
                    charStartIdx[textMap[charIdx]], 0, 
                    charStartIdx[textMap[charIdx]]+charWidth, charImageHeight,
                    null );
                
                charDrawX += charWidth + charImageSeparation;
            }
        }
        
        // Restore the original graphics affine transform
        graphics2D.setTransform( originalAffineTransofrm );        
    }       
}