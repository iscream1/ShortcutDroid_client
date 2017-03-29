package com.iscream.shortcutdroid.network;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.iscream.shortcutdroid.R;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;


public class StringSenderOnClickListener implements View.OnClickListener {
            String toSend;
            final Socket socket;

            public StringSenderOnClickListener(String out, Socket socket) {
                super();
                toSend=out;
                this.socket=socket;
            }

            @Override
            public void onClick(View view) {
                //final String ipAddress=((EditText)view.findViewById(R.id.ipET)).getText().toString();
                new AsyncTask<Void, Void, Void>(){
                    @Override
                    protected Void doInBackground(Void... voids) {
                        try {
                            //Socket socket;
                            //socket = new Socket(ipAddress, 115);

                            OutputStream out = socket.getOutputStream();
                            PrintWriter output = new PrintWriter(out);

                            Log.d("LOG", "Sending Data to PC");
                            output.print(toSend);
                            output.flush();
                            //output.close();
                            Log.d("LOG", "Data sent to PC");
                            //socket.close();
                            //Log.d("LOG", "Socket closed");
                        }
                        catch(Exception e)
                        {
                            e.printStackTrace();
                        }
                        return null;
                    }
                }.execute();
            }
        }