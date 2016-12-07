
    Isolate the usages of SPI Fly dynamic weaving.
    
    The play, pause, and stop methods all work as expected from both the GUI
    and the Gogo Shell using the command line. The problem I'm trying to solve
    is strictly related to the load method. The load method works as expected
    when executed from the Gogo Shell using the command line, but does not
    work as expected when executed from the GUI.
    
    The purpose of this commit is to isolate the functionality that is causing
    the load method to fail from the GUI, specifically, to isolate the integration
    with the Apache Aries SPI Fly dynamic weaving capability. This commit makes
    it possible to configure turning this functionality on or off. If dynamic
    weaving is disabled, the load method works as expected from both the GUI
    and the Gogo Shell using the command line. If dynamic weaving is enabled
    the load method fails when executed from the GUI but succeeds when executed
    from the command line.
    
    Obviously disabling dynamic weaving is equivalent to disabling audio when
    the Media Player is playing because dynamic weaving is required within the
    OSGi container to play an MP3 file.
    
    The problem is located in MP3Player on line 124:
    
        '''java
    
        temp = (SourceDataLine) AudioSystem.getLine(info);
    
        '''
    
    When dynamic weaving is enabled this line will execute successfully when
    executed with the following command line in the Gogo Shell:
    
        '''shell
    
        mp3:load [path_to_mp3_file]
    
        '''
    
    The argument `path_to_mp3_file` should be the canonical path to the mp3 file
    and should be wrapped in quote marks. For example if there was test.mp3 file
    located in a folder called music in the user's home directory then the command
    would look like this when run in a *nix OS:
    
        '''shell
    
        mp3:load "/home/user/music/test.mp3"
    
        '''
    
    But it will fail with the following error when executed from the GUI:
    
        '''text
    
        java.lang.IllegalArgumentException: No line matching interface SourceDataLine supporting format PCM_SIGNED 44100.0 Hz, 16 bit, stereo, 4 bytes/frame, little-endian is supported.
          at javax.sound.sampled.AudioSystem.getLine(AudioSystem.java:479)
          at com.github.axiopisty.plarpebu.mediaplayer.mp3.provider.internal.MP3Player$MP3Reader.<init>(MP3Player.java:124)
    
        '''
    
    I'm trying to figure out how to fix this so the load method will work
    from the GUI too. But I'm going to need help from an OSGi expert because
    this is beyond my current scope of knowledge.


