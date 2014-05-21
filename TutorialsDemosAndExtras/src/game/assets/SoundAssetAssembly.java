package game.assets;

import java.net.URL;
import javax.sound.sampled.*;

/**
 * SoundAssetAssembly is intended to provide a determinable collection of
 * instances of a particular sound Clip that have managed playback control.
 * <P>
 * In particular, repeated calls to the play method will start playback of
 * clips within the assembly up until all clips within the assembly are
 * currently playing. In other words, this class is intended to be used in
 * situations where there may be a sizeable number of requests to play the
 * same sound clip (e.g. an explosion sfx) and it is desirable to limited
 * the number of simultaneous playbacks to a manageable quantity.
 * <P>
 * Note: If all clips are currently playing then any calls to play() will
 * cut short the playback of the clip that has been playing the longest.
 * An alternative model would be to ignore the play request until a free
 * channel becomes available.
 * <P>
 * Note: sound playback and control within the class is closely based on
 * that available within Davison's 'Killer Game Programming in Java'.
 *
 * @author <A HREF="mailto:P.Hanna@qub.ac.uk">Philip Hanna</A>
 * @version $Revision: 1 $ $Date: 2006/08 $
 */
public class SoundAssetAssembly extends SoundAsset {

    ///////////////////////////////////////////////////////////////////////////
    // Class data                                                            //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Clip array comprising copies of a single Clip and index identifying the
     * next clip that playback should be attempted for
     */
    private Clip[] clipAssembly;
    private int nextPlaybackIdx = 0;

    /**
     * Variables that determine if the playback volume of the next playback is 
     * to be modified
     */
    private boolean alterNextClipVolume = false;
    private float nextClipVolume;

    /**
     * Variables that determine if the pan of the next playback is to be modified
     */
    private boolean alterNextClipPan = false;
    private float nextClipPan;


