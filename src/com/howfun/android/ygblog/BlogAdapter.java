package com.howfun.android.ygblog;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BlogAdapter extends ArrayAdapter<Blog> {
   private static final String TAG = "BlogAdapter";
   private LayoutInflater mInflater;
   private Context mContext;
   private int mResource;

   public BlogAdapter(Context context, int resource, List<Blog> objects) {
      super(context, resource, objects);
      mContext = context;
      mResource = resource;
      mInflater = (LayoutInflater) context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
   }

   @Override
   public View getView(int position, View convertView, ViewGroup parent) {
      ViewHolder holder;

      Blog item = getItem(position);

      if (convertView == null) {
         convertView = mInflater.inflate(mResource, parent, false);
         holder = new ViewHolder();
         holder.title = (TextView) convertView.findViewById(R.id.title);
         holder.outline = (TextView) convertView.findViewById(R.id.outline);
         holder.thumbnail = (ImageView) convertView.findViewById(R.id.image);
         convertView.setTag(holder);
      } else {
         holder = (ViewHolder) convertView.getTag();
      }

      holder.title.setText(item.getTitle());
      holder.outline.setText(item.getOutline());
      holder.thumbnail.setImageBitmap(item.getThumbnail());

      return convertView;
   }

   private static class ViewHolder {
      TextView title;
      TextView outline;
      ImageView thumbnail;
   }

}
