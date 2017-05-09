package com.cmpe273.sam.facerecognition;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener  {

    static final int REQUEST_IMAGE_CAPTURE = 100;
    static final int REQUEST_IMAGE_CHOOSE = 100;
    private static final String IMAGE_LOCATION = "cmpe273 project";
    static int REQUEST_IMAGE_UPLOAD = 0;
     private Uri fileUri;
    private Uri selectedImage;
    private String imgPath;
    private byte[] ba;
    Button btnJumpToCam;
    Button btnUpload, btnGallery, btnGet;
    private static final int SELECT_PICTURE = 100;
    private static final String TAG = "MainActivity";
    AppCompatImageView mImageView;
    AppCompatImageView mImageView2;
    private String img_upload;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView = (AppCompatImageView) findViewById(R.id.imgView);
        mImageView2 = (AppCompatImageView) findViewById(R.id.imgView2);

        btnJumpToCam = (Button) findViewById(R.id.btnJump);
        btnUpload = (Button) findViewById(R.id.btnUpload);
        btnGallery = (Button) findViewById(R.id.btnGallery);
        btnGet = (Button) findViewById(R.id.btnGet);
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
                RecognitionWorker worker = new RecognitionWorker();
                worker.execute();
            }
        });

        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), REQUEST_IMAGE_CHOOSE);

            }
        });

        btnGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetWorker getWorker = new GetWorker();
                getWorker.execute();
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                selectedImage = data.getData();
            if (null != selectedImage) {
                mImageView.setImageURI(selectedImage);
            }

        }
        if(requestCode==REQUEST_IMAGE_CHOOSE && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            imgPath = getRealPathFromURI(selectedImage);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                mImageView.setImageBitmap(bitmap);
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

    public void upload() throws FileNotFoundException, UnsupportedEncodingException {
//        Bitmap bm = BitmapFactory.decodeFile(imgPath);
        Bitmap bm= ((BitmapDrawable) mImageView.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 50, stream);
        byte[] byteImage = stream.toByteArray();
        System.out.println("byte from image bitmap size: "+byteImage.length);
        img_upload = Base64.encodeToString(byteImage, Base64.DEFAULT);
//        imgPath = "file:////"+imgPath;
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
        System.out.println("image path: "+imgPath);
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
                    Log.d("conn" ,"connection start");
                    URL url = new URL("https://zc2oz2npjg.execute-api.us-east-1.amazonaws.com/ImageTable/images");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF8"); // here you are setting the `Content-Type` for the data you are sending which is `application/json`
                    conn.setReadTimeout(100000);
                    conn.setConnectTimeout(150000);
                    conn.setRequestMethod("POST");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);

                //json or ?
                /*
                Request
    {
        "body": {
            "TableName": "Image",
            "ImageName": "Henry17",
            "Content": "He is going hiking"
        }
    }
    {
        "object": {
            "key": "Henry19.jpg",
            "data": "What about you?"
        }
    }
                 */
                upload();

                JSONObject req = new JSONObject();
                JSONObject object = new JSONObject();
                object.put("key", "testwenjin.jpg");
                object.put("data", img_upload);
                System.out.println("image to upload size: "+img_upload.length());
                req.put("object", object);
                DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                wr.writeBytes(req.toString());
                wr.flush();
                wr.close();


                Log.d("response", ""+conn.getResponseMessage());

//                conn.connect();
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