    ///////////////////////////////////////////////////////////////////////////
    // Constructors                                                          //
    ///////////////////////////////////////////////////////////////////////////
    /**
     * Construct the sound asset assembly using the specified clip URL and 
     * assembly size
     *
     * @param  assetName the name of this asset
     * @param  clipURL URL location of the sound clip to be loaded
     * @param  clipAssemblySize size of the assembly containing the loaded clip
     *
     * @exception NullPointerException if a null URL has been specified
     * @exception IllegalArgumentException if an invalid clip assembly size 
     *            is specified
     */
    public SoundAssetAssembly(String assetName, URL clipURL, int clipAssemblySize) {
        super(assetName);

        if (clipURL == null) {
            throw new NullPointerException("SoundAsset.constructor: " +
                    "NULL audio clip URL specified.");
        }
        
        if (clipAssemblySize < 1) {
            throw new IllegalArgumentException("SoundAssetAssembly.constructor: " +
                    "The clip assembly size must be greater than zero.");
        }
        
        this.clipURL = clipURL;

        clipAssembly = new Clip[clipAssemblySize];
        for (int clipIdx = 0; clipIdx < clipAssemblySize; clipIdx++) {
            clipAssembly[clipIdx] = loadAudioClip(clipURL);
            clipAssembly[clipIdx].addLineListener(this);
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    // Methods: Set Playback Details                                         //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Set the clip playback volume for the playback of all clips within the
     * assembly.
     * <P>
     * Note: The volume ranges from 0.0 (lowest) to 1.0 (highest)
     *
     * @param clipVolume clip playback volume from 0.0 (lowest) to 1.0 (highest)
     *
     * @exception IllegalStateException if the volume cannot be changed on 
     *            the assembly clip
     */
    public void setAssemblyVolume(float clipVolume) {
        if (clipAssembly[0].isControlSupported(
                FloatControl.Type.MASTER_GAIN) == false) {
            throw new IllegalStateException("SoundAssetAssembly.setAssemblyVolume: " +
                    "The stored clip does not support volume changes.");
        }
        
        for (int clipIdx = 0; clipIdx < clipAssembly.length; clipIdx++) {
            FloatControl gainControl = (FloatControl) clipAssembly[clipIdx].
                    getControl( FloatControl.Type.MASTER_GAIN );
            float volumeRange = gainControl.getMaximum() - gainControl.getMinimum();
            float newGain = volumeRange * clipVolume + gainControl.getMinimum();

            gainControl.setValue(newGain);
        }
    }

    /**
     * Set the clip playback volume for the playback of the *next* clip to be
     * started following a call to the play method
     *
     * @param clipVolume clip playback volume from 0.0 (lowest) to 1.0 (highest)
     */
    public void setVolume(float clipVolume) {
        nextClipVolume = clipVolume;
        alterNextClipVolume = true;
    }

    /**
     * Set the clip pan for the playback of all clips within the assembly.
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
    public void setAssemblyPan(float clipPan) {
        boolean panAvailable = false;
        if (clipAssembly[0].isControlSupported(FloatControl.Type.PAN)) {
            panAvailable = true;
        } else if (clipAssembly[0].isControlSupported(
                FloatControl.Type.BALANCE) == false) {
            throw new IllegalStateException("SoundAssetAssembly.setAssemblyPan: " +
                    "Cannot change pan or balance on loaded clip.");
        }
        
        for (int clipIdx = 0; clipIdx < clipAssembly.length; clipIdx++) {
            if (panAvailable) {
                FloatControl panControl = 
                        (FloatControl) clipAssembly[clipIdx].getControl( 
                            FloatControl.Type.PAN );
                panControl.setValue(clipPan);
            } else {
                FloatControl balControl = 
                        (FloatControl) clipAssembly[clipIdx].getControl( 
                            FloatControl.Type.BALANCE );
                balControl.setValue(clipPan);
            }
        }
    }

    /**
     * Set the clip pan for the playback of the *next* clip to be started 
     * following a call to the play method
     *
     * @param clipPan clip pan from -1.0 (all left) to 1.0 (all right)
     */
    public void setPan(float clipPan) {
        nextClipPan = clipPan;
        alterNextClipPan = true;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Methods: Playback, Stop and Clone                                     //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Attempt to start playback of a clip within the assembly. Currently if 
     * all clips within the assembly are playing, then the play request will
     * result in the longest playing clip being restarted.
     * <P>
     * If requested the playback will be modified to have the volume or pan as
     * requested by a previous call to either setVolume or setPan.
     * <P>
     * An index to the assembly Clip selected for playback will be returned
     * by this method (a value of -1 signifies that no Clip was available for
     * playback). The clip index returned by this method will also be returned
     * to observers when notifying the termination of clip playback (i.e. if
     * needed the index value can be used to track when the playback of a
     * particular clip has completed).
     *
     * @return integer index to the Clip which was selected for playback (or -1 
     *         if no Clip was available for playback).
     *
     * @exception IllegalStateException if a previous request to change the 
     *            volume or pan has been requested but this is not possible 
     *            for the clip
     */
    public int play() {
        int clipIdx = -1;

        // Check if there are currently any free clips to play
        if (clipAssembly[nextPlaybackIdx].isRunning() == false) {
            clipIdx = nextPlaybackIdx;
            clipAssembly[clipIdx].setFramePosition(0);

            // If needed alter the playback volume
            if (alterNextClipVolume) {
                if (clipAssembly[clipIdx].isControlSupported(
                        FloatControl.Type.MASTER_GAIN) == false) {
                    throw new IllegalStateException("SoundAssetAssembly.play: " +
                            "The stored clip does not support volume changes.");
                }
                
                FloatControl gainControl 
                        = (FloatControl) clipAssembly[clipIdx].getControl( 
                            FloatControl.Type.MASTER_GAIN );
                float volumeRange = gainControl.getMaximum() - gainControl.getMinimum();
                float newGain = volumeRange * nextClipVolume + gainControl.getMinimum();

                gainControl.setValue(newGain);
            }

            // If needed alter the playback pan
            if (alterNextClipPan) {
                if (clipAssembly[clipIdx].isControlSupported(FloatControl.Type.PAN)) {
                    FloatControl panControl 
                            = (FloatControl) clipAssembly[clipIdx].getControl( 
                                FloatControl.Type.PAN );
                    panControl.setValue(nextClipPan);
                } else if (clipAssembly[clipIdx].isControlSupported(
                        FloatControl.Type.BALANCE)) {
                    FloatControl balControl 
                            = (FloatControl) clipAssembly[clipIdx].getControl( 
                                FloatControl.Type.BALANCE );
                    balControl.setValue(nextClipPan);
                } else {
                    throw new IllegalStateException("" + "SoundAssetAssembly.play: " +
                            "Cannot change pan or balance on loaded clip.");
                }
            }

            clipAssembly[clipIdx].start();

            // Move onto the next clip in the playback sequence
            nextPlaybackIdx = (nextPlaybackIdx + 1) % clipAssembly.length;
        } else {
            // If the next clip is already playing, then it is stopped and
            // the play request reissued.
            clipAssembly[nextPlaybackIdx].stop();
            clipIdx = play();
        }


        // Irrespective of weither a clip was selected for playback, reset the 
        // next clip and next pan changes to false.
        alterNextClipVolume = false;
        alterNextClipPan = false;

        return clipIdx;
    }

    /**
     * Stop playback of all clips within the assembly
     */
    public void stop() {
        for (Clip clip : clipAssembly) {
            clip.stop();
        }
    }

    /**
     * Stop playback of the specified clip index within the assembly
     *
     * @param clipIdx interger index of the clip to be stopped
     *
     * @exception IllegalArgumentException if an invalid clip index has been 
     *            specified
     */
    public void stop(int clipIdx) {
        if (clipIdx < 0 || clipIdx > clipAssembly.length) {
            throw new IllegalArgumentException("SoundAssetAssembly.stop: " +
                    "Invalid clip index specified: " + clipIdx);
        }
        
        clipAssembly[clipIdx].stop();

        // Update the next playback index to point to the stopped clip
        nextPlaybackIdx = clipIdx;
    }

    /**
     * Inform all observing objects when a clip has finished its playback
     *
     * @param lineEvent LineEvent event
     */
    public void update(LineEvent lineEvent) {
        int clipIdx = 0;
        boolean triggingClipFound = false;

        while (triggingClipFound == false && clipIdx < clipAssembly.length) {
            if (clipAssembly[clipIdx] == lineEvent.getSource()) {
                if (lineEvent.getType() == LineEvent.Type.STOP) {
                    clipAssembly[clipIdx].setFramePosition(0);

                    this.setChanged();
                    this.notifyObservers("SoundPlayed " + clipIdx);
                }

                triggingClipFound = true;
            } else {
                clipIdx++;
            }
        }
    }

    /**
     * Return a shallow clone of this sound asset assembly
     *
     * @return  new Asset instance containing a shallow clone of this instance
     */
    public Asset shallowClone() {
        return this;
    }

    /**
     * Return a deep clone of this sound asset assembly
     *
     * @return  new Asset instance containing a deep clone of this instance
     */
    public Asset deepClone() {
        SoundAssetAssembly clone = new SoundAssetAssembly(
                assetName, clipURL, clipAssembly.length);

        return clone;
    }
}