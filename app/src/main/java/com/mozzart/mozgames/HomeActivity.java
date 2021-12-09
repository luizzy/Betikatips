package com.mozzart.mozgames;

import static android.content.ContentValues.TAG;
import static com.mopub.common.Constants.TEN_SECONDS_MILLIS;
import static com.mopub.common.logging.MoPubLog.LogLevel.DEBUG;
import static com.mopub.common.logging.MoPubLog.LogLevel.INFO;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.Task;
import com.google.android.ump.ConsentForm;
import com.google.android.ump.ConsentInformation;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.SdkInitializationListener;
import com.mopub.common.logging.MoPubLog;
import com.mopub.common.privacy.ConsentDialogListener;
import com.mopub.common.privacy.PersonalInfoManager;
import com.mopub.mobileads.BuildConfig;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.mozzart.mozgames.databinding.ActivityHome2Binding;

import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener, ForceUpdateChecker.OnUpdateNeededListener {

    private ActivityHome2Binding binding;
    private boolean doubleBack = false;
    private ConsentInformation consentInformation;
    private ConsentForm consentForm;
    //private MoPubView moPubViews;
    private MoPubInterstitial moPubInterstitial;
    String[] adColonyAllZoneIds = {"vzd3d19821f853439194", "vz3981ceebcbb3473797"};
    String adColonyAppId = "app4f94d4237532463595";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }

        binding = ActivityHome2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //AdSettings.addTestDevice("e253c48b-de07-45dd-9b79-078d6b445694");


       /* MobileAds.initialize(this, initializationStatus -> {
            Map<String, AdapterStatus> statusMap = initializationStatus.getAdapterStatusMap();
            for (String adapterClass : statusMap.keySet()) {
                AdapterStatus status = statusMap.get(adapterClass);
                Log.d("MyApp", String.format(
                        "Adapter name: %s, Description: %s, Latency: %d",
                        adapterClass, status.getDescription(), status.getLatency()));
            }
            LoadAdmobInt();
        });*/

        ForceUpdateChecker.with(this).onUpdateNeeded(this).check();

        final FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        // set in-app defaults
        Map<String, Object> remoteConfigDefaults = new HashMap();
        remoteConfigDefaults.put(ForceUpdateChecker.KEY_UPDATE_REQUIRED, false);
        remoteConfigDefaults.put(ForceUpdateChecker.KEY_CURRENT_VERSION, "1.0.0");
        remoteConfigDefaults.put(ForceUpdateChecker.KEY_UPDATE_URL,
                "https://play.google.com/store/apps/details?id=com.mozzart.mozgames");

        firebaseRemoteConfig.setDefaultsAsync(remoteConfigDefaults);
        firebaseRemoteConfig.fetch(60) // fetch every minutes
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "remote config is fetched.");
                            firebaseRemoteConfig.activate();
                        }
                    }
                });


        CardView smart_picks = binding.smartPicks;
        CardView expert_tips = binding.expertTips;
        CardView combo_tips = binding.comboTips;
        CardView elite_picks = binding.elitePicks;
        CardView super_single = binding.superSingle;
        CardView all_sports = binding.allSports;
        //CardView telegram_join = binding.telegramJoin;

        smart_picks.setOnClickListener(this);
        expert_tips.setOnClickListener(this);
        elite_picks.setOnClickListener(this);
        combo_tips.setOnClickListener(this);
        super_single.setOnClickListener(this);
        all_sports.setOnClickListener(this);
        // telegram_join.setOnClickListener((View.OnClickListener) this);

        FirebaseMessaging.getInstance().subscribeToTopic("moz");

        askRatings();

        // getConsentInfo();
        final SdkConfiguration.Builder configBuilder = new SdkConfiguration.Builder(getString(R.string.Mopub_Int));

        if (BuildConfig.DEBUG) {
            configBuilder.withLogLevel(DEBUG);
        } else {
            configBuilder.withLogLevel(INFO);
        }

        MoPub.initializeSdk(this, configBuilder.build(), initSdkListener());


    }

    private SdkInitializationListener initSdkListener() {
        return new SdkInitializationListener() {
            @Override
            public void onInitializationFinished() {
                // moPubInterstitial.load();
                getConsentInfo();

            }
        };
    }

    void askRatings() {
        ReviewManager manager = ReviewManagerFactory.create(this);
        Task<ReviewInfo> request = manager.requestReviewFlow();
        request.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // We can get the ReviewInfo object
                ReviewInfo reviewInfo = task.getResult();
                Task<Void> flow = manager.launchReviewFlow(this, reviewInfo);
                flow.addOnCompleteListener(task2 -> {
                    // The flow has finished. The API does not indicate whether the user
                    // reviewed or not, or even whether the review dialog was shown. Thus, no
                    // matter the result, we continue our app flow.
                });
            } else {
                // @ReviewErrorCode int reviewErrorCode = ((TaskException) task.getException()).getErrorCode();
                // There was some problem, continue regardless of the result.
            }
        });

    }


    private void showMopubInt() {
        moPubInterstitial = new MoPubInterstitial(this, getString(R.string.Mopub_Int));
        moPubInterstitial.setInterstitialAdListener(new MoPubInterstitial.InterstitialAdListener() {
            @Override
            public void onInterstitialLoaded(MoPubInterstitial moPubInterstitial) {

                if (moPubInterstitial != null && moPubInterstitial.isReady()) {
                    try {
                        if (moPubInterstitial.isReady()) {
                            moPubInterstitial.show();
                        } else {
                            // Caching is likely already in progress if `isReady()` is false.
                            // Avoid calling `load()` here and instead rely on the callbacks as suggested below.
                            Log.d(TAG, "Interstitial ad is loaded and ready to be displayed!");
                        }
                    } catch (Throwable e) {
                        // Do nothing, just skip and wait for ad loading
                    }
                }


                // Show the ad

            }

            @Override
            public void onInterstitialFailed(MoPubInterstitial moPubInterstitial, MoPubErrorCode moPubErrorCode) {
                final Handler handler = new Handler();

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        moPubInterstitial.load();
                    }
                }, TEN_SECONDS_MILLIS);

            }

            @Override
            public void onInterstitialShown(MoPubInterstitial moPubInterstitial) {

            }

            @Override
            public void onInterstitialClicked(MoPubInterstitial moPubInterstitial) {

            }

            @Override
            public void onInterstitialDismissed(MoPubInterstitial moPubInterstitial) {

            }
        });
        moPubInterstitial.load();
        if (moPubInterstitial.isReady()) {
            moPubInterstitial.show();
        } else {
            // Caching is likely already in progress if `isReady()` is false.
            // Avoid calling `load()` here and instead rely on the callbacks as suggested below.
        }


    }

    /*private SdkInitializationListener initSdkListener() {
        return () -> {
            // moPubInterstitial.load();
            getConsentInfo();

        };
    }*/
    private void getConsentInfo() {

        PersonalInfoManager mPersonalInfoManager = MoPub.getPersonalInformationManager();
        mPersonalInfoManager.shouldShowConsentDialog();
        mPersonalInfoManager.loadConsentDialog(new ConsentDialogListener() {
            @Override
            public void onConsentDialogLoaded() {
                if (mPersonalInfoManager != null) {
                    mPersonalInfoManager.showConsentDialog();
                }
            }

            @Override
            public void onConsentDialogLoadFailed(@NonNull MoPubErrorCode moPubErrorCode) {
                MoPubLog.i("Consent dialog failed to load.");
            }
        });

    }


    @Override
    public void onStart() {
        super.onStart();

    }


    @Override
    public void onBackPressed() {

        if (doubleBack) {
            super.onBackPressed();
            return;

        }

        this.doubleBack = true;
        Toast.makeText(HomeActivity.this, "Press back again to exit", Toast.LENGTH_SHORT).show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBack = false;
            }
        }, 2000);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Fragment frag = null;
        int id = item.getItemId();
        if (id == android.R.id.home) {

            finish();
        }
        if (id == R.id.telegram) {
            startActivity(new Intent(HomeActivity.this, Telegram_Websites.class));
        }


        if (id == R.id.about) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("MozGames");
            try {
                alert.setMessage(
                        "Version " + getApplication().getPackageManager().getPackageInfo(getPackageName(), 0).versionCode +
                                "\n" + HomeActivity.this.getString(R.string.app_name) + "\n" +
                                "All Rights Reserved \n"
                );
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            alert.show();

        } else if (id == R.id.ppolicy) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Privacy Policy");
            try {
                alert.setMessage(
                        "Winner Tips Developers built the MozGames app as a free app. This SERVICE is provided by WinnerTips Developers at no cost and is intended for use as is.\n" +
                                "\n" +
                                "\n" +
                                "\n" +
                                "\n" +
                                "This page is used to inform website visitors regarding our policies with the collection, use, and disclosure of Personal Information if anyone decided to use our Service.\n" +
                                "If you choose to use our Service, then you agree to the collection and use of information in relation to this policy. The Personal Information that we collect is used for providing and improving the Service. We will not use or share your information with anyone except as described in this Privacy Policy.\n" +
                                "The terms used in this Privacy Policy have the same meanings as in our Terms and Conditions, which is accessible at Win bet unless otherwise defined in this Privacy Policy.\n" +
                                "\n" +
                                "Information Collection and Use\n" +
                                "For a better experience, while using our Service, we may require you to provide us with certain personally identifiable information, including but not limited to a username. The information that we request is retained on your device and is not collected by us in any way.\n" +
                                "The app does use third-party services that may collect information used to identify you. Google play services are such.\n" +
                                "\n" +
                                "Log Data\n" +
                                "We want to inform you that whenever you use our Service, in case of an error in the app we collect data and information (through third-party products) on your phone called Log Data. This Log Data may include information such as your devices' Internet Protocol (“IP”) address, device name, operating system version, the configuration of the app when utilizing our Service, the time and date of your use of the Service, and other statistics.\n" +
                                "\n" +
                                "Cookies\n" +
                                "Cookies are files with small amount of data that is commonly used as an anonymous unique identifier. These are sent to your browser from the website that you visit and are stored on your devices' internal memory.\n" +
                                "\n" +
                                "Service Providers\n" +
                                "We may employ third-party companies and individuals due to the following reasons:\n" +
                                "· To facilitate our Service;\n" +
                                "· To provide the Service on our behalf;\n" +
                                "· To perform Service-related services; or\n" +
                                "· To assist us in analyzing how our Service is used.\n" +
                                "We want to inform users of this Service that these third parties have access to your Personal Information. The reason is to perform the tasks assigned to them on our behalf. However, they are obligated not to disclose or use the information for any other purpose.\n" +
                                "\n" +
                                "Security\n" +
                                "We value your trust in providing us your Personal Information, thus we are striving to use commercially acceptable means of protecting it. But remember that no method of transmission over the internet, or method of electronic storage is 100% secure and reliable, and We cannot guarantee its absolute security.\n" +
                                "\n" +
                                "Links to Other Sites\n" +
                                "This Service may contain links to other sites. If you click on a third-party link, you will be directed to that site. Note that these external sites are not operated by us. Therefore, I strongly advise you to review the Privacy Policy of these websites. I have no control over and assume no responsibility for the content, privacy policies, or practices of any third-party sites or services.\n" +
                                "\n" +
                                "Children’s Privacy\n" +
                                "These Services do not address anyone under the age of 13. We do not knowingly collect personally identifiable information from children under 13. In the case We discover that a child under 13 has provided us with personal information, We immediately delete this from our servers. If you are a parent or guardian and you are aware that your child has provided us with personal information, please contact us so that We will be able to do necessary actions.\n" +
                                "\n" +
                                "Changes to This Privacy Policy\n" +
                                "We may update our Privacy Policy from time to time. Thus, you are advised to review this page periodically for any changes. We will notify you of any changes by posting the new Privacy Policy on this page. These changes are effective immediately after they are posted on this page.\n" +
                                "\n" +
                                "Contact Us\n" +
                                "If you have any questions or suggestions about our Privacy Policy, do not hesitate to contact us at victorpredictz@gmail.com" + "\n" + getApplication().getPackageManager().getPackageInfo(getPackageName(), 0).versionCode +
                                "\n"
                );
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            alert.show();

        } else if (id == R.id.feedback) {
            startActivity(new Intent(HomeActivity.this, com.mozzart.mozgames.Feedback.class));

        } else if (id == R.id.ppolicy) {

            View messageView = getLayoutInflater().inflate(R.layout.about, null, false);

            TextView textView = messageView.findViewById(R.id.about_credits);
            TextView textView1 = messageView.findViewById(R.id.about_description);
            int defaultColor = textView.getResources().getColor(R.color.colorBlack);
            int defaultColor1 = textView1.getResources().getColor(R.color.colorBlack);
            //int defaultColor = textView.getTextColors().getDefaultColor();
            textView.setTextColor(defaultColor);
            textView1.setTextColor(defaultColor1);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.app_name);
            builder.setView(messageView);
            builder.create();
            builder.show();
        } else if (id == R.id.rate) {

            Uri uri = Uri.parse("market://details?id=" + getPackageName());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(HomeActivity.this, "Unable to find play store", Toast.LENGTH_SHORT).show();
            }

        } else if (id == android.R.id.home) {
            finish();
        } else if (id == R.id.menu_share) {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody = "Win Big with the best Sports predictions app on playstore . Download here https://play.google.com/store/apps/details?id=com.mozzart.mozgame";
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Best Sports Predictions App on Play Store");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
            startActivity(sharingIntent);
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View view) {
        Fragment fragment = null;
        String title = "";
        switch (view.getId()) {

            case R.id.expert_tips:
                Intent intent6 = new Intent(view.getContext(), games.class);
                intent6.putExtra("title", "Expert Tips");
                intent6.putExtra("db", "moz");
                intent6.putExtra("selectedp", "expert tips");
                startActivity(intent6);
                showMopubInt();
                break;

            case R.id.combo_tips:
                Intent intent1 = new Intent(view.getContext(), games.class);
                intent1.putExtra("title", "Combo Tips");
                intent1.putExtra("db", "moz");
                intent1.putExtra("selectedp", "high odd tips");
                startActivity(intent1);
                showMopubInt();
                break;

            case R.id.super_single:
                Intent intent2 = new Intent(view.getContext(), games.class);
                intent2.putExtra("title", "Super Single");
                intent2.putExtra("db", "moz");
                intent2.putExtra("selectedp", "two plus");
                startActivity(intent2);
                showMopubInt();
                break;

            case R.id.elite_picks:
                Intent intent3 = new Intent(view.getContext(), games.class);
                intent3.putExtra("title", "Elite Picks");
                intent3.putExtra("db", "moz");
                intent3.putExtra("selectedp", "elite picks");
                startActivity(intent3);
                showMopubInt();
                break;

            case R.id.all_sports:
                Intent intent4 = new Intent(view.getContext(), games.class);
                intent4.putExtra("title", "All Sports Tips");
                intent4.putExtra("db", "moz");
                intent4.putExtra("selectedp", "all sports");
                startActivity(intent4);
                showMopubInt();
                break;

            case R.id.smart_picks:
                Intent intent5 = new Intent(view.getContext(), games.class);
                intent5.putExtra("title", "Smart Picks");
                intent5.putExtra("db", "moz");
                intent5.putExtra("selectedp", "smart picks");
                startActivity(intent5);
                showMopubInt();
                break;


        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    @Override
    public void onUpdateNeeded(final String updateUrl) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("New version available")
                .setMessage("Please, update app to new version to continue enjoying.")
                .setPositiveButton("Update",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                redirectStore(updateUrl);
                            }
                        }).setNegativeButton("No, thanks",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).create();
        dialog.show();
    }

    private void redirectStore(String updateUrl) {
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}