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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {
   private static final int MSG_REFRESH = 1;
   private static final String URL = "http://www.williamlong.info/";
   private static final String TAG = "MainActivity";

   private TextView mTextView = null;
   private ListView mList = null;
   private ProgressDialog mProgress;

   private BlogAdapter mAdapter = null;
   private ArrayList<Blog> mBlogList = new ArrayList<Blog>();
   private Handler mHandler = new Handler() {
      @Override
      public void handleMessage(Message msg) {
         switch (msg.what) {
         case MSG_REFRESH:
            mProgress.dismiss();
            mAdapter.notifyDataSetChanged();
            mList.setSelection(0);
            break;

         default:
            break;
         }
      }
   };

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);
      findViews();
      mAdapter = new BlogAdapter(this, R.layout.list_item, mBlogList);
      mList.setAdapter(mAdapter);
      mProgress = ProgressDialog.show(this, "", "loading,please wait", true);
      new Thread() {
         public void run() {
            refreshBlog();
         }
      }.start();
   }

   private void findViews() {
      mTextView = (TextView) findViewById(R.id.text);
      mList = (ListView) findViewById(R.id.list);
   }

   private void refreshBlog() {
      try {
         DefaultHttpClient httpClient = new DefaultHttpClient();
         HttpGet httpGet = new HttpGet(URL);
         ResponseHandler<String> responseHandler = new BasicResponseHandler();
         String responseBody = httpClient.execute(httpGet, responseHandler);
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

               mBlogList.add(blog);
            }
         }
         Message msg = new Message();
         msg.what = MSG_REFRESH;
         msg.arg1 = items.length;
         mHandler.sendMessage(msg);
      } catch (ClientProtocolException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
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
}