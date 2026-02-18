package com.rnote.app

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.rnote.app.data.local.RNoteDatabase
import com.rnote.app.data.repository.NoteRepository

class RNoteApplication : Application() {

    val database: RNoteDatabase by lazy { RNoteDatabase.getInstance(this) }
    val noteRepository: NoteRepository by lazy { NoteRepository(database.noteDao()) }

    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this) {}
    }
}
