package com.dsz.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.dsz.threads.MsgThreads;
import com.dsz.wifi.HPWifiSetup;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    public final static String CONTROLER_POSITION = "/remoteCamera/";   //图片保存路径
    public final String TAG = "MainActivity";                                 //log信息
    private boolean mIsServiceRunning = false;              //是否开启服务线程
    SharedPreferences mSharedPreferences;                   //保存数据用的

    private Button btnOpen;
    private Button btnClose;
    private Context context;
    MsgThreads msgThreads;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        apWifiSetting();


        setContentView(R.layout.activity_main);
        context = MainActivity.this;
        initSys();
        //初始化一些参数
        if (!initialize()) {
            Toast.makeText(this, "不能初始化参数", Toast.LENGTH_LONG).show();
        }
        initViews();

        initEvents();

        msgThreads = new MsgThreads(context);
        msgThreads.start();

    }

    /**
     * 按钮点击事件
     */
    private void initEvents() {
        btnOpen.setOnClickListener(this);
        btnClose.setOnClickListener(this);

    }

    /**
     * 视图布局
     */
    private void initViews() {
        btnOpen = (Button) findViewById(R.id.main_btn_open);
        btnClose = (Button)findViewById(R.id.main_btn_close);
    }

    /**
     * 程序本身自动生成的代码 被我抽取出来了
     */
    private void initSys() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void apWifiSetting() {
        HPWifiSetup hpwifi = HPWifiSetup.getInstance(this);
        try {
            hpwifi.setupWifiAp("<01>" + Build.MODEL + "相机","12345678",true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private long firstTime = 0;
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:
                long secondTime = System.currentTimeMillis();
                if(secondTime - firstTime > 2000){
                    Toast.makeText(this, "再按一次将会退出程序", Toast.LENGTH_SHORT).show();
                    firstTime = secondTime;
                    return true;
                }else{
                    System.exit(0);
                }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.main_btn_open:
                openTest();
                break;
            case R.id.main_btn_close:
                break;
        }
    }



    /**
     * 打开测试
     */
    private void openTest() {
        Intent intent = new Intent(context,ControledView.class);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        msgThreads.destroy();
    }

    /**
     * 初始化参数
     *
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private boolean initialize() {

        //每个应用有一个默认的偏好文件preferences.xml，使用getDefaultSharedPreferences获取
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        File fileFolder = new File(Environment.getExternalStorageDirectory()
                + CONTROLER_POSITION);
        if (!fileFolder.exists()) { // 如果目录不存在，则创建一个目录
            fileFolder.mkdir();
        }
        boolean firstRun = !mSharedPreferences.contains("settings_camera");    //判断SharedPreference是否包含特定的settings_camera(key)的数据 如果有返回真 （！真=假）
        if (firstRun) {
            Log.v(TAG, "First run");

            //对SharedPreferences进行编辑
            SharedPreferences.Editor editor = mSharedPreferences.edit();

            //获取camera的数量
            int cameraNumber = Camera.getNumberOfCameras();
            Log.v(TAG, "Camera number: " + cameraNumber);

           /*
            *获取相机名称设置
            */
            TreeSet<String> cameraNameSet = new TreeSet<String>();//跟HashSet是基于HashMap实现一样,TreeSet同样是基于TreeMap实现的。
            if (cameraNumber == 1) {
                cameraNameSet.add("back");
            } else if (cameraNumber == 2) {
                cameraNameSet.add("back");
                cameraNameSet.add("front");
            } else if (cameraNumber > 2) {           // rarely happen
                for (int id = 0; id < cameraNumber; id++) {
                    cameraNameSet.add(String.valueOf(id));
                }
            } else {                                 // no camera available
                Log.v(TAG, "没有相机");
                Toast.makeText(this, "没有相机", Toast.LENGTH_SHORT).show();

                return false;
            }

            // 获取相机id设置
//            String[] cameraIds = new String[cameraNumber];
            TreeSet<String> cameraIdSet = new TreeSet<String>();
            for (int id = 0; id < cameraNumber; id++) {
                cameraIdSet.add(String.valueOf(id));
            }

           /*
            * 保存相机名称和id集合
            */
            editor.putStringSet("camera_name_set", cameraNameSet);
            editor.putStringSet("camera_id_set", cameraIdSet);

           /*
            * 获取和保存相机参数
            */
            for (int id = 0; id < cameraNumber; id++) {
                Camera camera = Camera.open(id);
                if (camera == null) {
                    String msg = "Camera " + id + " is not available";
                    Log.v(TAG, msg);
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

                    return false;
                }
//

                Camera.Parameters parameters = camera.getParameters();

               /*
                * 获取和保存预览大小
                */
                List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();

                TreeSet<String> sizeSet = new TreeSet<String>(new Comparator<String>() {
                    @Override
                    public int compare(String s1, String s2) {
                        int spaceIndex1 = s1.indexOf(" ");
                        int spaceIndex2 = s2.indexOf(" ");
                        int width1 = Integer.parseInt(s1.substring(0, spaceIndex1));
                        int width2 = Integer.parseInt(s2.substring(0, spaceIndex2));

                        return width2 - width1;
                    }
                });
                for (Camera.Size size : sizes) {
                    sizeSet.add(size.width + " x " + size.height);
                }
                editor.putStringSet("preview_sizes_" + id, sizeSet);

                Log.v(TAG, sizeSet.toString());

               /*
                * 设置默认的预览大小,使用相机：0
                */
                if (id == 0) {
                    Log.v(TAG, "Set default preview size");

                    Camera.Size defaultSize = parameters.getPreviewSize();
                    editor.putString("settings_size", defaultSize.width + " x " + defaultSize.height);
                }


               /*
                * 获取和保存
                */
                List<int[]> ranges = parameters.getSupportedPreviewFpsRange();
                TreeSet<String> rangeSet = new TreeSet<String>();
                for (int[] range : ranges) {
                    rangeSet.add(range[0] + " ~ " + range[1]);
                }
                editor.putStringSet("preview_ranges_" + id, rangeSet);

                if (id == 0) {
                    Log.v(TAG, "Set default fps range");

                    int[] defaultRange = new int[2];
                    parameters.getPreviewFpsRange(defaultRange);
                    editor.putString("settings_range", defaultRange[0] + " ~ " + defaultRange[1]);
                }
                camera.release();
            }
            editor.putString("settings_camera", "0");
            editor.commit();
        }

        return true;
    }
}
