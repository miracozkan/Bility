package com.mihanitylabs.bilitylib.util

import android.content.Context
import com.mihanitylabs.bilitylib.data.model.BillingConfig
import com.mihanitylabs.bilitylib.data.repository.BillingRepository


// Code with ❤️
//┌─────────────────────────────┐
//│ Created by Mirac Ozkan      │
//│ ─────────────────────────── │
//│ mirac.ozkan123@gmail.com    │            
//│ ─────────────────────────── │
//│ 1/27/2021 - 5:31 PM         │
//└─────────────────────────────┘

object DependencyUtil {

    fun provideBillingRepository(context: Context, billingConfig: BillingConfig) =
        BillingRepository(context, billingConfig)

}
