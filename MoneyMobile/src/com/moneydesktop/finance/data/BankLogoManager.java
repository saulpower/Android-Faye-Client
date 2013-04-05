package com.moneydesktop.finance.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.widget.ImageView;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.util.CacheUtils;

public class BankLogoManager {

    //	private static final String IMAGES_SD = "https://s3.amazonaws.com/MD_Assets/Ipad%20Logos/50x50";
    private static final String IMAGES_HD = "https://s3.amazonaws.com/MD_Assets/Ipad%20Logos/100x100";
    private static LruCache<String, Bitmap> mMemoryCache;

    public static void getBankImage(final ImageView view, String guid) {

        if (mMemoryCache == null) {
            setupMemoryCache();
        }

        if (guid == null || guid.equals("") || guid.toLowerCase().contains("manual")) guid = "bank";

        if (checkForPackagedImage(view, guid)) return;

        new AsyncTask<String, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(String... params) {

                String guid = params[0];

                final Bitmap bitmap = getBitmapFromMemCache(guid);

                if (bitmap == null) {


                    String imageUrl = String.format("%s/%s_100x100.png", IMAGES_HD, guid);
                    String filePath = CacheUtils.cacheResource(imageUrl);

                    Bitmap image = BitmapFactory.decodeFile(filePath);

                    addBitmapToMemoryCache(String.valueOf(params[0]), image);
                    return image;
                } else {
                    return bitmap;
                }
            }

            @Override
            protected void onPostExecute(Bitmap image) {

                if (isCancelled()) return;

                if (image != null & view != null) {
                    view.setImageBitmap(image);
                }
            };

        }.execute(guid);
    }

    private static boolean checkForPackagedImage(ImageView view, String guid) {

        if (guid.equalsIgnoreCase("bank")) {

            view.setImageResource(R.drawable.bank);
            return true;
        }

        if (guid.equalsIgnoreCase("bank1")) {

            view.setImageResource(R.drawable.bank1);
            return true;
        }

        if (guid.equalsIgnoreCase("bank2")) {

            view.setImageResource(R.drawable.bank2);
            return true;
        }

        if (guid.equalsIgnoreCase("bank3")) {

            view.setImageResource(R.drawable.bank3);
            return true;
        }

        if (guid.equalsIgnoreCase("bank4")) {

            view.setImageResource(R.drawable.bank4);
            return true;
        }

        return false;
    }


    public static void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (mMemoryCache == null) {
            setupMemoryCache();
        }
        if (getBitmapFromMemCache(key) == null) {
            if (key == null || bitmap == null) {
                //don't try to put null values into our cache
                return;
            } else {
                mMemoryCache.put(key, bitmap);
            }
        }
    }

    public static Bitmap getBitmapFromMemCache(String key) {
        if (mMemoryCache == null) {
            setupMemoryCache();
        }
        return mMemoryCache.get(key);
    }

    private static void setupMemoryCache() {
        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
            }
        };
    }
}
