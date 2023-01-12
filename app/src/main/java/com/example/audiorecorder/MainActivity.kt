package com.example.audiorecorder
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.botton_sheet.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

const val REQUEST_CODE = 200
@Suppress("DEPRECATION")

class MainActivity : AppCompatActivity(), Timer.OnTimerTickListener {

    private  lateinit var amplitudes: ArrayList<Float>
    private  var permission = arrayOf(Manifest.permission.RECORD_AUDIO)
    private var permissionGranted = false
    private lateinit var recorder : MediaRecorder
    private var dirPath =""
    private var filename  =""
    private var isRecording = false
    private var isPaused = false
    private lateinit var vibrator: Vibrator
    private lateinit var timer: Timer

    private  lateinit var bottonSheetBehavior: BottomSheetBehavior<LinearLayout>



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        permissionGranted = ActivityCompat.checkSelfPermission(this, permission[0])== PackageManager.PERMISSION_GRANTED

        if(!permissionGranted)
            ActivityCompat.requestPermissions(this,permission, REQUEST_CODE)

        bottonSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottonSheetBehavior.peekHeight =0
        bottonSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED


        timer = Timer(this)

        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        btnRecord.setOnClickListener {
            when {
                isPaused -> resumeRecorder()
                isRecording -> pauseRecorder()
                else ->startRecording()
            }
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        }


        btnList.setOnClickListener {
            //TODO
            Toast.makeText(this, "List  button ", Toast.LENGTH_SHORT).show()
        }

        btnDone.setOnClickListener {
            stopRecorder()
            Toast.makeText(this, "Record saved ", Toast.LENGTH_SHORT).show()

            bottonSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            bottomSheetBG.visibility = View.VISIBLE
            filenameInput.setText(filename)///////////////
        }

        btnCancel.setOnClickListener {
            File("$dirPath$filename.mp3").delete()
            bottomSheetBG.visibility = View.GONE
            bottonSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            /////hidekeyboard(filenameInput)

            dismiss()
        }


        btnok.setOnClickListener {
        dismiss()
            save()
        }

        bottomSheetBG.setOnClickListener {
            File("$dirPath$filename.mp3").delete()
            dismiss()
        }


        btnDelete.setOnClickListener {
            stopRecorder()
            File("$dirPath$filename.mp3").delete()
            Toast.makeText(this, "Record deleted  ", Toast.LENGTH_SHORT).show()

        }

        btnDelete.isClickable = false
    }

    private fun save(){

        val newFilename = filenameInput.text. toString()////////7
        if(newFilename!= filename){
            var newFile = File("$dirPath$newFilename.mp3")
            File("$dirPath$filename.mp3").renameTo(newFile)
        }


    }

    private fun dismiss(){
        bottomSheetBG.visibility = View.GONE
       bottonSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        /////hidekeyboard(filenameInput)


    }
    private fun hidekeyboard (view: View){
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE)  as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE)
            permissionGranted =grantResults[0] == PackageManager.PERMISSION_GRANTED
    }

    private fun pauseRecorder(){
        recorder.pause()
        isPaused =true
        btnRecord.setImageResource(R.drawable.ic_pause)
        timer.pause()
    }

    private fun resumeRecorder(){
        recorder.resume()
        isPaused =false
        btnRecord.setImageResource(R.drawable.ic_pause)
        timer.start()
    }

    private fun startRecording(){
        if (!permissionGranted){
            ActivityCompat.requestPermissions(this,permission, REQUEST_CODE)
            return

        }
        // start recording
        recorder = MediaRecorder()
        dirPath = "${externalCacheDir?.absolutePath}/"

        var simpleDateFormat = SimpleDateFormat("YYY.MM.DD_HH.MM.SS")
        var date = simpleDateFormat.format(Date())
        filename ="audio_record_$date"

        recorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile("$dirPath$filename.mp3")
            try {
                prepare()
            }catch (e: IOException){}
            start()
        }
        btnRecord.setImageResource(R.drawable.ic_pause)
        isRecording =true
        isPaused =false

        timer.start()

        btnDelete.isClickable =true
        btnDelete.setImageResource(R.drawable.ic_delete)

        btnList.visibility = View.GONE
        btnDone.visibility = View.VISIBLE

    }
    private fun stopRecorder(){
        timer.stop()
        recorder.apply {
            stop()
            release()
        }
        isPaused = false
        isRecording = false

        btnList.visibility = View.VISIBLE
        btnDone.visibility = View.GONE

        btnDelete.isClickable = false
        btnDelete.setImageResource(R.drawable.ic_delete_disable)

        btnDelete.setImageResource(R.drawable.ic_record)
        tvTimer.text = "00:00.00"
        amplitudes  = waveformView.clear()


    }

    override fun onTimerTick(duration: String) {
        tvTimer.text = duration
        waveformView.addAmplitude(recorder.maxAmplitude.toFloat())
    ////println(duration)
        ///tvTimer.text =duration
    }
}
