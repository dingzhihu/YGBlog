package com.howfun.android.ygblog;

import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public final class Utils {

   public static void log(String tag, String info) {
      if (BlogApplication.DEBUG) {
         Log.d("YGBlog >>>>>>>>>" + tag, "-------->" + info);
      }
   }

   public static Bitmap getThumbnailBitmap(byte[] buffer) {
      Bitmap thumbnail = null;
      if (buffer != null) {
         try {
            thumbnail = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
         } catch (OutOfMemoryError e) {
            e.printStackTrace();
         }
      }
      return thumbnail;
   }

   public static byte[] getThumbnailBlob(Bitmap bitmap) {
      if (bitmap != null) {
         final ByteArrayOutputStream os = new ByteArrayOutputStream();
         bitmap.compress(Bitmap.CompressFormat.PNG, 80, os);
         return os.toByteArray();
      }
      return null;
   }
}
