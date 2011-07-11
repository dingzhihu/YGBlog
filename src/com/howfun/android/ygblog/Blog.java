package com.howfun.android.ygblog;

public class Blog {

   private String mTitle = "";
   private String mPostDate = "";
   private String mOutline = "";
   private String mUrl = "";

   private String mImgUrl = "";
   private String mAuthor = "";
   private String mCategory = "";
   private String mCategoryUrl = "";

   private String mBody = "";

   private int mCommentCount = 0;
   private int mReadCount = 0;

   public Blog() {

   }

   public Blog(String title, String date, String url) {
      mTitle = title;
      mPostDate = date;
      mUrl = url;
   }

   public Blog(String title, String date, String url, String outline) {
      mTitle = title;
      mPostDate = date;
      mUrl = url;
      mOutline = outline;
   }

   public String getTitle() {
      return mTitle;
   }

   public void setTitle(String title) {
      mTitle = title;
   }

   public String getPostDate() {
      return mPostDate;
   }

   public void setPostDate(String date) {
      mPostDate = date;
   }

   public String getOutline() {
      return mOutline;
   }

   public void setOutline(String outline) {
      mOutline = outline;
   }

   public String getUrl() {
      return mUrl;
   }

   public void setUrl(String url) {
      mUrl = url;
   }

   public String getImgUrl() {
      return mImgUrl;
   }

   public void setImgUrl(String imgUrl) {
      mImgUrl = imgUrl;
   }

   public String getAuthor() {
      return mAuthor;
   }

   public void setAuthor(String author) {
      mAuthor = author;
   }

   public String getCategory() {
      return mCategory;
   }

   public void setCategory(String category) {
      mCategory = category;
   }

   public String getCategoryUrl() {
      return mCategoryUrl;
   }

   public void setCategoryUrl(String catUrl) {
      mCategoryUrl = catUrl;
   }

   public String getBody() {
      return mBody;
   }

   public void setBody(String body) {
      mBody = body;
   }

   public int getCommentCount() {
      return mCommentCount;
   }
   
   public void setCommentCount(int count){
      mCommentCount = count;
   }

   public int getReadCount() {
      return mReadCount;
   }
   
   public void setReadCount(int count){
      mReadCount = count;
   }

}
