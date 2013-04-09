package main.java.com.moneydesktop.finance.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.provider.MediaStore.Images;
import android.util.Log;

import main.java.com.moneydesktop.finance.ApplicationContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FileIO {

    public static final String TAG = "FileIO";

    public static final String STORAGE_PATH = "/MoneyMobile/";

    public static List<String[]> loadCSV(int resource) throws IOException {

        List<String[]> csv = new ArrayList<String[]>();

        InputStream inputStream = ApplicationContext.getContext().getResources().openRawResource(resource);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line = null;

        while ((line = reader.readLine()) != null) {

            String[] values = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
            csv.add(values);
        }

        return csv;
    }

    public static File getExternalDirectory() {

        String dir = Environment.getExternalStorageDirectory().getAbsolutePath();
        File dataDir = new File(dir + STORAGE_PATH);

        if (!dataDir.exists())
            dataDir.mkdirs();

        return  dataDir;
    }

    public static String loadRemoteData(String link, String filename, ProgressListener listener) {

        File outputFile = null;

        try {

            Log.i(TAG, "Downloading File: " + link);

            File dataDir = getExternalDirectory();

            // Deletes the file if it exists
            outputFile = new File(dataDir, filename);
            outputFile.delete();

            //this will be used to write the downloaded data into the file we created
            FileOutputStream fileOutput = new FileOutputStream(outputFile);

            URL url = new URL(link);

            //create the new connection
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            //set up some things on the connection
            urlConnection.setRequestMethod("GET");

            //and connect!
            urlConnection.connect();
            int total = urlConnection.getContentLength();

            //this will be used in reading the data from the internet
            InputStream inputStream = urlConnection.getInputStream();

            //create a buffer...
            byte[] buffer = new byte[1024];
            int bufferLength = 0; //used to store a temporary size of the buffer
            int dataRead = 0;

            //now, read through the input buffer and write the contents to the file
            while ((bufferLength = inputStream.read(buffer)) > 0) {

                dataRead += bufferLength;

                notifyListener(listener, dataRead, total);

                //add the data in the buffer to the file in the file output stream (the file on the sd card
                fileOutput.write(buffer, 0, bufferLength);
            }

            //close the output stream when done
            fileOutput.close();

        } catch (Exception e) {

            if (outputFile != null)
                outputFile.delete();

            return "";
        }

        return outputFile.getAbsolutePath();
    }

    private static void notifyListener(ProgressListener listener, int dataRead, int total) {

        if (listener == null)
            return;

         listener.onProgressUpdate(dataRead, total);
    }

    public static int remoteDataLength(String link) {

        int total = 0;

        try {

            URL url = new URL(link);

            //create the new connection
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            //set up some things on the connection
            urlConnection.setRequestMethod("GET");

            //and connect!
            urlConnection.connect();

            total = urlConnection.getContentLength();

        } catch (Exception e) {}

        return total;
    }

    public static String saveBitmap(Context context, Bitmap bitmap, String name) {

        return Images.Media.insertImage(context.getContentResolver(), bitmap, name, null);
    }

    public interface ProgressListener {
        public abstract void onProgressUpdate(int downloaded, int total);
    }
}
