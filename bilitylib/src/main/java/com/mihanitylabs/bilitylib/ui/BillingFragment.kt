package com.mihanitylabs.bilitylib.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.android.billingclient.api.PurchaseHistoryRecord
import com.android.billingclient.api.SkuDetails
import com.mihanitylabs.bilitylib.R
import com.mihanitylabs.bilitylib.data.model.BillingConfig
import com.mihanitylabs.bilitylib.databinding.FragmentBillingBinding
import com.mihanitylabs.bilitylib.util.*
import com.mihanitylabs.bilitylib.util.response.BillingClientResponse
import com.mihanitylabs.bilitylib.util.response.PurchaseResponse

class BillingFragment : Fragment(R.layout.fragment_billing) {

    private val binding by viewBinding(FragmentBillingBinding::bind)

    private val billingRepository by lazy {
        DependencyUtil.provideBillingRepository(
            requireContext(),
            billingConfig ?: throw Exception("Billing Config is null")
        )
    }
    private val billingViewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelFactory(billingRepository)
        ).get(BillingViewModel::class.java)
    }

    private val billingConfig: BillingConfig? by lazy { arguments?.getParcelable(BILLING_CONFIG) }

    //region Observers
    private val billingClientObserver = EventObserver<BillingClientResponse> { clientResponse ->
        when (clientResponse) {
            BillingClientResponse.Connected -> {
                billingViewModel.setPurchaseListener()
                if (!billingConfig?.inAppSkuList.isNullOrEmpty()) billingViewModel.setSkuListener()
                if (!billingConfig?.subsSkuList.isNullOrEmpty()) billingViewModel.setSubListener()
                logDebug("Connected")
            }
            BillingClientResponse.Loading -> logDebug("BillingClientResponse.Loading")
            BillingClientResponse.ServerDisconnected -> logError("ServerDisconnected")
            BillingClientResponse.ServiceTimeOut -> logError("ServiceTimeOut")
            BillingClientResponse.ServiceUnavailable -> logError("ServiceUnavailable")
            is BillingClientResponse.Error -> logError(clientResponse.exception?.localizedMessage)
            BillingClientResponse.ServiceNotReady -> logError("ServiceNotReady")
        }
    }
    private val skuListObserver by lazy {
        Observer<Resource<List<SkuDetails>>> { skuResource ->
            when (skuResource) {
                is Resource.Success -> {
                    logDebug("skuListObserver Success : " + skuResource.data.toString())
                }
                is Resource.Error -> {
                    logError("skuListObserver Error : " + skuResource.exception.message.toString())
                }
                Resource.Loading -> logDebug("skuListObserver Loading")
            }
        }
    }
    private val subListObserver by lazy {
        Observer<Resource<List<SkuDetails>>> { skuResource ->
            when (skuResource) {
                is Resource.Success -> {
                    logDebug("subListObserver Success : " + skuResource.data.toString())
                }
                is Resource.Error -> {
                    logError("subListObserver Error : " + skuResource.exception.message.toString())
                }
                Resource.Loading -> logDebug("skuListObserver Loading")
            }
        }
    }
    private val purchaseObserver by lazy {
        Observer<PurchaseResponse> { purchaseResponse ->
            when (purchaseResponse) {
                is PurchaseResponse.Success -> logDebug(purchaseResponse.purchaseList.toString())
                is PurchaseResponse.Pending -> logError(purchaseResponse.pendingItem.toString())
                PurchaseResponse.FeatureNotSupported -> logError("PurchaseResponse.FeatureNotSupported")
                PurchaseResponse.UserCancelled -> logError("PurchaseResponse.UserCancelled")
                is PurchaseResponse.Error -> logError(purchaseResponse.exception?.message.toString())
                PurchaseResponse.BillingNotAvailable -> logError("PurchaseResponse.BillingNotAvailable")
                PurchaseResponse.ItemAlreadyOwned -> logError("ItemAlreadyOwned")
            }
        }
    }
    private val inAppHistoryObserver by lazy {
        Observer<List<PurchaseHistoryRecord>> {}
    }
    private val subHistoryObserver by lazy {
        Observer<List<PurchaseHistoryRecord>> {}
    }
    //endregion

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
    }

    private fun initObservers() {
        billingViewModel.billingClientResult.observe(viewLifecycleOwner, billingClientObserver)
        billingViewModel.purchaseListener.observe(viewLifecycleOwner, purchaseObserver)
        if (billingConfig?.inAppSkuList?.isNullOrEmpty() == false) {
            billingViewModel.skuDetailListener.observe(viewLifecycleOwner, skuListObserver)
        }
        if (billingConfig?.subsSkuList?.isNullOrEmpty() == false) {
            billingViewModel.subDetailListener.observe(viewLifecycleOwner, subListObserver)
        }
        if (billingConfig?.isPurchaseHistoryNeeded == true) {
            billingViewModel.inAppHistory.observe(viewLifecycleOwner, inAppHistoryObserver)
            billingViewModel.subHistory.observe(viewLifecycleOwner, subHistoryObserver)
        }
    }

    private fun onMakePurchase(sku: SkuDetails) {
        billingViewModel.makePurchase(requireActivity(), sku)
    }

    private fun navBack() {
        parentFragmentManager.popBackStack()
    }

    companion object {

        private const val BILLING_CONFIG = "billingConfig"

        @JvmStatic
        fun newInstance(billingConfig: BillingConfig) = BillingFragment().apply {
            arguments = Bundle().apply { putParcelable(BILLING_CONFIG, billingConfig) }
        }
    }
}
