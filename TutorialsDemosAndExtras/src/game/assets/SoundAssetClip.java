package game.assets;

import javax.sound.sampled.*;
import java.net.URL;

/**
 * SoundAssetClip is intended to provide a playable sound clip that can 
 * either be played once (repeatedly) or continuously looped.
 * <P>
 * Note: sound playback and control within the class is closely based on
 * that available within Davison's 'Killer Game Programming in Java'.
 *
 * @author <A HREF="mailto:P.Hanna@qub.ac.uk">Philip Hanna</A>
 * @version $Revision: 1 $ $Date: 2006/08 $
 */
public class SoundAssetClip extends SoundAsset {

    ///////////////////////////////////////////////////////////////////////////
    // Class data                                                            //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Loaded Clip to be played with looping playback control
     */
    private Clip clip;
    private boolean continuallyPlayClip;


    ///////////////////////////////////////////////////////////////////////////
    // Constructors                                                          //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Construct the sound asset using the specified URL reference to the audio 
     * clip
     *
     * @param  assetName the name of this asset
     * @param  clipURL URL location of the sound clip to be loaded
     * @param  continuallyPlayClip boolean flag controlling if playback should 
     *         be continuous
     *
     * @exception NullPointerException if a null URL has been specified
     */
    public SoundAssetClip(
            String assetName, URL clipURL, boolean continuallyPlayClip) {
        super(assetName);

        if (clipURL == null) {
            throw new NullPointerException("SoundAsset.constructor: " +
                    "NULL audio clip URL specified.");
        }
        
        this.clipURL = clipURL;
        this.clip = this.loadAudioClip(clipURL);
        this.clip.addLineListener(this);
        this.continuallyPlayClip = continuallyPlayClip;
    }

    /**
     * Construct the sound asset using the specified Clip object.
     * <P>
     * Note: If using this constructor then it will not be possible to retrieve 
     * a deep clone of this asset. If a deep clone will be required then make 
     * use of the SoundAssetClip( String, URL, boolean) constructor.
     *
     * @param  assetName the name of this asset
     * @param  clip Clip to be stored within the asset
     * @param  continuallyPlayClip boolean flag controlling if playback should 
     *         be continuous
     *
     * @exception NullPointerException if a null URL has been specified
     */
    public SoundAssetClip(
            String assetName, Clip clip, boolean continuallyPlayClip) {
        super(assetName);

        if (clip == null) {
            throw new NullPointerException("SoundAsset.constructor: " +
                    "NULL audio clip specified.");
        }
                
        this.clip = clip;
        this.clip.addLineListener(this);
        this.continuallyPlayClip = continuallyPlayClip;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Methods:                                                              //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Set the clip playback volume.
     * <P>
     * Note: The volume ranges from 0.0 (lowest) to 1.0 (highest)
     *
     * @param clipVolume clip playback volume from 0.0 (lowest) to 1.0 (highest)
     *
     * @exception IllegalStateException if the volume cannot be changed on 
     *            the assembly clip
     */
    public void setVolume(float clipVolume) {
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN) == false) {
            throw new IllegalStateException("SoundAsset.setVolume: " +
                    "The stored clip does not support volume changes.");
        }
        
        FloatControl gainControl = (FloatControl) clip.getControl( 
                FloatControl.Type.MASTER_GAIN );
        float volumeRange = gainControl.getMaximum() - gainControl.getMinimum();
        float newGain = volumeRange * clipVolume + gainControl.getMinimum();

        gainControl.setValue(newGain);
    }

    /**
     * Set the clip pan for the playback of the clip.
     * <P>
     * The pan value ranges from -1.0 (all left speaker), through
     * 0.0 (equal left and right) to 1.0 (all right speaker). If clip panning
     * is not supported then this method will attempt to mimic the effect
     * using a balance control.
     * <P>
     * Note: The default mixed in J2SE 1.6 does not support panning, which
     * means that a balance control will typically be requested. However,
     * unless the clip is in stereo then this too will fail, i.e. as a
     * failsafe, ensure any clips intended to be played with different pan
     * settings are loaded in stereo (i.e. a mono clip should be converted
     * into a equal channel stereo equivalent).
     *
     * @param clipPan clip pan value from -1.0 (all left) to 1.0 (all right)
     *
     * @exception IllegalStateException if neither a pan or blance control 
     *            is available
     */
    public void setPan(float clipPan) {
        if (clip.isControlSupported(FloatControl.Type.PAN)) {
            FloatControl panControl 
                    = (FloatControl) clip.getControl( FloatControl.Type.PAN );
            panControl.setValue(clipPan);
        } else if (clip.isControlSupported(FloatControl.Type.BALANCE)) {
            FloatControl balControl 
                    = (FloatControl) clip.getControl( FloatControl.Type.BALANCE );
            balControl.setValue(clipPan);
        } else {
            throw new IllegalStateException("" + "SoundAsset.setPan: " +
                    "Cannot change pan or balance on loaded clip.");
        }
    }

    /**
     * Start playback of the clip, continually looping if requested.
     * <P>
     * Note: As a SoundAssetClip can only contain one clip the return from this
     * method will always be an index of 0.
     *
     * @return integer index to the Clip which was selected for playback (always 0)
     */
    public int play() {
        clip.setFramePosition(0);

        if (continuallyPlayClip == true) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        } else {
            clip.start();
        }
        
        return 0;
    }

    /**
     * Return if this clip is currently playing.
     *
     * @return boolean true if the clip is currently playing, otherwise false)
     */
    public boolean isPlaying() {
        return clip.isRunning();
    }

    /**
     * Stop playback of the clip.
     */
    public void stop() {
        clip.stop();
    }

    /**
     * Inform all observing objects when the clip has finished playback.
     *
     * @param lineEvent LineEvent event
     */
    public void update(LineEvent lineEvent) {
        if (lineEvent.getType() == LineEvent.Type.STOP) {
            clip.stop();
            clip.setFramePosition(0);

            this.setChanged();
            this.notifyObservers("SoundPlayed");
        }
    }

    /**
     * Return a shallow clone of this sound asset clip.
     *
     * @return  new Asset instance containing a shallow clone of this instance
     */
    public Asset shallowClone() {
        SoundAssetClip clone 
                = new SoundAssetClip(assetName, clip, continuallyPlayClip);
        return clone;
    }

    /**
     * Return a deep clone of this sound asset clip.
     * <P>
     * Note: If this object was constructed without providing a URL to the 
     * Clip then a deep clone of the object cannot be obtained and an 
     * exception will be generated.
     *
     * @return  new Asset instance containing a deep clone of this instance
     *
     * @exception IllegalStateException if it is not possible to obtain 
     *            a deep clone this asset
     */
    public Asset deepClone() {
        if (clipURL == null) {
            throw new IllegalStateException("SoundAsset.deepClone: " +
                    "Object constructed without clip URL specified. " +
                    "Deep clone not possible.");
        }
        
        SoundAssetClip clone 
                = new SoundAssetClip(assetName, clipURL, continuallyPlayClip);
        return clone;
    }
}
