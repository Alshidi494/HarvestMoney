package com.harvestmoney.bounty.ui.home

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel(
    private val context: Context
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    private var rewardedAd: RewardedAd? = null
    private var interstitialAd: InterstitialAd? = null

    private val _points = MutableStateFlow(0)
    val points: StateFlow<Int> = _points

    private val _withdrawalState = MutableStateFlow<WithdrawalState>(WithdrawalState.Initial)
    val withdrawalState: StateFlow<WithdrawalState> = _withdrawalState

    private val _withdrawalHistory = MutableStateFlow<List<Withdrawal>>(emptyList())
    val withdrawalHistory: StateFlow<List<Withdrawal>> = _withdrawalHistory

    private val _profile = MutableStateFlow(Profile())
    val profile: StateFlow<Profile> = _profile

    val minWithdrawalPoints = 1000
    val interstitialInterval = 50
    private var adCounter = 0

    init {
        observeUserPoints()
        observeProfile()
        observeWithdrawals()
        loadRewardedAd()
        loadInterstitialAd()
    }

    private fun observeUserPoints() {
        val userId = auth.currentUser?.uid ?: return
        database.reference.child("users").child(userId).child("points")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _points.value = snapshot.getValue(Int::class.java) ?: 0
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun observeProfile() {
        val userId = auth.currentUser?.uid ?: return
        database.reference.child("users").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val method = snapshot.child("binance").value as? String ?: ""
                    val payeer = snapshot.child("payeer").value as? String ?: ""
                    _profile.value = Profile(
                        defaultMethod = if (method.isNotEmpty()) "Binance" else if (payeer.isNotEmpty()) "Payeer" else "",
                        account = if (method.isNotEmpty()) method else payeer
                    )
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun observeWithdrawals() {
        val userId = auth.currentUser?.uid ?: return
        database.reference.child("withdrawals").child(userId)
            .orderByChild("timestamp")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = snapshot.children.mapNotNull {
                        it.getValue(Withdrawal::class.java)?.copy(id = it.key ?: "")
                    }.reversed()
                    _withdrawalHistory.value = list
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, "ca-app-pub-7816293804229825/7905083121", adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                }
            })
    }

    fun showRewardedAd() {
        rewardedAd?.show(context as Activity) { rewardItem: RewardItem ->
            val userId = auth.currentUser?.uid ?: return@show
            val pointsRef = database.reference.child("users").child(userId).child("points")
            pointsRef.get().addOnSuccessListener {
                val current = it.getValue(Int::class.java) ?: 0
                pointsRef.setValue(current + rewardItem.amount)
            }
            loadRewardedAd()
        } ?: loadRewardedAd()
    }

    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, "ca-app-pub-7816293804229825/9342573287", adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                }
            })
    }

    fun showInterstitialAd() {
        adCounter++
        if (adCounter % 3 == 0) {
            interstitialAd?.let { ad ->
                ad.show(context as Activity)
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        interstitialAd = null
                        loadInterstitialAd()
                    }
                }
            } ?: loadInterstitialAd()
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
        if (_points.value < minWithdrawalPoints) {
            _withdrawalState.value = WithdrawalState.Error("You need at least $minWithdrawalPoints points")
            return
        }
        if (_withdrawalHistory.value.any { it.status == "pending" }) {
            _withdrawalState.value = WithdrawalState.Error("You already have a pending request")
            return
        }
        val last = _withdrawalHistory.value.maxByOrNull { it.timestamp }
        last?.let {
            if (System.currentTimeMillis() - it.timestamp < 24 * 60 * 60 * 1000) {
                _withdrawalState.value = WithdrawalState.Error("You can only withdraw once every 24 hours")
                return
            }
        }
        val ref = database.reference.child("withdrawals").child(userId).push()
        val withdrawal = Withdrawal(
            id = ref.key ?: "",
            method = method,
            account = account,
            amount = minWithdrawalPoints,
            status = "pending",
            timestamp = System.currentTimeMillis(),
            userId = userId
        )
        ref.setValue(withdrawal).addOnSuccessListener {
            _withdrawalState.value = WithdrawalState.Success
            database.reference.child("users").child(userId).child("points")
                .setValue(_points.value - minWithdrawalPoints)
        }.addOnFailureListener {
            _withdrawalState.value = WithdrawalState.Error(it.message ?: "Withdrawal failed")
        }
    }
}

sealed class WithdrawalState {
    object Initial : WithdrawalState()
    object Success : WithdrawalState()
    data class Error(val message: String) : WithdrawalState()
}

data class Profile(
    val defaultMethod: String = "",
    val account: String = ""
)

class HomeViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
