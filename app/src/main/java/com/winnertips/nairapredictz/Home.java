package com.winnertips.nairapredictz;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.TextView;
import android.widget.Toast;

import com.facebook.ads.AudienceNetworkAds;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.SdkInitializationListener;
import com.mopub.mobileads.BuildConfig;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;

import hotchemi.android.rate.AppRate;
import hotchemi.android.rate.OnClickButtonListener;

import static com.mopub.common.Constants.TEN_SECONDS_MILLIS;
import static com.mopub.common.logging.MoPubLog.LogLevel.DEBUG;
import static com.mopub.common.logging.MoPubLog.LogLevel.INFO;


public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private boolean doubleBack = false;
    private static final String TAG ="FACEBOOK_ADS" ;
    private MoPubInterstitial moPubInterstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AudienceNetworkAds.initialize(this);

        setContentView(R.layout.activity_home);
        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        FirebaseMessaging.getInstance().subscribeToTopic("naira");

        DrawerLayout drawer =  findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView =  findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        displayView(R.id.nav_view);

        final SdkConfiguration.Builder configBuilder = new SdkConfiguration.Builder("6e67f7b8d61c494d86a409169f088f5d");

        if (BuildConfig.DEBUG) {
            configBuilder.withLogLevel(DEBUG);
        } else {
            configBuilder.withLogLevel(INFO);
        }


        MoPub.initializeSdk(this, configBuilder.build(), initSdkListener());


        showMopubInt();
    }
    private void showMopubInt(){
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

    }


    private SdkInitializationListener initSdkListener() {
        return new SdkInitializationListener() {
            @Override
            public void onInitializationFinished() {
                moPubInterstitial.load();

            }
        };
    }



    @Override
    public void onStart(){
        super.onStart();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (doubleBack) {
            AppRate.with(this)
                    .setInstallDays(0) // default 10, 0 means install day.
                    .setLaunchTimes(2) // default 10
                    .setRemindInterval(2) // default 1
                    .setShowLaterButton(true) // default true
                    .setDebug(false) // default false
                    .setMessage("Do you Love  " + Home.this.getString(R.string.app_name) +
                            "App? Please rate us 5 stars. It keeps us Motivated")
                    .setTitle("Rate us 5 stars please")
                    .setOnClickButtonListener(new OnClickButtonListener() { // callback listener.
                        @Override
                        public void onClickButton(int which) {
                            finish();
                        }
                    })
                    .monitor();

            // Show a dialog if meets conditions
            AppRate.showRateDialogIfMeetsConditions(this);
            if (!AppRate.showRateDialogIfMeetsConditions(this)) {
                super.onBackPressed();
                return;
            }
        }

        this.doubleBack = true;
        Toast.makeText(Home.this, "Press back again to exit", Toast.LENGTH_SHORT).show();
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
       Fragment frag= null;
        int id = item.getItemId();
        if (id == android.R.id.home) {

            finish();
        }
        if (id == R.id.about) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Victor Predictz");
            try {
                alert.setMessage(
                        "Version " + getApplication().getPackageManager().getPackageInfo(getPackageName(), 0).versionCode +
                                "\n" + Home.this.getString(R.string.app_name) + "\n" +
                                "All rights reserved \n"
                );
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            alert.show();

        }else if (id == R.id.ppolicy) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Privacy Policy");
            try {
                alert.setMessage(
                        "Winner Tips Developers built the Victor Predictz app as a free app. This SERVICE is provided by WinnerTips Developers at no cost and is intended for use as is.\n" +
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

        }
        else if (id == R.id.feedback) {
            startActivity(new Intent(Home.this, com.winnertips.nairapredictz.Feedback.class));

        }else if (id == R.id.ppolicy) {

            View messageView = getLayoutInflater().inflate(R.layout.about, null, false);

            TextView textView =  messageView.findViewById(R.id.about_credits);
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
        }

       else if (id == R.id.rate) {

            Uri uri = Uri.parse("market://details?id=" + getPackageName());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(Home.this, "Unable to find play store", Toast.LENGTH_SHORT).show();
            }

        } else if (id == android.R.id.home) {
            finish();
        } else if (id == R.id.menu_share) {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody = "Win Big with the best Sports predictions app on playstore . Download here https://play.google.com/store/apps/details?id=com.winnertips.nairapredictz";
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Best Sports Predictions App on Play Store");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
            startActivity(sharingIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        displayView(id);

        DrawerLayout drawer =  findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void displayView(int viewId) {

        Fragment fragment = null;
        String title = "";

        switch (viewId) {
            case R.id.nav_dailytoptips:
                fragment = new DailyTopTips();
                title = "Top Picks";
                break;
            case R.id.nav_megajackpot:
                fragment = new com.winnertips.nairapredictz.MegaJackpotTips();
                title = "Combo Tips";
                break;
            case R.id.nav_midweekjackpot:
                fragment = new com.winnertips.nairapredictz.MidweekJackpot();
                title = "Expert Tips";
                break;
            case R.id.nav_elite:
                fragment = new com.winnertips.nairapredictz.Elite();
                title = "Elite Tips";
                break;
            case R.id.nav_twoplus:
                fragment = new com.winnertips.nairapredictz.Twoplus();
                title = "Super Single";
                break;
            case R.id.nav_allsports:
                fragment = new com.winnertips.nairapredictz.Allsports();
                title = "All Sports Tips";
                break;
            case R.id.nav_telegram:
                fragment = new com.winnertips.nairapredictz.Telegram_Websites();
                title = "Telegram Channel";
                break;

            default:
                fragment = new DailyTopTips();
                title = "Top Picks";
        }

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        // set the toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
        DrawerLayout drawer =  findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

    }


}
