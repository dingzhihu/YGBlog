package com.howfun.android.ygblog;

import java.lang.ref.SoftReference;
import java.util.HashMap;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;

public class AsyncImageLoader {

   private HashMap<String, SoftReference<BitmapDrawable>> imageCache;

   private BitmapDrawable mDrawable;

   public AsyncImageLoader(Context context) {
      imageCache = new HashMap<String, SoftReference<BitmapDrawable>>();

   }

   
   public Drawable loadDrawableAsync(final String imageUrl,
         final ImageCallback imageCallback) {
      BitmapDrawable drawable = null;
      if (imageCache.containsKey(imageUrl)) {
         SoftReference<BitmapDrawable> softReference = imageCache.get(imageUrl);
         drawable = softReference.get();
         if (drawable != null) {
            return drawable;
         }
      }

      // To decode async
      final Handler handler = new Handler() {
         @Override
         public void handleMessage(Message message) {
            imageCallback.imageLoaded((Drawable) mDrawable, imageUrl);
         }
      };
      new Thread() {
         @Override
         public void run() {
            // BitmapDrawable drawable = loadImageFromUrl(imageUrl);
            mDrawable = loadImageFromUrl(imageUrl);
            imageCache.put(imageUrl, new SoftReference<BitmapDrawable>(
                  mDrawable));
            Message message = handler.obtainMessage(0, null);
            handler.sendMessage(message);
         }
      }.start();
      return null;
   }

   public static BitmapDrawable loadImageFromUrl(String url) {

      BitmapDrawable bitmapDrawable = null;
      try {
         Utils.log("AsyncImageLoader", "Loading new card.......");
         bitmapDrawable = (BitmapDrawable) Drawable.createFromPath(url);
      } catch (OutOfMemoryError e) {
         Utils.log("AsyncImageLoader", "Out of memory!!!!!!!!!");
      }
      return bitmapDrawable;
   }

   public interface ImageCallback {
      public void imageLoaded(Drawable imageDrawable, String imageUrl);
   }

   

}