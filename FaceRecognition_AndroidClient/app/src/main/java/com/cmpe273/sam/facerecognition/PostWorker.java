package com.cmpe273.sam.facerecognition;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by sam on 5/8/17.
 */

public class PostWorker extends AsyncTask{

    @Override
    protected Object doInBackground(Object[] params) {

        DataOutputStream wr;
        InputStreamReader isw;

        try {
            Log.d("conn", "connection start");
            URL url = new URL("https://zc2oz2npjg.execute-api.us-east-1.amazonaws.com/ImageTable/images");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF8"); // here you are setting the `Content-Type` for the data you are sending which is `application/json`
            conn.setReadTimeout(100000);
            conn.setConnectTimeout(150000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            File file = new File((String) params[0]);
            FileInputStream fis = new FileInputStream(file);
            //System.out.println(file.exists() + "!!");
            //InputStream in = resource.openStream();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            try {
                for (int readNum; (readNum = fis.read(buf)) != -1;) {
                    bos.write(buf, 0, readNum); //no doubt here is 0

                }
            } catch (IOException ex) {

            }
            byte[] bytes = bos.toByteArray();
            String img_upload = Base64.encodeToString(bytes, Base64.DEFAULT);
            JSONObject req = new JSONObject();
            JSONObject object = new JSONObject();
//                object.put("key", "testwenjin.jpg");
//                object.put("data", img_upload);
            object.put("key", System.currentTimeMillis()+".jpg");
            object.put("data", img_upload);
            System.out.println("image to upload size: "+img_upload.length());
//                req.put("object", object);
            req.put("object", object);
            wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(req.toString());
            wr.flush();
            wr.close();

            System.out.println("upload complete");
            int responseCode = conn.getResponseCode();

            if(responseCode == HttpURLConnection.HTTP_OK){

            }

            System.out.println(conn.getResponseMessage());

        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);

    }
}
