package com.howfun.android.ygblog;

import java.io.IOException;
import java.util.ArrayList;

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
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;

public class FullArticle extends Activity{
   
   private static final String TAG = "FullArticle";

   protected static final int MSG_REFRESH_DONE = 0;

   private ArrayList<ContentItem> mAllContentList;
  
   private ArrayList<ImgItem> mImgList;
   
   private ProgressDialog mProgress;
   
   private String mTitleStr;
   private TextView mTitleView; 
   
   private ListView mContentListView;
   private FullArticleAdapter mAdapter;
   
   
   
   private Handler mHandler = new Handler() {
      @Override
      public void handleMessage(Message msg) {
         switch (msg.what) {
         case MSG_REFRESH_DONE:
            mProgress.dismiss();
            
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
      setContentView(R.layout.full_article);
      
      init();
   }
   
   private void init() {
      
      mTitleStr = "";
      
      mAllContentList = new ArrayList<ContentItem>();
      mImgList = new ArrayList<ImgItem>();
      
      String url = "";
      url = this.getIntent().getStringExtra(Utils.FULL_URL_REF);
      
      mProgress = ProgressDialog.show(this, "", "Loading...");
      
//      mAdapter = new FullArticleAdapter(this, mAllContentList);
      
      findViews();
      
      parseContent(url);
      
      showContent();
      
   }
   
   private void findViews() {
      mTitleView = (TextView) findViewById(R.id.full_article_title);
      mContentListView = (ListView)findViewById(R.id.full_article_list);
      
//      if (mContentListView != null) {
//         mContentListView.setAdapter(mAdapter);
//      }
      
   }
   
   private void parseContent(String Url) {
      try {
         
            Utils.log(TAG, "url = " + Url);
            
         DefaultHttpClient httpClient = new DefaultHttpClient();
         HttpGet httpGet = new HttpGet(Url);
         ResponseHandler<String> responseHandler = new BasicResponseHandler();
         String responseBody = httpClient.execute(httpGet, responseHandler);
         HtmlCleaner cleaner = new HtmlCleaner();
         TagNode tagNode = cleaner.clean(responseBody);
         Object[] items = tagNode
               .evaluateXPath("//hl[@class='post-title']");
//         Object[] items = tagNode.evaluateXPath("/head/title[1]"); 
         
         if (items.length > 0) {
            mTitleStr = ((TagNode)items[0]).getText().toString();
            Utils.log(TAG, "title = " + mTitleStr);
         }
         
         
         items = tagNode //div id="artibody"
               .evaluateXPath("//div[@id='artibody']/p");
         if (items.length > 0) {
            int count = items.length - 1; 
            Utils.log(TAG, "p count = " + count);
            
            for (int i = 0; i < count; i++) {
               TagNode paragraph = (TagNode)items[i];
               //TODO:Add text or image
               
               //Test :
               String content = paragraph.getText().toString();
               ContentItem listItem = new ContentItem();
               listItem.setmType(ContentItem.TYPE_TEXT_CONTENT);
               listItem.setmContent(content);
               mAllContentList.add(listItem);
               
               Utils.log(TAG, "content ="  + content);
               
            }
         }
         mHandler.sendEmptyMessage(MSG_REFRESH_DONE);
      } catch (ClientProtocolException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } catch (XPatherException e) {
         e.printStackTrace();
      }
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
      
      //Type is text or Image
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
