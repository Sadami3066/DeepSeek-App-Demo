package com.opends.chat

import android.app.Application
import com.opends.chat.core.OpenDS

class OpenDSApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        OpenDS.init(this)
    }
}
