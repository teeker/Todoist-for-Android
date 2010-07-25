/*    
	This file is part of Todoist for Android�.

    Todoist for Android� is free software: you can redistribute it and/or 
    modify it under the terms of the GNU General Public License as published 
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Todoist for Android� is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Todoist for Android�.  If not, see <http://www.gnu.org/licenses/>.
    
    This file incorporates work covered by the following copyright and  
 	permission notice:
 	
 	Copyright [2010] pskink <pskink@gmail.com>
 	Copyright [2010] ys1382 <ys1382@gmail.com>
 	Copyright [2010] JonTheNiceGuy <JonTheNiceGuy@gmail.com>

   	Licensed under the Apache License, Version 2.0 (the "License");
   	you may not use this file except in compliance with the License.
   	You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   	Unless required by applicable law or agreed to in writing, software
   	distributed under the License is distributed on an "AS IS" BASIS,
   	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   	See the License for the specific language governing permissions and
   	limitations under the License.
*/

package com.drewdahl.android.todoist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.drewdahl.android.todoist.models.user.User;

/**
 * An Activity that allows the user to Login.
 *
 * TODO Implement error dialogs.
 * TODO Implement remember password.
 *
 * @see     android.app.Activity
 */
public class Login extends Activity {
	private String email;
	private String password;
    
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        findViewById(R.id.btn_signIn).setOnClickListener(new Button.OnClickListener() {
        	public void onClick(View view) {
        		signIn();
        	}
        });
    }
    
    private void signIn()
    {
    	email = ((EditText)findViewById(R.id.edit_email)).getText().toString();
    	password = ((EditText)findViewById(R.id.edit_pass)).getText().toString();

    	if (email.length() < 1) {
    		showToast("Please, input your e-mail address.");
    	} else if (password.length() < 1) {
    		this.showToast("Please, input your password.");
    	} else {
    		User user = null;
    		try {
    			user = User.login(email, password);
    		}
    		catch (Exception e) { 
    			/**
    			 * TODO Catch and handle a specific exception ...
    			 */
    		}
    		
    		Intent intent = new Intent("com.drewdahl.todoist.ItemList");
    		intent.putExtra("com.drewdahl.todoist.model.user", user);
    		startActivity(intent);
   			finish();
   		}
   	}
    
    /**
     * TODO What is toast?
     * @param msg
     */
	private void showToast(CharSequence msg) {
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}
}
