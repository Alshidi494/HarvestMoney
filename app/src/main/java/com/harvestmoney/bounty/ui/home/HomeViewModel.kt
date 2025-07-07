package com.harvestmoney.bounty.ui.home

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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
        if (adCounter % 3 == 0) {
            interstitialAd?.let { ad ->
                ad.show(context as Activity)
                ad.setFullScreenContentCallback(object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        interstitialAd = null
                        loadInterstitialAd()
                    }
                })
            } ?: loadInterstitialAd()
        }
    }

    private fun observeUserPoints() {
        val userId = auth.currentUser?.uid ?: return
        database.reference
            .child("users").child(userId).child("points")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _points.value = snapshot.getValue(Int::class.java) ?: 0
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            "ca-app-pub-7816293804229825/7905083121",
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
        rewardedAd?.show(context as Activity) { rewardItem: RewardItem ->
            val userId = auth.currentUser?.uid ?: return@show
            val pointsRef = database.reference.child("users").child(userId).child("points")
            pointsRef.get().addOnSuccessListener { snapshot ->
                val currentPoints = snapshot.getValue(Int::class.java) ?: 0
                pointsRef.setValue(currentPoints + rewardItem.amount)
            }
            loadRewardedAd()
        } ?: loadRewardedAd()
    }

    private fun observeWithdrawals() {
        val userId = auth.currentUser?.uid ?: return
        database.reference
            .child("withdrawals").child(userId)
            .orderByChild("timestamp")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = snapshot.children.mapNotNull { child ->
                        child.getValue(Withdrawal::class.java)?.copy(id = child.key ?: "")
                    }.reversed()
                    _withdrawalHistory.value = list
                }
                override fun onCancelled(error: DatabaseError) {}
            })
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
        if (_withdrawalHistory.value.any { it.status == "pending" }) {
            _withdrawalState.value = WithdrawalState.Error("You have a pending withdrawal request")
            return
        }
        val last = _withdrawalHistory.value.maxByOrNull { it.timestamp }
        last?.let {
            if (System.currentTimeMillis() - it.timestamp < 24 * 60 * 60 * 1000) {
                _withdrawalState.value = WithdrawalState.Error("Please wait 24 hours between withdrawals")
                return
            }
        }
        val ref = database.reference.child("withdrawals").child(userId).push()
        val withdrawal = Withdrawal(
            id = ref.key ?: "",
            method = method,
            account = account,
            amount = 1000,
            status = "pending",
            timestamp = System.currentTimeMillis(),
            userId = userId
        )
        ref.setValue(withdrawal)
            .addOnSuccessListener {
                _withdrawalState.value = WithdrawalState.Success
                database.reference.child("users").child(userId)
                    .child("points").setValue(_points.value - 1000)
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
