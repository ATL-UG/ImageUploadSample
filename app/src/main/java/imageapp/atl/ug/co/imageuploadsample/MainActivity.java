package imageapp.atl.ug.co.imageuploadsample;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import retrofit.mime.TypedFile;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button uploadButton;
    private Button cancelButton;

    private ImageView uploadedImage;

    //photo result code...
    private static final int GALLERY_PHOTO_CODE = 1;

    private static final String TAG = MainActivity.class.getSimpleName();

    //usused
    Bitmap bitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //uploading picking view components...
        uploadButton = (Button)findViewById(R.id.upload_button);
        cancelButton = (Button)findViewById(R.id.cancel_button);
        uploadedImage = (ImageView)findViewById(R.id.uploaded_image);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_photo_upload) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture..."), GALLERY_PHOTO_CODE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //getting the result code...
        if(resultCode == RESULT_OK && requestCode == GALLERY_PHOTO_CODE){
            Uri selectedImage = data.getData();
            Log.i(TAG, "Uploaded Photo from Gallery: " + selectedImage.toString());

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                if(bitmap != null){
                    uploadedImage.setImageBitmap(bitmap);
                }
            } catch (IOException e) {
                e.printStackTrace();
                //informthe user of the error...

            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.cancel_button:
                Log.i(TAG, "upload cancelled...");
                //show toast that the user chose to cancel...
                Toast.makeText(this, getString(R.string.upload_cancelled), Toast.LENGTH_SHORT).show();
                break;
            case R.id.upload_button:
                Log.i(TAG, "Going to upload...");

                //uploading the file...
                UploadFileExecutor executor = new UploadFileExecutor();
                executor.execute(bitmap);

                break;

        }
    }

    /**
     * Converts the bitmap uploaded to TypedFile that can be uploaded to the server.
     * PS: Better implementation can be done at this point, the bottom line is to succesfully return
     * a corresponding TypedFile
     * @param image - the image bitmap to be uploaded
     * @return - returns the same image with
     */
    private TypedFile getFileTyped(Bitmap image){
        //the image folder with content...
        File folderPath = new File(getCacheDir(),"imageUploadFolder");
        if( !folderPath.exists() ){
            folderPath.mkdir();
        }

        try {
            File file = new File(folderPath, "img-" + System.currentTimeMillis() + ".jpg");
            Log.d(TAG, "File Path: " + file.toString());
            file.createNewFile();

            FileOutputStream out = null;

            out = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.JPEG, 85, out);
            out.flush();

            return new TypedFile("image/jpeg", file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Returns the interface that is used to acceess the network
     * @return
     */
    private NetworkInterceptor getNetworkInterceptor(){
        BaseApplication app = (BaseApplication)this.getApplication();
        return app.getNetworkInterceptor();
    }

    private class UploadFileExecutor extends AsyncTask<Bitmap, Void, String>{

        ProgressDialog progressDialog;

        public UploadFileExecutor(){
            progressDialog = new ProgressDialog(MainActivity.this);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //processing the progress dialog...
            progressDialog.setMessage(getString(R.string.uploading));
            progressDialog.setIndeterminate(true);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Bitmap... params) {
            //get and process the bitmap...
            Bitmap bitmap = params[0]; //check for null pointer exceptions here
            TypedFile typedFile = getFileTyped(bitmap);

            //sending values with String..
            Date date = new Date();
            String sampleSender = date.toString();

            //doing the actual sending...
            if(typedFile != null){
                try {
                    String result = getNetworkInterceptor().postValues(sampleSender, typedFile);
                    return result;
                }catch (Exception ex){
                    Log.e(TAG, "Error on sending items..", ex);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(progressDialog.isShowing())
                progressDialog.dismiss();

            //check if result is not null, and send it..
            if(s == null){
                Toast.makeText(MainActivity.this, getString(R.string.upload_error), Toast.LENGTH_LONG).show();
                return;
            }

            //otherwise show content...
            Toast.makeText(MainActivity.this, s, Toast.LENGTH_LONG).show();
            Log.i(TAG, "Successfully uploaded the file...");
        }
    }
}
