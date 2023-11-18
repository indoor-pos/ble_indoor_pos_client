package com.ckos.ble.ui.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.ckos.ble.MainActivity;
import com.ckos.ble.R;

import java.math.BigDecimal;
import java.util.List;

public class SecondFragment extends Fragment {
    public static View root = null;
    private SharedPreferences sharedPreferences = null;
    private SharedPreferences.Editor editor = null;
    private AlertDialog.Builder builder = null;
    private AlertDialog alertDialog = null;
    private PendingIntent sentPI = null, deliverPI = null;
    private ScanCallback mScanCallback = null;

    private String name;
    private int one_meter_rssi;
    private float n_value;
    private String phone_number;
    private float distance;
    private boolean flag_send = false;

    private TextView position;
    private EditText name_et;
    private EditText one_meter_rssi_et;
    private EditText n_value_et;
    private Button position_button;
    private EditText phone_number_et;
    private EditText distance_et;
    private Button sms_button;

    public static SecondFragment newInstance(int index) {
        SecondFragment fragment = new SecondFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, final ViewGroup container,
            Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_tab_2, container, false);

        builder = new AlertDialog.Builder(getActivity());

        position = (TextView) root.findViewById(R.id.position);

        sharedPreferences = getActivity().getSharedPreferences("share", getActivity().MODE_PRIVATE);
        editor = sharedPreferences.edit();

//        name = sharedPreferences.getString("name", "BTLE Precision Mouse");
        name = sharedPreferences.getString("name", "HC-08");
        name_et = (EditText) root.findViewById(R.id.name);
        name_et.setText(name);
        one_meter_rssi = sharedPreferences.getInt("one_meter_rssi", 69);
        one_meter_rssi_et = (EditText) root.findViewById(R.id.one_meter_rssi);
        one_meter_rssi_et.setText(String.valueOf(one_meter_rssi));
        n_value = sharedPreferences.getFloat("n_value", 2.5f);
        n_value_et = (EditText) root.findViewById(R.id.n_value);
        n_value_et.setText(String.valueOf(n_value));

