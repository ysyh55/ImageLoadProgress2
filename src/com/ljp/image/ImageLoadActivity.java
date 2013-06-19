package com.ljp.image;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ImageLoadActivity extends Activity {
    /** Called when the activity is first created. */
	ImageView iv ;ProgressBar pb;
	Button btn;TextView tv;
	String img_url = "http://img1.moko.cc/users/0/2/808/post/f0/img1_src_7743078.jpg";
	String SAVE_PATH = "/connotation/cache/";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        iv = (ImageView) findViewById(R.id.weibo_img);
        btn = (Button) findViewById(R.id.btn);
        pb = (ProgressBar) findViewById(R.id.load_pb);
        tv = (TextView) findViewById(R.id.percent_tv);

        btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO step3
				new ImageLoader(ImageLoadActivity.this, iv, tv, pb).LoadImage(img_url);

			}
		});
       
			
    }

	Handler UIHandler = new Handler() {
		public void handleMessage(Message msg) {
			iv.setImageBitmap((Bitmap) msg.obj);
			pb.setVisibility(8);
		}
	};

	public boolean loadImageFromUrl(Context context, String imageUrl,
			String saveUrl) {
		InputStream is = null;

		File cacheDir = new File(android.os.Environment.getExternalStorageDirectory(),
					saveUrl);

		if (!cacheDir.exists() && !cacheDir.isDirectory()) {
			cacheDir.mkdirs();
		}
		 
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
				int newPersent = 0, oldPersent = 0;
				while ((ch = is.read(buf)) != -1) {
					fos.write(buf, 0, ch);
					count += ch;
					newPersent = (int) (count * 100 / length);
					if (newPersent > oldPersent && pb != null){
						pb.setProgress(newPersent);
				    	percentHandler.sendEmptyMessage(newPersent);
					}
					oldPersent = newPersent;
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
	Handler percentHandler = new Handler() {
		public void handleMessage(Message msg) {
			tv.setText("当前进度:"+msg.what+"%");
		}
	};
}