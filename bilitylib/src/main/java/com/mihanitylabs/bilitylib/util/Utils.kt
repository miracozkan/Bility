package com.mihanitylabs.bilitylib.util

import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding


// Code with ❤️
//┌─────────────────────────────┐
//│ Created by Mirac Ozkan      │
//│ ─────────────────────────── │
//│ mirac.ozkan123@gmail.com    │            
//│ ─────────────────────────── │
//│ 1/27/2021 - 5:33 PM         │
//└─────────────────────────────┘


fun <T : ViewBinding> Fragment.viewBinding(viewBindingFactory: (View) -> T) =
    FragmentViewBindingDelegate(this, viewBindingFactory)

fun Any.logError(message: String = "Empty") {
    Log.e(this::class.simpleName, message)
}

fun Any.logDebug(message: String = "Empty") {
    Log.d(this::class.simpleName, message)
}