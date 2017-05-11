package com.cmpe273.sam.facerecognition;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AsyncResponse  {

    static final int REQUEST_IMAGE_CAPTURE = 100;
    static final int REQUEST_IMAGE_CHOOSE = 100;
    private static final String IMAGE_LOCATION = "cmpe273 project";
    static int REQUEST_IMAGE_UPLOAD = 0;
     private Uri fileUri;
    private Uri selectedImage;
    private String imgPath;
    private byte[] ba;
    Button btnJumpToCam;
    Button btnUpload, btnGallery, btnPostModel;
    TextView txtRes;
    private static final int SELECT_PICTURE = 100;
    private static final String TAG = "MainActivity";
    AppCompatImageView mImageView;
    AppCompatImageView mImageView2;
    private String img_upload;
    DBHelper dbHelper;
    String returnFromServer;
    RecognitionWorker worker;

    public AsyncResponse returnAsycnRes() {
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView = (AppCompatImageView) findViewById(R.id.imgView);

        txtRes = (TextView)findViewById(R.id.txtRes);
        btnJumpToCam = (Button) findViewById(R.id.btnJump);
        btnUpload = (Button) findViewById(R.id.btnUpload);
        btnGallery = (Button) findViewById(R.id.btnGallery);
        btnPostModel = (Button) findViewById(R.id.btnPostModel);
        dbHelper = new DBHelper(this);

        verifyStoragePermissions(this);

        btnJumpToCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpToCamera();
            }
        });
      btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                upload();
                worker = new RecognitionWorker();
                worker.execute();
                worker.delegate = returnAsycnRes();
            }
        });


        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), REQUEST_IMAGE_CHOOSE);

            }
        });

        btnPostModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PostWorker getWorker = new PostWorker();
                getWorker.execute(imgPath);
            }
        });
    }

    // Storage Permissions variables
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    //persmission method.
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have read or write permission
        int writePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.CAMERA},
                MainActivity.REQUEST_IMAGE_CAPTURE);

    }


    public void jumpToCamera(){

        final Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //fileUri = Uri.fromFile(getOutPutImageFile());
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
        else {
            Toast.makeText(getApplication(), "Camera not supported", Toast.LENGTH_LONG).show();
        }
    }


    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);

    }


    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                selectedImage = data.getData();

            imgPath = getRealPathFromURI(selectedImage);
            if (null != selectedImage) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
//                    mImageView.setImageMatrix(matrix);
                    mImageView.setImageBitmap(bitmap);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }
        if(requestCode==REQUEST_IMAGE_CHOOSE && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            imgPath = getRealPathFromURI(selectedImage);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                Matrix matrix = new Matrix();
                Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap , 0, 0, bitmap .getWidth(), bitmap .getHeight(), matrix, true);
                matrix.postRotate(180);
                mImageView.setImageBitmap(rotatedBitmap);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    Boolean saveImageInDB(Uri selectedImageUri) {
        try {
            dbHelper.open();
            InputStream iStream = getContentResolver().openInputStream(selectedImageUri);
            byte[] inputData = this.getBytes(iStream);
            dbHelper.insertImage(inputData);
            dbHelper.close();
            Log.d("snmds","000000000000");
            return true;
        }
        catch (IOException ioe) {
            Log.d("nsdmnsmd","1.0101010");
            Log.e(TAG, "<saveImageInDB> Error : " + ioe.getLocalizedMessage());
            dbHelper.close();
            return false;
        }

    }

    Boolean loadImageFromDB() {
        try {
            dbHelper.open();
            byte[] bytes = dbHelper.retreiveImageFromDB();
            dbHelper.close();
            // Show Image from DB in ImageView
            Log.d("retreiveImageFromDB","((((((((((retreiveImageFromDB");
            if(bytes != null){
                mImageView2.setImageBitmap(this.getImage(bytes));
                mImageView2.setImageURI(selectedImage);
                Log.d("bytes","bytes nt null");
            }
            return true;
        }
        catch (Exception e) {
            Log.e(TAG, "<loadImageFromDB> Error : " + e.getLocalizedMessage());
            dbHelper.close();
            return false;
        }
    }

    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    public static byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public void getImage() throws FileNotFoundException, UnsupportedEncodingException {

        File file = new File(imgPath);
        FileInputStream fis = new FileInputStream(file);
        //System.out.println(file.exists() + "!!");
        //InputStream in = resource.openStream();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        try {
            for (int readNum; (readNum = fis.read(buf)) != -1;) {
                bos.write(buf, 0, readNum); //no doubt here is 0
                //Writes len bytes from the specified byte array starting at offset off to this byte array output stream.
//                System.out.println("read " + readNum + " bytes,");
            }
        } catch (IOException ex) {
//            Logger.getLogger(genJpeg.class.getName()).log(Level.SEVERE, null, ex);
        }
        byte[] bytes = bos.toByteArray();
