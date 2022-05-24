package de.lemke.nakbuch.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.play.core.review.ReviewManagerFactory
import dagger.hilt.android.AndroidEntryPoint
import de.dlyt.yanndroid.oneui.dialog.AlertDialog
import de.dlyt.yanndroid.oneui.layout.DrawerLayout
import de.dlyt.yanndroid.oneui.utils.ThemeUtil
import de.lemke.nakbuch.R
import de.lemke.nakbuch.domain.DiscoverEasterEggUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.dionsegijn.konfetti.xml.KonfettiView
import javax.inject.Inject

/*import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.material.button.MaterialButton;
import com.google.android.gms.ads.rewarded.RewardedAd;*/

@AndroidEntryPoint
class AboutMeActivity : AppCompatActivity() {
    private lateinit var konfettiView: KonfettiView
    //private lateinit var mRewardedAd: RewardedAd
    //private lateinit var watchAdButton: MaterialButton

    @Inject
    lateinit var discoverEasterEgg: DiscoverEasterEggUseCase

    companion object {
        private const val AD_UNIT_ID = "ca-app-pub-5655920768524739/5575349013"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtil(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_me)
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_support_me)
        drawerLayout.setNavigationButtonIcon(AppCompatResources.getDrawable(this, de.dlyt.yanndroid.oneui.R.drawable.ic_oui_back))
        drawerLayout.setNavigationButtonOnClickListener { onBackPressed() }
        konfettiView = findViewById(R.id.konfettiViewAboutMePage)

        findViewById<View>(R.id.websiteButton).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.myWebsite))))
        }
        findViewById<View>(R.id.ticktocktrollButton).setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch { discoverEasterEgg(konfettiView, R.string.easterEggEntryTikTok) }
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.rickRollTrollLink)))) //Rick Roll :D
        }
        findViewById<View>(R.id.supportMeButton).setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:") // only email apps should handle this
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.email)))
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
            intent.putExtra(Intent.EXTRA_TEXT, "")
            try {
                startActivity(intent)
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(this, getString(R.string.noEmailAppInstalled), Toast.LENGTH_SHORT).show()
            }
        }
        findViewById<View>(R.id.reviewCommentButton).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.writeReview))
                .setMessage(getString(R.string.reviewComment))
                .setNeutralButton(R.string.ok, null)
        }
        findViewById<View>(R.id.writeReviewButton).setOnClickListener {
            val manager = ReviewManagerFactory.create(this)
            //val manager = FakeReviewManager(mContext);
            val request = manager.requestReviewFlow()
            request.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val reviewInfo = task.result
                    val flow = manager.launchReviewFlow(this, reviewInfo)
                    flow.addOnCompleteListener { task2 ->
                        if (task2.isSuccessful) {
                            //Toast.makeText(mContext, "Vielen Dank für deine Bewertung", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, getString(R.string.error) + ": " + task2.exception, Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, R.string.taskFailed, Toast.LENGTH_SHORT).show()
                    // There was some problem, log or handle the error code.
                    //@ReviewErrorCode val reviewErrorCode = (task.exception as TaskException).errorCode
                }
            }
        }
        findViewById<View>(R.id.shareAppButton).setOnClickListener {
            val sendIntent = Intent(Intent.ACTION_SEND)
            sendIntent.type = "text/plain"
            sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.shareAppText) + packageName)
            sendIntent.putExtra(Intent.EXTRA_TITLE, getString(R.string.shareAppTitle))
            startActivity(Intent.createChooser(sendIntent, "Share Via"))
        }
    }
}


/*
watch ad java:
watchAdButton = findViewById(R.id.watchAdButton);
        watchAdButton.setEnabled(false);
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, AD_UNIT_ID,
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.d("werbung", loadAdError.getMessage());
                        mRewardedAd = null;
                    }

                    @Override
                    public void onAdLoaded(RewardedAd rewardedAd) {
                        mRewardedAd = rewardedAd;
                        watchAdButton.setEnabled(true);
                        Log.d("werbung", "Ad was loaded.");
                    }
                });

        findViewById(R.id.coffeeButtonSmall).setOnClickListener(v -> {
            Toast.makeText(mContext, "Kleiner Kaffee", Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.coffeeButtonMedium).setOnClickListener(v -> {
            Toast.makeText(mContext, "Mittlerer Kaffee", Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.coffeeButtonLarge).setOnClickListener(v -> {
            Toast.makeText(mContext, "Großer Kaffee", Toast.LENGTH_SHORT).show();
        });
        watchAdButton.setOnClickListener(v -> {
            mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdShowedFullScreenContent() {
                    Log.d("werbung", "Ad was shown.");
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    Log.d("werbung", "Ad failed to show.");
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    // Set the ad reference to null so you don't show the ad a second time.
                    Log.d("werbung", "Ad was dismissed.");
                    mRewardedAd = null;
                }
            });

            if (mRewardedAd != null) {
                mRewardedAd.show(this, rewardItem -> {
                    Log.d("werbung", "The user earned the reward.");
                    int rewardAmount = rewardItem.getAmount();
                    String rewardType = rewardItem.getType();
                });
            } else {
                Log.d("werbung", "The rewarded ad wasn't ready yet.");
            }
        });*/
