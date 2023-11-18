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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.ckos.ble.MainActivity;
import com.ckos.ble.Point;
import com.ckos.ble.R;

import java.math.BigDecimal;
import java.util.List;
import java.util.ResourceBundle;

import static java.lang.Math.sqrt;

public class ThirdFragment extends Fragment {
    public static View root = null;
    private SharedPreferences sharedPreferences = null;
    private SharedPreferences.Editor editor = null;
    private AlertDialog.Builder builder = null;
    private AlertDialog alertDialog = null;
    private PendingIntent sentPI = null, deliverPI = null;
    private ScanCallback mScanCallback = null;

    private String name_1;
    private double x1;
    private double y1;
    private String name_2;
    private double x2;
    private double y2;
    private String name_3;
    private double x3;
    private double y3;
    private String phone_number;
    private float distance;
    private boolean flag_send = false;
    private int one_meter_rssi;
    private float n_value;
    private double[] distances;
    private Point[] points;

    private TextView device_1_state;
    private TextView device_2_state;
    private TextView device_3_state;
    private TextView position;
    private EditText name_et_1;
    private EditText x_point_et_1;
    private EditText y_point_et_1;
    private EditText name_et_2;
    private EditText x_point_et_2;
    private EditText y_point_et_2;
    private EditText name_et_3;
    private EditText x_point_et_3;
    private EditText y_point_et_3;
    private Button position_button;
    private EditText phone_number_et;
    private EditText distance_et;
    private Button sms_button;


    public static ThirdFragment newInstance(int index) {
        ThirdFragment fragment = new ThirdFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_tab_3, container, false);

        builder = new AlertDialog.Builder(getActivity());

        device_1_state = (TextView) root.findViewById(R.id.device_1_state);
        device_2_state = (TextView) root.findViewById(R.id.device_2_state);
        device_3_state = (TextView) root.findViewById(R.id.device_3_state);
        position = (TextView) root.findViewById(R.id.position);

        sharedPreferences = getActivity().getSharedPreferences("share", getActivity().MODE_PRIVATE);
        editor = sharedPreferences.edit();

        name_1 = sharedPreferences.getString("name_1", "HC-08");
        name_et_1 = (EditText) root.findViewById(R.id.name_1);
        name_et_1.setText(name_1);
        x1 = twochg(sharedPreferences.getFloat("x1", 10.0f));
        x_point_et_1 = (EditText) root.findViewById(R.id.x_point_1);
        x_point_et_1.setText(String.valueOf(x1));
        y1 = twochg(sharedPreferences.getFloat("y1", 10.0f));
        y_point_et_1 = (EditText) root.findViewById(R.id.y_point_1);
        y_point_et_1.setText(String.valueOf(y1));

        name_2 = sharedPreferences.getString("name_2", "HC-05");
        name_et_2 = (EditText) root.findViewById(R.id.name_2);
        name_et_2.setText(name_2);
        x2 = twochg(sharedPreferences.getFloat("x2", 10.0f));
        x_point_et_2 = (EditText) root.findViewById(R.id.x_point_2);
        x_point_et_2.setText(String.valueOf(x2));
        y2 = twochg(sharedPreferences.getFloat("y2", 50.0f));
        y_point_et_2 = (EditText) root.findViewById(R.id.y_point_2);
        y_point_et_2.setText(String.valueOf(y2));

        name_3 = sharedPreferences.getString("name_3", "HC-01");
        name_et_3 = (EditText) root.findViewById(R.id.name_3);
        name_et_3.setText(name_3);
        x3 = twochg(sharedPreferences.getFloat("x3", 50.0f));
        x_point_et_3 = (EditText) root.findViewById(R.id.x_point_3);
        x_point_et_3.setText(String.valueOf(x3));
        y3 = twochg(sharedPreferences.getFloat("y3", 10.0f));
        y_point_et_3 = (EditText) root.findViewById(R.id.y_point_3);
        y_point_et_3.setText(String.valueOf(y3));

