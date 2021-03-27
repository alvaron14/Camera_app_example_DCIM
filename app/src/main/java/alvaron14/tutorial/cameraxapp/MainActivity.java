package alvaron14.tutorial.cameraxapp;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import alvaron14.tutorial.cameraxapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ImageCapture imageCapture;
    private ActivityMainBinding binding;
    private int REQUEST_CODE_PERMISSIONS = 101;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA",
            "android.permission.WRITE_EXTERNAL_STORAGE"};

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // DAM: Unless you ask for permissions there is nothing to do
        // DAM: Also grant all the permissions for the app in the device
        requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        allPermissionsGranted();


        PreviewView previewView = binding.viewFinder;

        ListenableFuture cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                // Camera provider is now guaranteed to be available
                ProcessCameraProvider cameraProvider = (ProcessCameraProvider) cameraProviderFuture.get();

                // Set up the view finder use case to display camera preview
                Preview preview = new Preview.Builder().build();

                // Set up the capture use case to allow users to take photos
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                // Choose the camera by requiring a lens facing
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK) //<------- Change Lens facing
                        .build();

                // Attach use cases to the camera with the same lifecycle owner
                Camera camera = cameraProvider.bindToLifecycle(
                        ((LifecycleOwner) this),
                        cameraSelector,
                        preview,
                        imageCapture);

                // Connect the preview use case to the previewView
                preview.setSurfaceProvider(
                        previewView.getSurfaceProvider());
            } catch (InterruptedException | ExecutionException e) {
                // Currently no exceptions thrown. cameraProviderFuture.get()
                // shouldn't block since the listener is being called, so no need to
                // handle InterruptedException.
            }
        }, ContextCompat.getMainExecutor(this));

        binding.cameraCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.onClick();
            }
        });

        /*
        // The use case is bound to an Android Lifecycle with the following code
        Camera camera = cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview);

        // PreviewView creates a surface provider, using a Surface from a different
        // kind of view will require you to implement your own surface provider.
        preview.previewSurfaceProvider = viewFinder.getSurfaceProvider();
         */
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (allPermissionsGranted()) {
            //startCamera();
        } else {
            Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
            finish();
        }
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

    public void onClick() {

        // DAM: The image will be stored at /storage/emulated/0/Android/data/alvaron14.tutorial.cameraxapp/files/Pictures
        File outputFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "test_" + System.currentTimeMillis() + ".png");
        try {
            outputFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(outputFile).build();
        // DAM: I have changed the Executor, just in case
        imageCapture.takePicture(outputFileOptions, Executors.newSingleThreadExecutor(), //Executors.newSingleThreadExecutor()
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {
                        Log.d("PHOTO", "onImageSaved: saved");
                        File image = new File(outputFile.toURI());
                        if (image.exists()) {
                            Bitmap myBitmap = BitmapFactory.decodeFile(image.getAbsolutePath());

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    binding.imageView.setImageBitmap(myBitmap);
                                }
                            });


                        }

                    }

                    @Override
                    public void onError(ImageCaptureException error) {
                        Log.d("PHOTO", "onError: " + error);
                    }
                });
    }
}
