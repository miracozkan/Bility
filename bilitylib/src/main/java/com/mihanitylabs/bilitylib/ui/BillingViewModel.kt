package com.mihanitylabs.bilitylib.ui

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.billingclient.api.SkuDetails
import com.mihanitylabs.bilitylib.data.repository.BillingRepository
import com.mihanitylabs.bilitylib.util.response.BillingClientResponse
import com.mihanitylabs.bilitylib.util.Event
import com.mihanitylabs.bilitylib.util.response.PurchaseResponse
import com.mihanitylabs.bilitylib.util.Resource


// Code with ❤️
//┌─────────────────────────────┐
//│ Created by Mirac Ozkan      │
//│ ─────────────────────────── │
//│ mirac.ozkan123@gmail.com    │            
//│ ─────────────────────────── │
//│ 1/27/2021 - 5:30 PM         │
//└─────────────────────────────┘


class BillingViewModel(private val billingRepository: BillingRepository) : ViewModel() {

    private val _billingClientResult by lazy { MutableLiveData<Event<BillingClientResponse>>() }
    val billingClientResult: LiveData<Event<BillingClientResponse>> get() = _billingClientResult

    private val _skuDetailListener by lazy { MutableLiveData<Resource<List<SkuDetails>>>() }
    val skuDetailListener: LiveData<Resource<List<SkuDetails>>> get() = _skuDetailListener

    private val _subDetailListener by lazy { MutableLiveData<Resource<List<SkuDetails>>>() }
    val subDetailListener: LiveData<Resource<List<SkuDetails>>> get() = _subDetailListener

    private val _purchaseListener by lazy { MutableLiveData<PurchaseResponse>() }
    val purchaseListener: LiveData<PurchaseResponse> get() = _purchaseListener

    init {
        billingRepository.startDataSourceConnections()
        setBillingClientListener()
    }

    private fun setBillingClientListener() {
        billingRepository.setBillingClientListener { _billingClientResult.postValue(Event(it)) }
    }

    fun setSkuListener() {
        billingRepository.setSkuDetailListener { _skuDetailListener.postValue(it) }
            .also { billingRepository.getSkuDetail() }
    }

    fun setSubListener(){
        billingRepository.setSubDetailListener { _subDetailListener.postValue(it) }
            .also { billingRepository.getSubscriptionDetail() }
    }

    fun setPurchaseListener() {
        billingRepository.setPurchaseListener { _purchaseListener.postValue(it) }
            .also { billingRepository.queryPurchasesAsync() }
    }

    fun makePurchase(activity: Activity, sku: SkuDetails) {
        billingRepository.startBillingFlow(activity, sku)
    }

    override fun onCleared() {
        super.onCleared()
        billingRepository.endDataSourceConnections()
    }
}
