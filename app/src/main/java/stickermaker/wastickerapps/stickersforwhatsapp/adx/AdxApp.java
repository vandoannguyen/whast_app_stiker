package stickermaker.wastickerapps.stickersforwhatsapp.adx;

import androidx.multidex.MultiDexApplication;

import com.flurry.android.FlurryAgent;
import com.uib17iwfv2.adx.service.AdsExchange;

public class AdxApp extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        AdsExchange.init(this, Config.ADX_API_KEY);
        new FlurryAgent.Builder()
                .withLogEnabled(true)
                .build(this, Config.FLURRY_API_KEY);
    }
}
