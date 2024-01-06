package sound.recorder.widget.model

import sound.recorder.widget.notes.Note

interface MyListener {
    fun onCallback(result: Note)
}