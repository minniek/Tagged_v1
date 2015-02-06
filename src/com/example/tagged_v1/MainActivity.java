/* Tagged - Detects Request Header Modification
 * References:
 * http://developer.android.com/training/basics/network-ops/connecting.html
 * http://www.compiletimeerror.com/2013/01/why-and-how-to-use-asynctask.html#.VMKb5P7F95A		
 * 
 */

package com.example.tagged_v1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity implements View.OnClickListener {

	// Declare widgets
	Button send, clear;
	EditText urlText;
	TextView responseStrTxt, serverOutputTxt;
	
	// URL variables
	final String serverIP = "*"; // localhost for now...
	final String serverPage = "server_v1.php"; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Map widgets to xml file
		send = (Button)findViewById(R.id.send_btn);
		clear = (Button)findViewById(R.id.clear_btn);
		urlText = (EditText)findViewById(R.id.url_editText);
		responseStrTxt = (TextView)findViewById(R.id.responseStr_textView);
		
		// Set onClickListener to buttons
		send.setOnClickListener((OnClickListener) this);
		clear.setOnClickListener((OnClickListener) this);
		
		// Preload URL to connect to Tagged! server that is running on localhost
		urlText.setText("http://" + serverIP + "/" + serverPage, TextView.BufferType.EDITABLE);
	}

	// Assign tasks for buttons
	public void onClick(View v) {
		if (v == send) {
			// Check for network connection before proceeding...
			String stringURL = urlText.getText().toString();
			ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo nwInfo = connMgr.getActiveNetworkInfo();
			// If network is present, start AsyncTask to connect to given URL
			if ((nwInfo != null) && (nwInfo.isConnected())) {
				new GoToWebpageTask().execute(stringURL);
			} else {
				responseStrTxt.setText("ERROR: No network connection detected.");
			}	
		}
		
		if (v == clear) {
			responseStrTxt.setText("");
		}
	}
	
	private class GoToWebpageTask extends AsyncTask<String, Void, String> {
		String responseStr = ""; // Will display result in Textview "responseStrTxt"
		
		@Override
		protected String doInBackground(String... urls) {
			// params comes from the execute() call: params[0] is the url
			try {
				connectToUrl(urls[0]);
				return responseStr; // return value to be used in onPostExecute
			} catch (IOException e) {
				responseStr = "Error: could not connect to URL. Please check URL";
				return responseStr;
			}
		}
			
		// onPostExecute displays the results of the AsyncTask
		protected void onPostExecute(String responseStr) {
        		responseStrTxt.setText(responseStr);
        	}
		
		private String connectToUrl(String url) throws IOException {
			URL myURL = new URL(url);
			HttpURLConnection conn = (HttpURLConnection)myURL.openConnection();
			
			// Connect to URL and display HTML of Tagged Server
			try {
				conn.setReadTimeout(10000); // milliseconds
				conn.setConnectTimeout(15000); // milliseconds
				// Set header requests and view in LogCat
				/*conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.111 Safari/537.36");
				conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
				String reqHeaders = conn.getRequestProperties().toString(); // View request headers
				Log.d("tagged_v1", reqHeaders);
				*/
				conn.connect();
		
				// Read and display HTML of Tagged Server (should also output modified request headers made by Tagged Proxy...)
				InputStream htmlContent = conn.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(htmlContent, "UTF-8"));
				String content = null;
				String serverPageContent = "";
				while ((content = reader.readLine()) != null) {
					serverPageContent += content + "\n";
				}
				responseStr = serverPageContent;
				
				// Close connection and return server's output
				conn.disconnect();
				return responseStr;
			} catch (MalformedURLException e) {
				e.printStackTrace();
				responseStr = "ERROR: MalformedURLException caught!";
				return responseStr;
			} catch (IOException e) {
				e.printStackTrace();
				responseStr = "ERROR: IOException caught!";
				return responseStr;
			}
		}
	}
}
