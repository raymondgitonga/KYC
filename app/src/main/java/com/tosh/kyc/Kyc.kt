package com.tosh.kyc

import android.app.Application
import com.microblink.MicroblinkSDK
import com.microblink.intent.IntentDataTransferMode

class Kyc: Application() {

    override fun onCreate() {
        super.onCreate()

        MicroblinkSDK.setLicenseFile("MB_com.tosh.kyc_BlinkID_Android_2020-09-21.mblic", this)

        MicroblinkSDK.setIntentDataTransferMode(IntentDataTransferMode.PERSISTED_OPTIMISED);
    }
}