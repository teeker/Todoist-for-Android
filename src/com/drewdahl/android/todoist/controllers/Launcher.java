package com.drewdahl.android.todoist.controllers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.drewdahl.android.todoist.models.User;
import com.drewdahl.android.todoist.views.ItemList;
import com.drewdahl.android.todoist.views.Login;

public class Launcher extends Activity {
	private static final int LOGIN_REQUEST = 1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		/**
		 * TODO Start syncing service.
		 * TODO Maybe: If option remember me is set and we have credentials just go to view.
		 */
		
		Intent intent = new Intent(this, Login.class);
		this.startActivityForResult(intent, LOGIN_REQUEST);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(resultCode, resultCode, data);
		
		switch (requestCode) {
		case LOGIN_REQUEST:
			switch (resultCode) {
			case Activity.RESULT_CANCELED:
				Log.d(this.toString(), "Login Activity Canceled");
				finish();
				break;
			case Activity.RESULT_OK:
				Log.d(this.toString(), "Login Activity Ok");
				startActivityUserStartPage();
				break;
			default:
				Log.e(this.toString(), "Unhandled requestCode");
				break;
			}
		default:
			Log.d(this.toString(), "Not our request");
			break;
		}
	}
	
	private void startActivityUserStartPage() {
		/**
		 * TODO Start user's start page.
		 * TODO Fix this query.
		 */
		User user = User.getUser();
		Log.d(this.toString(), "User's start page: " + user.getStartPage());
		Intent intent = new Intent(this, ItemList.class);
		intent.putExtra(User.KEY, user);
		startActivity(intent);
		finish();
	}
}
