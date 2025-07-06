
```markdown
# TODO List for Harvest Money

- [ ] **Authentication Screen**
  - Create sign-up and sign-in UIs.
  - Add password visibility toggle.
  - Implement "Forgot Password" link.

- [ ] **Firebase Integration**
  - Setup Firebase Authentication in `build.gradle`.
  - Initialize Realtime Database.
  - On account creation, write user record:
    ```json
    users/{userId}: {
      email: string,
      points: 0,
      binance: "",
      payeer: ""
    }
    ```

- [ ] **Home Screen**
  - Display `users/{userId}/points`.
  - Add "Watch Ad" button.

- [ ] **Rewarded Ads**
  - Load and show `RewardedAd` from AdMob.
  - On ad completion, increment points by 5 in Realtime Database.

- [ ] **Withdrawal Flow**
  - Enable "Withdraw" button if points >= 1000.
  - Show form to select Binance or Payeer and enter account info.
  - Push request under:
    ```json
    withdrawals/{userId}/{requestId}: {
      method: string,
      account: string,
      amount: 1000,
      status: "pending",
      timestamp: long
    }
    ```
  - Display list of user requests with status.
````
