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

package com.drewdahl.android.todoist.apihandlers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.android.applications.todoist.Constants;
import com.drewdahl.android.todoist.users.User;
import com.drewdahl.android.todoist.projects.Project;

import java.util.ArrayList;
import java.util.Map;

/**
 * @brief A singleton that holds a persistent but sleepable connection to todoist.
 * @author alunduil
 *
 * TODO Is this thread safe?  I hope so ... it would be pretty awesome if so.
 */
public class TodoistApiHandler {
	
	private static TodoistApiHandler instance = null;
	
	private String token = "";
	private WebRequest browser = null;
	
	private User user = null;
	
	protected TodoistApiHandler()
	{
	}
	
	public static TodoistApiHandler getInstance()
	{
		if (instance == null) {
			instance = new TodoistApiHandler();
		}
		return instance;
	}
	
	public static TodoistApiHandler getInstance(String token)
	{
		TodoistApiHandler tmp = getInstance();
		tmp.token = token;
		return tmp;
	}
	
	public String getToken()
	{
		return token;
	}

	protected String call(String Uri)
	{
		String ret = "";
		
		try
		{
			browser = new WebRequest(Uri);
			ret = (String)browser.getContent();
		}
		catch (Exception e)
		{
			Log.e("Todoist WebRequest: ",e.getMessage());
		}
		
		return ret;
	}
	
