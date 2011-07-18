package com.howfun.android.ygblog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;

public class BlogDB {
   private static final String TAG = "Blog DB";

   private static final String DATABASE_NAME = "database";
   private static final int DATABASE_VERSION = 1;

   private static final String KEY_ROWID = "_id";
   private static final String KEY_TITLE = "title";
   private static final String KEY_URL = "url";
   private static final String KEY_POST_DATE = "postDate";
   private static final String KEY_OUTLINE = "outline";
   private static final String KEY_IMAGE_URL = "imageUrl";
   private static final String KEY_THUMBNAIL = "thumbnail";
   private static final String KEY_AUTHOR = "author";
   private static final String KEY_CATEGORY = "category";
   private static final String KEY_CATEGORY_URL = "categoryUrl";
   private static final String KEY_BODY = "body";
   private static final String KEY_COMMENT_COUNT = "commentCount";
   private static final String KEY_READ_COUNT = "readCount";

   String[] TABLE_BLOGS_COLUMNS = { KEY_ROWID, KEY_TITLE, KEY_URL,
         KEY_POST_DATE, KEY_OUTLINE, KEY_IMAGE_URL, KEY_THUMBNAIL, KEY_AUTHOR,
         KEY_CATEGORY, KEY_CATEGORY_URL, KEY_BODY, KEY_COMMENT_COUNT,
         KEY_READ_COUNT };

   private static final String TABLE_BLOGS = "blogs";

   private static final String CREATE_TABLE_BLOGS = "CREATE TABLE "
         + TABLE_BLOGS + "(" + KEY_ROWID
         + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_TITLE
         + " TEXT NOT NULL," + KEY_URL + " TEXT NOT NULL," + KEY_POST_DATE
         + " TEXT," + KEY_OUTLINE + " TEXT," + KEY_IMAGE_URL + " TEXT,"
         + KEY_THUMBNAIL + " BLOB," + KEY_AUTHOR + " TEXT," + KEY_CATEGORY
         + " TEXT," + KEY_CATEGORY_URL + " TEXT," + KEY_BODY + " TEXT,"
         + KEY_COMMENT_COUNT + " INTEGER," + KEY_READ_COUNT + " INTEGER" + ");";

   private static final String DROP_TABLE_BLOGS = "DROP TABLE IF EXISTS "
         + TABLE_BLOGS;

   private final Context mCtx;

   private DatabaseHelper mDbHelper;
   private SQLiteDatabase mDb;

   public BlogDB(Context context) {
      mCtx = context;
   }

   public BlogDB open() {
      mDbHelper = new DatabaseHelper(mCtx);
      mDb = mDbHelper.getWritableDatabase();
      return this;
   }

   public void close() {
      mDbHelper.close();
   }

   public void addBlogs(List<Blog> blogs) {
      if (blogs != null) {
         Iterator<Blog> it = blogs.iterator();
         while (it.hasNext()) {
            Blog blog = it.next();
            addBlog(blog);
         }
      }
   }

   public long addBlog(Blog blog) {
      Utils.log(TAG, "add one blog into db");
      ContentValues values = new ContentValues();
      values.put(KEY_TITLE, blog.getTitle());
      values.put(KEY_URL, blog.getUrl());
      values.put(KEY_OUTLINE, blog.getOutline());
      values.put(KEY_IMAGE_URL, blog.getImgUrl());
      Bitmap thumbnail = blog.getThumbnail();
      // put thumbnail
      values.put(KEY_THUMBNAIL, Utils.getThumbnailBlob(thumbnail));
      values.put(KEY_CATEGORY, blog.getCategory());
      values.put(KEY_CATEGORY_URL, blog.getCategoryUrl());
      values.put(KEY_BODY, blog.getBody());
      values.put(KEY_COMMENT_COUNT, blog.getCommentCount());
      values.put(KEY_READ_COUNT, blog.getReadCount());
      values.put(KEY_AUTHOR, blog.getAuthor());
      values.put(KEY_POST_DATE, blog.getPostDate());
      return mDb.insert(TABLE_BLOGS, null, values);
   }

   public List<Blog> getAllBlogs() {
      return getBlogs(0);
   }

