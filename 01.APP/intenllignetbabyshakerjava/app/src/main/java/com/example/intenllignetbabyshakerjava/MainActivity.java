package com.example.intenllignetbabyshakerjava;

import static com.example.intenllignetbabyshakerjava.utils.Common.DeviceOnline;
import static com.example.intenllignetbabyshakerjava.utils.Common.PushTopic;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.blankj.utilcode.util.LogUtils;
import com.example.intenllignetbabyshakerjava.bean.DataDTO;
import com.example.intenllignetbabyshakerjava.bean.Receive;
import com.example.intenllignetbabyshakerjava.bean.Send;
import com.example.intenllignetbabyshakerjava.customizeview.CustomBottomSheetDialogFragment;
import com.example.intenllignetbabyshakerjava.databinding.ActivityMainBinding;
import com.example.intenllignetbabyshakerjava.db.Nurse;
import com.example.intenllignetbabyshakerjava.db.NurseDao;
import com.example.intenllignetbabyshakerjava.utils.BeatingAnimation;
import com.example.intenllignetbabyshakerjava.utils.Common;
import com.example.intenllignetbabyshakerjava.utils.DeviceIsOnline;
import com.example.intenllignetbabyshakerjava.utils.HandlerAction;
import com.example.intenllignetbabyshakerjava.utils.MToast;
import com.example.intenllignetbabyshakerjava.utils.TimeCycle;
import com.google.gson.Gson;
import com.gyf.immersionbar.ImmersionBar;
import com.itfitness.mqttlibrary.MQTTHelper;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements HandlerAction {
    private ActivityMainBinding binding;
    private boolean isDebugView = false;//是否显示debug界面
    private final List<String> arrayList = new ArrayList<String>();// debug消息数据
    private ArrayAdapter adapter = null; // debug消息适配器
    private NurseDao dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        dao = new NurseDao(this);
        getPermission();
        initView();
        mqttConfig();
        isOnline();
    }

    /***
     * 判断硬件是否在线
     */
    private void isOnline() {
        new Thread(() -> {
            try {
                while (true) {
                    DeviceIsOnline.getOnline();
                    Thread.sleep(5000);
                    runOnUiThread(() -> binding.online.setText(DeviceOnline ? "在线" : "离线"));
                    Thread.sleep(10000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }).start();

    }

    private void initView() {
        setSupportActionBar(findViewById(R.id.toolbar));
        ImmersionBar.with(this).init();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayList);
        binding.debugView.setAdapter(adapter);

        eventConfig();
    }

    /***
     * 接收数据处理
     * @param data
     */
    private void analysisOfData(Receive data) {
        try {
            if (data.getHumi() != null) {
                binding.humiText.setText(data.getHumi());
            }

            if (data.getTemp() != null) {
                binding.tempText.setText(data.getTemp());
            }
            if (data.getLight() != null) {
                binding.lightText.setText(data.getLight());
            }
            if (data.getEat() != null) {
                binding.eatText.setText(data.getEat().equals("0") ? "正在进食" : "未进食");
            }
            if (data.getLed() != null) {
                binding.ledSwitch.setSelected(data.getLed().equals("1"));
                binding.ledText.setText(data.getLed().equals("1") ? "开启" : "关闭");
            }
            if (data.getMusic() != null) {
                binding.musicSwitch.setSelected(data.getMusic().equals("1"));
                binding.musicText.setText(data.getMusic().equals("1") ? "开启" : "关闭");
            }
            if (data.getBed() != null) {
                binding.bedSwitch.setSelected(data.getBed().equals("1"));
                binding.bedText.setText(data.getBed().equals("1") ? "开启" : "关闭");
            }
            if (data.getMusic_s() != null) {
                binding.musicCheck.setSelection(Integer.parseInt(data.getMusic_s()) - 1, false);
            }
            if (data.getWarning() != null) {
                warringLayout(data.getWarning().equals("1"), "婴儿醒啦");
            }
        } catch (Exception e) {
            e.printStackTrace();
            MToast.mToast(this, "数据解析失败");
        }
    }

    /**
     * 警告弹窗
     *
     * @param visibility
     * @param str
     */
    private void warringLayout(boolean visibility, String str) {
        if (visibility) {
            binding.warringLayout.setVisibility(View.VISIBLE);
            binding.warringText.setText(str);
            new BeatingAnimation().onAnimation(binding.warringImage);
        } else {
            binding.warringLayout.setVisibility(View.GONE);
        }
    }

    /**
     * 发送数据
     *
     * @param cmd
     * @param message
     */
    private void sendMessage(int cmd, String... message) {
        if (Common.mqttHelper.getConnected() && Common.mqttHelper.getSubscription()) {
            String str = "";
            DataDTO dto = new DataDTO();
            Send send = new Send();
            switch (cmd) {
                case 1:
                    dto.setMusic_s(Integer.parseInt(message[0]));
                    send.setCmd(cmd);
                    send.setData(dto);
                    str = new Gson().toJson(send);
                    break;
                case 2:
                    dto.setMusic(Integer.parseInt(message[0]));
                    send.setCmd(cmd);
                    send.setData(dto);
                    str = new Gson().toJson(send);
                    break;
                case 3:
                    dto.setLed(Integer.parseInt(message[0]));
                    send.setCmd(cmd);
                    send.setData(dto);
                    str = new Gson().toJson(send);
                    break;
                case 4:
                    dto.setBed(Integer.parseInt(message[0]));
                    send.setCmd(cmd);
                    send.setData(dto);
                    str = new Gson().toJson(send);
                    break;

            }
            Common.mqttHelper.publish(PushTopic, str, 1);
            debugViewData(1, str);
        }
    }

    /***
     * 控件监听
     */
    private void eventConfig() {
        binding.warringLayout.setOnClickListener(view -> warringLayout(false, ""));

        binding.addRecord.setOnClickListener(view -> {
            Nurse data = new Nurse();
            data.setNdatetime(TimeCycle.getDateTime());
            dao.addData(data);
            postDelayed(() -> {
                MToast.mToast(this, "添加成功");
            }, 500);
        });

        binding.bedSwitch.setOnClickListener(view -> {
            if (Common.mqttHelper.getConnected() && Common.mqttHelper.getSubscription()) {
                binding.bedText.setText(!binding.bedSwitch.isSelected() ? "开启" : "关闭");
                sendMessage(4, !binding.bedSwitch.isSelected() ? "1" : "0");
                binding.bedSwitch.setSelected(!binding.bedSwitch.isSelected());
            } else {
                binding.bedSwitch.setSelected(!binding.bedSwitch.isSelected());
                MToast.mToast(this, "未连接或订阅");
            }
        });

        binding.ledSwitch.setOnClickListener(view -> {
            if (Common.mqttHelper.getConnected() && Common.mqttHelper.getSubscription()) {
                binding.ledText.setText(!binding.ledSwitch.isSelected() ? "开启" : "关闭");
                sendMessage(3, !binding.ledSwitch.isSelected() ? "1" : "0");
                binding.ledSwitch.setSelected(!binding.ledSwitch.isSelected());
            } else {
                binding.ledSwitch.setSelected(!binding.ledSwitch.isSelected());
                MToast.mToast(this, "未连接或订阅");
            }
        });

        binding.musicSwitch.setOnClickListener(view -> {
            if (Common.mqttHelper.getConnected() && Common.mqttHelper.getSubscription()) {
                binding.musicText.setText(!binding.musicSwitch.isSelected() ? "开启" : "关闭");
                sendMessage(2, !binding.musicSwitch.isSelected() ? "1" : "0");
                binding.musicSwitch.setSelected(!binding.musicSwitch.isSelected());
            } else {
                binding.musicSwitch.setSelected(!binding.musicSwitch.isSelected());
                MToast.mToast(this, "未连接或订阅");
            }
        });

        binding.musicCheck.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (Common.mqttHelper.getConnected() && Common.mqttHelper.getSubscription()) {
                    sendMessage(1, String.valueOf(i + 1));
                    binding.musicSwitch.setSelected(true);
                    binding.musicText.setText("开启");
                } else {
                    MToast.mToast(MainActivity.this, "未连接或订阅");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    /***
     * mqtt配置
     */
    private void mqttConfig() {
        Common.mqttHelper = new MQTTHelper(this, Common.Sever, Common.DriveID, Common.DriveName, Common.DrivePassword, true, 30, 60);
        try {
            Common.mqttHelper.connect(Common.ReceiveTopic, 1, true, new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {

                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    //收到消息
                    Receive data = new Gson().fromJson(message.toString(), Receive.class);
                    LogUtils.eTag("接收到消息", message.getPayload() != null ? new String(message.getPayload()) : "");
                    debugViewData(2, message.getPayload() != null ? new String(message.getPayload()) : "");
                    System.out.println(data);
                    analysisOfData(data);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
            MToast.mToast(this, "MQTT连接错误");
        }
    }

    /**
     * @brief 动态获取权限
     */
    private void getPermission() {

        List<String> perms = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            perms.add(Manifest.permission.SCHEDULE_EXACT_ALARM);
        }

        // 转换为String数组
        String[] permsArray = perms.toArray(new String[0]);
        if (!EasyPermissions.hasPermissions(this, permsArray)) {
            //请求权限
            EasyPermissions.requestPermissions(this, "这是必要的权限", 100, permsArray);
        }

    }


    /**
     * @param str  如果为 1 添加发送数据到界面   为 2 添加接受消息到界面
     * @param data 数据字符串
     * @brief debug界面数据添加
     */
    private void debugViewData(int str, String data) {
        if (arrayList.size() >= 255) {
            arrayList.clear();
        }
        runOnUiThread(() -> {
            switch (str) {
                case 1: //发送的消息
                    arrayList.add("目标主题:" + Common.ReceiveTopic + " \n时间:" + TimeCycle.getDateTime() + "\n发送消息:" + data);
                    break;
                case 2:
                    arrayList.add("来自主题:" + Common.ReceiveTopic + " \n时间:" + TimeCycle.getDateTime() + "\n接到消息:" + data);
                    break;
            }
            // 在添加新数据之后调用以下方法，滚动到列表底部
            binding.debugView.post(() -> {
                binding.debugView.setSelection(adapter != null ? adapter.getCount() - 1 : 0);
            });
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.setDebugView) {
            isDebugView = !isDebugView;
            binding.debugView.setVisibility(isDebugView ? View.VISIBLE : View.GONE);
        } else if (id == R.id.historyData) {
            CustomBottomSheetDialogFragment dialogFragment = new CustomBottomSheetDialogFragment();
            dialogFragment.show(getSupportFragmentManager(), dialogFragment.getTag());
        }
        return super.onOptionsItemSelected(item);
    }
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
//        return super.onCreateOptionsMenu(menu);
//    }
}