
````markdown
# Harvest Money – Ad Reward App

## Overview
Harvest Money is an Android application built with Java and JDK 17. Users earn points by watching rewarded video ads and can withdraw those points as real money via Binance or Payeer.

## Features
- **User Authentication**: Email/password sign-up and sign-in using Firebase Authentication.
- **Password Visibility Toggle**: Show or hide password field.
- **Forgot Password**: Reset password via email using Firebase `sendPasswordResetEmail()`.
- **Realtime Database**: Store user data and points in Firebase Realtime Database.
- **Rewarded Ads**: Google AdMob Rewarded Ads grant 5 points per completed ad.
- **Points Display**: Home screen shows current user points.
- **Withdraw Requests**: Users can request a withdrawal at a minimum of 1000 points.
- **Payment Methods**: Binance or Payeer account details collected.
- **Request Tracking**: Withdrawal requests saved under `withdrawals/{userId}/{requestId}` with `pending` or `done` status.

## Technical Stack
- **Language**: Java
- **JDK Version**: 17
- **Android SDK**: API Level 33+
- **Firebase**: Authentication + Realtime Database
- **Google Mobile Ads**: `com.google.android.gms:play-services-ads:24.4.0`

## Project Setup
1. Install JDK 17 and Android SDK 33+.
2. Add Firebase to the project: https://firebase.google.com/docs/android/setup
3. Add AdMob dependency:
   ```gradle
   implementation 'com.google.android.gms:play-services-ads:24.4.0'
````

4. Configure `google-services.json` in `app/`.
5. Sync Gradle and run the app.

````