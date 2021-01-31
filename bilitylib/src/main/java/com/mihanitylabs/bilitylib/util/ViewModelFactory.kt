package com.mihanitylabs.bilitylib.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mihanitylabs.bilitylib.data.repository.BillingRepository
import com.mihanitylabs.bilitylib.ui.BillingViewModel


// Code with ❤️
//┌─────────────────────────────┐
//│ Created by Mirac Ozkan      │
//│ ─────────────────────────── │
//│ mirac.ozkan123@gmail.com    │            
//│ ─────────────────────────── │
//│ 1/27/2021 - 5:33 PM         │
//└─────────────────────────────┘

class ViewModelFactory constructor(private val billingRepository: BillingRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(BillingViewModel::class.java)) {
            BillingViewModel(billingRepository) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}
