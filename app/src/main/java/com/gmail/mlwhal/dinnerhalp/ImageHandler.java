package com.gmail.mlwhal.dinnerhalp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

//import kotlin.Metadata;

/**
 * Created by marika on 1/19/17.
 * Written to hold static methods for resizing, downsampling and rotating image files picked by the
 * user and stored in the DinnerHalp user database.
 */

class ImageHandler {

    private static final String TAG = ImageHandler.class.getSimpleName();

    static long getImageWidthPref(Context ctx, int imageScalePref) {
        //Get width of current device window
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        assert windowManager != null;
        windowManager.getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;

        //Multiply device width by mImageScalePref to get preferred size for image
        long imageWidthPref = Math.round(width * (imageScalePref * 0.01));
//        Log.d(TAG, "Screen width is " + width + " pixels");
//        Log.d(TAG, "Scale factor is " + imageScalePref);
//        Log.d(TAG, "Preferred image width is " + imageWidthPref);
        return imageWidthPref;
    }

    //Method to downsample large images read from Uri before loading into ImageView
    static Bitmap resizeImage(Context ctx, Uri selectedImage, long REQUIRED_SIZE)
            throws FileNotFoundException {

//        Log.d(TAG, "Resizing image...");
        Bitmap scaledBitmap = null;
        try {
            /* Decode image size
             * Helpful code was found here:
             * http://stackoverflow.com/questions/2507898/how-to-pick-an-image-from-gallery-sd-card-for-my-app
             */

            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(ctx.getContentResolver().openInputStream(selectedImage), null, o);

            // Find the correct scale value. It should be the power of 2.
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (width_tmp / 2 >= REQUIRED_SIZE && height_tmp / 2 >= REQUIRED_SIZE) {
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            //Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;

            scaledBitmap = BitmapFactory.decodeStream(ctx.getContentResolver().openInputStream(selectedImage),
                    null, o2);

        } catch (FileNotFoundException e) {
            Log.d(TAG, Log.getStackTraceString(e));
            Toast.makeText(ctx, ctx.getResources().getString(R.string.toast_filenotfound),
                    Toast.LENGTH_LONG).show();
        }

        return scaledBitmap;

    }

    //Method to downsample large images read from byte array before loading into ImageView
    static Bitmap resizeByteArray (Context ctx, byte[] imageByteArray, long REQUIRED_SIZE) {
        Bitmap scaledBitmap;

        //Todo: Does this need to be inside try block, as for Uri decoding?
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length, o);

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (width_tmp / 2 >= REQUIRED_SIZE && height_tmp / 2 >= REQUIRED_SIZE) {
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        //Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;

        scaledBitmap = BitmapFactory.decodeByteArray(imageByteArray, 0,
                imageByteArray.length, o2);
        return scaledBitmap;
    }

    //Method to rotate images if needed before loading into ImageView
    //Thanks to the metadata-extractor library: https://github.com/drewnoakes/metadata-extractor
    //Note that images loaded from byte arrays lose their metadata and can't be rotated,
    //so there's no need for a rotateByteArray method
    static Bitmap rotateImage(Context ctx, Uri selectedImage, Bitmap selectedBitmap)
            throws FileNotFoundException {

//        Log.d(TAG, "rotateImage: Rotating image...");
        Bitmap rotatedBitmap = null;

        try {
            //Read rotation metadata from Uri stream
            InputStream inStream = ctx.getContentResolver().openInputStream(selectedImage);
            Metadata metadata = ImageMetadataReader.readMetadata(inStream);
            //Obtain the Exif directory
            ExifIFD0Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
//            Log.d(TAG, "Directory is " + directory);

            //Check whether there is Exif metadata for the image
            if (directory != null && directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
                //Get the tag's value
                int rotation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
//                Log.d(TAG, "Rotation value of the image is " + rotation);

                //Rotate the matrix for the image based on Exif rotation value
                Matrix matrix = new Matrix();
                switch (rotation) {
                    case 1:
                        break;
                    case 3:
                        matrix.postRotate(180);
                        break;
                    case 6:
                        matrix.postRotate(90);
                        break;
                    case 8:
                        matrix.postRotate(270);
                        break;
                }

                rotatedBitmap = Bitmap.createBitmap(selectedBitmap, 0, 0,
                        selectedBitmap.getWidth(),
                        selectedBitmap.getHeight(),
                        matrix, true);
            } else {
                //No need to run rotation calculation if Exif is missing (avoids NullPointerException)
                //or rotation metadata is missing (avoids MetadataException)
//                Log.d(TAG, "No rotation calc run; bitmap returned unchanged");
                return selectedBitmap;
            }

        } catch (FileNotFoundException | ImageProcessingException e) {
            Log.d(TAG, Log.getStackTraceString(e));
            Toast.makeText(ctx, ctx.getResources().getString(R.string.toast_filenotfound),
                    Toast.LENGTH_LONG).show();
        } catch (IOException | MetadataException e) {
            Log.d(TAG, Log.getStackTraceString(e));
            Toast.makeText(ctx, ctx.getResources().getString(R.string.toast_exception),
                    Toast.LENGTH_LONG).show();

        }

        return rotatedBitmap;
    }

}
