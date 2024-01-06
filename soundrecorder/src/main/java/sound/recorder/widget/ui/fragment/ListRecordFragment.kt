package sound.recorder.widget.ui.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.*
import sound.recorder.widget.CustomAppBarLayoutBehavior
import sound.recorder.widget.adapter.AudioRecorderAdapter
import sound.recorder.widget.base.BaseFragmentWidget
import sound.recorder.widget.databinding.ActivityListingNewBinding
import sound.recorder.widget.db.AppDatabase
import sound.recorder.widget.db.AudioRecord
import sound.recorder.widget.ui.activity.PlayerActivityWidget
import java.io.File


class ListRecordFragment : BaseFragmentWidget(), AudioRecorderAdapter.OnItemClickListener {
    private lateinit var audioRecorderAdapter : AudioRecorderAdapter
    private lateinit var audioRecords : List<AudioRecord>
    private lateinit var db : AppDatabase
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private var allSelected = false
    private var nbSelected = 0

    private var _binding: ActivityListingNewBinding? = null
    private val binding get() = _binding!!


    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("NotifyDataSetChanged")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ActivityListingNewBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {

        }


        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        audioRecords = emptyList()
        audioRecorderAdapter = AudioRecorderAdapter(audioRecords, this)

        binding.recyclerview.layoutManager = LinearLayoutManager(activity)
        binding.recyclerview.adapter = audioRecorderAdapter
        binding.recyclerview.itemAnimator = null

        db = Room.databaseBuilder(
            requireActivity(),
            AppDatabase::class.java,
            "audioRecords")
            //.fallbackToDestructiveMigration()
            .build()

        fetchAll()

        binding.searchInput.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val query = p0.toString()
                searchDatabase("%$query%")
            }

        })

        binding.btnSelectAll.setOnClickListener {
            allSelected = !allSelected
            Log.d("ListingTag", allSelected.toString())
            audioRecords.forEach {
                it.isChecked = allSelected
            }

            nbSelected = if (allSelected) audioRecords.size else 0
            updateBottomSheet()

            audioRecorderAdapter.notifyDataSetChanged()
        }

        binding.btnClose.setOnClickListener {
            closeEditor()
        }

        binding.btnCancel.setOnClickListener {
            closeEditor()
        }

        binding.btnDelete.setOnClickListener {
            closeEditor()
            val toDelete : List<AudioRecord> = audioRecords.filter { it.isChecked }
            audioRecords = audioRecords.filter { !it.isChecked }

            GlobalScope.launch {
                db.audioRecordDAO().delete(toDelete)
                if(audioRecords.isEmpty()){
                    fetchAll()
                }else{
                    activity?.runOnUiThread {
                        audioRecorderAdapter.setData(audioRecords)

                    }
                }
            }
        }

        binding.btnRename.visibility = View.GONE
        binding.btnRename.setOnClickListener {
            Toast.makeText(requireActivity(), "rename clicked", Toast.LENGTH_SHORT).show()
        }

        lockAppBarClosed()


    }



    private fun lockAppBarClosed() {
        binding.appBarLayout.setExpanded(false, true)
        val layoutParams = binding.appBarLayout.layoutParams as CoordinatorLayout.LayoutParams
        (layoutParams.behavior as CustomAppBarLayoutBehavior?)?.setScrollBehavior(false)
        val params = binding.collapsingToolbarLayout.layoutParams as AppBarLayout.LayoutParams
        params.scrollFlags = 0
        params.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
       // params.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED
    }

    private fun closeEditor(){
        allSelected = false
        audioRecorderAdapter.setEditMode(false)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        // show relative layout
        binding.editorBar.visibility = View.GONE
        nbSelected = 0
    }


    private fun fetchAll(){
        MainScope().launch {
            withContext(Dispatchers.Default) {
                audioRecords = db.audioRecordDAO().getAll()
            }
            audioRecorderAdapter.setData(audioRecords)
        }

    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun searchDatabase(query: String){
        GlobalScope.launch {
            audioRecords = db.audioRecordDAO().searchDatabase(query)
            activity?.runOnUiThread{
                audioRecorderAdapter.setData(audioRecords)
            }
        }
    }

    private fun updateBottomSheet(){
        when(nbSelected){
            0 -> {
                binding.btnRename.isClickable = false
                binding.btnDelete.isClickable = false

            }
            1 -> {
                binding.btnRename.isClickable = true

                binding.btnDelete.isClickable = true

            }
            else -> {
                binding.btnRename.isClickable = false

                binding. btnDelete.isClickable = true

            }
        }
    }

    override fun onItemClick(position: Int) {
        val intent = Intent(activity, PlayerActivityWidget::class.java)
        val audioRecord = audioRecords[position]

        if(audioRecorderAdapter.isEditMode()){
            Log.d("ITEMCHANGE", audioRecord.isChecked.toString())
            audioRecord.isChecked = !audioRecord.isChecked
            audioRecorderAdapter.notifyItemChanged(position)

            nbSelected = if (audioRecord.isChecked) nbSelected+1 else nbSelected-1
            updateBottomSheet()

        }else{
            if(isExist(audioRecord.filePath)){
                intent.putExtra("filepath", audioRecord.filePath)
                intent.putExtra("filename", audioRecord.filename)
                startActivity(intent)
            }else{
                setToastError(activity,"This Audio Not Found Anymore \uD83D\uDE1E")
            }
        }

    }

    private fun isExist(path : String): Boolean {
        return File(path).exists()
    }

    override fun onItemLongClick(position: Int) {
        audioRecorderAdapter.setEditMode(true)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        val audioRecord = audioRecords[position]

        audioRecord.isChecked = !audioRecord.isChecked

        nbSelected = if (audioRecord.isChecked) nbSelected+1 else nbSelected-1
        updateBottomSheet()
        binding.editorBar.visibility = View.VISIBLE

    }

    fun onBackPressed(): Boolean {
        activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
        return false
    }

}