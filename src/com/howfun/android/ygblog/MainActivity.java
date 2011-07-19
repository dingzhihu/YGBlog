package com.howfun.android.ygblog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity {

   private static final int MSG_REFRESH_DONE = 1;
   private static final int MSG_REFRESH_TIMEOUT = 2;

   private static final String PREFERENCES = "preferences";
   private static final String TAG = "MainActivity";

   private static final String KEY_LAST_UPDATED = "lastUpdated";

   private RelativeLayout mTopBar = null;
   private RelativeLayout mBottomBar = null;
   private ImageView mRefrshView = null;
   private ImageView mInfoView = null;
   private TextView mEmptyBlogText = null;
   private TextView mInfoText = null;
   private ListView mBlogListView = null;
   private ProgressDialog mProgress;

   private Context mCtx = null;
   private BlogDB mBlogDb = null;
   private BlogAdapter mAdapter = null;
   private List<Blog> mBlogList = null;

   SharedPreferences mSettings = null;

   private Handler mHandler = new Handler() {
      @Override
      public void handleMessage(Message msg) {
         switch (msg.what) {
         case MSG_REFRESH_DONE:
            mHandler.removeMessages(MSG_REFRESH_TIMEOUT);
            mProgress.dismiss();
            mEmptyBlogText.setVisibility(View.GONE);
            mAdapter.notifyDataSetChanged();
            mBlogListView.setSelection(0);
            String updated = Utils.getDate();
            mSettings.edit().putString(KEY_LAST_UPDATED, updated).commit();
            int blogNumUpdated = msg.arg1;
            setInfo(getResources().getString(R.string.num_updated)
                  + blogNumUpdated);
            break;

         case MSG_REFRESH_TIMEOUT:

            mProgress.dismiss();
            setInfo(getResources().getString(R.string.error_updated));
            break;
         default:
            break;
         }
      }
   };

   @Override
   public void onCreate(Bundle savedInstanceState) {
      requestWindowFeature(Window.FEATURE_NO_TITLE);
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);

      findViews();
      setupListeners();
      init();
   }

   private void findViews() {
      mTopBar = (RelativeLayout) findViewById(R.id.top_bar);
      mBottomBar = (RelativeLayout) findViewById(R.id.bottom_bar);
      mRefrshView = (ImageView) findViewById(R.id.refresh_view);
      mInfoView = (ImageView) findViewById(R.id.info_view);
      mEmptyBlogText = (TextView) findViewById(R.id.empty_blog);
      mInfoText = (TextView) findViewById(R.id.info_text);
      mBlogListView = (ListView) findViewById(R.id.blog_list);
      mBlogListView.setCacheColorHint(0);
   }

   private void setupListeners() {
      if (mBlogListView != null) {
         mBlogListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                  int position, long id) {
               // TODO browse the blog
               Blog blog = (Blog) parent.getAdapter().getItem(position);

               // Intent viewIntent = new Intent("android.intent.action.VIEW",
               // Uri
               // .parse(blog.getUrl()));
               Intent intent = new Intent(MainActivity.this, FullArticle.class);
               intent.putExtra(Utils.BLOG_ID_REF, blog.getId());
               intent.putExtra(Utils.BLOG_URL_REF, blog.getUrl());
               intent.putExtra(Utils.BLOG_TITLE_REF, blog.getTitle());
               intent.putExtra(Utils.BLOG_POSTDATE_REF, blog.getPostDate());
               startActivity(intent);

            }

         });
         if (mRefrshView != null) {
            mRefrshView.setOnClickListener(new OnClickListener() {

               @Override
               public void onClick(View v) {
                  refresh();
               }
            });
         }
         
         if(mInfoView != null){
            mInfoView.setOnClickListener(new OnClickListener() {
               
               @Override
               public void onClick(View v) {
                  Utils.showMessageDlg(MainActivity.this, R.string.about);
               }
            });
         }
      }
   }

   private void init() {
      mCtx = this;
      mBlogDb = new BlogDB(mCtx);
      mBlogDb.open();
      // mBlogList = mBlogDb.getAllBlogs();
      mBlogList = mBlogDb.getBlogs(20);

      mAdapter = new BlogAdapter(this, R.layout.blog_list_item, mBlogList);
      mBlogListView.setAdapter(mAdapter);
      if (mBlogList.size() == 0) {
         mEmptyBlogText.setVisibility(View.VISIBLE);
      }

      mSettings = getSharedPreferences(PREFERENCES, 0);
      String lastUpdated = mSettings.getString(KEY_LAST_UPDATED, "");
      if (!"".equals(lastUpdated)) {
         setInfo(getResources().getString(R.string.last_updated) + lastUpdated);
      } else {
         setInfo(getResources().getString(R.string.empty_blog));
      }
   }

   private void setInfo(String info) {
      if (mInfoText != null) {
         mInfoText.setText(info);
      }
   }

   private void refresh() {
      mProgress = ProgressDialog.show(this, "", "loading,please wait", true);
      setInfo(getResources().getString(R.string.updating));

      new Thread() {
         public void run() {
            refreshBlog();
         }
      }.start();
   }

   private void refreshBlog() {
      List<Blog> newBlogList = new ArrayList<Blog>();
      String responseBody = Utils.getHtml(Utils.URL);

      if ("".equals(responseBody)) {
         mHandler.sendEmptyMessage(MSG_REFRESH_TIMEOUT);
         return;
      }
      try {
         HtmlCleaner cleaner = new HtmlCleaner();
         TagNode tagNode = cleaner.clean(responseBody);
         Object[] items = tagNode
               .evaluateXPath("//div[@id='divMain']/div[@class]");
         if (items.length > 0) {
            int count = items.length - 1; // blog num
            for (int i = 0; i < count; i++) {
               Blog blog = new Blog();
               String date = "";
               String title = "";
               String url = "";
               String outline = "";
               String imgUrl = "";
               String footer = "";
               String category = "";
               String categoryUrl = "";
               String author = "";
               int commentCount = 0;
               int readCount = 0;

               TagNode item = (TagNode) items[i];
               Object[] dateNodes = item
                     .evaluateXPath("/h4[@class='post-date'][1]");
               TagNode dateNode = (TagNode) dateNodes[0];
               date = dateNode.getText().toString();
               blog.setPostDate(date);

               Object[] titleNodes = item
                     .evaluateXPath("/h2[@class='post-title']/a[1]");
               TagNode titleNode = (TagNode) titleNodes[0];
               title = titleNode.getText().toString();
               url = titleNode.getAttributeByName("href").toString().trim();
               blog.setTitle(title);
               blog.setUrl(url);

               Object[] outlineNodes = item.evaluateXPath("/div[1]");
               TagNode outlineNode = (TagNode) outlineNodes[0];
               outline = outlineNode.getText().toString().trim();
               blog.setOutline(outline);

               Object[] imgUrlNodes = item.evaluateXPath("/div/a/img[1]");
               TagNode imgUrlNode = (TagNode) imgUrlNodes[0];
               imgUrl = imgUrlNode.getAttributeByName("src").toString();
               blog.setImgUrl(imgUrl);
               // get image thumbnail

               Object[] categoryNodes = item
                     .evaluateXPath("/h6[@class='post-footer']/a[1]");
               TagNode categoryNode = (TagNode) categoryNodes[0];
               category = categoryNode.getText().toString();
               categoryUrl = categoryNode.getAttributeByName("href").toString()
                     .trim();
               blog.setCategory(category);
               blog.setCategoryUrl(categoryUrl);

               Object[] footerNodes = item
                     .evaluateXPath("/h6[@class='post-footer'][1]");
               TagNode tempNode = (TagNode) footerNodes[0];
               footer = tempNode.getText().toString();
               author = getAuthor(footer);
               commentCount = getCommentCount(footer);
               readCount = getReadCount(footer);
               blog.setAuthor(author);
               blog.setCommentCount(commentCount);
               blog.setReadCount(readCount);
               if (!mBlogDb.blogExists(title, date)) {
                  // Utils.log(TAG, "blog:"+title+"  does not exist");
                  blog.setThumbnail(Utils.getBitmapByUrl(imgUrl)); // slow

                  long id = mBlogDb.addBlog(blog);
                  blog.setId(id);
                  newBlogList.add(blog);
               }
            }
            List<Blog> tempBlogList = new ArrayList<Blog>();
            tempBlogList.addAll(mBlogList);
            mBlogList.clear();
            mBlogList.addAll(newBlogList);
            mBlogList.addAll(tempBlogList);
         }
         Message msg = new Message();
         msg.what = MSG_REFRESH_DONE;
         msg.arg1 = newBlogList.size();
         mHandler.sendMessage(msg);
      } catch (XPatherException e) {
         e.printStackTrace();
      }
   }

   private String getAuthor(String footer) {
      int start = footer.indexOf("作者");
      int end = footer.indexOf("|", start);
      String temp = footer.substring(start, end).trim();
      String author = temp.substring(3);
      return author;
   }

   private int getCommentCount(String footer) {
      int start = footer.indexOf("评论");
      String str = footer.substring(start);

      int end = str.indexOf("|");
      String temp = str.substring(0, end).trim();
      String comment = temp.substring(3);
      return Integer.parseInt(comment);
   }

   private int getReadCount(String footer) {
      int start = footer.indexOf("浏览");
      String str = footer.substring(start);

      int end = str.indexOf("|");
      String temp = str.substring(0, end).trim();
      String comment = temp.substring(3);
      return Integer.parseInt(comment);
   }

   public void onDestroy() {
      super.onDestroy();
      mBlogDb.close();
   }
}