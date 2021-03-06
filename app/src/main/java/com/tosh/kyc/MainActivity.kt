package com.tosh.kyc

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import smile.identity.core.IDApi
import smile.identity.core.IDParameters
import smile.identity.core.PartnerParameters


class MainActivity : AppCompatActivity() {

    private lateinit var mRecognizer: BlinkIdCombinedRecognizer
    private lateinit var mRecognizerBundle: RecognizerBundle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        mRecognizer = BlinkIdCombinedRecognizer()

        mRecognizerBundle = RecognizerBundle(mRecognizer)

        mRecognizer.setReturnFaceImage(true)
        mRecognizer.setReturnFullDocumentImage(true)
        val successFrameGrabberRecognizer = SuccessFrameGrabberRecognizer(mRecognizer)
        mRecognizerBundle = RecognizerBundle(successFrameGrabberRecognizer)

        mRecognizer.fullDocumentImageDpi

        val status = RecognizerCompatibility.getRecognizerCompatibilityStatus(this)
        if (status == RecognizerCompatibilityStatus.RECOGNIZER_SUPPORTED) {
            startScanning()
        } else {
            Toast.makeText(this, "BlinkID is not supported! Reason: " + status.name, Toast.LENGTH_LONG).show()
        }

        GlobalScope.launch(Dispatchers.IO){
            getIdApi()
        }


    }

    private fun startScanning() {
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
                    if (result.firstName.isEmpty()) {
                        nameTv.text = result.fullName.capitalize()
                    } else {
                        nameTv.text = "${result.firstName.capitalize()} ${result.lastName.capitalize()}"
                    }
                    dateTv.text = result.dateOfBirth.date.toString()
                    idNumberTv.text = result.personalIdNumber

                    if(result.documentNumber[0].isLetter()){
                        idTypeTv.text = "Passport"
                        idNumberTv.text = result.documentNumber
                    }else{
                        idTypeTv.text = "Identity Card"
                        idNumberTv.text = result.personalIdNumber
                    }
                    image.setImageBitmap(result.faceImage?.convertToBitmap())
                    frontIdPic.setImageBitmap(result.fullDocumentFrontImage?.convertToBitmap())
                    backIdPic.setImageBitmap(result.fullDocumentBackImage?.convertToBitmap())

                }
            }
        } else {
            onScanCanceled();
        }
    }

    private fun onScanCanceled() {
        Toast.makeText(this, "Scan cancelled!", Toast.LENGTH_SHORT).show()
    }

    private fun getIdApi(){
        val partnerParameters = PartnerParameters("0000000005", "ktm",  5)

        val idInfo = IDParameters("Raymond","", "Gitonga",
            "KE","NATIONAL_ID", "32324842" ,"23/11/1995",
            "+254729320243" )


        val connection = IDApi("822", this.getString(R.string.si), 0)

        val response = connection.submit_job(partnerParameters.get(), idInfo.get())

        Log.e("KEKEKE", ""+response)
    }

    companion object {
        private const val RESULT_CODE = 123
    }

}

