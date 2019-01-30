package com.akshanshgusain.activityonly;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button mBindButton, mUnBindButton, mGetRandomNumberButton;
    private TextView mTextView;

    public static final int GET_RANDOM_NUMBER_FLAG=0;

    private Intent mServiceIntent;
    private int randomNumberValue;
    private boolean mIsBound;
    //2. Create 2 Messengers
    //randomNUmberRequestMessenger is used to send the request to the service
    //randomNmberReceiveMessenger is used to handle the received message from the service.
    Messenger randomNumberRequestMessenger, randomNumberReceiveMessenger;

    //4. Create Service intent
    Intent serviceIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBindButton = findViewById(R.id.button_bind_service);
        mUnBindButton = findViewById(R.id.button_unbind_service);
        mGetRandomNumberButton = findViewById(R.id.button_get_random_number);
        mTextView = findViewById(R.id.textView_random_number);

        mBindButton.setOnClickListener(this);
        mUnBindButton.setOnClickListener(this);
        mGetRandomNumberButton.setOnClickListener(this);

        serviceIntent = new Intent();
        serviceIntent.setComponent(new ComponentName("com.akshanshgusain.serviceonly","com.akshanshgusain.serviceonly.MyService"));
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.button_bind_service: bindToRemoteService();
                break;
            case R.id.button_unbind_service:unbindToRemoteService();
                break;
            case R.id.button_get_random_number:
                fetchRandomNumber();
                break;
        }
    }

    private void fetchRandomNumber() {
       if(mIsBound==true)
        {
            Message requestMessage = Message.obtain(null,GET_RANDOM_NUMBER_FLAG);
            requestMessage.replyTo = randomNumberReceiveMessenger;

            try {
                randomNumberRequestMessenger.send(requestMessage);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
       else
        {
           Toast.makeText(this, "Service is not bound", Toast.LENGTH_SHORT).show();
        }

    }

    private void unbindToRemoteService() {
        unbindService(randomNumberServiceConnection);
        mIsBound=false;
        Toast.makeText(this, "Service Unbound", Toast.LENGTH_SHORT).show();

    }

    private void bindToRemoteService() {
        bindService(serviceIntent, randomNumberServiceConnection,BIND_AUTO_CREATE);
        Toast.makeText(this, "Service Bound", Toast.LENGTH_SHORT).show();

    }

    //  1. This ServiceConnection Connection object is used to BIND to the service
    ServiceConnection randomNumberServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //3. initilize the 2 messengers
            randomNumberRequestMessenger = new Messenger(service);
           randomNumberReceiveMessenger = new Messenger(new ReceiveRandomNumberHandler());
            mIsBound = true;


        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            randomNumberRequestMessenger=null;
            randomNumberReceiveMessenger=null;
            mIsBound = false;

        }
    };

    //2.2 Handler class to receive the random number
    private class ReceiveRandomNumberHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg) {
            randomNumberValue = 0;
            switch(msg.what)

            {
                case GET_RANDOM_NUMBER_FLAG:
                      randomNumberValue = msg.arg1;
                       mTextView.setText("Randome Number is: "+randomNumberValue);
                    break;
            }
            super.handleMessage(msg);
        }
    }
}
