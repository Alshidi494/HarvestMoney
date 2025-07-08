package com.harvestmoney.bounty.ui.home

data class Withdrawal(
    val id: String = "",
    val method: String = "",
    val account: String = "",
    val amount: Int = 0,
    val status: String = "",
    val timestamp: Long = 0,
    val userId: String = ""
)
