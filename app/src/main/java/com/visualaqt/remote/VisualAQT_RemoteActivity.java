package com.visualaqt.remote;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class VisualAQT_RemoteActivity extends Activity {

	private String serverIpSaved;
	private String serverIpAddress = "192.168.49.100";
	private boolean connected = false;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {// display each item in a single
												// line
			Bundle myMsgBndl = msg.getData();

			String msgText = myMsgBndl.getString("Message");

			if (msgText.contains("[MSG]")) {
				msgText = msgText.replace("[MSG]", "");
				
				if (msgText.contains("[NL]")) {
					
					msgText = msgText.replace("[NL]", "\n");
					Toast.makeText(VisualAQT_RemoteActivity.this, msgText,
							Toast.LENGTH_LONG).show();
					
				} else {
				
					msgText = msgText.replace("[NL]", "\n");
					Toast.makeText(VisualAQT_RemoteActivity.this, msgText,
							Toast.LENGTH_SHORT).show();
					
				}
				
			} else if (msgText != "") {

				if (msgText.equals("[SEPARATOR]")) {
					View vSpacer = new View(VisualAQT_RemoteActivity.this);
					LinearLayout.LayoutParams lpvSpacer = new LinearLayout.LayoutParams(
							25, LayoutParams.MATCH_PARENT);
					vSpacer.setLayoutParams(lpvSpacer);
					vSpacer.setBackgroundColor(0xFFFFFFFF);
					llImages.addView(vSpacer);

				} else {
					ImageView imTemp = new ImageView(
							VisualAQT_RemoteActivity.this);
					int resID = getResources().getIdentifier(msgText,
							"drawable", getPackageName());

					if (resID != 0) {
						imTemp.setImageResource(resID);
						imTemp.setLayoutParams(new LinearLayout.LayoutParams(
								30, 30));

						llImages.addView(imTemp);
						View vSpacer = new View(VisualAQT_RemoteActivity.this);
						LinearLayout.LayoutParams lpvSpacer = new LinearLayout.LayoutParams(
								10, LayoutParams.MATCH_PARENT);
						vSpacer.setLayoutParams(lpvSpacer);
						vSpacer.setBackgroundColor(0xFFFFFFFF);
						llImages.addView(vSpacer);
					}

				}

			}

		}
	};

	private String cmdToSend = "";

	SharedPreferences preferences;

	public static final String PREFS_COUNT = "MyPrefsFile";

	Thread cThread;

	LinearLayout llImages;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		preferences = this.getSharedPreferences(PREFS_COUNT, MODE_PRIVATE);

		serverIpSaved = (readPreferencesString("ServerIP", "192.168.1.1"));
		serverIpAddress = serverIpSaved;

		llImages = (LinearLayout) findViewById(R.id.llImages);

		attachButton((Button) findViewById(R.id.btnEnglish));
		attachButton((Button) findViewById(R.id.btnGujarati));
		attachButton((Button) findViewById(R.id.btnHindi));
		attachButton((Button) findViewById(R.id.btnNumbers));
		attachButton((Button) findViewById(R.id.btnC));
		attachButton((Button) findViewById(R.id.btnE));
		attachButton((Button) findViewById(R.id.btnDots));
		attachButton((Button) findViewById(R.id.btnPediatric));
		attachButton((Button) findViewById(R.id.btnPrevLine));
		attachButton((Button) findViewById(R.id.btnNextLine));
		attachButton((Button) findViewById(R.id.btnFirst));
		attachButton((Button) findViewById(R.id.btnLast));
		attachButton((Button) findViewById(R.id.btnContrastIncrease));
		attachButton((Button) findViewById(R.id.btnContrastDecrease));
		attachButton((Button) findViewById(R.id.btnMultiLine));
		attachButton((Button) findViewById(R.id.btnVerticalMask));
		attachButton((Button) findViewById(R.id.btnRandom));
		attachButton((Button) findViewById(R.id.btnRedGreen));

	}

	private void attachButton(Button btn) {
		btn.setOnClickListener(btnClickListener);
		//btn.getBackground().setColorFilter(0xFFFFFF00, android.graphics.PorterDuff.Mode.MULTIPLY); 
		btn.setTextAppearance(this, R.style.ButtonFontStyle);
	}

	public OnClickListener btnClickListener = new OnClickListener() {
		public void onClick(final View view) {
			if (!connected) {
				llImages.removeAllViews();
				View vSpacer = new View(VisualAQT_RemoteActivity.this);
				LinearLayout.LayoutParams lpvSpacer = new LinearLayout.LayoutParams(
						35, LayoutParams.MATCH_PARENT);
				vSpacer.setLayoutParams(lpvSpacer);
				vSpacer.setBackgroundColor(0xFFFFFFFF);
				llImages.addView(vSpacer);

				//serverIpAddress = serverIp.getText().toString();
				Button btnSource = (Button) view;
				cmdToSend = btnSource.getTag().toString();
				if (!serverIpAddress.equals("")) {
					cThread = new Thread(new ClientThread());
					cThread.start();
				}
			}
		}
	};

	// Initiating Menu XML file (menu.xml)
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (!connected) {
			llImages.removeAllViews();
			View vSpacer = new View(VisualAQT_RemoteActivity.this);
			LinearLayout.LayoutParams lpvSpacer = new LinearLayout.LayoutParams(
					35, LayoutParams.MATCH_PARENT);
			vSpacer.setLayoutParams(lpvSpacer);
			vSpacer.setBackgroundColor(0xFFFFFFFF);
			llImages.addView(vSpacer);

			cmdToSend = item.getTitleCondensed().toString();
			cmdToSend = cmdToSend.replace(" ", "");
			cmdToSend = cmdToSend.replace("/", "");

			if (cmdToSend.equals("ServerIP")) {
				AlertDialog.Builder alert = new AlertDialog.Builder(this);
				alert.setTitle("Server IP");
				alert.setMessage("Please enter Server IP Address:");
				// Set an EditText view to get user input
				final EditText input = new EditText(this);
				input.setText(serverIpSaved);
				alert.setView(input);
				alert.setPositiveButton("Ok",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								String value = input.getText().toString();
								serverIpAddress = value;
								// Do something with value!
							}
						});
				alert.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// Canceled.
							}
						});
				alert.show();
			} else if (cmdToSend.equals("ClearDefault")) {
				try {
					PackageManager localPackageManager = getPackageManager();
					localPackageManager.clearPackagePreferredActivities(this
							.getPackageName());
					showToast("Defaults cleared. OK.");

				} catch (Exception e) {
					showToast("Clear Defaults failed.");
				}
			} else if (!serverIpAddress.equals("")) {
				cThread = new Thread(new ClientThread());
				cThread.start();
			}

		}
		return true;
	}

	private void showToast(String msgText) {
		Toast.makeText(VisualAQT_RemoteActivity.this, msgText,
				Toast.LENGTH_SHORT).show();

	}

	public class ClientThread implements Runnable {

		InetAddress serverAddr;
		Socket socket;
		Boolean connected;

		public void run() {
			try {
				serverAddr = InetAddress.getByName(serverIpAddress);
				socket = new Socket(serverAddr, 8080);
				connected = true;
				if (connected) {

					if (serverIpSaved != serverIpAddress) {
						// Save if changed.
						writePreferencesString("ServerIP", serverIpAddress);
						serverIpSaved = serverIpAddress;
					}

					try {
						// Send Command
						PrintWriter out = new PrintWriter(
								new BufferedWriter(new OutputStreamWriter(
										socket.getOutputStream())), true);
						BufferedReader input = new BufferedReader(
								new InputStreamReader(socket.getInputStream()));
						out.println(cmdToSend);

						// Receive Respones
						String st = null;
						while (!input.ready()) {
						}

						st = input.readLine();
						while (st != null) {
							Bundle messageData;
							Message msg;
							messageData = new Bundle();
							messageData.putString("Message", st);
							msg = handler.obtainMessage();
							msg.what = 0;
							msg.setData(messageData);
							handler.sendMessage(msg);
							st = input.readLine();
						}

					} catch (Exception e) {

					}

				}
				socket.close();
			} catch (Exception e) {
				connected = false;
			}
		}

	}


	void writePreferencesString(String prefName, String prefValue) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(prefName, prefValue.toString());
		editor.commit();
	}

	String readPreferencesString(String prefName, String defaValue) {
		try {
			String myRet = preferences.getString(prefName, defaValue);
			return myRet;
		} catch (Exception e) {
			return defaValue;
		}
	}

}
