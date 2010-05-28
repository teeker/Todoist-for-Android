package com.android.applications.todoist;

import java.net.URI;

import org.apache.http.conn.HttpHostConnectException;
import org.xmlrpc.android.XMLRPCClient;
import org.xmlrpc.android.XMLRPCException;
import org.xmlrpc.android.XMLRPCFault;

import com.android.applications.todoist.containers.SupportCase;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class SupportForm extends Activity {
	private EditText nameText;
	private EditText emailText;
	private EditText problemText;
	private Spinner areaSpinner;
	private Button submitButton;
	private XMLRPCClient client;
	private URI uri;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.support);
		initControls();
	}
	
	private void initControls()
	{
		this.uri = URI.create("http://dev.drewdahl.com/server.php");
		this.client = new XMLRPCClient(uri);
		
		this.nameText = (EditText)findViewById(R.id.EditText_Name);
		this.emailText = (EditText)findViewById(R.id.EditText_Email);
		this.problemText = (EditText)findViewById(R.id.EditText_Problem);
		this.areaSpinner = (Spinner)findViewById(R.id.Spinner_Area);
		this.submitButton = (Button)findViewById(R.id.Button_Submit);
		this.submitButton.setOnClickListener(new Button.OnClickListener() 
				{
					public void onClick(View view)
					{ 
						rpcCall();
					}
				});
	}
	
	private void rpcCall()
	{
		SupportCase newCase = new SupportCase(this.nameText.getText().toString(),this.emailText.getText().toString(),
				this.problemText.getText().toString(), "Some Problem_Data", this.areaSpinner.toString(), "Some Version");
		
		XMLRPCMethod method = new XMLRPCMethod("reportproblem", new XMLRPCMethodCallback() {
			public void callFinished(Object result)
			{
				if((Boolean)result)
				{
					showAlert("Success!","The problem was reported successfully.  If you entered your e-mail address, you should receive an e-mail in the next few hours.","OK",true);
				}
				else
				{
					showAlert("Failure!","The problem failed to be reported.  Please try again shortly.  If this problem persists, please contact Admin@DrewDahl.com.","OK",false);
				}
			}
		});
		
		Object[] params = {
				newCase,				
		};
		
		method.call(params);
	}
	
	private void showAlert(String title, String message, String button_text, Boolean finish)
	{
		AlertDialog.Builder dialog = new AlertDialog.Builder(this).setTitle(title).setMessage(message);
		if(finish)
		{
			dialog.setNeutralButton(button_text, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();							
				}
			});
		}
		else
		{
			dialog.setNeutralButton(button_text, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//Do nothing						
				}
			});
		}
		
		dialog.show();
	}
	
	interface XMLRPCMethodCallback {
		void callFinished(Object result);
	}
	
	class XMLRPCMethod extends Thread {
		private String method;
		private Object[] params;
		private Handler handler;
		private XMLRPCMethodCallback callBack;
		public XMLRPCMethod(String method, XMLRPCMethodCallback callBack) {
			this.method = method;
			this.callBack = callBack;
			handler = new Handler();
		}
		public void call() {
			call(null);
		}
		public void call(Object[] params) {
			Log.i("RPC Call", "Calling host " + uri.getHost());
			//TODO: Show that we're calling the host
			this.params = params;
			start();
		}
		@Override
		public void run() {
    		try {
    			final long t0 = System.currentTimeMillis();
    			final Object result = client.callEx(method, params);
    			final long t1 = System.currentTimeMillis();
    			handler.post(new Runnable() {
					public void run() {
						//TODO: Call went good, run along now...
						Log.i("RPC Call", "XML-RPC call took " + (t1-t0) + "ms");
						callBack.callFinished(result);
					}
    			});
    		} catch (final XMLRPCFault e) {
    			handler.post(new Runnable() {
					public void run() {
						//TODO: Make it known that there was an error
						Log.e("RPC Call", "Fault message: " + e.getFaultString() + "\nFault code: " + e.getFaultCode());
					}
    			});
    		} catch (final XMLRPCException e) {
    			handler.post(new Runnable() {
					public void run() {
						//TODO: Make it known that we were unable to connect.  Try again later...
						
						Throwable couse = e.getCause();
						if (couse instanceof HttpHostConnectException) {
							Log.e("RPC Call", "Cannot connect to " + uri.getHost() + "\nTry again later.  If problem persists, e-mail Admin@DrewDahl.com!");
						} else {
							Log.e("RPC Call", "Error " + e.getMessage());
						}
					}
    			});
    		}
		}
	}
}
