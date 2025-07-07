package com.harvestmoney.bounty

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.firebase.auth.FirebaseAuth
import com.harvestmoney.bounty.ui.auth.SignInScreen
import com.harvestmoney.bounty.ui.auth.SignUpScreen
import com.harvestmoney.bounty.ui.components.AppDrawer
import com.harvestmoney.bounty.ui.faq.FAQScreen
import com.harvestmoney.bounty.ui.history.HistoryScreen
import com.harvestmoney.bounty.ui.home.HomeScreen
import com.harvestmoney.bounty.ui.PrivacyPolicyScreen
import com.harvestmoney.bounty.ui.profile.ProfileScreen
import com.harvestmoney.bounty.ui.support.SupportScreen
import com.harvestmoney.bounty.ui.theme.HarvestMoneyTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var appOpenAd: AppOpenAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize AdMob
        MobileAds.initialize(this)

        // Load app open ad
        loadAppOpenAd()

        setContent {
            HarvestMoneyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    App()
                }
            }
        }
    }

    @Composable
    fun App() {
        val navController = rememberNavController()
        val startDestination = if (FirebaseAuth.getInstance().currentUser != null) "home" else "signIn"
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                AppDrawer(
                    onNavigateToHome = { navController.navigate("home") },
                    onNavigateToHistory = { navController.navigate("history") },
                    onNavigateToProfile = { navController.navigate("profile") },
                    onNavigateToFAQ = { navController.navigate("faq") },
                    onNavigateToSupport = { navController.navigate("support") },
                    onNavigateToPrivacyPolicy = { navController.navigate("privacy-policy") },
                    onSignOut = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("signIn") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    closeDrawer = {
                        scope.launch { drawerState.close() }
                    }
                )
            },
            gesturesEnabled = FirebaseAuth.getInstance().currentUser != null
        ) {
            NavHost(navController = navController, startDestination = startDestination) {
                composable("signIn") {
                    SignInScreen(
                        onNavigateToSignUp = { navController.navigate("signUp") },
                        onSignInSuccess = {
                            navController.navigate("home") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
                composable("signUp") {
                    SignUpScreen(
                        onNavigateToSignIn = { navController.navigate("signIn") },
                        onSignUpSuccess = {
                            navController.navigate("home") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
                composable("home") {
                    HomeScreen(onMenuClick = {
                        scope.launch { drawerState.open() }
                    })
                }
                composable("profile") {
                    ProfileScreen(onNavigateBack = { navController.navigateUp() })
                }
                composable("history") {
                    HistoryScreen(onNavigateBack = { navController.navigateUp() })
                }
                composable("faq") {
                    FAQScreen(onNavigateBack = { navController.navigateUp() })
                }
                composable("support") {
                    SupportScreen(onNavigateBack = { navController.navigateUp() })
                }
                composable("privacy-policy") {
                    PrivacyPolicyScreen(onNavigateBack = { navController.navigateUp() })
                }
            }
        }
    }

    private fun loadAppOpenAd() {
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            this,
            "ca-app-pub-7816293804229825/6981053694",
            request,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    showAppOpenAd()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    appOpenAd = null
                }
            }
        )
    }

    private fun showAppOpenAd() {
        appOpenAd?.apply {
            setFullScreenContentCallback(object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    appOpenAd = null
                    loadAppOpenAd()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    appOpenAd = null
                    loadAppOpenAd()
                }
            })
            show(this@MainActivity)
        }
    }
}
