package com.howfun.android.ygblog;

import android.util.Log;

public final class Utils {

   public static void log(String tag, String info) {
      if (BlogApplication.DEBUG) {
         Log.d("YGBlog >>>>>>>>>" + tag, "-------->" + info);
      }
   }
}
