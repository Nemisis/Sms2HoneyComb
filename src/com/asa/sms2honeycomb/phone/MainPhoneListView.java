package com.asa.sms2honeycomb.phone;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.asa.sms2honeycomb.DatabaseAdapter;
import com.asa.sms2honeycomb.MessageItem;
import com.asa.sms2honeycomb.Preferences;
import com.asa.sms2honeycomb.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainPhoneListView extends ListActivity {

	private ListView messageListView;
	private TextView messageListText;
	private TextView toText;
	private TextView messageText;
	private EditText toField;
	private EditText messageField;
	private Button sendButton;
	private Button logoutButton;
	private ArrayList<String> messageArrayList;
	private DatabaseAdapter dbAdapter;

	private final String TAG = "MainPhoneListView";

	// Called when the activity is first created.
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		dbAdapter = new DatabaseAdapter(MainPhoneListView.this);
		dbAdapter.open();

		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
				dbAdapter.getMessageArrayList("1234567")));
		messageListView = getListView();
		messageListView.setTextFilterEnabled(true);

		messageListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// When clicked, show a toast with the TextView text
				Toast.makeText(getApplicationContext(),
						((TextView) view).getText(), Toast.LENGTH_SHORT).show();

			}
		});
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		dbAdapter.close();
	}
}