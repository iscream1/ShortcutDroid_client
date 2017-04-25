package com.iscream.shortcutdroid;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.BufferedReader;
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
    PrintWriter output;
    Button connectBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonLL=(LinearLayout)findViewById(R.id.ButtonLL);
        ipET=(EditText)findViewById(R.id.ipET);
        Button camBtn=(Button)findViewById(R.id.camBtn);
        connectBtn=(Button)findViewById(R.id.connectBtn);
        final Intent camIntent=new Intent(getApplicationContext(), ReaderActivity.class);
        camBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(camIntent, 123);
            }
        });

        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                init();
            }
        });

        //init();
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
        /*try{
            socket.close();
        }
        catch (Exception e)
        {
            //e.printStackTrace();
            Log.d("socket", "can't close socket, probably not initialized yet");
        }*/
        if(buttons==null) buttons=new ArrayList<>();
        final String ipAddress=ipET.getText().toString();
        new AsyncTask<Void, Void, Void>(){
            String line = null;
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    if(socket==null) socket = new Socket(ipAddress, 115);

                    out = socket.getOutputStream();
                    output = new PrintWriter(out);

                    Log.d("LOG", "Sending setup request to PC");
                    output.print("setup");
                    output.flush();
                    //output.close();
                    Log.d("LOG", "Setup request sent to PC");
                    //socket.close();

                    //socket = new Socket(ipAddress, 115);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    Log.d("LOG", "reader created");
                    line = in.readLine();
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
                else ProcessSetup(line);
            }
        }.execute();
    }

    private void ProcessSetup(String line)
    {
        buttons.clear();
        buttonLL.removeAllViews();
        String[] commands = line.split("<sprtr>");

        //from 1 because 0th is "setup"
        for(int i=1;i<commands.length;i+=2) //label,command,label,command...
        {
            Button btn = new Button(this);
            btn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            btn.setText(commands[i]); //set label
            btn.setOnClickListener(new StringSenderOnClickListener(commands[i+1], socket)); //set command with socket
            //btnTag.setId();
            buttons.add(btn);
            //buttonLL.addView(btn);
        }

        for(Button btn : buttons)
        {
            buttonLL.addView(btn);
        }

        init();
    }
}