        position_button = (Button) root.findViewById(R.id.position_button);
        position_button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position_button.getText().toString().equals("开始定位")) {
                    String deviceName = name_et.getText().toString();
                    if (deviceName.equals("")) {
                        showInfo("请输入设备名！");
                        return;
                    }
                    try {
                        one_meter_rssi = Integer.parseInt(one_meter_rssi_et.getText().toString());
                    } catch (Exception e) {
                        showInfo("请输入有效的1米Rssi！");
                        return;
                    }
                    try {
                        n_value = Float.parseFloat(n_value_et.getText().toString());
                    } catch (Exception e) {
                        showInfo("请输入有效的n_Value！");
                        return;
                    }
                    name = deviceName;
                    editor.putString("name", name);
                    editor.putInt("one_meter_rssi", one_meter_rssi);
                    editor.putFloat("n_value", n_value);
                    editor.apply();

                    name_et.setEnabled(false);
                    one_meter_rssi_et.setEnabled(false);
                    n_value_et.setEnabled(false);
                    sms_button.setEnabled(false);
                    if (!flag_send) {
                        phone_number_et.setEnabled(false);
                        distance_et.setEnabled(false);
                    }
                    position_button.setText(R.string.position_button_stop);
                    position.setTextColor(getResources().getColor(R.color.BLACK));
                    position.setText(R.string.device_not_found);
                    startScan();
                } else {
                    name_et.setEnabled(true);
                    one_meter_rssi_et.setEnabled(true);
                    n_value_et.setEnabled(true);
                    sms_button.setEnabled(true);
                    if (sms_button.getText().toString().equals("确认")) {
                        phone_number_et.setEnabled(true);
                        distance_et.setEnabled(true);
                    } else {
                        flag_send = true;
                    }
                    position_button.setText(R.string.position_button_start);
                    position.setTextColor(getResources().getColor(R.color.colorOff));
                    stopScan();
                }
            }
        });

        mScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                BluetoothDevice device = result.getDevice();
                String deviceName = device.getName();
                if (deviceName == null || !deviceName.equals(name)) {
                    return;
                }
                int rssi = result.getRssi();
                double dst = getDistance(rssi);
                if (flag_send && dst <= distance) {
                    sendSMS(phone_number, "距离 " + deviceName + " 小于 " + distance + " 米！");
                    flag_send = false;
                }
                position.setText("RSSI: " + rssi + ", DST: " + dst);
            }
        };

        phone_number = sharedPreferences.getString("phone_number", " ");
        phone_number_et = (EditText) root.findViewById(R.id.phone_number);
        phone_number_et.setText(phone_number);
        distance = sharedPreferences.getFloat("distance", 0.2f);
        distance_et = (EditText) root.findViewById(R.id.distance);
        distance_et.setText(String.valueOf(distance));

        sms_button = (Button) root.findViewById(R.id.sms_button);
        sms_button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sms_button.getText().toString().equals("确认")) {
                    String number = phone_number_et.getText().toString();
                    if (number.equals("")) {
                        showInfo("请输入手机号码");
                        return;
                    }
                    int lengh = number.length();
                    if (lengh != 11) {
                        showInfo("请输入11位手机号码");
                        return;
                    }
                    int i = 0;
                    while (i < lengh) {
                        if (!(number.charAt(i) >= '0' && number.charAt(i) <= '9')) {
                            break;
                        }
                        i++;
                    }
                    if (i != lengh) {
                        showInfo("请输入有效数字");
                        return;
                    }
                    try {
                        distance = Float.parseFloat(distance_et.getText().toString());
                    } catch (Exception e) {
                        showInfo("请输入有效距离");
                        return;
                    }
                    phone_number = number;
                    editor.putString("phone_number", phone_number);
                    editor.putFloat("distance", distance);
                    editor.apply();

                    phone_number_et.setEnabled(false);
                    distance_et.setEnabled(false);
                    sms_button.setText(R.string.cancel_button);
                    flag_send = true;
                } else {
                    phone_number_et.setEnabled(true);
                    distance_et.setEnabled(true);
                    sms_button.setText(R.string.confirm_button);
                    flag_send = false;
                }
            }
        });

        String SENT_SMS_ACTION = "SENT_SMS_ACTION";
        Intent sentIntent = new Intent(SENT_SMS_ACTION);
        sentPI = PendingIntent.getBroadcast(getContext(), 0, sentIntent, 0);
        getContext().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context _context, Intent _intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getContext(), "短信发送成功", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        break;
                }
            }
        }, new IntentFilter(SENT_SMS_ACTION));

        String DELIVERED_SMS_ACTION = "DELIVERED_SMS_ACTION";
        Intent deliverIntent = new Intent(DELIVERED_SMS_ACTION);
        deliverPI = PendingIntent.getBroadcast(getContext(), 0, deliverIntent, 0);
        getContext().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context _context, Intent _intent) {
                Toast.makeText(getContext(), "收信人已经成功接收", Toast.LENGTH_SHORT).show();
            }
        }, new IntentFilter(DELIVERED_SMS_ACTION));

        return root;
    }

    private void showInfo(String info) {
        builder.setTitle("提示");
        builder.setMessage(info);
        builder.setPositiveButton("确定", null);
        alertDialog = builder.create();
        alertDialog.show();
    }

    private void sendSMS(String phoneNumber, String message) {
        SmsManager smsManager = SmsManager.getDefault();
        List<String> divideContents = smsManager.divideMessage(message);
        for (String text : divideContents) {
            smsManager.sendTextMessage(phoneNumber, null, text, sentPI, deliverPI);
        }
    }

    private void startScan() {
        if (!MainActivity.checkBluetooth()) {
            Toast.makeText(getActivity(), "该手机不支持蓝牙", Toast.LENGTH_LONG).show();
            return;
        }
        if (!MainActivity.openBluetooth(getActivity())) {
            return;
        }
        MainActivity.mBluetoothLeScanner.startScan(mScanCallback);
    }

    private void stopScan() {
        MainActivity.mBluetoothLeScanner.stopScan(mScanCallback);
    }

    private double getDistance(int rssi) {
        double power = (Math.abs(rssi) - one_meter_rssi) / (10 * n_value);
        return twochg(Math.pow(10, power));
    }

    private double twochg(double lf){
        try{
            BigDecimal bg = new BigDecimal(lf);
            return bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        }catch (Exception e){
            return 0.0f;
        }
    }
}