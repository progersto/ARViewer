package com.natife.arproject.utils

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.support.v4.content.ContextCompat.getSystemService
import android.net.ConnectivityManager
import android.support.annotation.NonNull
import java.util.*



    /**
     * CHECK WHETHER INTERNET CONNECTION IS AVAILABLE OR NOT
     */
    fun checkConnection(context: Context): Boolean {
        return (Objects.requireNonNull(context.getSystemService(Context.CONNECTIVITY_SERVICE)) as ConnectivityManager).activeNetworkInfo != null
    }