        position_button = (Button) root.findViewById(R.id.position_button);
        position_button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position_button.getText().toString().equals("开始定位")) {
                    name_1 = name_et_1.getText().toString();
                    String x_point_1 = x_point_et_1.getText().toString();
                    String y_point_1 = y_point_et_1.getText().toString();
                    if (!checkContent(name_1, x_point_1, y_point_1)) {
                        return;
                    }
                    name_2 = name_et_2.getText().toString();
                    String x_point_2 = x_point_et_2.getText().toString();
                    String y_point_2 = y_point_et_2.getText().toString();
                    if (!checkContent(name_2, x_point_2, y_point_2)) {
                        return;
                    }
                    name_3 = name_et_3.getText().toString();
                    String x_point_3 = x_point_et_3.getText().toString();
                    String y_point_3 = y_point_et_3.getText().toString();
                    if (!checkContent(name_3, x_point_3, y_point_3)) {
                        return;
                    }
                    if (name_1.equals(name_2) || name_2.equals(name_3) || name_3.equals(name_1)) {
                        showInfo("需要三个不同的信源");
                        return;
                    }
                    x1 = Double.parseDouble(x_point_1);
                    y1 = Double.parseDouble(y_point_1);
                    x2 = Double.parseDouble(x_point_2);
                    y2 = Double.parseDouble(y_point_2);
                    x3 = Double.parseDouble(x_point_3);
                    y3 = Double.parseDouble(y_point_3);
                    points = new Point[3];
                    points[0] = new Point(x1, y1);
                    points[1] = new Point(x2, y2);
                    points[2] = new Point(x3, y3);

                    editor.putString("name_1", name_1);
                    editor.putFloat("x1", (float) x1);
                    editor.putFloat("y1", (float) y1);
                    editor.putString("name_2", name_2);
                    editor.putFloat("x2", (float) x2);
                    editor.putFloat("y2", (float) y2);
                    editor.putString("name_3", name_3);
                    editor.putFloat("x3", (float) x3);
                    editor.putFloat("y3", (float) y3);
                    editor.apply();

                    one_meter_rssi = sharedPreferences.getInt("one_meter_rssi", 69);
                    n_value = sharedPreferences.getFloat("n_value", 2.5f);
                    distances = new double[] {-1, -1, -1};

                    setEditable(false);
                    sms_button.setEnabled(false);
                    if (!flag_send) {
                        phone_number_et.setEnabled(false);
                        distance_et.setEnabled(false);
                    }
                    position_button.setText(R.string.position_button_stop);
                    device_1_state.setTextColor(getResources().getColor(R.color.BLACK));
                    device_2_state.setTextColor(getResources().getColor(R.color.BLACK));
                    device_3_state.setTextColor(getResources().getColor(R.color.BLACK));
                    position.setTextColor(getResources().getColor(R.color.BLACK));
                    device_1_state.setText("1.Rssi: 无");
                    device_2_state.setText("2.Rssi: 无");
                    device_3_state.setText("3.Rssi: 无");
                    position.setText("缺少有效信源");
                    startScan();
                } else {
                    setEditable(true);
                    sms_button.setEnabled(true);
                    if (sms_button.getText().toString().equals("确认")) {
                        phone_number_et.setEnabled(true);
                        distance_et.setEnabled(true);
                    } else {
                        flag_send = true;
                    }
                    position_button.setText(R.string.position_button_start);
                    device_1_state.setTextColor(getResources().getColor(R.color.colorOff));
                    device_2_state.setTextColor(getResources().getColor(R.color.colorOff));
                    device_3_state.setTextColor(getResources().getColor(R.color.colorOff));
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
                if (deviceName == null) {
                    return;
                }
                int rssi = result.getRssi();
                if (deviceName.equals(name_1)) {
                    device_1_state.setText("1.Rssi: " + rssi);
                    distances[0] = getDistance(rssi);
                } else if (deviceName.equals(name_2)) {
                    device_2_state.setText("2.Rssi: " + rssi);
                    distances[1] = getDistance(rssi);
                } else if (deviceName.equals((name_3))) {
                    device_3_state.setText("3.Rssi: " + rssi);
                    distances[2] = getDistance(rssi);
                } else {
                    return;
                }
                double dst = getDistance(rssi);
                if (flag_send && dst <= distance) {
                    sendSMS(phone_number, "距离 " + deviceName + " 小于 " + distance + " 米！");
                    flag_send = false;
                }
                int i = 0;
                while (i < distances.length) {
                    if (distances[i] == -1) {
                        break;
                    }
                    i++;
                }
                if (i == distances.length) {
                    Point result_p = threePoints(distances, points);
                    position.setText("X: " + twochg(result_p.getX()) + ", Y: " + twochg(result_p.getY()));
                }
            }
        };

        phone_number = sharedPreferences.getString(" ", " ");
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

    private void setEditable(boolean flag) {
        name_et_1.setEnabled(flag);
        x_point_et_1.setEnabled(flag);
        y_point_et_1.setEnabled(flag);
        name_et_2.setEnabled(flag);
        x_point_et_2.setEnabled(flag);
        y_point_et_2.setEnabled(flag);
        name_et_3.setEnabled(flag);
        x_point_et_3.setEnabled(flag);
        y_point_et_3.setEnabled(flag);
    }

    private boolean checkContent(String checkName, String checkX, String checkY) {
        if (checkName.equals("")) {
            showInfo("请输入设备名！");
            return false;
        }
        try {
            Double.parseDouble(checkX);
        } catch (Exception e) {
            showInfo("请输入有效的X坐标！");
            return false;
        }
        try {
            Double.parseDouble(checkY);
        } catch (Exception e) {
            showInfo("请输入有效的Y坐标！");
            return false;
        }
        return true;
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

    private void stopScan() {
        MainActivity.mBluetoothLeScanner.stopScan(mScanCallback);
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

    private Point threePoints(double dis[], Point ps[]) {

        Point p = new Point(0, 0);

        double px = 0;
        double py = 0;

        //dis[0]=-64;
        //dis[1]=-54;
        //dis[2]=-50;

        //System.out.println("====!>");
        for (int i = 0; i < 3; ++i) {
            //检查距离是否有问题
            if (dis[i] < 0)
                return p;
            for (int j = i + 1; j < 3; ++j) {
                //圆心距离3
                float p2p = (float)sqrt((ps[i].getX() - ps[j].getX())*(ps[i].getX() - ps[j].getX()) +
                        (ps[i].getY() - ps[j].getY())*(ps[i].getY() - ps[j].getY()));
                //判断两圆是否相交
                if (dis[i] + dis[j] <= p2p) {
                    //不相交，按比例求
                    px += ps[i].getX() + (ps[j].getX() - ps[i].getX())*dis[i] / (dis[i] + dis[j]);
                    py += ps[i].getY() + (ps[j].getY() - ps[i].getY())*dis[i] / (dis[i] + dis[j]);
                }
                else {
                    //相交则套用公式（上面推导出的）
                    double dr = p2p / 2 + (dis[i] * dis[i] - dis[j] * dis[j]) / (2 * p2p);
                    px += ps[i].getX() + (ps[j].getX() - ps[i].getX())*dr / p2p;
                    py += ps[i].getY() + (ps[j].getY() - ps[i].getY())*dr / p2p;
                }
            }
        }

        //三个圆两两求点，最终得到三个点，求其均值
        px /= 3;
        py /= 3;

        System.out.println("====!"+px+"_"+py);
        p.setX(px);
        p.setY(py);

        return p;
    }
}