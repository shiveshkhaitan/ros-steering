package com.shivesh.robotcontroller;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.DecimalFormat;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    TextView tv;
    ImageView iv;
    String message, msg_odom_x, msg_odom_z, msg_vel_x, msg_vel_z;
    EditText ed, ed_vel_x, ed_vel_z, ed_odom_x, ed_odom_z;
    Double linear = 0.0;
    Double angular = 0.0;
    int count = 0;
    boolean connected = false;
    DecimalFormat df = new DecimalFormat("#0.0");

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = findViewById(R.id.textView);
        ed = findViewById(R.id.editText);
        ed_odom_x = findViewById(R.id.editText5);
        ed_odom_z = findViewById(R.id.editText4);
        ed_vel_x = findViewById(R.id.editText7);
        ed_vel_z = findViewById(R.id.editText8);
        iv = findViewById(R.id.imageView);

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try{
                        Thread.sleep(100);
                    }
                    catch (InterruptedException e){
                        e.printStackTrace();
                    }

                    tv.post(new Runnable() {
                        @Override
                        public void run() {
                            if(connected) {
                                iv.setImageResource(R.mipmap.connected_foreground);
                                ed_vel_x.setText(msg_vel_x);
                                ed_vel_z.setText(msg_vel_z);
                            }else {
                                iv.setImageResource(R.mipmap.disconnected_foreground);
                                ed_vel_x.setText("N/A");
                                ed_vel_z.setText("N/A");
                            }
                            if(count > 5){
                                ed_odom_x.setText("N/A");
                                ed_odom_z.setText("N/A");
                            }
                            else {
                                ed_odom_x.setText(msg_odom_x);
                                ed_odom_z.setText(msg_odom_z);
                            }
                            Receiver receive = new Receiver();
                            receive.execute(ed.getText().toString());
                        }
                    });
                }
            }
        };
        Thread myThread = new Thread(myRunnable);
        myThread.start();

        ed.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Sender send = new Sender();
                send.execute(ed.getText().toString(), "-9999");
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Sender send = new Sender();
                send.execute(ed.getText().toString(), "-9999");
            }
        });
    }

    public void onLeftClick(View v) {
        Sender send = new Sender();
        send.execute(ed.getText().toString(), "left");
    }

    public void onRightClick(View v) {
        Sender send = new Sender();
        send.execute(ed.getText().toString(), "right");
    }

    public void onTopClick(View v) {
        Sender send = new Sender();
        send.execute(ed.getText().toString(), "top");
    }

    public void onBottomClick(View v) {
        Sender send = new Sender();
        send.execute(ed.getText().toString(), "bottom");
    }

    public void onReset(View v){
        Sender send = new Sender();
        send.execute(ed.getText().toString(), "reset");
    }

    public void onClick(View v) {
    }


    class Sender extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                connected = false;
                InetAddress serveraddress = InetAddress.getByName(params[0]);
                int port = 5678;
                Socket sock = new Socket();
                sock.connect(new InetSocketAddress(serveraddress, port), 100);
                if(sock.isConnected())
                    connected = true;
                if(!params[1].equals("-9999")) {
                    OutputStream out = sock.getOutputStream();
                    PrintWriter output = new PrintWriter(out);
                    if(params[1].equals("left"))
                        angular = angular + 0.1;
                    else if(params[1].equals("right"))
                        angular = angular - 0.1;
                    else if(params[1].equals("top"))
                        linear = linear + 0.1;
                    else if(params[1].equals("bottom"))
                        linear = linear - 0.1;
                    else
                        linear = angular = 0.0;
                    String send = df.format(linear) + " " + df.format(angular);
                    output.write(send);
                    output.flush();
                    output.close();
                    out.close();
                    msg_vel_x = df.format(linear);
                    msg_vel_z = df.format(angular);
                }
                sock.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }


    class Receiver extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                count++;
                InetAddress serveraddress = InetAddress.getByName(params[0]);
                int port = 5679;
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(serveraddress,port), 80);
                OutputStream out = socket.getOutputStream();
                PrintWriter output = new PrintWriter(out);
                output.write("Request Odom");
                output.flush();
                InputStream in = socket.getInputStream();
                InputStreamReader input = new InputStreamReader(in);
                BufferedReader br = new BufferedReader(input);
                message = br.readLine();
                msg_odom_x = msg_odom_z = "";
                boolean first = true;
                for(int i = 0; i<message.length(); i++){
                    if(message.charAt(i) == ' '){
                        first = false;
                        continue;
                    }
                    if(first)
                        msg_odom_x += message.charAt(i);
                    else
                        msg_odom_z += message.charAt(i);

                }
                socket.close();
                count = 0;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return null;

        }
    }
}