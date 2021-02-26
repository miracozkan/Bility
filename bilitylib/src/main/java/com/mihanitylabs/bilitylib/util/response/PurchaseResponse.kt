package com.mihanitylabs.bilitylib.util.response

import com.android.billingclient.api.Purchase


// Code with ❤️
//┌─────────────────────────────┐
//│ Created by Mirac Ozkan      │
//│ ─────────────────────────── │
//│ mirac.ozkan123@gmail.com    │            
//│ ─────────────────────────── │
//│ 1/27/2021 - 5:32 PM         │
//└─────────────────────────────┘

sealed class PurchaseResponse {
    data class Success(val purchaseList: List<Purchase>) : PurchaseResponse()
    data class Pending(val pendingItem: Purchase) : PurchaseResponse()
    data class Error(val exception: Exception? = null) : PurchaseResponse()
    object FeatureNotSupported : PurchaseResponse()
    object UserCancelled : PurchaseResponse()
    object BillingNotAvailable : PurchaseResponse()
    object ItemAlreadyOwned : PurchaseResponse()
}
