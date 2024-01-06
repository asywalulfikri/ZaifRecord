package sound.recorder.widget.model

import sound.recorder.widget.notes.Note

object MyEventBus {
    private var myListener: MyListener? = null

    fun setMyListener(listener: MyListener) {
        myListener = listener
    }
    fun postActionCompleted(note: Note) {
        myListener?.onCallback(note)
    }
}