package com.ckos.ble;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.ckos.ble.ui.main.FirstFragment;
import com.ckos.ble.ui.main.SecondFragment;
import com.ckos.ble.ui.main.ThirdFragment;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.ckos.ble.ui.main.SectionsPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static List<Bluetooth> bt_list = null;
    public static List<Bluetooth> ble_list = null;
    public static MyAdapter btAdapter = null;
    public static MyAdapter bleAdapter = null;
    public static BluetoothAdapter mBluetoothAdapter = null;
    public static BluetoothLeScanner mBluetoothLeScanner = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_main);

        bt_list = new ArrayList<Bluetooth>();
        btAdapter = new MyAdapter(this, R.layout.list_item, bt_list);
        ble_list = new ArrayList<Bluetooth>();
        bleAdapter = new MyAdapter(this, R.layout.list_item, ble_list);

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        Button scan_button = (Button) FirstFragment.root.findViewById(R.id.scan_button);
                        if (scan_button.getText().toString().equals("停止扫描")) {
                            Toast.makeText(MainActivity.this, tab.getText() + "暂停", Toast.LENGTH_LONG).show();
                            scan_button.callOnClick();
                        }
                        break;
                    case 1:
                        Button single_position_button = (Button) SecondFragment.root.findViewById(R.id.position_button);
                        if (single_position_button.getText().toString().equals("停止定位")) {
                            Toast.makeText(MainActivity.this, tab.getText() + "暂停", Toast.LENGTH_LONG).show();
                            single_position_button.callOnClick();
                        }
                        break;
                    case 2:
                        Button three_position_button = (Button) ThirdFragment.root.findViewById(R.id.position_button);
                        if (three_position_button.getText().toString().equals("停止定位")) {
                            Toast.makeText(MainActivity.this, tab.getText() + "暂停", Toast.LENGTH_LONG).show();
                            three_position_button.callOnClick();
                        }
                        break;
                    default:
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        checkfPermission();
        if(checkBluetooth()) {
            openBluetooth(this);
        } else {
            Toast.makeText(this, "该手机不支持蓝牙", Toast.LENGTH_LONG).show();
        }
    }

    public void checkfPermission() {
        String[] check_permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.SEND_SMS};
        List<String> request_permissions_list = new ArrayList<String>();
        for (String permission : check_permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                request_permissions_list.add(permission);
            }
        }
        if (request_permissions_list.size() > 0) {
            String[] request_permissions = request_permissions_list.toArray(new String[request_permissions_list.size()]);
            ActivityCompat.requestPermissions(this, request_permissions, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i < permissions.length; i++) {
            switch (permissions[i]) {
                case Manifest.permission.ACCESS_COARSE_LOCATION:
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "位置权限申请成功", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "位置权限申请失败", Toast.LENGTH_LONG).show();
                    }
                    break;
                case Manifest.permission.SEND_SMS:
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "发送短信权限申请成功", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "发送短信权限申请失败", Toast.LENGTH_LONG).show();
                    }
                    break;
                default:
            }
        }
    }

    public static boolean checkBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            return false;
        }
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        return true;
    }

    public static boolean openBluetooth(Activity activity) {
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, 1);
            return false;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == -1) {
                    Toast.makeText(this, "蓝牙打开成功", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "蓝牙打开失败", Toast.LENGTH_LONG).show();
                }
                break;
            default:
        }
    }
}