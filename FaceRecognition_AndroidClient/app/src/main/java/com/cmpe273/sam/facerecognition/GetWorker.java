package com.cmpe273.sam.facerecognition;

import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by sam on 5/8/17.
 */

public class GetWorker extends AsyncTask{

    @Override
    protected Object doInBackground(Object[] params) {

        try {
            Log.d("conn", "connection start");
            URL url = new URL("https://zc2oz2npjg.execute-api.us-east-1.amazonaws.com/ImageTable/images/testwenjin.jpg");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF8"); // here you are setting the `Content-Type` for the data you are sending which is `application/json`
            conn.setReadTimeout(100000);
            conn.setConnectTimeout(150000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setDoOutput(true);
//            InputStream in = conn.getInputStream();
//            InputStreamReader isw = new InputStreamReader(in);
//
//            int data = isw.read();
//            while (data != -1) {
//                char current = (char) data;
//                data = isw.read();
//                System.out.print(current);
//            }
            int responseCode = conn.getResponseCode();

            if(responseCode == HttpURLConnection.HTTP_OK){

            }

            System.out.println(conn.getResponseMessage());

        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
