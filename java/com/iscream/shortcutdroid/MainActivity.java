package com.iscream.shortcutdroid;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import com.iscream.shortcutdroid.network.*;

public class MainActivity extends AppCompatActivity {

    EditText ipET;
    ArrayList<Button> buttons;
    LinearLayout buttonLL;
    Socket socket;
    OutputStream out;
    InputStream inputStream;
    PrintWriter output;
    BufferedReader in;
    Button connectBtn;
    TextView appNameTV;
    Spinner appsSpinner;
    AsyncTask<Void, Void, Void> task;
    Button camBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonLL=(LinearLayout)findViewById(R.id.ButtonLL);
        ipET=(EditText)findViewById(R.id.ipET);
        camBtn=(Button)findViewById(R.id.camBtn);
        connectBtn=(Button)findViewById(R.id.connectBtn);
        appNameTV=(TextView)findViewById(R.id.AppNameTV);
        appsSpinner=(Spinner)findViewById(R.id.appsSpinner);
        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                init();
            }
        });
        camBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestNeededPermission();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==123&&resultCode==123)
        {
            ipET.setText(data.getStringExtra("code"));
        }
    }

    public void init()
    {
        Log.d("DEBUG", "init");
        try{
            socket.close();
        }
        catch (Exception e)
        {
            //e.printStackTrace();
            Log.d("init", "can't close socket, probably not initialized yet");
        }
        try
        {
            if(task!=null) task.cancel(true);
        }
        catch (Exception e)
        {
            Log.d("init", "can't terminate task, probably not initialized yet");
        }
        if(buttons==null) buttons=new ArrayList<>();
        final String ipAddress=ipET.getText().toString();
        task=new AsyncTask<Void, Void, Void>(){
            String line = null;
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    if(socket==null||socket.isClosed()) socket = new Socket(ipAddress, 115);

                    out = socket.getOutputStream();
                    output = new PrintWriter(out);

                    Log.d("LOG", "Sending setup request to PC");
                    printOut("setup");
                    //output.close();
                    Log.d("LOG", "Setup request sent to PC");
                    //socket.close();

                    //socket = new Socket(ipAddress, 115);
                    inputStream=socket.getInputStream();
                    in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                    Log.d("LOG", "reader created");
                    line = readLine();
                    Log.d("LOG", "line read");

                    Log.d("RECV", line);

                    //socket.close();
                    //Log.d("LOG", "Socket closed");
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                //connectBtn.setEnabled(false);
                super.onPostExecute(aVoid);
                if(line==null) Toast.makeText(getApplicationContext(), "Connection timed out", Toast.LENGTH_SHORT).show();
                else
                {
                    ProcessSetup(line);
                    continueListening();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void ProcessSetup(String line)
    {
        String[] commands = line.split("<sprtr>");
        if(commands[0].equals("setup"))
        {
            buttons.clear();
            buttonLL.removeAllViews();
            appNameTV.setText(commands[1]);
            if(appsSpinner.getAdapter()!=null&&!commands[1].equals(appsSpinner.getSelectedItem().toString())) appsSpinner.setSelection(0);

            //from 2 because 0th is "setup", 1st is app name
            for(int i=2;i<commands.length;i+=2) //label,command,label,command...
            {
                Button btn = new Button(this);
                btn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                btn.setText(commands[i]); //set label
                btn.setOnClickListener(new StringSenderOnClickListener(commands[i+1], output)); //set command with socket
                //btnTag.setId();
                buttons.add(btn);
                //buttonLL.addView(btn);
            }

            for(Button btn : buttons)
            {
                buttonLL.addView(btn);
            }
        }
        else if(commands[0].equals("apps"))
        {
            commands[0]="(Select app)";
            ArrayAdapter<CharSequence> adapter = new ArrayAdapter(this,
                    android.R.layout.simple_spinner_item, commands);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            appsSpinner.setAdapter(adapter);
            appsSpinner.setVisibility(View.VISIBLE);

            appsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, final View selectedItemView, final int position, long id) {
                    new AsyncTask<Void, Void, Void>(){
                        @Override
                        protected Void doInBackground(Void... voids) {
                            try {
                                Log.d("spinner", ""+position);
                                if(position!=0)
                                {
                                    printOut("selected<sprtr>"+(position-1));
                                }
                            }
                            catch(Exception e)
                            {
                                e.printStackTrace();
                            }
                            return null;
                        }
                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {}
            });
        }
    }

    private void continueListening()
    {
        final String ipAddress=ipET.getText().toString();
        task=new AsyncTask<Void, Void, Void>(){
            String line = null;
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    while(inputStream.available()==0){
                        Thread.sleep(100);
                    }
                    line = readLine();
                    Log.d("LOG", "line read");

                    Log.d("RECV", line);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if(line==null) Toast.makeText(getApplicationContext(), "Connection timed out", Toast.LENGTH_SHORT).show();
                else
                {
                    ProcessSetup(line);
                    continueListening();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void printOut(String toPrint)
    {
        output.print(toPrint);
        output.flush();
    }

    private String readLine()
    {
        try
        {
            return in.readLine();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static final int REQUEST_CODE_CAMERA_PERMISSION = 3541;
    public void requestNeededPermission() {
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA))
            {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        REQUEST_CODE_CAMERA_PERMISSION);
            }
            else ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CODE_CAMERA_PERMISSION);
        } else
        {
            final Intent camIntent=new Intent(getApplicationContext(), ReaderActivity.class);
            startActivityForResult(camIntent, 123);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[],int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_CAMERA_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {
                    final Intent camIntent=new Intent(getApplicationContext(), ReaderActivity.class);
                    startActivityForResult(camIntent, 123);
                } else {
                    Toast.makeText(this, "Can't get camera permission", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }
}
