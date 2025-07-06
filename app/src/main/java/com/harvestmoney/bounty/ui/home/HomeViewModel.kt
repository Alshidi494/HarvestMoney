package com.harvestmoney.bounty.ui.home

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel(
    private val context: Context
) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private var rewardedAd: RewardedAd? = null
    private var interstitialAd: InterstitialAd? = null
    private var adCounter = 0

    private val _points = MutableStateFlow(0)
    val points: StateFlow<Int> = _points

    private val _withdrawalState = MutableStateFlow<WithdrawalState>(WithdrawalState.Initial)
    val withdrawalState: StateFlow<WithdrawalState> = _withdrawalState

    private val _withdrawalHistory = MutableStateFlow<List<Withdrawal>>(emptyList())
    val withdrawalHistory: StateFlow<List<Withdrawal>> = _withdrawalHistory

    init {
        observeUserPoints()
        loadRewardedAd()
        loadInterstitialAd()
        observeWithdrawals()
    }
    
    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            "ca-app-pub-7816293804229825/9342573287",
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                }
            }
        )
    }

    fun showInterstitialAd() {
        adCounter++
        if (adCounter % 3 == 0) { // Show interstitial every 3 actions
            val ad = interstitialAd
            if (ad != null) {
                ad.show(context as Activity)
                ad.setFullScreenContentCallback(object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        interstitialAd = null
                        loadInterstitialAd()
                    }
                })
            } else {
                loadInterstitialAd()
            }
        }
    }

    private fun observeUserPoints() {
        val userId = auth.currentUser?.uid ?: return
        database.reference.child("users").child(userId).child("points")
            .addValueEventListener { snapshot ->
                _points.value = snapshot.getValue(Int::class.java) ?: 0
            }
    }

    private fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            "ca-app-pub-7816293804229825/7905083121", // Replace with your actual AdMob ad unit ID
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedAd = null
                }
            }
        )
    }

    fun showRewardedAd() {
        val ad = rewardedAd ?: run {
            loadRewardedAd()
            return
        }

        ad.show(context as Activity) { rewardItem ->
            // User earned reward
            val userId = auth.currentUser?.uid ?: return@show
            val pointsRef = database.reference.child("users").child(userId).child("points")
            pointsRef.get().addOnSuccessListener { snapshot ->
                val currentPoints = snapshot.getValue(Int::class.java) ?: 0
                pointsRef.setValue(currentPoints + 5)
            }
            
            // Load the next ad
            loadRewardedAd()
        }
    }

    private fun observeWithdrawals() {
        val userId = auth.currentUser?.uid ?: return
        database.reference
            .child("withdrawals")
            .child(userId)
            .orderByChild("timestamp")
            .addValueEventListener { snapshot ->
                val withdrawals = mutableListOf<Withdrawal>()
                snapshot.children.forEach { child ->
                    child.getValue(Withdrawal::class.java)?.let {
                        withdrawals.add(it.copy(id = child.key ?: ""))
                    }
                }
                _withdrawalHistory.value = withdrawals.reversed()
            }
    }

    fun requestWithdrawal(method: String, account: String) {
        val userId = auth.currentUser?.uid ?: run {
            _withdrawalState.value = WithdrawalState.Error("User not authenticated")
            return
        }

        if (account.isBlank()) {
            _withdrawalState.value = WithdrawalState.Error("Please enter your ${method.lowercase()} account")
            return
        }

        if (_points.value < 1000) {
            _withdrawalState.value = WithdrawalState.Error("Insufficient points. You need at least 1000 points")
            return
        }

        // Check if there's any pending withdrawal
        if (_withdrawalHistory.value.any { it.status == "pending" }) {
            _withdrawalState.value = WithdrawalState.Error("You have a pending withdrawal request")
            return
        }

        // Check withdrawal frequency (prevent multiple withdrawals in 24 hours)
        val lastWithdrawal = _withdrawalHistory.value.maxByOrNull { it.timestamp }
        if (lastWithdrawal != null) {
            val timeElapsed = System.currentTimeMillis() - lastWithdrawal.timestamp
            if (timeElapsed < 24 * 60 * 60 * 1000) { // 24 hours in milliseconds
                _withdrawalState.value = WithdrawalState.Error("Please wait 24 hours between withdrawals")
                return
            }
        }

        val withdrawalRef = database.reference
            .child("withdrawals")
            .child(userId)
            .push()

        val withdrawal = Withdrawal(
            id = withdrawalRef.key ?: "",
            method = method,
            account = account,
            amount = 1000,
            status = "pending",
            timestamp = System.currentTimeMillis(),
            userId = userId
        )

        withdrawalRef.setValue(withdrawal)
            .addOnSuccessListener {
                _withdrawalState.value = WithdrawalState.Success
                // Deduct points after successful withdrawal request
                database.reference.child("users").child(userId)
                    .child("points")
                    .setValue(_points.value - 1000)
            }
            .addOnFailureListener {
                _withdrawalState.value = WithdrawalState.Error(it.message ?: "Withdrawal request failed")
            }
    }
}

sealed class WithdrawalState {
    object Initial : WithdrawalState()
    object Success : WithdrawalState()
    data class Error(val message: String) : WithdrawalState()
}
