package com.dsz.threads;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;

import com.dsz.activity.MainActivity;
import com.dsz.activity.TestActivity;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Administrator on 2016/10/12.
 */

public class MyThreads extends Thread{
    public ServerSocket serverSocket;
    public byte[] readBuffer = new byte[1024];
    public int readBufferSize = 0;
    private  Context context;
    public MyThreads(Context context){
        this.context = context;
    }

    @Override
    public void run() {
        Bundle bundle = new Bundle();
        bundle.clear();

        try {
            serverSocket = new ServerSocket(30000);
            while (true){
                Message msg = new Message();
                msg.what = 0x11;

                try{
                    Socket socket = serverSocket.accept();
                    readBufferSize = socket.getInputStream().read(readBuffer);
                    if(readBufferSize == 9){
                        if(readBuffer[0] == 'o'){
                            Intent intent = new Intent(context, TestActivity.class);
                            context.startActivity(intent);
                        }

                        if(readBuffer[0] == 'c'){
                            Intent intent = new Intent(context, MainActivity.class);
                            context.startActivity(intent);
                        }
                    }
                }catch (Exception e){

                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
