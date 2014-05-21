package game.assets;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

/**
 * SoundAsset provides core functionality for all sound based assets (i.e.
 * any sound asset should inherit directly or indirectly from this class).
 *
 * Note: SoundAssets are unlike GraphicalAssets in that they can optionally
 * (and it is recommended that they do) hold a URL reference to the sound
 * clip which can be loaded upon demand. The reason for this is to ensure
 * that the deep clone method can return a seperate copy of the sound clip.
 *
 * @author <A HREF="mailto:P.Hanna@qub.ac.uk">Philip Hanna</A>
 * @version $Revision: 1 $ $Date: 2006/08 $
 */
public abstract class SoundAsset extends Asset implements LineListener {

    ///////////////////////////////////////////////////////////////////////////
    // Class data                                                            //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Optional URL link to the sound clip contained within this asset.
     */
    protected URL clipURL = null;


    ///////////////////////////////////////////////////////////////////////////
    // Constructors                                                          //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Construct the sound asset to use the specified asset name
     *
     * @param  assetName the name of this asset
     */
    public SoundAsset(String assetName) {
        super(assetName);
    }


    ///////////////////////////////////////////////////////////////////////////
    // Methods:                                                              //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Play the clip.
     * <P>
     * Note: Where several clips can be potentially played, this method will
     * return an integer value which can be used to identify the play clip.
     * The interpretation of the integer value will depend upon the
     * implementing class.
     *
     * @return integer value identifying the clip that was played.
     */
    public abstract int play();

    /**
     * Stop playback of this sound asset.
     * <P>
     * Note: Where a sound asset is playing several clips, then all the
     * playback of alll clips will be stopped.
     */
    public abstract void stop();

    /**
     * Determine how this sound asset should react to LineEvents
     *
     * @param lineEvent LineEvent event
     */
    public abstract void update(LineEvent lineEvent);

    /**
     * Attempt to Load the sound clip from the specified URL
     *
     * @param clipName URL specified location of the source clip
     * @return Clip containing the loaded sound clip
     *
     * @exception IllegalArgumentException if the specified audio clip cannot be 
     *            loaded
     */
    public Clip loadAudioClip(URL clipName) {
        Clip loadedClip = null;

        try {
            AudioInputStream inputStream = AudioSystem.getAudioInputStream(clipName);
            AudioFormat format = inputStream.getFormat();

            // Convert ULAW/ALAW compressed clip formats to (uncompressed)
            // PCM format. This is to prevent any problems with a lack of support 
            // for ULAW/ALAQ within the AudioSystem
            if ((format.getEncoding() == AudioFormat.Encoding.ULAW) 
                    || (format.getEncoding() == AudioFormat.Encoding.ALAW)) {
                AudioFormat pcmFormat = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED, format.getSampleRate(), 
                        format.getSampleSizeInBits() * 2, format.getChannels(), 
                        format.getFrameSize() * 2, format.getFrameRate(), true); 
                
                // Update the stream and format details
                inputStream = AudioSystem.getAudioInputStream(pcmFormat, inputStream);
                format = pcmFormat;
            }

            DataLine.Info clipInfo = new DataLine.Info(Clip.class, format);
            if (AudioSystem.isLineSupported(clipInfo) == false) {
                throw new IllegalArgumentException("AssetLoader.loadAudioClip: " +
                        "Clip format for " + clipName + " is not supported.");
            }
            
            loadedClip = (Clip) AudioSystem.getLine( clipInfo );
            loadedClip.open(inputStream);
            inputStream.close();

            loadedClip.setFramePosition(0);
        } catch (UnsupportedAudioFileException exception) {
            throw new IllegalArgumentException("SoundAsset.loadAudioClip: " +
                    "Unsupported audio file exception generated for " + clipName);
        } catch (LineUnavailableException exception) {
            throw new IllegalArgumentException("SoundAsset.loadAudioClip: " +
                    "Unsupported audio file exception generated for " + clipName);
        } catch (IOException exception) {
            throw new IllegalArgumentException("SoundAsset.loadAudioClip: " +
                    "Could not read from " + clipName);
        }

        return loadedClip;
    }
}
