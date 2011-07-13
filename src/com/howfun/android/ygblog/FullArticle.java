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
   private String mTitleStr;
   private TextView mTitleView;

   private ListView mContentListView;
   private FullArticleAdapter mAdapter;

   private Handler mHandler = new Handler() {
      @Override
      public void handleMessage(Message msg) {
         switch (msg.what) {
         case MSG_REFRESH_DONE:
            mHandler.removeMessages(MSG_REFRESH_TIMEOUT);
            mProgress.dismiss();
            final String mimeType = "text/html";
            final String encoding = "utf-8";
            String data = (String)msg.obj;
            mWebView.loadDataWithBaseURL(null, data, mimeType, encoding, null);
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
   
   private Thread mThread = new Thread(){
      public void run(){
         String url = getIntent().getStringExtra(Utils.BLOG_URL_REF);
         mHandler.sendEmptyMessageDelayed(MSG_REFRESH_TIMEOUT, SECONDS*1000L);
         try {
            refreshBlogBody(url);
         } catch (Exception e) {
            e.printStackTrace();
         }
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

   private void findViews() {
      mTitleView = (TextView) findViewById(R.id.full_article_title);
      mContentListView = (ListView) findViewById(R.id.full_article_list);

      mWebView = (WebView) findViewById(R.id.web_view);
   }
   
   private void init() {

      mTitleStr = "";

      mAllContentList = new ArrayList<ContentItem>();
      mImgList = new ArrayList<ImgItem>();

      String url = "";

      mProgress = ProgressDialog.show(this, "", "Loading...");

      // mAdapter = new FullArticleAdapter(this, mAllContentList);

      mThread.start();


   }



   private void refreshBlogBody(String strUrl) throws Exception {
      URL url = new URL(strUrl);
      HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
      InputStreamReader inStreamReader = new InputStreamReader(httpConn
            .getInputStream());
      BufferedReader bufReader = new BufferedReader(inStreamReader);
      String line = "";
      String data = "";
      
      
      while ((line = bufReader.readLine()) != null) {
         data += line + " ";
      }
      
      HtmlCleaner cleaner = new HtmlCleaner();
      TagNode tagNode = cleaner.clean(data);
      StringBuffer bodyBuffer = null;
      Object[] bodyNodes = tagNode.evaluateXPath("//div[@id='artibody'][1]");
      if (bodyNodes.length > 0) {
         TagNode bodyNode = (TagNode) bodyNodes[0];
         bodyBuffer = bodyNode.getText();
      }
      String datas = "<p>" + bodyBuffer.toString() +"</p>";
      Message msg = new Message();
      msg.what = MSG_REFRESH_DONE;
      msg.obj = datas;
      mHandler.sendMessage(msg);
   }

   private void showContent() {
      if (mTitleView != null) {
         mTitleView.setText(mTitleStr);
      }
      mAdapter = new FullArticleAdapter(this, mAllContentList);
      if (mContentListView != null) {
         mContentListView.setAdapter(mAdapter);
      }
      mAdapter.notifyDataSetChanged();
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
