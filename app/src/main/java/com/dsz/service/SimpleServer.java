package com.dsz.service;

import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class SimpleServer implements Runnable {
    public final static String TAG = "SimpleServer";

    //ArrayList 的一个线程安全的变体,其中所有可变操作(add、set等等)都是通过对底层数组进行一次新的复制来实现的。
    private final CopyOnWriteArrayList<ConnectionThread> mConnectionThreads =
            new CopyOnWriteArrayList<ConnectionThread>();

    private Callback mCallback = null;
    
    private ServerSocket mServer;
    private volatile boolean mStopServer = true;    //volatile ： 用来修饰被不同线程访问和修改的变量
    private Thread mServerThread;

    public interface Callback {
        public void onConnect();

        public void onDisconnect();
    }
    
    protected abstract void handleConnection(Socket socket) throws IOException;

    public void run() {
        Log.v(TAG, "Server is running");
        
        while (!mStopServer) {
            try {
                Socket socket = mServer.accept();       //（启动完相机服务器之后，线程会在这里进行监听客户端是否连接上，如果连接上线程继续执行，否则进入等待监听状态，也就是停在这里，但相机的预览不会停止）
                
                Log.v(TAG, "Accept socket: " + socket.getPort());
                if (!mStopServer) {
                    startConnectionThread(socket);          //开启连接线程
                    Log.d(TAG,"程序线程系");
                } else {
                    socket.close();
                }
            } catch (IOException e) {
                if (!mStopServer) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
    }

    /**
     * 开始线程
     * @param port  ：端口号
     * @throws IOException
     */
    public void start(int port) throws IOException {
        mServer = new ServerSocket(port);
        
        mStopServer = false;
        mServerThread = new Thread(this);
        mServerThread.start();      //开启线程，执行run()方法
    }
    
    public void close() {
        mStopServer = true;
        if (mServer != null) {
            try {
                mServer.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        
        for (ConnectionThread connectionThread : mConnectionThreads) {
            connectionThread.close();
        }
        
        mCallback = null;
    }
    
    public void setCallback(Callback callback) {
        mCallback = callback;
    }
    
    public int getNumberOfConnections() {
        return mConnectionThreads.size();
    }
    
    
    private final class ConnectionThread extends Thread {
        private final Socket mmSocket;
        
        private ConnectionThread(Socket socket) {
            setName("SimpleServer ConnectionThread" + getId());
//            Log.v(TAG,getId() + "DDDD");
            mmSocket = socket;
        }
        

        @Override
        public void run() {
            Log.v(TAG, "Server thread " + getId() + " started");
            
            if (mCallback != null) {
                Log.i(TAG,"mCallback已经不为空了");
                mCallback.onConnect();
            }else{
                Log.i(TAG,"mCallback处于空状态");
            }
            
            try {
                handleConnection(mmSocket);
            } catch (IOException e) {
                if (!mStopServer) {
                    Log.e(TAG, e.getMessage());
                }
            }
            
            if (mCallback != null) {
                mCallback.onDisconnect();
            }
            
            Log.v(TAG, "Server thread " + getId() + " died");
        }
        
        private void close() {
            if (mmSocket != null) {
                try {
                    mmSocket.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
    }
    
    private void startConnectionThread(final Socket socket) {
        ConnectionThread connectionThread = new ConnectionThread(socket);
        mConnectionThreads.add(connectionThread);       //将线程添加到集合中
        connectionThread.start();           //开启ConnectionThread线程，并执行ConnectionThread类中的run()方法
    }
}
