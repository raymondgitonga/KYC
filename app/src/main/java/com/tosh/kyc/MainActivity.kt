package com.tosh.kyc

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.microblink.entities.recognizers.Recognizer
import com.microblink.entities.recognizers.RecognizerBundle
import com.microblink.entities.recognizers.blinkid.generic.BlinkIdCombinedRecognizer
import com.microblink.entities.recognizers.successframe.SuccessFrameGrabberRecognizer
import com.microblink.uisettings.ActivityRunner
import com.microblink.uisettings.BlinkIdUISettings
import com.microblink.util.RecognizerCompatibility
import com.microblink.util.RecognizerCompatibilityStatus
import kotlinx.android.synthetic.main.activity_main.*



class MainActivity : AppCompatActivity() {

    private lateinit var mRecognizer: BlinkIdCombinedRecognizer
    private lateinit var mRecognizerBundle: RecognizerBundle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val status = RecognizerCompatibility.getRecognizerCompatibilityStatus(this)
        if (status == RecognizerCompatibilityStatus.RECOGNIZER_SUPPORTED) {

        } else {
            Toast.makeText(this, "BlinkID is not supported! Reason: " + status.name, Toast.LENGTH_LONG).show()
        }

        mRecognizer = BlinkIdCombinedRecognizer()

        mRecognizerBundle = RecognizerBundle(mRecognizer)

        mRecognizer.setReturnFaceImage(true)
        mRecognizer.setReturnFullDocumentImage(true)
        val successFrameGrabberRecognizer = SuccessFrameGrabberRecognizer(mRecognizer)
        mRecognizerBundle = RecognizerBundle(successFrameGrabberRecognizer)

        mRecognizer.fullDocumentImageDpi


        startScanning()
    }

    private fun startScanning(){
        var settings = BlinkIdUISettings(mRecognizerBundle)

        settings.activityTheme = R.style.Theme_AppCompat_DayNight_NoActionBar

        ActivityRunner.startActivityForResult(this, RESULT_CODE, settings)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RESULT_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                mRecognizerBundle.loadFromIntent(data)
                val result = mRecognizer.result
                if (result.resultState == Recognizer.Result.State.Valid) {
                    name.text = "Name ${result.firstName} ${result.lastName}"
                    nationality.text = "Nationality ${result.nationality}"
                    date.text = "DOB: ${result.dateOfBirth.date}"
                    number.text = "Id Number: ${result.documentNumber}"

                    image.setImageBitmap(result.faceImage?.convertToBitmap())
                    imagePic.setImageBitmap(result.fullDocumentFrontImage?.convertToBitmap())

                }
            }
        }else{
            onScanCanceled();
        }
    }

    private fun onScanCanceled() {
        Toast.makeText(this, "Scan cancelled!", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val RESULT_CODE = 123
    }

}

