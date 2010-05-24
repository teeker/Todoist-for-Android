package com.android.applications.todoist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.applications.todoist.containers.Project;
import com.android.applications.todoist.containers.Projects;
import com.android.applications.todoist.containers.Task;
import com.android.applications.todoist.containers.User;
import com.android.applications.todoist.handlers.TodoistAPIHandler;

public class TasksList extends ListActivity {
	protected HashMap<Integer, ResultCallbackIF> _callbackMap = new HashMap<Integer, ResultCallbackIF>();
	private String token = "";
	private ProgressDialog m_ProgressDialog = null;
    private ArrayList<Task> taskArray = null;
    private ItemAdapter adapter;
    private Runnable viewProjects;
    private TodoistAPIHandler handler;
   
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        handler = new TodoistAPIHandler();
        Bundle extras = getIntent().getExtras();
        
        setContentView(R.layout.main);
        
        if(extras != null)
        {
        	//We're being called >.> I think it came from inside the house :|
        	handler.setToken(extras.getString("token"));
        	this.getTasks();
        }
        else
        {
        	String token = this.getToken();
        	if(token != "") 
        	{
        		//We're logged in!  Let's see us some tasks :*]
            	handler.setToken(token);
            	this.getTasks();
            }
        	else 
        	{
        		//We're not logged it... TO THE LOGINMOBILE, BATMAN
        		this.createLogin();
        	}
        }
    }
    
    // Will probably be eventually replaced or use the SQLite3 DB
    private String getToken() 
    {
    	
    	return "";
    }
    
    // Call the LoginPage Activity and deal with that
    private void createLogin()
	{
		this.launchActivity(LoginPage.class, new TasksList.ResultCallbackIF() {
			
			@Override
			public void resultOk(Intent data) 
			{
				Bundle extras = data.getExtras();
				if(extras != null)
		        {
					storeToken(extras.getString("token"));
		        	Log.e("Login-resultOk():",token);
		        	handler.setToken(token);
		        	getTasks();
		        	//createTasksList();
		        }
		        else
		        {
		        	Log.e("Login-resultOk():","No Token!!!");
		        }
			}
			
			@Override
			public void resultCancel(Intent data) 
			{
				// Most likely, we got here only by the user hitting the back button, so quit.
				Log.e("Login-resultCancel:", "Login Canceled!");
				finish();
			}
		});
	}

    // More than likely this will be replaced or just use the SQLite DB
	private void storeToken(String token)
	{
		// TODO Store Token
		this.token = token; 
	}
    
	
    private Runnable returnRes = new Runnable() {

        @Override
        public void run() {
            if(projectArray != null && projectArray.size() > 0){
                adapter.notifyDataSetChanged();
                for(int i=0;i<projectArray.size();i++)
                adapter.add(projectArray.get(i));
            }
            m_ProgressDialog.dismiss();
            adapter.notifyDataSetChanged();
        }
    };
    
    private void getTasks() 
    {
    	taskArray = new ArrayList<Task>();
        this.adapter = new ItemAdapter(this, R.layout.row, projectArray);
        setListAdapter(this.adapter);
       
        viewProjects = new Runnable() {
            @Override
            public void run() 
            {
                getItems();
            }
        };
        Thread thread =  new Thread(null, viewProjects, "MagentoBackground");
        thread.start();
        m_ProgressDialog = ProgressDialog.show(TasksList.this,    
              "Please wait...", "Retrieving data ...", true);
    }
    
    private void getItems(){

        	Projects projects = handler.getProjects();
        	projectArray = new ArrayList<Project>();
        	User user = handler.login("user", "pass");
        	Project project = new Project();
        	project.setName(user.getAPIToken());
        	projectArray.add(project);
        	for(int i = 0; i < projects.getSize(); i++)
        	{
	        	projectArray.add(projects.getProjectsAt(i));
        	}
            runOnUiThread(returnRes);
        }
    private class ItemAdapter extends ArrayAdapter<Project> {

        private ArrayList<Project> projects;

        public ItemAdapter(Context context, int textViewResourceId, ArrayList<Project> items) {
                super(context, textViewResourceId, items);
                this.projects = items;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.row, null);
                }
                
                Project project = projects.get(position);
                if (project != null) {
                        TextView tt = (TextView) v.findViewById(R.id.TextView01);
                        TextView bt = (TextView) v.findViewById(R.id.TextView02);
                        if (tt != null) {
                              tt.setText(project.getName());                            }
                        if(bt != null){
                              bt.setText(project.getID());
                        }
                }
                return v;
        }
    }
    
    
    @SuppressWarnings("unchecked")
	public void launchActivity(Class subActivityClass, ResultCallbackIF callback, Bundle bundle) 
	{
		  int correlationId = new Random().nextInt();
		  _callbackMap.put(correlationId, callback);
		  startActivityForResult(new Intent(this, subActivityClass).putExtras(bundle), correlationId);
	}
	
	@SuppressWarnings("unchecked")
	public void launchActivity(Class subActivityClass, ResultCallbackIF resultCallbackIF) 
	{
		launchActivity(subActivityClass, resultCallbackIF, new Bundle());
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		super.onActivityResult(requestCode, resultCode, data); 
		try 
		{
			ResultCallbackIF callback = _callbackMap.get(requestCode);

			switch (resultCode) 
			{
			case Activity.RESULT_CANCELED:
				callback.resultCancel(data);
				_callbackMap.remove(requestCode);
				break;
			case Activity.RESULT_OK:
				callback.resultOk(data);
				_callbackMap.remove(requestCode);
				break;
			default:
				Log.e("Error:","requestCode not found in hash");
			}
		}
		catch (Exception e) 
		{
			Log.e("ERROR:","Issue processing Activity", e);
		}
	}

	public static interface ResultCallbackIF 
	{
	  public void resultOk(Intent data);
	  public void resultCancel(Intent data);

	}
}