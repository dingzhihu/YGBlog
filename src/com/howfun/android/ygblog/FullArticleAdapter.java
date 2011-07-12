package com.howfun.android.ygblog;

import java.util.ArrayList;
import java.util.List;

import com.howfun.android.ygblog.FullArticle.ContentItem;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FullArticleAdapter extends BaseAdapter {
   private static final String TAG = "FullArticleAdapter";
   private LayoutInflater mInflater;
   private Context mContext;
   private int mResource;

   private ArrayList<ContentItem> mAllContentList;
   
   public FullArticleAdapter(Context context, List<ContentItem> allContentList) {
      super();
      mContext = context;
      mInflater = (LayoutInflater) context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
   }

   @Override
   public View getView(int position, View convertView, ViewGroup parent) {
      ViewHolder holder;

      if (convertView == null) {
         TextView text = new TextView(mContext);
         text.setText(mAllContentList.get(position).getmContent());
         
         convertView = text;
      } else {
      }

      return convertView;
   }

   private static class ViewHolder {
      TextView title;
      TextView outline;
      ImageView thumbnail;
   }

   @Override
   public int getCount() {
      if (mAllContentList != null) {
         Utils.log(TAG, "count = " + mAllContentList.size());
         return mAllContentList.size();
      }
      return 0;
   }

   @Override
   public Object getItem(int position) {
      return position;
   }

   @Override
   public long getItemId(int position) {
      return position;
   }

}
