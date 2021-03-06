package com.asa.texttotab;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

import com.asa.texttotab.Util.Util;
import com.asa.texttotab.tablet.MessageFragment;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class IncomingPushReceiver extends BroadcastReceiver {

	private final String TAG = "IncomingPushReceiver";

	private DatabaseAdapter dbAdapter;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.e(TAG, "Received intent: " + Preferences.PUSH_RECEIVED);

		Preferences.DEVICE_IS_HONEYCOMB = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB;

		dbAdapter = new DatabaseAdapter(context);
		dbAdapter.open();

		ReciverQueryParseAsyncTask queryParse = new ReciverQueryParseAsyncTask(
				Preferences.DEVICE_IS_HONEYCOMB, context);
		queryParse.execute();

	}

	public class ReciverQueryParseAsyncTask extends AsyncTask<Void, Void, Void> {

		Context mContext;

		private final String TAG = "IncommingPushReceiver.QueryParseAsyncTask";
		private boolean isHoneycomb;

		public ReciverQueryParseAsyncTask(boolean honeycomb, Context context) {
			isHoneycomb = honeycomb;
			mContext = context; 
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			if (isHoneycomb) {
				final ParseQuery query = new ParseQuery(
						Preferences.PARSE_TABLE_SMS);
				// Sort the Parse Object so only the username of the current
				// user can be accessed.
				query.whereEqualTo(Preferences.PARSE_USERNAME_ROW,
						Util.getUsernameString());
				// The most recent message added will be on top.
				query.orderByDescending("createdAt");
				// Only need to get one message from the server, each message
				// will be from a push
				query.setLimit(1);
				try {
					List<ParseObject> messageList = query.find();
					for (ParseObject messageObject : messageList) {
						// Open up the database
						// Get the parse object id
						String objectId = messageObject.objectId();
						// with the objectid you can query the server
						ParseObject message = query.get(objectId);
						// Get the time the message was added to the server
						Date time = message.createdAt();
						// Format the time to (Fri Jan 13 12:00)
						SimpleDateFormat sdf = new SimpleDateFormat(
								"E MMM dd hh:mm");
						String formatedTime = sdf.format(time);
						String timeDB = formatedTime.toString();
						String addressDB = message.getString("address");
						String bodyDB = message.getString("body");
						int readDB = message.getInt("read");
						int smsIdDB = message.getInt("smsId");
						String subjectDB = message.getString("subject");
						int threadIdDB = message.getInt("threadId");
						int typeDB = message.getInt("type");
						int onDeviceDB = message.getInt("onDevice");
						String usernameDB = message.getString("username");
						// Display the total message queryed for
						// logging
						String totalMessage = "Sent: " + timeDB + "\n"
								+ "Address: " + addressDB + "\n" + "Message : "
								+ bodyDB + "\n";
						Log.d(TAG, "New message is: " + totalMessage);
						// Get the MessageItem object so you can
						// create the db entry.
						MessageItem item = new MessageItem(timeDB, addressDB,
								bodyDB, readDB, smsIdDB, subjectDB, threadIdDB,
								typeDB, onDeviceDB, usernameDB);
						// Insert the MessageItem into the sms2honeycomb.db.
						dbAdapter.insertMessageItem(item);

						// TODO update the listadapter to display the new
						// message via an intent/ service?

						String intentString = "com.asa.texttotab.UPDATE_LIST";
						Intent updateIntent = new Intent();
						updateIntent.setAction(intentString);
						mContext.sendBroadcast(updateIntent);

						// TODO NOTICIFATIONS
					}
				} catch (ParseException e) {
					// TODO - Handle situation where querying server failed
					dbAdapter.close();
					Log.e(TAG, "querying server failed");
					e.printStackTrace();

				}
			} else {
				Log.e(TAG, "The device is not honeycomb is its a phone.");
				// If the device is not a tablet it is a phone so you pull from
				// the
				// server, but then send a sms message from the data recived.
				// We want to query the sms table
				final ParseQuery query = new ParseQuery(
						Preferences.PARSE_TABLE_SMS);
				// Sort the Parse Object so only the username of the current
				// user
				// can be accessed.
				query.whereEqualTo(Preferences.PARSE_USERNAME_ROW,
						Util.getUsernameString());
				// The most recent message added will be on top.
				query.orderByDescending("createdAt");
				// Only need to get one message from the server, each message
				// will be from a push
				query.setLimit(1);
				try {
					List<ParseObject> messageList = query.find();
					// For the ParseObjects quering get all that needs
					// to be done.
					for (ParseObject messageObject : messageList) {
						// Get the parse object id
						String objectId = messageObject.objectId();
						// with the objectid you can query the
						// server
						ParseObject message = query.get(objectId);
						// Get the time the message was created at
						// for
						// logging, do not need a time to send a
						// message.
						Date time = message.createdAt();
						String timeString = time.toString();
						// Get who the message is coming from
						// (phonenumber).
						String address = message
								.getString(Preferences.PARSE_SMS_ADDRESS);
						// Get the body of the message
						String body = message
								.getString(Preferences.PARSE_SMS_BODY);
						// Display the total message queryed for
						// logging
						String totalMessage = "Sent: " + timeString + "\n"
								+ "To: " + address + "\n" + "Message : " + body
								+ "\n";
						Log.d(TAG, "New message is: " + totalMessage);
						// get the smsmanager as sms
						SmsManager sms = SmsManager.getDefault();
						// If the message is over the 160 Char limit
						// it
						// will be choped up.
						if (body.length() > 160) {
							// Chops up the message
							sms.divideMessage(body);
							// Send the sms message in its parts
							sms.sendMultipartTextMessage(address, null,
									sms.divideMessage(body), null, null);
						} else {
							// Sends the message without cutting it
							sms.sendTextMessage(address, null, body, null, null);
						}
					}
				} catch (ParseException e) {
					// TODO - Handle situation where querying server failed
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void params) {
			dbAdapter.close();

			if (isHoneycomb) {
				Log.d(TAG,
						"TextToTab - IncomingPushReceiver in tablet is done.");
			} else {
				Log.d(TAG, "TextToTab - IncomingPushReceiver in phone is done.");
			}
		}
		
	}
}
