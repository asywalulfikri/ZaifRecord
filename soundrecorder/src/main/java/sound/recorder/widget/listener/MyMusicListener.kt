package sound.recorder.widget.listener

import android.media.MediaPlayer

object MyMusicListener {
    private var myListener: MusicListener? = null

    fun setMyListener(listener: MusicListener) {
        myListener = listener
    }
    fun postAction(mediaPlayer: MediaPlayer?) {
        myListener?.onMusic(mediaPlayer)
    }
}