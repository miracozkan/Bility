package com.mihanitylabs.bilitylib.util


// Code with ❤️
//┌─────────────────────────────┐
//│ Created by Mirac Ozkan      │
//│ ─────────────────────────── │
//│ mirac.ozkan123@gmail.com    │            
//│ ─────────────────────────── │
//│ 1/27/2021 - 5:31 PM         │
//└─────────────────────────────┘

sealed class BillingClientResponse {
    object Connected : BillingClientResponse()
    object ServiceNotReady : BillingClientResponse()
    object Loading : BillingClientResponse()
    object ServerDisconnected : BillingClientResponse()
    object ServiceTimeOut : BillingClientResponse()
    object ServiceUnavailable : BillingClientResponse()
    data class Error(val exception: Exception? = null) : BillingClientResponse()
}
