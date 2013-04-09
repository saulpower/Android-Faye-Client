package main.java.com.moneydesktop.finance.util;

import java.io.File;
import java.util.Date;

import main.java.com.moneydesktop.finance.util.FileIO.ProgressListener;

public class CacheUtils {

    public static String cacheResource(String url) {
        return cacheResource(url, -1, null);
    }

    public static String cacheResource(String url, ProgressListener listener) {
        return cacheResource(url, -1, listener);
    }

    public static String cacheResource(String url, long refreshTime, ProgressListener listener) {

        boolean refresh = false;
        String filename = null;

        if (!url.equals("")) {

            filename = getFileName(url);

            File dataDir = FileIO.getExternalDirectory();
            File file = new File(dataDir, filename);

            if (refreshTime != -1) {
                long age = (new Date()).getTime() - file.lastModified();
                refresh = (age > refreshTime);
            }

            if (!file.exists() || refresh)
                filename = FileIO.loadRemoteData(url, filename, listener);
            else
                filename = file.getAbsolutePath();
        }

        return filename;
    }

    private static String getFileName(String url) {

        String[] parts = url.split("[/]");

        if (parts.length > 1)
            return parts[(parts.length - 1)];

        return "";
    }
}
