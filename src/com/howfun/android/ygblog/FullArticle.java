package com.howfun.android.ygblog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class FullArticle extends Activity {

   private static final String TAG = "FullArticle";

   protected static final int MSG_REFRESH_DONE = 0;
   protected static final int MSG_REFRESH_TIMEOUT = 1;

   private static final int SECONDS = 10;
   private ArrayList<ContentItem> mAllContentList;

   private ArrayList<ImgItem> mImgList;

   private ProgressDialog mProgress;

   private WebView mWebView = null;
   private TextView mTitleText = null;
   private TextView mPostdateText = null;
   
   private TextView mBodyText = null;

   private ListView mContentListView;
   private FullArticleAdapter mAdapter;

   private BlogDB mBlogDb = null;

   private Handler mHandler = new Handler() {
      @Override
      public void handleMessage(Message msg) {
         switch (msg.what) {
         case MSG_REFRESH_DONE:
            mHandler.removeMessages(MSG_REFRESH_TIMEOUT);
            String body = (String) msg.obj;
            mBlogDb.setBlogBody(getIntent().getLongExtra(Utils.BLOG_ID_REF, 0),
                  body);
            setBlogTitle(getIntent().getStringExtra(Utils.BLOG_TITLE_REF));
            setBlogPostdate(getIntent().getStringExtra(Utils.BLOG_POSTDATE_REF));
            setBlogBody(body);
            mProgress.dismiss();
            break;

         case MSG_REFRESH_TIMEOUT:
            mThread.interrupt();
            mProgress.dismiss();
            break;
         default:
            break;
         }
      }
   };

   private Thread mThread = new Thread() {
      public void run() {
         String url = getIntent().getStringExtra(Utils.BLOG_URL_REF);
         // mHandler.sendEmptyMessageDelayed(MSG_REFRESH_TIMEOUT, SECONDS *
         // 1000L);
         refreshBlogBody(url);
      }
   };

   @Override
   public void onCreate(Bundle savedInstanceState) {
      requestWindowFeature(Window.FEATURE_NO_TITLE);
      super.onCreate(savedInstanceState);
      setContentView(R.layout.full_article);
      findViews();
      init();
   }

   public void onDestroy() {
      super.onDestroy();
      mBlogDb.close();
   }

   private void findViews() {
      mTitleText = (TextView) findViewById(R.id.blog_title);
      mPostdateText = (TextView) findViewById(R.id.blog_postdate);
      mWebView = (WebView) findViewById(R.id.blog_body);
      
      mBodyText = (TextView) findViewById(R.id.blog_article);
   }

   private void init() {
      mBlogDb = new BlogDB(this);
      mBlogDb.open();
      
      long blogId = getIntent().getLongExtra(Utils.BLOG_ID_REF, 0);
      String body = "";
      if (blogId > 0) {
         body = mBlogDb.getBlogBody(blogId);
      }
      if ("".equals(body)) {
         mProgress = ProgressDialog.show(this, "", "Loading...");
         mThread.start();
      } else {
         setBlogTitle(getIntent().getStringExtra(Utils.BLOG_TITLE_REF));
         setBlogPostdate(getIntent().getStringExtra(Utils.BLOG_POSTDATE_REF));
         setBlogBody(body);
      }

   }

   private void setBlogBody(String body) {
      final String mimeType = "text/html";
      final String encoding = "utf-8";
      mWebView.loadDataWithBaseURL(null, body, mimeType, encoding, null);
//      mBodyText.setText(body);
   }

   private void setBlogTitle(String title) {
      mTitleText.setText(title);
   }

   private void setBlogPostdate(String date) {
      mPostdateText.setText(date);
   }

   private void refreshBlogBody(String strUrl) {

      String rawBody = Utils.getHtml(strUrl);
      HtmlCleaner cleaner = new HtmlCleaner();
      TagNode tagNode = cleaner.clean(rawBody);
      String body = null;
      Object[] bodyNodes = {};
      try {
         bodyNodes = tagNode.evaluateXPath("//div[@id='artibody'][1]");
      } catch (XPatherException e) {
         e.printStackTrace();
      }
      if (bodyNodes.length > 0) {
         TagNode bodyNode = (TagNode) bodyNodes[0];
         body = bodyNode.getText().toString();
      }
      String datas = body;
      Message msg = new Message();
      msg.what = MSG_REFRESH_DONE;
      msg.obj = datas;
      mHandler.sendMessage(msg);
   }

   public class ContentItem {

      public static final int TYPE_TEXT_CONTENT = 1;
      public static final int TYPE_IMAGE_CONTENT = 2;

      // Type is text or Image
      private int mType;
      private String mContent;
      private ImgItem mImgItem;

      public ContentItem(int type) {
         setmType(type);
      }

      public ContentItem() {
         // TODO Auto-generated constructor stub
      }

      public void setmType(int mType) {
         this.mType = mType;
      }

      public int getmType() {
         return mType;
      }

      public void setmContent(String mContent) {
         this.mContent = mContent;
      }

      public String getmContent() {
         return mContent;
      }

      public void setmImgItem(ImgItem mImgItem) {
         this.mImgItem = mImgItem;
      }

      public ImgItem getmImgItem() {
         return mImgItem;
      }

   }

   private class ImgItem {
      private String mUrl;
      private Bitmap mBitmap;

      public ImgItem(String url) {
         mUrl = url;
      }

      public void setmBitmap(Bitmap mBitmap) {
         this.mBitmap = mBitmap;
      }

      public Bitmap getmBitmap() {
         return mBitmap;
      }

      public void setmUrl(String mUrl) {
         this.mUrl = mUrl;
      }

      public String getmUrl() {
         return mUrl;
      }

   }

}
