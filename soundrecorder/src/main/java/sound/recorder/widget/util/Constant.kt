package sound.recorder.widget.util

open class Constant {


    interface keyShared{
        companion object {
            const val backgroundColor = "backgroundColor"
            const val volume = "volume"
            const val animation = "animation"
            const val colorWidget = "colorWidget"
            const val colorRunningText = "colorRunningText"
        }
    }

    interface typeFragment{
        companion object{
            const val listRecordFragment       = "listRecordFragment"
            const val listMusicFragment        = "listMusicFragment"
            const val listNoteFragment         = "listNoteFragment"
            const val settingFragment          = "settingFragment"
            const val videoFragment            = "videoFragment"
            const val listNoteFirebaseFragment = "listNoteFirebaseFragment"
        }
    }

}