//        System.out.println("image path: "+imgPath);
        img_upload = Base64.encodeToString(bytes, Base64.DEFAULT);
//        System.out.println("image from gallery "+str64.length());
//
//        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        //create upload worker and execute
//       RecognitionWorker worker = new RecognitionWorker();
//        worker.execute();
    }
    private static File getOutPutImageFile(){
        File imageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),IMAGE_LOCATION);
        if(!imageDir.exists()){
            if(!imageDir.mkdirs()){
                Log.d(IMAGE_LOCATION, "failed to create image directory");
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        mediaFile = new File(imageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        return mediaFile;
    }

    public static byte[] getImageBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }
    @Override
    public void onClick(View v) {

    }

    @Override
    public void processFinish(String output) {
        txtRes.setText(output);
    }

    /**
     * inner upload conn class
     */
    public class RecognitionWorker extends AsyncTask<Void, Void, String> {

        private ProgressDialog pd;
        public AsyncResponse delegate = null;

        public RecognitionWorker () {
            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("pairing...");
            pd.show();
        }



        public void upload() throws IOException, JSONException {
            DataOutputStream wr;
            InputStreamReader isw;
            HttpURLConnection conn;

            URL url = new URL("https://zc2oz2npjg.execute-api.us-east-1.amazonaws.com/ImageTable/results/1");

            Log.d("conn" ,"connection start");
//                    URL url = new URL("https://zc2oz2npjg.execute-api.us-east-1.amazonaws.com/ImageTable/images");

            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF8"); // here you are setting the `Content-Type` for the data you are sending which is `application/json`
            conn.setReadTimeout(10000000);
            conn.setConnectTimeout(15000000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);


            getImage();

            JSONObject req = new JSONObject();
            JSONObject object = new JSONObject();
//                object.put("key", "testwenjin.jpg");
//                object.put("data", img_upload);
            object.put("ImageName", System.currentTimeMillis()+".jpg");
            object.put("Content", img_upload);
            System.out.println("image to upload size: "+img_upload.length());
//                req.put("object", object);
            req.put("body", object);
            wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(req.toString());
            wr.flush();
            wr.close();

            System.out.println("upload complete");
            Log.d("response", ""+conn.getResponseCode()+", "+conn.getResponseMessage());
            // get response
            InputStream responseStream = new BufferedInputStream(conn.getInputStream());
            BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(responseStream));
            String line = "";
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = responseStreamReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            responseStreamReader.close();
            String response = stringBuilder.toString();
//            JSONObject jsonResponse = new JSONObject(response);
            System.out.println("response from server: "+ response);
            returnFromServer = response;


        }
        @Override
        protected String doInBackground(Void... params){

            try{
                upload();
            }catch (Exception e){
                e.printStackTrace();
            }

            return returnFromServer;

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pd.hide();
            pd.dismiss();
            delegate.processFinish(s);

        }
    }
}
