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

    // إعلانات ومتغيرات المكافآت
    val rewardAmount = 5  // النقاط الممنوحة لكل إعلان
    val minWithdrawalPoints = 1000  // الحد الأدنى للسحب
    val interstitialInterval = 50  // عدد النقاط بين كل إعلان

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    private var rewardedAd: RewardedAd? = null
    private var interstitialAd: InterstitialAd? = null
    private var adCounter = 0

    // حالات البيانات
    private val _points = MutableStateFlow(0)
    val points: StateFlow<Int> = _points

    private val _withdrawalState = MutableStateFlow<WithdrawalState>(WithdrawalState.Initial)
    val withdrawalState: StateFlow<WithdrawalState> = _withdrawalState

    private val _withdrawalHistory = MutableStateFlow<List<Withdrawal>>(emptyList())
    val withdrawalHistory: StateFlow<List<Withdrawal>> = _withdrawalHistory

    private val _profile = MutableStateFlow(Profile())
    val profile: StateFlow<Profile> = _profile

    init {
        loadInitialData()
        loadAds()
    }

    private fun loadInitialData() {
        observeUserPoints()
        observeProfile()
        observeWithdrawals()
    }

    private fun loadAds() {
        loadRewardedAd()
        loadInterstitialAd()
    }

    private fun observeUserPoints() {
        auth.currentUser?.uid?.let { userId ->
            database.reference.child("users").child(userId).child("points")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        _points.value = snapshot.getValue(Int::class.java) ?: 0
                    }
                    override fun onCancelled(error: DatabaseError) {
                        _withdrawalState.value = WithdrawalState.Error("Failed to load points")
                    }
                })
        }
    }

    private fun observeProfile() {
        auth.currentUser?.uid?.let { userId ->
            database.reference.child("users").child(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val method = snapshot.child("binance").value as? String ?: ""
                        val payeer = snapshot.child("payeer").value as? String ?: ""
                        _profile.value = Profile(
                            defaultMethod = if (method.isNotEmpty()) "Binance" else "Payeer",
                            account = method.ifEmpty { payeer }
                        )
                    }
                    override fun onCancelled(error: DatabaseError) {
                        _withdrawalState.value = WithdrawalState.Error("Failed to load profile")
                    }
                })
        }
    }

    private fun observeWithdrawals() {
        auth.currentUser?.uid?.let { userId ->
            database.reference.child("withdrawals").child(userId)
                .orderByChild("timestamp")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        _withdrawalHistory.value = snapshot.children.mapNotNull {
                            it.getValue(Withdrawal::class.java)?.copy(id = it.key ?: "")
                        }.reversed()
                    }
                    override fun onCancelled(error: DatabaseError) {
                        _withdrawalState.value = WithdrawalState.Error("Failed to load withdrawals")
                    }
                })
        }
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
            updateUserPoints(rewardItem.amount)
            loadRewardedAd()
        } ?: loadRewardedAd()
    }

    private fun updateUserPoints(amount: Int) {
        auth.currentUser?.uid?.let { userId ->
            database.reference.child("users").child(userId).child("points")
                .get().addOnSuccessListener { snapshot ->
                    val current = snapshot.getValue(Int::class.java) ?: 0
                    database.reference.child("users").child(userId).child("points")
                        .setValue(current + amount)
                }
        }
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
        if (adCounter++ % 3 == 0) {
            interstitialAd?.show(context as Activity)?.also {
                it.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
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

        when {
            account.isBlank() -> {
                _withdrawalState.value = WithdrawalState.Error("Please enter your account details")
            }
            _points.value < minWithdrawalPoints -> {
                _withdrawalState.value = WithdrawalState.Error("Minimum withdrawal is $minWithdrawalPoints points")
            }
            hasPendingWithdrawal() -> {
                _withdrawalState.value = WithdrawalState.Error("You have a pending withdrawal")
            }
            !canWithdrawAgain() -> {
                _withdrawalState.value = WithdrawalState.Error("Only one withdrawal per 24 hours")
            }
            else -> processWithdrawal(userId, method, account)
        }
    }

    private fun hasPendingWithdrawal(): Boolean {
        return _withdrawalHistory.value.any { it.status == "pending" }
    }

    private fun canWithdrawAgain(): Boolean {
        val lastWithdrawal = _withdrawalHistory.value.maxByOrNull { it.timestamp }
        return lastWithdrawal?.let {
            System.currentTimeMillis() - it.timestamp > 24 * 60 * 60 * 1000
        } ?: true
    }

    private fun processWithdrawal(userId: String, method: String, account: String) {
        val withdrawalRef = database.reference.child("withdrawals").child(userId).push()
        val withdrawal = Withdrawal(
            id = withdrawalRef.key ?: "",
            method = method,
            account = account,
            amount = minWithdrawalPoints,
            status = "pending",
            timestamp = System.currentTimeMillis()
        )

        withdrawalRef.setValue(withdrawal)
            .addOnSuccessListener {
                updatePointsAfterWithdrawal(userId)
                _withdrawalState.value = WithdrawalState.Success
            }
            .addOnFailureListener {
                _withdrawalState.value = WithdrawalState.Error(it.message ?: "Withdrawal failed")
            }
    }

    private fun updatePointsAfterWithdrawal(userId: String) {
        database.reference.child("users").child(userId).child("points")
            .setValue(_points.value - minWithdrawalPoints)
    }
}

// Data Classes
sealed class WithdrawalState {
    object Initial : WithdrawalState()
    object Success : WithdrawalState()
    data class Error(val message: String) : WithdrawalState()
}

data class Profile(
    val defaultMethod: String = "",
    val account: String = ""
)

data class Withdrawal(
    val id: String = "",
    val method: String = "",
    val account: String = "",
    val amount: Int = 0,
    val status: String = "",
    val timestamp: Long = 0L
)

// Factory Class
class HomeViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
