package com.jefferson.application.br;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.net.*;
import android.os.*;
import android.support.v4.widget.*;
import android.util.*;
import android.view.*;
import android.webkit.*;
import android.widget.*;
import java.io.*;
import java.net.*;

import java.lang.Process;

public class Web extends Activity
{   SwipeRefreshLayout   mySwipeRefreshLayout;
	Runnable runnable;
	boolean proceder;
	Handler	handler;
WebView pagina;
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.web);
		
		 pagina=(WebView)findViewById(R.id.pagina);
		pagina.setLongClickable(true);
		pagina.loadUrl("http://www.google.com/");
		pagina.getSettings().setDomStorageEnabled(true);
        pagina.getSettings().setJavaScriptEnabled(true);     
		mySwipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swiperefresh);

		
		mySwipeRefreshLayout.setOnRefreshListener(
			new SwipeRefreshLayout.OnRefreshListener() {

				private String LOG_TAG;
				@Override
				public void onRefresh() {
					
					Log.i(LOG_TAG, "onRefresh called from SwipeRefreshLayout");

					// This method performs the actual data-refresh operation.
					// The method calls setRefreshing(false) when it's finished.
					pagina.reload();
				}});
		
		pagina.setWebViewClient(new WebViewClient(){
				
				@Override
				public void onPageStarted(WebView view, String url, Bitmap favicon)
				{
					
					Toast.makeText(getApplicationContext(),"iniciou",Toast.LENGTH_LONG).show();
				}

				@Override public
				void onPageFinished(WebView view, String url)
		
				{  mySwipeRefreshLayout.setRefreshing(false);
			}
			});}
		
	

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

	private void down(){
		
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

		File mvfile = new File("/sdcard/"+Environment.DIRECTORY_DOWNLOADS+"/hid-gadget-test");
		while (!mvfile.exists()) {}  /*****WAIT UNTIL DOWNLOAD COMPLETE*****/
		try {
			Thread.sleep(5000);
		} catch (InterruptedException ignored) {}


		try {  /*****RUN MV-COMMAND TO MOVE TO ROOT DIR*****/
			Process su = Runtime.getRuntime().exec("su");
			DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());

			outputStream.writeBytes("mv /sdcard/"+Environment.DIRECTORY_DOWNLOADS+"/hid-gadget-test /data/local/tmp/hid-gadget-test\n");
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
	public void onBackPressed()
	{
		if(pagina.canGoBack()){
			
			pagina.goBack();
		}
		
	
	pagina.setOnLongClickListener(new View.OnLongClickListener() {
	@Override
	public boolean onLongClick(View v) {
		
		if (pagina.getUrl().endsWith(".jpg")||pagina.getUrl().endsWith(".png")){
			DownloadImage(pagina.getUrl());
		
		} else{
			return true;
		}
		return false;}});
		
		}
		private void DownloadImage(String url){
			

				Uri source = Uri.parse(url);
				// Make a new request pointing to the mp3 url
				DownloadManager.Request request = new DownloadManager.Request(source);
				// Use the same file name for the destination
				File destination=new File(Environment.getExternalStorageDirectory(),File.separator);
				File destinationFile = new File (destination, source.getLastPathSegment());
				request.setDestinationUri(Uri.fromFile(destinationFile));
				// Add it to the manager
			    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
				DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

				manager.enqueue(request);
			}
		}
