package alvaron14.tutorial.camera_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import alvaron14.tutorial.camera_app.databinding.ActivityDefaultCameraBinding;

public class defaultCamera extends AppCompatActivity {
    private ActivityDefaultCameraBinding binding;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private int REQUEST_CODE_PERMISSIONS = 101;
    private String mCurrentPhotoPath;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA",
            "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDefaultCameraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        allPermissionsGranted();

        initializeBindings();
    }

    private void initializeBindings() {
        binding.takePhotoButton.setOnClickListener(v -> inflateCamera());

        binding.randomPhotoButton.setOnClickListener(v -> randomImageToImageView());
    }

    private void inflateCamera() {
        try {
            Intent intent = new Intent();
            intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri photoURI = FileProvider.getUriForFile(
                    defaultCamera.this,
                    getApplicationContext().getPackageName() + ".provider", //(use your app signature + ".provider" )
                    createImageFile());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        } catch (Exception e) {
            Log.e("Exception intent", e.toString());
        }
    }

    private void randomImageToImageView() {
        File storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_DCIM), "ClosetAssistant");
        File[] images = storageDir.listFiles();
        if (images == null) {
            Log.e("randomImageToImageView", "Error at searching the directory, null array, try to take a photo first");
            return;
        }
        if (images.length == 0) {
            Log.e("randomImageToImageView", "No images in the gallery, take one first");
            return;
        }

        File randomImage = images[(int)(Math.random()*images.length)];

        try {
            InputStream ims = new FileInputStream(randomImage);
            binding.imageViewDefaultCamera.setImageBitmap(BitmapFactory.decodeStream(ims));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Show the thumbnail on ImageView
            Uri imageUri = Uri.parse(mCurrentPhotoPath);
            File file = new File(imageUri.getPath());
            try {
                InputStream ims = new FileInputStream(file);
                binding.imageViewDefaultCamera.setImageBitmap(BitmapFactory.decodeStream(ims));
            } catch (FileNotFoundException e) {
                return;
            }

            //TODO: Fix gallery indexing https://stackoverflow.com/questions/63869824/mediascannerconnection-scanfile-returning-null-uri
            /*
            String type = null;
            String extension = MimeTypeMap.getFileExtensionFromUrl(file.getAbsolutePath());
            if (extension != null) {
                type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            }
            Log.e("scanFile", "type: " + type);

            // ScanFile so it will be appeared on Gallery
            MediaScannerConnection.scanFile(defaultCamera.this,
                    new String[]{imageUri.getPath()},
                    new String[]{type},
                    (path, uri) -> {
                        Log.e("scanFile", "Path: " + path);
                        Log.e("scanFile", "Uri: " + uri);
                    });
            */
        }
    }

    private void scanFileToGallery(Uri imageUri) {

    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                Log.d("DAM", "Permissions denied");
                return false;
            }
        }
        Log.d("DAM", "Permissions granted");
        return true;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_DCIM), "ClosetAssistant");
        if (storageDir.exists()) Log.i("mkDir", "Exists");
        else {
            Log.i("mkDir", "" + storageDir.mkdirs());
        }
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }
}