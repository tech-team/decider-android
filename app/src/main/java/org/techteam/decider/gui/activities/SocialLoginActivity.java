package org.techteam.decider.gui.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import org.techteam.decider.R;
import org.techteam.decider.rest.api.SocialProviders;
import org.techteam.decider.rest.service_helper.ServiceCallback;

import me.zhanghai.android.materialprogressbar.HorizontalProgressDrawable;
import me.zhanghai.android.materialprogressbar.IndeterminateHorizontalProgressDrawable;

public class SocialLoginActivity extends ToolbarActivity {
    private static final String TAG = SocialLoginActivity.class.getName();

    private WebView webView;
    private ProgressBar progressBar;

    public static final class IntentKeys {
        public static final String URL = "URL";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        setContentView(R.layout.activity_social_login);

        // toolbar stuff
        Toolbar toolbar = (Toolbar) findViewById(R.id.social_auth_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        progressBar = (ProgressBar) findViewById(R.id.progress_spinner);

        int color = getResources().getColor(R.color.accent);
        HorizontalProgressDrawable toolbarDrawable = new HorizontalProgressDrawable(this);
        toolbarDrawable.setUseIntrinsicPadding(false);
        toolbarDrawable.setShowTrack(false);
        toolbarDrawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        progressBar.setProgressDrawable(toolbarDrawable);

        CookieManager cookieManager = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.removeAllCookies(null);
        } else {
            cookieManager.removeAllCookie();
        }

        webView = (WebView) findViewById(R.id.web_view);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d(TAG, "Got url: " + url);
                if (url.startsWith(SocialProviders.getSocialCompletePath())) {
                    Uri finalUri = Uri.parse(url);

                    String canceledStr = finalUri.getQueryParameter("canceled");
                    if (canceledStr != null) {
                        int canceled = Integer.parseInt(canceledStr);
                        if (canceled == 1) {
                            setResult(Activity.RESULT_CANCELED);
                            finish();
                            return;
                        }
                    }

                    String accessToken = finalUri.getQueryParameter("access_token");
                    long expires = System.currentTimeMillis() + Long.parseLong(finalUri.getQueryParameter("expires")) * 1000;
                    String refreshToken = finalUri.getQueryParameter("refresh_token");
                    String userId = finalUri.getQueryParameter("user_id");
                    String username = finalUri.getQueryParameter("username");
                    boolean registrationFinished = true;
                    String regFinishedQuery = finalUri.getQueryParameter("reg");
                    if (regFinishedQuery != null) {
                        registrationFinished = Integer.parseInt(regFinishedQuery) == 1;
                    }

                    Intent data = new Intent();
                    data.putExtra(ServiceCallback.LoginRegisterExtras.TOKEN, accessToken);
                    data.putExtra(ServiceCallback.LoginRegisterExtras.EXPIRES, expires);
                    data.putExtra(ServiceCallback.LoginRegisterExtras.REFRESH_TOKEN, refreshToken);
                    data.putExtra(ServiceCallback.LoginRegisterExtras.USER_ID, userId);
                    data.putExtra(ServiceCallback.LoginRegisterExtras.USERNAME, username);
                    data.putExtra(ServiceCallback.LoginRegisterExtras.REGISTRATION_UNFINISHED, !registrationFinished);
                    setResult(Activity.RESULT_OK, data);
                    finish();
                }
                super.onPageFinished(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.d(TAG, "Getting url: " + url);
                progressBar.setVisibility(View.VISIBLE);
                super.onPageStarted(view, url, favicon);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        String url = getIntent().getStringExtra(IntentKeys.URL);
        webView.loadUrl(url);


    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }
}
