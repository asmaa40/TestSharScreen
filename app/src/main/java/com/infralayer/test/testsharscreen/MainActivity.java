package com.infralayer.test.testsharscreen;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {
    private MediaProjectionManager projectionManager;
    private MediaProjection mediaProjection;
    private static final int REQUEST_CODE = 1;
    private boolean projectionStarted;
    Button takeScreenShot;
    ImageView capturedScreen;
    private int displayWidth;
    private int displayHeight;
    private ImageReader imageReader;
    StorageReference screenshotImagesRef;
    private Firebase firebase;
    Bitmap bitmap = null;
    screenShot shot;
    private Handler handler;
    byte[] encodeByte;
    Bitmap decodedBitmap;
    Button stopSharing;
    private String android_id;
    private int isSender;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Firebase.setAndroidContext(this);
        isSender=0;
         android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
        Log.i("&&&&&&&&&&",android_id);
        capturedScreen = (ImageView) findViewById(R.id.iv_capturedImage);

        //initiate firebase connections
        firebase = new Firebase("https://testsharescreen.firebaseio.com/");
        //remove all old screenShots
        firebase.child("screenShotes").removeValue();


// retrieve every screen shot added
        firebase.child("screenShotes")
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        //  Log.i("==========",dataSnapshot.child("pic").toString());
                        if(isSender==0) {
                            decodeBase64toImage(dataSnapshot.child("pic").getValue().toString());


                            Firebase ref = new Firebase("https://firelab.firebaseio.com/" + dataSnapshot.child("pic").getKey());
                            //    Log.i("))))))))",ref.toString());
                            ref.removeValue();
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        Log.i("+++++++++",dataSnapshot.getValue().toString()+"is deleted ");

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
        //------------------initatite firbase storage connection ------------------

//        storage = FirebaseStorage.getInstance();
//        storageRef = storage.getReferenceFromUrl("gs://testsharescreen.appspot.com");
//        screenshotImagesRef = storageRef.child("images/screenshot.jpg");
        //------------------------------------

        //get Media Projection Service
        projectionManager = (MediaProjectionManager) getSystemService
                (Context.MEDIA_PROJECTION_SERVICE);

        takeScreenShot = (Button) findViewById(R.id.but_takeScreenShot);
        stopSharing=(Button)findViewById(R.id.stop_projection);

        takeScreenShot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isSender=1;
                startActivityForResult(projectionManager.createScreenCaptureIntent(), REQUEST_CODE);

            }
        });

        stopSharing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaProjection.stop();
                firebase.onDisconnect();
                capturedScreen.setImageBitmap(null);

            }
        });

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                handler = new Handler();
            Looper.loop();
            }
        }.start();
    }

    private void decodeBase64toImage(String encodedString) {
        try {

            encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
             decodedBitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
              capturedScreen.setImageBitmap(decodedBitmap);
            stopSharing.setVisibility(View.GONE);
            takeScreenShot.setVisibility(View.GONE);
            //bitmap.recycle();
            if (bitmap != null && !bitmap.isRecycled())
                bitmap.recycle();


        } catch (Exception e) {
            e.getMessage();

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {

            mediaProjection = projectionManager.getMediaProjection(resultCode, data);
            if (mediaProjection != null) {

                projectionStarted = true;

                Log.i("**********", "onActivity result ");
// Initialize the media projection
                DisplayMetrics metrics = getResources().getDisplayMetrics();
                int density = metrics.densityDpi;
                int flags = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY
                        | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;

                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);

                displayWidth = size.x;
                displayHeight = size.y;

                imageReader = ImageReader.newInstance(displayWidth, displayHeight
                        , PixelFormat.RGBA_8888, 2);
                mediaProjection.createVirtualDisplay("screencap",
                        displayWidth, displayHeight, density,
                        flags, imageReader.getSurface(), null, handler);
                imageReader.setOnImageAvailableListener(new ImageAvailableListener(), handler);
            }
        }
    }


    private class ImageAvailableListener implements ImageReader.OnImageAvailableListener {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = null;
            FileOutputStream fos = null;


            ByteArrayOutputStream stream = null;

            try {
                image = imageReader.acquireLatestImage();
                if (image != null) {
                    Image.Plane[] planes = image.getPlanes();
                    ByteBuffer buffer = planes[0].getBuffer();
                    int pixelStride = planes[0].getPixelStride();
                    int rowStride = planes[0].getRowStride();
                    int rowPadding = rowStride - pixelStride * displayWidth;

                    // create bitmap
                    bitmap = Bitmap.createBitmap(displayWidth + rowPadding / pixelStride,
                            displayHeight, Bitmap.Config.ARGB_8888);
                    bitmap.copyPixelsFromBuffer(buffer);

//
//                    runOnUiThread(new Runnable() {
//                        public void run() {
//                            capturedScreen.setImageBitmap(bitmap);
//                        }
//                    });
                    sendImageToServer(bitmap);
                    // sendImageToServerAsFile(bitmap);


                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }

                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }

                if (image != null) {
                    image.close();
                }
            }
        }
    }

    private void sendImageToServerAsFile(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos);
        byte[] data = baos.toByteArray();
        UploadTask uploadTask = screenshotImagesRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.i("************", "failed to upload file");

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                Log.i("+++++++++++++", downloadUrl.toString());
            }
        });
        firebase.addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i("==========", "We're done loading the initial " + dataSnapshot.getChildrenCount() + " items");
            }

            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    private void sendImageToServer(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos);
        byte[] data = baos.toByteArray();
        String base64Image = Base64.encodeToString(data, Base64.DEFAULT);
        shot = new screenShot(base64Image,android_id,isSender);
        firebase.child("screenShotes")
                .push().setValue(shot);
        Log.i("***************", "Stored image with length: " + data.length);

    }



}