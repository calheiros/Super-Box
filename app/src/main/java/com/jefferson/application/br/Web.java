/*
 * Copyright (C) 2023 Jefferson Calheiros


 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.jefferson.application.br;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.net.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.webkit.*;
import android.widget.*;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.*;
import java.net.*;

import java.lang.Process;

public class Web extends Activity {   

    SwipeRefreshLayout mySwipeRefreshLayout;

    Runnable runnable;
	boolean proceder;
	Handler	handler;
    WebView webView;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.web);

        webView = (WebView)findViewById(R.id.pagina);
		webView.setLongClickable(true);
		webView.loadUrl("http://www.google.com/");
		webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);     
		mySwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh);


		mySwipeRefreshLayout.setOnRefreshListener(
			new SwipeRefreshLayout.OnRefreshListener() {

				private String LOG_TAG;
				@Override
				public void onRefresh() {

					Log.i(LOG_TAG, "onRefresh called from SwipeRefreshLayout");
					// This method performs the actual data-refresh operation.
					// The method calls setRefreshing(false) when it's finished.
					webView.reload();
				}});

		webView.setWebViewClient(new WebViewClient(){

				@Override
				public void onPageStarted(WebView view, String url, Bitmap favicon) {

					Toast.makeText(getApplicationContext(), "iniciou", Toast.LENGTH_LONG).show();
				}

				@Override public
				void onPageFinished(WebView view, String url) {  mySwipeRefreshLayout.setRefreshing(false);
                }
            }
        );
    }
    
	public static Bitmap getBitmapFromURL(String imgUrl) {

        try {
			URL url = new URL(imgUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			Bitmap myBitmap = BitmapFactory.decodeStream(input);
			return myBitmap;
		} catch (IOException e) {
			// Log exception
			return null;
		}
	}

	private void down() {

		/*****DOWNLOAD FILE*****/
		DownloadManager.Request request = new DownloadManager.Request(Uri.parse("https://github.com/pelya/android-keyboard-gadget/blob/master/hid-gadget-test/hid-gadget-test?raw=true"));
		request.setDescription("hid-gadget-test");
		request.setTitle("hid-gadget-test");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			request.allowScanningByMediaScanner();
			request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
		}
		request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "hid-gadget-test"); /*****SAVE TO DOWNLOAD FOLDER*****/


		DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
		manager.enqueue(request);

		File mvfile = new File("/sdcard/" + Environment.DIRECTORY_DOWNLOADS + "/hid-gadget-test");
		while (!mvfile.exists()) {}  /*****WAIT UNTIL DOWNLOAD COMPLETE*****/
		try {
			Thread.sleep(5000);
		} catch (InterruptedException ignored) {}


		try {  /*****RUN MV-COMMAND TO MOVE TO ROOT DIR*****/
			Process su = Runtime.getRuntime().exec("su");
			DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());

			outputStream.writeBytes("mv /sdcard/" + Environment.DIRECTORY_DOWNLOADS + "/hid-gadget-test /data/local/tmp/hid-gadget-test\n");
			outputStream.flush();

			outputStream.writeBytes("exit\n");
			outputStream.flush();
			su.waitFor();
		} catch (IOException e) {
			Toast.makeText(getApplicationContext(), "IOException", Toast.LENGTH_SHORT).show();
		} catch (InterruptedException e) {
			Toast.makeText(getApplicationContext(), "InterruptedException", Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	public void onBackPressed() {
		if (webView.canGoBack()) {
            webView.goBack();
		}
        webView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    if (webView.getUrl().endsWith(".jpg") || webView.getUrl().endsWith(".png")) {
                        DownloadImage(webView.getUrl());

                    } else {
                        return true;
                    }
                    return false;}});

    }
    private void DownloadImage(String url) {

        Uri source = Uri.parse(url);
        // Make a new request pointing to the mp3 url
        DownloadManager.Request request = new DownloadManager.Request(source);
        // Use the same file name for the destination
        File destination=new File(Environment.getExternalStorageDirectory(), File.separator);
        File destinationFile = new File(destination, source.getLastPathSegment());
        request.setDestinationUri(Uri.fromFile(destinationFile));
        // Add it to the manager
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        manager.enqueue(request);
    }
}