   public List<Blog> getBlogs(int num) {
      List<Blog> blogs = new ArrayList<Blog>();
      if (num < 0) {
         return blogs;
      }

      Cursor cur = null;
      if (num == 0) { // query all
         cur = mDb.query(TABLE_BLOGS, null, null, null, null, null, null);
      } else {
         String sql = "select * from " + TABLE_BLOGS + " limit "
               + String.valueOf(num);
         cur = mDb.rawQuery(sql, null);
      }

      if (cur != null) {
         cur.moveToFirst();
      }
      if (cur.getCount() == 0) { // blog list is empty
         cur.close();
         return blogs;
      }
      do {
         Blog blog = new Blog();
         blog.setId(cur.getLong(cur.getColumnIndex(KEY_ROWID)));
         blog.setTitle(cur.getString(cur.getColumnIndex(KEY_TITLE)));
         blog.setUrl(cur.getString(cur.getColumnIndex(KEY_URL)));
         blog.setPostDate(cur.getString(cur.getColumnIndex(KEY_POST_DATE)));
         blog.setOutline(cur.getString(cur.getColumnIndex(KEY_OUTLINE)));
         blog.setImgUrl(cur.getString(cur.getColumnIndex(KEY_IMAGE_URL)));
         // thumbnail
         byte[] buffer = cur.getBlob(cur.getColumnIndex(KEY_THUMBNAIL));
         Bitmap thumbnail = Utils.getThumbnailBitmap(buffer);
         blog.setThumbnail(thumbnail);

         blog.setAuthor(cur.getString(cur.getColumnIndex(KEY_AUTHOR)));
         blog.setCategory(cur.getString(cur.getColumnIndex(KEY_CATEGORY)));
         blog.setCategoryUrl(cur
               .getString(cur.getColumnIndex(KEY_CATEGORY_URL)));
         blog.setBody(cur.getString(cur.getColumnIndex(KEY_BODY)));

         blog.setReadCount(cur.getInt(cur.getColumnIndex(KEY_READ_COUNT)));
         blog
               .setCommentCount(cur.getInt(cur
                     .getColumnIndex(KEY_COMMENT_COUNT)));
         blogs.add(blog);

      } while (cur.moveToNext());
      cur.close();

      return blogs;
   }

   public int getBlogsCount() {
      int count = 0;
      Cursor cur = mDb.query(TABLE_BLOGS, null, null, null, null, null, null);
      if (cur != null) {
         count = cur.getCount();
         cur.close();
      }
      return count;
   }

   public String getBlogBody(long id) {
      String body = "";
      Cursor cur = mDb.query(true, TABLE_BLOGS, null, KEY_ROWID + "=" + id,
            null, null, null, null, null);
      if (cur != null) {
         cur.moveToFirst();
         body = cur.getString(cur.getColumnIndex(KEY_BODY));
         cur.close();
      }
      return body;
   }

   public void setBlogBody(long id, String body) {
      if (id > 0) {
         String sql = "UPDATE " + TABLE_BLOGS + " SET " + KEY_BODY + " = "
               + "'" + body + "'" + " WHERE " + KEY_ROWID + " = " + id;
         mDb.execSQL(sql);
      }
   }

   // check blog exists or not by blog title and post date
   public boolean blogExists(String title, String postDate) {
      boolean flag = false;
      List<Blog> blogs = getAllBlogs();
      Iterator<Blog> it = blogs.iterator();
      while (it.hasNext()) {
         Blog blog = it.next();
         String blogTitle = blog.getTitle();
         String blogPostDate = blog.getPostDate();
         if (title.equals(blogTitle) && (blogPostDate.equals(postDate))) {
            flag = true;
            return flag;
         }
      }
      return flag;
   }

   private static class DatabaseHelper extends SQLiteOpenHelper {
      public DatabaseHelper(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);

      }

      @Override
      public void onCreate(SQLiteDatabase db) {
         Utils.log(TAG, "create table " + TABLE_BLOGS);
         db.execSQL(CREATE_TABLE_BLOGS);

      }

      @Override
      public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         db.execSQL(DROP_TABLE_BLOGS);
         onCreate(db);
      }

   }
}
