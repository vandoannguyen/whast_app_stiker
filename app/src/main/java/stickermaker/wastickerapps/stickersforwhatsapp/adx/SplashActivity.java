package stickermaker.wastickerapps.stickersforwhatsapp.adx;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.uib17iwfv2.adx.service.AdsExchange;
import com.uib17iwfv2.adx.service.InterstitialAdsManager;
import com.uib17iwfv2.adx.service.SplashAdRequest;

import org.json.JSONObject;

import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import stickermaker.wastickerapps.stickersforwhatsapp.R;

public class SplashActivity extends AppCompatActivity {

    private InterstitialAdsManager adsManager;
    private int reCount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Async().execute();

        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ProgressBar pBar = new ProgressBar(this);
        pBar.setIndeterminate(true);
        layout.addView(pBar, layoutParams);
        setContentView(layout);
    }

    void initAd(int mode) {
        String banner = SharedPrefsUtils.getInstance(SplashActivity.this).getString(Config.AD_BANNER);
        String inter = SharedPrefsUtils.getInstance(SplashActivity.this).getString(Config.AD_INTERSTITIAL);
        if (mode == 1) {
            SplashAdRequest adRequest = new SplashAdRequest();
            adRequest.setBannerId(banner);
            adRequest.setBannerHidePercent(0);
            adRequest.setInterId(inter);
            adRequest.setResLogo(R.mipmap.ic_launcher);
            adRequest.setResBanner(R.mipmap.ic_launcher);
            adRequest.setAlwayShowAd(true);
            AdsExchange.loadSplashAd(this, adRequest);
        } else {
            adsManager = new InterstitialAdsManager();
            adsManager.init(true, this, inter, "#000000", getString(R.string.app_name));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        reCount++;
        if (adsManager != null)
            adsManager.onResume();

        if ((adsManager == null && reCount == 2) || (adsManager != null && reCount == 3)) {
            Intent intent = new Intent(SplashActivity.this, Config.MAIN);
            startActivity(intent);
            finish();
        }
    }

    private String decrypt(String seed, String encrypted) throws Exception {
        byte[] keyb = seed.getBytes("UTF-8");
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] thedigest = md.digest(keyb);
        SecretKeySpec skey = new SecretKeySpec(thedigest, "AES/ECB/PKCS7Padding");
        Cipher dcipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
        dcipher.init(Cipher.DECRYPT_MODE, skey);

        byte[] clearbyte = dcipher.doFinal(toByte(encrypted));
        return new String(clearbyte);
    }

    private byte[] toByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
        return result;
    }

    class Async extends AsyncTask<Void, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Void... voids) {
            HttpJsonParser jsonParser = new HttpJsonParser();
            return jsonParser.makeHttpRequest(Config.API, "GET", null);
        }

        @Override
        protected void onPostExecute(JSONObject s) {
            super.onPostExecute(s);


            try {
                String data = decrypt(Config.ADX_API_KEY, Config.AD_DATA_DEFAULT);
                JSONObject adObject = new JSONObject(data);

                String banner = adObject.getString("ad_banner_id");
                String interstitial = adObject.getString("ad_inter_id");
                int mode = 1;

                if (s != null) {
                    adObject = new JSONObject(decrypt(Config.ADX_API_KEY, s.getString("data")));
                    banner = adObject.getString("ad_banner_id");
                    interstitial = adObject.getString("ad_inter_id");
                    mode = adObject.getInt("ad_mode");

                }

                SharedPrefsUtils sharedPrefsUtils = SharedPrefsUtils.getInstance(SplashActivity.this);
                sharedPrefsUtils.putString(Config.AD_BANNER, banner);
                sharedPrefsUtils.putString(Config.AD_INTERSTITIAL, interstitial);

                initAd(mode);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
