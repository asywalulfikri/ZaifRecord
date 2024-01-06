package sound.recorder.widget.listener

object MyPauseListener {
    private var myListener: PauseListener? = null

    fun setMyListener(listener: PauseListener) {
        myListener = listener
    }
    fun postAction(pause : Boolean) {
        myListener?.onPause(pause)
    }
}