	/**
	 * @brief Log in the user.
	 * @param email
	 * @param password
	 * @return
	 * 
	 * TODO Catch failed logins ...
	 * 
	 * Must update the token item as well since this is returned by the object.
	 */
	public User login(String email, String password)
	{
		try
		{
			user = new User(new JSONObject(call(LOGIN.replace(PARAM_EMAIL, email).replace(PARAM_PASSWORD, password))));
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		token = user.getToken();
		return user;
	}
	
	public String[] getTimezones()
	{
		String response = call(GET_TIMEZONES);
		ArrayList<String> ret = new ArrayList<String>();
		JSONArray jArray = null;
		try
		{
			jArray = new JSONArray(response);
			for(int i = 0; i < jArray.length(); ++i)
			{
				JSONObject obj = jArray.getJSONObject(i);
				ret.add(obj.getString(Constants.JSON_TIMEZONE));
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}

		return (String[])ret.toArray();
	}
	
	/**
	 * 
	 * @param email
	 * @param full_name
	 * @param password
	 * @param timezone
	 * @return
	 * 
	 * TODO Proper error handling.
	 */
	public User register(String email, String full_name, String password, String timezone)
	{
		try
		{
			user = new User(new JSONObject(call(REGISTER.replace(PARAM_EMAIL, email).replace(PARAM_FULLNAME, full_name).replace(PARAM_PASSWORD, password).replace(PARAM_TIMEZONE, timezone))));
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		token = user.getToken();
		return user;
	}
	
	public User updateUser(Map.Entry<String, String>...entries)
	{
		String Uri = UPDATE_USER.replace(PARAM_TOKEN, token);
		for (Map.Entry<String, String> n : entries) {
			if (n.getKey().toLowerCase() == "email") {
				Uri += OPTIONAL_EMAIL.replace(PARAM_EMAIL, n.getValue());
			}
			else if (n.getKey().toLowerCase() == "full_name") {
				Uri += OPTIONAL_FULLNAME.replace(PARAM_FULLNAME, n.getValue());
			}
			else if (n.getKey().toLowerCase() == "password") {
				Uri += OPTIONAL_PASSWORD.replace(PARAM_PASSWORD, n.getValue());
			}
			else if (n.getKey().toLowerCase() == "timezone") {
				Uri += OPTIONAL_TIMEZONE.replace(PARAM_TIMEZONE, n.getValue());
			}
		}
		try 
		{
			user = new User(new JSONObject(call(Uri)));
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		token = user.getToken();
		return user;
	}

	public Project[] getProjects()
	{
		String response = call(GET_PROJECTS.replace(PARAM_TOKEN, token));
		ArrayList<Project> ret = new ArrayList<Project>();
		JSONArray jArray = null;
		try
		{
			jArray = new JSONArray(response); 
			for(int i = 0; i < jArray.length(); ++i)
			{
				ret.add(new Project(jArray.getJSONObject(i), user));
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		return (Project[])ret.toArray();
	}
	
	public Project getProject(Integer project_id)
	{
		Project ret = null;
		try
		{
			ret = new Project(new JSONObject(call(GET_PROJECT.replace(PARAM_TOKEN, token).replace(PARAM_PROJECTID, project_id.toString()))), user);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		return ret;
	}

	public Project addProject(String name)
	{
		Project ret = null;
		try
		{
			ret = new Project(new JSONObject(call(ADD_PROJECT.replace(PARAM_TOKEN, token).replace(PARAM_NAME, name))), user);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		return ret;
	}
	
	public Project updateProject(Map.Entry<String, String>...entries)
	{
		String Uri = UPDATE_PROJECT.replace(PARAM_TOKEN, token);
		/**
		 * TODO If there is no projectid we need to error out somehow!
		 */
		for (Map.Entry<String, String> n : entries) {
			if (n.getKey().toLowerCase() == "name") {
				Uri += OPTIONAL_NAME.replace(PARAM_NAME, n.getValue());
			}
			else if (n.getKey().toLowerCase() == "color") {
				Uri += OPTIONAL_COLOR.replace(PARAM_COLOR, n.getValue());
			}
			else if (n.getKey().toLowerCase() == "indent") {
				Uri += OPTIONAL_INDENT.replace(PARAM_INDENT, n.getValue());
			}
			else if (n.getKey().toLowerCase() == "projectid") { // REQUIRED!
				Uri += PARAM_PROJECTID.replace(PARAM_PROJECTID, n.getValue());
			}
		}
		Project ret = null;
		try
		{
			ret = new Project(new JSONObject(call(Uri)), user);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		return ret;
	}
	
	public void deleteProject(Integer project_id)
	{
		call(DELETE_PROJECT.replace(PARAM_TOKEN, token).replace(PARAM_PROJECTID, project_id.toString()));
	}
	
	/**
	 * TODO Implement label items from the API.
	 */
	
	/**
	 * TODO Verify documentation of API.
	 * @param project_id
	 * @return
	 */
	/*
	public String[] getLabels(Integer project_id)
	{
		String response = call(GET_LABELS.replace(PARAM_TOKEN, token).replace(PARAM_PROJECTID, project_id.toString()));
		ArrayList<String> ret = new ArrayList<String>();
		JSONArray jArray = new JSONArray(response); 
		for(int i = 0; i < jArray.length(); ++i)
		{
			JSONObject obj = jArray.getJSONObject(i);
			ret.add(obj.getString(Constants.JSON_LABEL));
		}
		return (String[])ret.toArray();
	}
	*/
	
	public Item[] getUncompletedItems(Integer project_id)
	{
		String response = call(GET_UNCOMPLETED_ITEMS.replace(PARAM_TOKEN, token).replace(PARAM_PROJECTID, project_id.toString()));
		ArrayList<Item> ret = new ArrayList<Item>();
		JSONArray jArray = new JSONArray(response); 
		for(int i = 0; i < jArray.length(); ++i)
		{
			ret.add(new Item(jArray.getJSONObject(i)));
		}
		return (Item[])ret.toArray();
	}
	
	public Item[] getCompletedItems(Integer project_id)
	{
		String response = call(GET_COMPLETED_ITEMS.replace(PARAM_TOKEN, token).replace(PARAM_PROJECTID, project_id.toString()));
		ArrayList<Item> ret = new ArrayList<Item>();
		JSONArray jArray = new JSONArray(response); 
		for(int i = 0; i < jArray.length(); ++i)
		{
			ret.add(new Item(jArray.getJSONObject(i)));
		}
		return (Item[])ret.toArray();
	}
	
	public Item[] getItemsById(Integer...ids)
	{
		boolean first = true;
		String idstring = "[";
		for (Integer n : ids)
		{
			if (first) {
				idstring += n.toString();
				first = false;
			}
			idstring += "," + n.toString(); 
		}
		idstring += "]";
		
		String response = call(GET_ITEMS_BY_ID.replace(PARAM_TOKEN, token).replace(PARAM_IDS, idstring));
		ArrayList<Item> ret = new ArrayList<Item>();
		JSONArray jArray = new JSONArray(response); 
		for(int i = 0; i < jArray.length(); ++i)
		{
			ret.add(new Item(jArray.getJSONObject(i)));
		}
		return (Item[])ret.toArray();
	}
	
	public Item addItem(Integer project_id, String content, Map.Entry<String, String>...entries)
	{
		String Uri = ADD_ITEM.replace(PARAM_TOKEN, token).replace(PARAM_PROJECTID, project_id.toString()).replace(PARAM_CONTENT, content);

		for (Map.Entry<String, String> n : entries)
		{
			if (n.getKey().toLowerCase() == "date_string") {
				Uri += OPTIONAL_DATESTRING.replace(PARAM_DATESTRING, n.getValue());
			}
			else if (n.getKey().toLowerCase() == "priority") {
				Uri += OPTIONAL_PRIORITY.replace(PARAM_PRIORITY, n.getValue());
			}
		}
		
		return new Item(call(Uri));
	}
	
	public Item updateItem(Integer item_id, Map.Entry<String, String>...entries)
	{
		String Uri = UPDATE_ITEM.replace(PARAM_TOKEN, token).replace(PARAM_ITEMID, item_id.toString());
		
		for (Map.Entry<String, String> n : entries) {
			if (n.getKey().toLowerCase() == "content") {
				Uri += OPTIONAL_CONTENT.replace(PARAM_CONTENT, n.getValue());
			}
			else if (n.getKey().toLowerCase() == "date_string") {
				Uri += OPTIONAL_DATESTRING.replace(PARAM_DATESTRING, n.getValue());
			}
			else if (n.getKey().toLowerCase() == "priority") {
				Uri += OPTIONAL_PRIORITY.replace(PARAM_PRIORITY, n.getValue());
			}
			else if (n.getKey().toLowerCase() == "indent") {
				Uri += OPTIONAL_INDENT.replace(PARAM_INDENT, n.getValue());
			}
			else if (n.getKey().toLowerCase() == "item_order") {
				Uri += OPTIONAL_ORDER.replace(PARAM_ORDER, n.getValue());
			}
		}
		
		return new Item(call(Uri));
	}
	
	public void updateOrders(Integer project_id, Integer...item_ids)
	{
		boolean first = true;
		String idstring = "[";
		for (Integer n : item_ids)
		{
			if (first) {
				idstring += n.toString();
				first = false;
			}
			else // TODO Crap, where else did I forget this?
			{
				idstring += "," + n.toString();
			}
		}
		idstring += "]";
		
		call(UPDATE_ORDERS.replace(PARAM_TOKEN, token).replace(PARAM_PROJECTID, project_id.toString()).replace(PARAM_IDS, idstring));
	}
	
	public Item[] updateRecurringDate(Integer...item_ids)
	{
		boolean first = true;
		String idstring = "[";
		for (Integer n : item_ids) {
			if (first) {
				idstring += n.toString();
				first = false;
			}
			else
			{
				idstring += "," + n.toString();
			}
		}
		idstring += "]";
		
		call(UPDATE_RECURRING_DATE.replace(PARAM_TOKEN, token).replace(PARAM_IDS, idstring));
	}
	
	public void deleteItems(Integer...item_ids)
	{
		boolean first = true;
		String idstring = "[";
		for (Integer n : item_ids) {
			if (first) {
				idstring += n.toString();
				first = false;
			}
			else
			{
				idstring += "," + n.toString();
			}
		}
		idstring += "]";
		
		call(DELETE_ITEMS.replace(PARAM_TOKEN, token).replace(PARAM_IDS, idstring));
	}
	
	public void completeItems(boolean in_history, Integer...item_ids)
	{
		/**
		 * TODO Make this a private method, listToString()?
		 */
		boolean first = true;
		String idstring = "[";
		for (Integer n : item_ids) {
			if (first) {
				idstring += n.toString();
				first = false;
			}
			else
			{
				idstring += "," + n.toString();
			}
		}
		idstring += "]";
		
		String Uri = COMPLETE_ITEMS.replace(PARAM_TOKEN, token).replace(PARAM_IDS, idstring);
		if (!in_history) Uri += OPTIONAL_INHISTORY.replace(PARAM_INHISTORY, "0");
		call(Uri);
	}
	
	public void uncompleteItems(Integer...item_ids)
	{
		boolean first = true;
		String idstring = "[";
		for (Integer n : item_ids) {
			if (first) {
				idstring += n.toString();
				first = false;
			}
			else {
				idstring += "," + n.toString();
			}
		}
		idstring += "]";
		
		call(UNCOMPLETE_ITEMS.replace(PARAM_TOKEN, token).replace(PARAM_IDS, idstring));
	}
	
	/**
	 * TODO Implement the query call.
	 */
	
	/* Todoist URLs */
	private static final String TODOIST = "http://todoist.com/API/";
	private static final String TODOIST_SSL = "https://todoist.com/API/";
	
	/* URI Parameter Values */
	private static final String PARAM_TOKEN = "MyToken";		// User's API Token
	private static final String PARAM_PROJECTID = "Project_ID";	// Project's ID
	private static final String PARAM_EMAIL = "MyEMAIL";		// User's Email Address
	private static final String PARAM_PASSWORD = "MyPassword";	// User's Password
	private static final String PARAM_FULLNAME = "MyFullName";	// User's FullName
	private static final String PARAM_TIMEZONE = "MyTimeZone";	// User's Timezone
	private static final String PARAM_NAME = "MyName";			// Project Name || Label's Name
	private static final String PARAM_COLOR = "MyColor";		// Project Color
	private static final String PARAM_INDENT = "MyIndent";		// Project Indent
	private static final String PARAM_ORDER = "MyOrder";		// Project Order
	private static final String PARAM_OLDNAME = "MyOldName";	// Label's Old Name
	private static final String PARAM_NEWNAME = "MyNewName";	// Label's New Name
	private static final String PARAM_IDS = "MyIDS";			// JSON List of Item ID's (tasks)
	private static final String PARAM_CONTENT = "MyContent";	// Text of the Item (task)
	private static final String PARAM_DATESTRING = "MyDateString"; // Date String of Item (task)
	private static final String PARAM_PRIORITY = "MyPriority";	// Priority of Item (task)
	private static final String PARAM_ITEMID = "MyItemID";		// ID of an Item (task)
	private static final String PARAM_QUERIES = "MyQuery";		// Query
	private static final String PARAM_INHISTORY = "InHistory";  // In History
	
	/* Todoist User API */
	private static final String LOGIN = TODOIST_SSL + "login?email=" + PARAM_EMAIL + "&password=" + PARAM_PASSWORD; // .Replace(PARAM_EMAIL,email).Replace(PARAM_PASSWORD,password);
	private static final String GET_TIMEZONES = TODOIST + "getTimezones";
	private static final String REGISTER = TODOIST_SSL + "register?email=" + PARAM_EMAIL + "&full_name=" + PARAM_FULLNAME + "&password=" + PARAM_PASSWORD + "&timezone=" + PARAM_TIMEZONE; // .Replace(PARAM_EMAIL,email).Replace(PARAM_FULLNAME,full_name).Replace(PARAM_PASSWORD,password).Replace(PARAM_TIMEZONE,timezone);
	private static final String UPDATE_USER = TODOIST_SSL + "updateUser?token=" + PARAM_TOKEN; // .Replace(PARAM_TOKEN,token);
		
	/* Todoist Projects API */
	private static final String GET_PROJECTS = TODOIST + "getProjects?token=" + PARAM_TOKEN; // .Replace(PARAM_TOKEN,token);
	private static final String GET_PROJECT = TODOIST + "getProject?token=" + PARAM_TOKEN + "&project_id=" + PARAM_PROJECTID; // .Replace(PARAM_TOKEN,token).Replace(PARAM_PROJECTID,project_id);
	private static final String ADD_PROJECT = TODOIST + "addProject?name=" + PARAM_NAME + "&token=" + PARAM_TOKEN; // .Replace(PARAM_NAME,name).Replace(PARAM_TOKEN,token);
	private static final String UPDATE_PROJECT = TODOIST + "updateProject?project_id=" + PARAM_PROJECTID + "&token=" + PARAM_TOKEN; // .Replace(PARAM_PROJECTID,project_id).Replace(PARAM_TOKEN,token);
	private static final String DELETE_PROJECT = TODOIST + "deleteProject?project_id=" + PARAM_PROJECTID + "&token=" + PARAM_TOKEN; // .Replace(PARAM_PROJECTID,project_id).Replace(PARAM_TOKEN,token);
	
	/* Todoist Labels API */
	private static final String GET_LABELS = TODOIST + "getLabels?project_id=" + PARAM_PROJECTID + "&token=" + PARAM_TOKEN; // .Replace(PARAM_PROJECTID, project_id).Replace(PARAM_TOKEN,token);
	private static final String UPDATE_LABEL = TODOIST + "updateLabel?old_name=" + PARAM_OLDNAME + "&new_name=" + PARAM_NEWNAME + "&token=" + PARAM_TOKEN; // .Replace(PARAM_OLDNAME,old_name).Replace(PARAM_NEWNAME,new_name).Replace(PARAM_TOKEN,token);
	private static final String DELETE_LABEL = TODOIST + "deleteLabel?name=" + PARAM_NAME + "&token=" + PARAM_TOKEN; // .Replace(PARAM_NAME, name).Replace(PARAM_TOKEN,token);
	
	/* Todoist Items API */
	private static final String GET_UNCOMPLETED_ITEMS = TODOIST + "getUncompletedItems?project_id=" + PARAM_PROJECTID + "&token=" + PARAM_TOKEN; //.Replace(PARAM_PROJECTID,project_id).Replace(PARAM_TOKEN,token);
	private static final String GET_COMPLETED_ITEMS = TODOIST + "getCompletedItems?project_id=" + PARAM_PROJECTID + "&token=" + PARAM_TOKEN; //.Replace(PARAM_PROJECTID,project_id).Replace(PARAM_TOKEN,token);
	private static final String GET_ITEMS_BY_ID = TODOIST + "getItemsById?ids=" + PARAM_IDS + "&token=" + PARAM_TOKEN; //.Replace(PARAM_IDS,ids).Replace(PARAM_TOKEN,token);
	private static final String ADD_ITEM = TODOIST + "addItem?project_id=" + PARAM_PROJECTID + "&content=" + PARAM_CONTENT + "&token=" + PARAM_TOKEN;
	private static final String UPDATE_ITEM = TODOIST + "updateItem?id=" + PARAM_ITEMID + "&token=" + PARAM_TOKEN; // .Replace(PARAM_ITEMID,id).Replace(PARAM_TOKEN,token);
	private static final String UPDATE_ORDERS = TODOIST + "updateOrders?project_id=" + PARAM_PROJECTID + "&item_id_list=" + PARAM_IDS + "&token=" + PARAM_TOKEN; // .Replace(PARAM_PROJECTID,project_id).Replace(PARAM_IDS,item_id_list).Replace(PARAM_TOKEN,token);
	private static final String UPDATE_RECURRING_DATE = TODOIST + "UpdateRecurringDate?ids=" + PARAM_IDS + "&token=" + PARAM_TOKEN; // .Replace(PARAM_IDS,ids).Replace(PARAM_TOKEN,token);
	private static final String DELETE_ITEMS = TODOIST + "DeleteItems?ids=" + PARAM_IDS + "&token=" + PARAM_TOKEN; // .Replace(PARAM_IDS,ids).Replace(PARAM_TOKEN,token);
	private static final String COMPLETE_ITEMS = TODOIST + "CompleteItems?ids=" + PARAM_IDS + "&token=" + PARAM_TOKEN; // .Replace(PARAM_IDS,ids).Replace(PARAM_TOKEN,token);
	private static final String UNCOMPLETE_ITEMS = TODOIST + "UncompleteItems?ids=" + PARAM_IDS + "&token=" + PARAM_TOKEN; // .Replace(PARAM_IDS,ids).Replace(PARAM_TOKEN,token);
	
	/* Date Query & Search API */
	private static final String QUERY = TODOIST + "query?queries=" + PARAM_QUERIES + "&token=" + PARAM_TOKEN; // .Replace(PARAM_QUERIES,queries).Replace(PARAM_TOKEN,token);
	
	/* Todoist Optional API Parameters 
	 * Check /docs/ for Details on OPTIONAL parameters 
	 */
	private static final String OPTIONAL_EMAIL = "&email=" + PARAM_EMAIL; // .Replace(PARAM_EMAIL,email);
	private static final String OPTIONAL_FULLNAME = "&full_name=" + PARAM_FULLNAME; // .Replace(PARAM_FULLNAME, full_name);
	private static final String OPTIONAL_PASSWORD = "&password=" + PARAM_PASSWORD; // .Replace(PARAM_PASSWORD, password);
	private static final String OPTIONAL_TIMEZONE = "&timezone=" + PARAM_TIMEZONE; // .Replace(PARAM_TIMEZONE, timezone);
	private static final String OPTIONAL_NAME = "&name=" + PARAM_NAME;	// .Replace(PARAM_NAME,name);
	private static final String OPTIONAL_COLOR = "&color=" + PARAM_COLOR;	// .Replace(PARAM_COLOR,color);
	private static final String OPTIONAL_INDENT = "&indent=" + PARAM_INDENT;	// .Replace(PARAM_INDENT,indent);
	private static final String OPTIONAL_ORDER = "&order=" + PARAM_ORDER;	// .Replace(PARAM_ORDER,order);
	private static final String OPTIONAL_DATESTRING = "&date_string=" + PARAM_DATESTRING; // .Replace(PARAM_DATESTRING, date_string);
	private static final String OPTIONAL_PRIORITY = "&priority=" + PARAM_PRIORITY;	// .Replace(PARAM_PRIORITY, priority);
	private static final String OPTIONAL_CONTENT = "&content=" + PARAM_CONTENT; // .Replace(PARAM_CONTENT, content);
	private static final String OPTIONAL_INHISTORY = "&in_history=" + PARAM_INHISTORY; // .Replace(PARAM_INHISTORY, inhistory);
}
