package com.ljp.image;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ImageLoader {
	public ImageView iv;
	public TextView tv;
	public ProgressBar pb;
	public Context context;
	String SAVE_PATH = "/test/cache/";
/**
 * 
 * @param context
 * @param iv  图片
 * @param tv 进度值显示
 * @param pb 进度条
 */
	public ImageLoader(Context context, ImageView iv, TextView tv,
			ProgressBar pb) {
		this.context = context;
		this.iv = iv;
		this.tv = tv;
		this.pb = pb;
	}

	public void LoadImage(final String url) {
		new Thread() {//启动一个线程
			public void run() {
				Message msg = UIHandler.obtainMessage();
				Bitmap bitmap = null;
				//下载图片 进度更新  进度数字更新
				boolean isLoad = loadImageFromUrl(context, url, SAVE_PATH);
				percentHandler.sendEmptyMessage(-1);
				if (isLoad) {
					bitmap = BitmapFactory.decodeFile(getFileCache(context, SAVE_PATH)+ "/" + url.hashCode());
				}
				msg.obj = bitmap;
				UIHandler.sendMessage(msg);//最后适配图片
			}
		}.start();
	}

	Handler UIHandler = new Handler() {
		public void handleMessage(Message msg) {
			Bitmap bitmap = (Bitmap) msg.obj;
			if (bitmap != null) {
				iv.setImageBitmap((Bitmap) msg.obj);
				pb.setVisibility(8);
				tv.setText("complete");
			} else {
				tv.setText("fail");
			}
		}
	};

	Handler percentHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == -1)
				tv.setText("Ready to show");
			else
				tv.setText("percent:" + msg.what + "%");
		}
	};
	
	public File getFileCache(Context context,String saveUrl) {
		// Find the dir to save cached images
		File cacheDir;
		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED))
			cacheDir = new File(android.os.Environment.getExternalStorageDirectory(),saveUrl);
		else
			cacheDir = context.getCacheDir();
		if (!cacheDir.exists())
			cacheDir.mkdirs();
		return cacheDir;
	}
	    
	public boolean loadImageFromUrl(Context context, String imageUrl,
			String saveUrl) {
		InputStream is = null;
		File cacheDir = getFileCache(context,saveUrl);

		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(imageUrl);
		HttpResponse response;
		try {
			response = client.execute(get);

			HttpEntity entity = response.getEntity();
			float length = entity.getContentLength();

			is = entity.getContent();
			FileOutputStream fos = null;
			if (is != null) {
				fos = new FileOutputStream(cacheDir + "/" + imageUrl.hashCode());
				byte[] buf = new byte[1024];
				int ch = -1;
				float count = 0;
				int newPercent = 0, oldPercent = 0;
				while ((ch = is.read(buf)) != -1) {
					fos.write(buf, 0, ch);
					count += ch;
					newPercent = (int) (count * 100 / length);
					if (newPercent > oldPercent) {
						pb.setProgress(newPercent);//设置进度
						percentHandler.sendEmptyMessage(newPercent);//进度tv更新
					}
					oldPercent = newPercent;
				}
			}
			fos.flush();
			if (fos != null) {
				fos.close();
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

}