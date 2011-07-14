package com.howfun.android.ygblog;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;
import android.util.Log;

public final class Utils {
   
   public static final String URL = "http://www.williamlong.info";

   protected static final String BLOG_ID_REF = "blog_id_ref";
   protected static final String BLOG_URL_REF = "blog_url_ref";
   protected static final String BLOG_TITLE_REF = "blog_title_ref";
   protected static final String BLOG_POSTDATE_REF = "blog_postdate_ref";

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

   public static Bitmap getBitmapByUrl(String url) {
      URL imageUrl = null;
      Bitmap bitmap = null;
      try {
         imageUrl = new URL(url);
      } catch (MalformedURLException e) {
         e.printStackTrace();
      }

      try {
         HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
         conn.setDoInput(true);
         conn.connect();
         InputStream is = conn.getInputStream();
         bitmap = BitmapFactory.decodeStream(is);
         is.close();
      } catch (IOException e) {
         e.printStackTrace();
      }
      return bitmap;
   }

   public static String getDate() {
      String date = "";
      Calendar calender = Calendar.getInstance();
      int year = calender.get(Calendar.YEAR);
      int month = calender.get(Calendar.MONTH) + 1;
      int day = calender.get(Calendar.DAY_OF_MONTH);
      date = year + "/" + month + "/" + day;
      return date;
   }

   public static String getHtml(String strUrl) {
      String html = "";
      try {
         URL url = new URL(strUrl);
         HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
         InputStreamReader inStreamReader = new InputStreamReader(httpConn
               .getInputStream());
         BufferedReader bufReader = new BufferedReader(inStreamReader);
         String line = "";
         while ((line = bufReader.readLine()) != null) {
            html += line + "\n";
         }
      } catch (MalformedURLException e) {
         e.printStackTrace();
      } catch (IOException e) {
         html = "fetch content error!";
         e.printStackTrace();
      }

      return html;
   }
}
