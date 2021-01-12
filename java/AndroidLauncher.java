package com.oleg.gg;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.oleg.gg.zdebug.Dbg;
import com.oleg.gg.zdebug.DbgFile;

import static android.view.KeyEvent.KEYCODE_BACK;
import static com.google.android.gms.ads.RequestConfiguration.MAX_AD_CONTENT_RATING_G;
import static com.google.android.gms.ads.RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE;
import static com.google.android.gms.ads.RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE;


public class AndroidLauncher extends AndroidApplication implements IAdController {

    private GG gg;
    // TEST ADS!
    private static final String TEST_INTERSTITIAL_AD_ID = "ca-app-pub-3940256099942544/1033173712";
    private static final String TEST_REWARDED_AD_ID = "ca-app-pub-3940256099942544/5224354917";
    // REAL ADS!
//    private static final String INTERSTITIAL_AD_ID = "HIDDEN";
//    private static final String REWARDED_AD_ID = "HIDDEN";

    private InterstitialAd interstitialAd;
    private RewardedAd rewardedAd;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useAccelerometer = false;
        config.useCompass = false;
        config.useWakelock = true;
        config.useImmersiveMode = true;

        gg = new GG(this);

        initialize(gg, config);

        Gdx.input.setCatchKey(KEYCODE_BACK, true); // catching back button on Android


        if (Dbg.LOG) {
            // very useful debugging feature
            Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
                //Catch your exception
                if (Dbg.LOG) e.printStackTrace();
                if (Dbg.WRITE_FILE) DbgFile.appendLog(e);
                if (Dbg.DEV_ON) Dbg.error("uncaughtException in AndroidLauncher");

                // Without System.exit() this will not work maybe.
                System.exit(2);
            });
        }


        if (Dbg.LOG) Dbg.log(" ----- ----- APP ACTIVITY onCreate");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (Dbg.LOG) Dbg.log(" ----- ----- APP ACTIVITY onDestroy");
    }

    @Override
    public void initAds() {
        if (!Dbg.ADS_ON) return;

        if (Dbg.LOG) Dbg.log("AndroidLauncher.initAds()");

        runOnUiThread(this::setupAds);
    }

    @Override
    public void firstCreateRewardedAd() {
        if (!Dbg.ADS_ON) return;

        if (Dbg.LOG) Dbg.log("AndroidLauncher.firstCreateRewardedAd()");

        runOnUiThread(() -> rewardedAd = createNewRewardedAd());

    }

    @Override
    public void checkForInterstitialAd() {
        if (!Dbg.ADS_ON) return;

        if (Dbg.LOG) Dbg.log("AndroidLauncher.checkForInterstitialAd()");

        runOnUiThread(() -> {
            if (interstitialAd.isLoaded()) {
                if (Dbg.LOG) Dbg.log("The interstitialAd was loaded already.");
            }
            else {
                if (Dbg.LOG) Dbg.log("The interstitialAd wasn't loaded. Trying to reload.");

                interstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });
    }

    @Override
    public void showInterstitialAd() {
        if (!Dbg.ADS_ON) return;

        if (Dbg.LOG) Dbg.log("AndroidLauncher.showInterstitialAd()");

        runOnUiThread(() -> {
            if (interstitialAd.isLoaded()) {
                interstitialAd.show();
            }
            else {
                if (Dbg.LOG) Dbg.log("The interstitialAd wasn't loaded yet.");

                interstitialAd.loadAd(new AdRequest.Builder().build());
            }

        });

    }

    @Override
    public void checkForRewardedAd() {
        if (!Dbg.ADS_ON) return;

        if (Dbg.LOG) Dbg.log("AndroidLauncher.checkForRewardedAd()");

        runOnUiThread(() -> {
            if (rewardedAd.isLoaded()) {
                gg.NotifyRewardedAdIsLoaded();
            }
            else {
                rewardedAd = createNewRewardedAd();
            }
        });
    }

    @Override
    public void showRewardedAd() {
        if (!Dbg.ADS_ON) return;

        if (Dbg.LOG) Dbg.log("AndroidLauncher.showRewardedAd()");

        runOnUiThread(() -> {
            if (rewardedAd.isLoaded()) {
                RewardedAdCallback adCallback = new RewardedAdCallback() {
                    @Override
                    public void onRewardedAdOpened() {
                        // Ad opened.
                    }

                    @Override
                    public void onRewardedAdClosed() {
                        // Ad closed.
                        if (Dbg.LOG) Dbg.log("onRewardedAdClosed");

                        rewardedAd = createNewRewardedAd();
                    }

                    @Override
                    public void onUserEarnedReward(@NonNull RewardItem reward) {
                        // User earned reward.
                        if (Dbg.LOG) Dbg.log("onUserEarnedReward");

                        gg.NotifyUserRewarded();
                    }

                    @Override
                    public void onRewardedAdFailedToShow(AdError var1) {
                        // Ad failed to display.
                    }
                };
                rewardedAd.show(AndroidLauncher.this, adCallback);
            }
            else {
                if (Dbg.LOG) Dbg.log("rewardedAd is not loaded!");
            }
        });

    }


    private RewardedAd createNewRewardedAd() {
        if (!Dbg.ADS_ON) return null;

        if (Dbg.LOG) Dbg.log("AndroidLauncher.createNewRewardedAd()");

        RewardedAd rewardedAd = new RewardedAd(this, TEST_REWARDED_AD_ID);
        RewardedAdLoadCallback adLoadCallback = new RewardedAdLoadCallback() {
            @Override
            public void onRewardedAdLoaded() {
                // Ad successfully loaded.
            }

            @Override
            public void onRewardedAdFailedToLoad(LoadAdError var1) {
                // Ad failed to load.
            }
        };
        rewardedAd.loadAd(new AdRequest.Builder().build(), adLoadCallback);
        return rewardedAd;
    }

    private void setupAds() {
        if (!Dbg.ADS_ON) return;

        // Comply with Google Play’s Families Policy:
        // Ad content filtering
        // Children's Online Privacy Protection Act (COPPA)
        // General Data Protection Regulation (GDPR)

        // first
        RequestConfiguration requestConfiguration = MobileAds.getRequestConfiguration()
                .toBuilder()
                .setMaxAdContentRating(MAX_AD_CONTENT_RATING_G)
                .setTagForChildDirectedTreatment(TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
                .setTagForUnderAgeOfConsent(TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE)
                .build();
        MobileAds.setRequestConfiguration(requestConfiguration);

        // second
        MobileAds.initialize(this, initializationStatus -> {});


        // interstitialAd
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(TEST_INTERSTITIAL_AD_ID);
        interstitialAd.loadAd(new AdRequest.Builder().build());

        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                interstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });
    }
}
