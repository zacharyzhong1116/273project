package com.cmpe273.sam.facerecognition;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener  {

    static final int REQUEST_IMAGE_CAPTURE = 100;
    private static final String IMAGE_LOCATION = "cmpe273 project";
    static int REQUEST_IMAGE_UPLOAD = 0;
     private Uri fileUri;
    private Uri selectedImage;
    private String imgPath;
    private String ba1;
    Button btnJumpToCam;
    Button btnUpload;
    private static final int SELECT_PICTURE = 100;
    private static final String TAG = "MainActivity";

    AppCompatImageView mImageView;
    AppCompatImageView mImageView2;

    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView = (AppCompatImageView) findViewById(R.id.imgView);
        mImageView2 = (AppCompatImageView) findViewById(R.id.imgView2);

        btnJumpToCam = (Button) findViewById(R.id.btnJump);
        btnUpload = (Button) findViewById(R.id.btnUpload);
        dbHelper = new DBHelper(this);

        btnJumpToCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpToCamera();
            }
        });

      /*  btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload();
            }
        });
   */ }

    public void jumpToCamera(){
        final Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //fileUri = Uri.fromFile(getOutPutImageFile());
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            btnUpload.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    REQUEST_IMAGE_UPLOAD=100;
                    if (saveImageInDB(selectedImage)) {
                        Log.d("saving","-------------------savingggggggg");
                        Toast.makeText(getApplication(),"Image Saved",Toast.LENGTH_LONG).show();
                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (loadImageFromDB()) {
                                Toast.makeText(getApplication(),"Loading",Toast.LENGTH_LONG).show();
                                Log.d("load try","load try");
                            }
                        }
                    }, 3000);


                    /*takePictureIntent.setAction(Intent.ACTION_INSERT);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_UPLOAD);
              */  }
            });

        }
        else {
            Toast.makeText(getApplication(), "Camera not supported", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            selectedImage = data.getData();
            if (null != selectedImage) {

                // Saving to Database...

            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            mImageView.setImageBitmap(imageBitmap);
            mImageView.setImageURI(selectedImage);

            }


         /*   String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            imgPath = cursor.getString(columnIndex);
            cursor.close();*/
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

    public void upload() {
       // Bitmap bm = BitmapFactory.decodeFile(imgPath);
        Bitmap bm= ((BitmapDrawable) mImageView.getDrawable()).getBitmap();
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 50, bao);
        byte[] ba = bao.toByteArray();
        ba1 = Base64.encodeToString(ba, Base64.NO_WRAP);

        //create upload worker and execute
       /* RecognitionWorker worker = new RecognitionWorker();
        worker.execute();*/
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

    /**
     * inner upload conn class
     */
    public class RecognitionWorker extends AsyncTask<Void, Void, String> {

        private ProgressDialog pd;

        public RecognitionWorker () {
            pd = new ProgressDialog(MainActivity.this);
        }
        @Override
        protected String doInBackground(Void... params) {

            try{
                URL url = new URL("http://10.250.6.8/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                //json or ?
                ContentValues cv = new ContentValues();
                cv.put("data", ba1);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(cv.toString());
                writer.flush();
                writer.close();
                os.close();

                conn.connect();
            }catch (Exception e){
                e.printStackTrace();
            }
            return "Success";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pd.hide();
            pd.dismiss();
        }
    }
}
