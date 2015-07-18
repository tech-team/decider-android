package org.techteam.decider.gui.activities;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.techteam.decider.R;
import org.techteam.decider.rest.api.ApiUI;
import org.techteam.decider.rest.api.SocialProviders;
import org.techteam.decider.rest.service_helper.ServiceCallback;

public class SocialLoginActivity extends AppCompatActivity {
    private static final String TAG = SocialLoginActivity.class.getName();

    private WebView webView;

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

        webView = (WebView) findViewById(R.id.web_view);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d(TAG, "Got url: " + url);
                if (url.startsWith(SocialProviders.getSocialCompletePath())) {
                    Uri finalUri = Uri.parse(url);
                    String accessToken = finalUri.getQueryParameter("access_token");
                    long expires = System.currentTimeMillis() + Long.parseLong(finalUri.getQueryParameter("expires")) * 1000;
                    String refreshToken = finalUri.getQueryParameter("refresh_token");
                    String username = finalUri.getQueryParameter("username");
                    String userId = finalUri.getQueryParameter("user_id"); // TODO: not sure about parameter name

                    Intent data = new Intent();
                    data.putExtra(ServiceCallback.LoginRegisterExtras.TOKEN, accessToken);
                    data.putExtra(ServiceCallback.LoginRegisterExtras.EXPIRES, expires);
                    data.putExtra(ServiceCallback.LoginRegisterExtras.REFRESH_TOKEN, refreshToken);
                    data.putExtra(ServiceCallback.LoginRegisterExtras.LOGIN, username);
                    data.putExtra(ServiceCallback.LoginRegisterExtras.USER_ID, userId);
                    setResult(0, data);
                    finish();
                }
                super.onPageFinished(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.d(TAG, "Getting url: " + url);
                super.onPageStarted(view, url, favicon);
            }
        });

        String url = getIntent().getStringExtra(IntentKeys.URL);
        webView.loadUrl(url);


    }

}