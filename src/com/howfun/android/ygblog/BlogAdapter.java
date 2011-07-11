package com.howfun.android.ygblog;

import java.util.List;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class BlogAdapter extends ArrayAdapter<Blog> {
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
         holder.date = (TextView) convertView.findViewById(R.id.date);
         convertView.setTag(holder);
      } else {
         holder = (ViewHolder) convertView.getTag();
      }

      holder.title.setText(item.getTitle());
      holder.date.setText(item.getPostDate());

      return convertView;
   }

   private static class ViewHolder {
      TextView title;
      TextView date;
   }

}
