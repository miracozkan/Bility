package com.mihanitylabs.bilitylib.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


// Code with ❤️
//┌─────────────────────────────┐
//│ Created by Mirac Ozkan      │
//│ ─────────────────────────── │
//│ mirac.ozkan123@gmail.com    │            
//│ ─────────────────────────── │
//│ 1/27/2021 - 5:29 PM         │
//└─────────────────────────────┘

@Parcelize
data class BillingConfig(
    val base64PublicKey: String,
    val inAppSkuList: List<String>,
    val subsSkuList: List<String>,
    val consumableSkuList: List<String>
) : Parcelable
