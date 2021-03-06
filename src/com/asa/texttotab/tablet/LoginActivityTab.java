package com.asa.texttotab.tablet;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.asa.texttotab.Preferences;
import com.asa.texttotab.R;
import com.asa.texttotab.Util.LoginUtil;
import com.asa.texttotab.Util.Util;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class LoginActivityTab extends Activity {
	private Button loginButton;
	private Button createAccountButton;
	private EditText usernameField;
	private EditText passwordField;

	private Intent mIntent;
	private Context mContext;
	private ProgressDialog loginProgress;

	private boolean validLogin;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_activity_tablet);

		if (ParseUser.getCurrentUser() != null) {
			mIntent = new Intent(LoginActivityTab.this,
					MainHoneycombActivity.class);
			startActivity(mIntent);
			finish();
		}

		createAccountButton = (Button) findViewById(R.id.login_create_account_btn_tab);
		loginButton = (Button) findViewById(R.id.login_button_tablet);
		usernameField = (EditText) findViewById(R.id.login_username_field_tablet);
		passwordField = (EditText) findViewById(R.id.login_password_field_tablet);

		createAccountButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				mIntent = new Intent(LoginActivityTab.this,
						RegisterActivityTab.class);
				startActivity(mIntent);
			}
		});

		loginButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				String usernameText = usernameField.getText().toString().trim();
				String passwordText = passwordField.getText().toString().trim();
				validLogin = true;
				mContext = LoginActivityTab.this;

				// Check if username has whitespace.
				if (Util.containsWhiteSpace(usernameText)) {
					validLogin = false;
					LoginUtil.displayLoginToast(mContext,
							R.string.invalid_username_none_entered);
				}

				// Check if username has been entered
				if (usernameText.length() == 0) {
					validLogin = false;
					LoginUtil.displayLoginToast(mContext,
							R.string.invalid_username_none_entered);
				}

				// Check if password has been entered.
				if (passwordText.length() == 0) {
					validLogin = false;
					LoginUtil.displayLoginToast(mContext,
							R.string.invalid_password_none_entered);
				}

				if (validLogin) {
					loginProgress = ProgressDialog.show(
							LoginActivityTab.this,
							"",
							getResources().getString(
									R.string.dialog_login_message), true);
					Log.e("TAB", "Logging in!");
					loginUser(usernameText, passwordText);
				}
			}
		});

		if (Preferences.DEBUG) {
			usernameField.setText("TestName");
			passwordField.setText("12345");
		}

		passwordField.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEND) {
					loginButton.performClick();
					return true;
				}
				return false;
			}
		});

	}

	private void loginUser(String usernameText, String passwordText) {
		ParseUser.logInInBackground(usernameText, passwordText,
				new LogInCallback() {
					@Override
					public void done(ParseUser user, ParseException e) {
						loginProgress.dismiss();
						if (e == null && user != null) {
							// Successful login.
							mIntent = new Intent(LoginActivityTab.this,
									MainHoneycombActivity.class);
							startActivity(mIntent);
							finish();
						} else if (user == null) {
							// Username or password is incorrect.
							LoginUtil.displayLoginToast(mContext,
									R.string.invalid_username_password);
						} else {
							// Error with connecting server.
							LoginUtil.displayLoginToast(mContext,
									R.string.parse_connection_error);
						}
					}
				});
	}

}
