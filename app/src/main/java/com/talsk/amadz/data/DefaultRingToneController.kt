package com.talsk.amadz.data

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import com.talsk.amadz.domain.RingToneController
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DefaultRingToneController @Inject constructor(
    @ApplicationContext private val context: Context
) : RingToneController {
    private val ringtone: Ringtone? by lazy {
        RingtoneManager.getRingtone(
            context,
            RingtoneManager.getActualDefaultRingtoneUri(
                context,
                RingtoneManager.TYPE_RINGTONE
            )
        )
    }


    override fun playCallRingTone() {
        if (ringtone?.isPlaying == false) {
            ringtone?.play()
        }
    }

    override fun stepCallRingTone() {
        if (ringtone?.isPlaying == true) {
            ringtone?.stop()
        }
    }
}