package com.natife.arproject.aractivity

import android.content.Context
import android.util.Log
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.sceneform.ux.ArFragment

class CustomArFragment : ArFragment() {

   private lateinit var onFragmentReady: OnFragmentReady

    override fun getSessionConfiguration(session: Session): Config {
        planeDiscoveryController.setInstructionView(null)
        val config = super.getSessionConfiguration(session)
        config.cloudAnchorMode = Config.CloudAnchorMode.ENABLED
        return config
    }

    override fun onResume() {
        super.onResume()
        Log.d("sss", "")
        onFragmentReady.fragmentReady()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        onFragmentReady = context as OnFragmentReady
    }
}