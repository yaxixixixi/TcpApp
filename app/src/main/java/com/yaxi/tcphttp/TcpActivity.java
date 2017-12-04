package com.yaxi.tcphttp;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.yaxi.tcphttp.utils.DateUtil;
import com.yaxi.tcphttp.utils.Pref;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TcpActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = TcpActivity.class.getSimpleName();
    private EditText mRequestData, mRequestCycle, mEditHost, mEditPort;
    private TextView show;
    private Button mStart, mSend;

    private Handler mHandler = new Handler();
    private Pref sp;

    private Socket mClient;
    private MsgRunnable msgRunnable;
    private InputStream input = null;
    private DataInputStream reader = null;
    private DataOutputStream writer = null;

    private boolean flag = false;//是否有delay的消息在等待发送

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tcp);
        mRequestData = findViewById(R.id.request_data);
        mRequestCycle = findViewById(R.id.request_cycle);
        mEditHost = findViewById(R.id.host);
        mEditPort = findViewById(R.id.port);
        show = findViewById(R.id.show);
        mStart = findViewById(R.id.start);
        mSend = findViewById(R.id.send);

        show.setMovementMethod(ScrollingMovementMethod.getInstance());


        mStart.setOnClickListener(this);
        mSend.setOnClickListener(this);


        sp = Pref.getInstance(getApplicationContext());
        mEditHost.setText(sp.getHost());
        mEditPort.setText(String.valueOf(sp.getPort()));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start:
                if (mClient != null && mClient.isConnected()) {
                    stopSocket();
                } else {
                    startSocket();
                }
                break;
            case R.id.send:
                String msg = mRequestData.getText().toString().trim();
                String cycleStr = mRequestCycle.getText().toString().trim();
                int period = 0;
                if (!cycleStr.equals(""))
                    period = Integer.parseInt(cycleStr);

                if (mClient == null || mClient.isClosed()) {
                    Toast.makeText(this, "socket未连接", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (msg.equals("")) {
                    Toast.makeText(this, "请输入需要发送的数据", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!flag) {
                    if (period <= 0) {
                        sendString(msg);
                    } else {
                        msgRunnable = new MsgRunnable(msg, period);
                        mHandler.post(msgRunnable);
                        flag = true;
                        mSend.setText("停止发送");
                    }
                } else {
                    mHandler.removeCallbacks(msgRunnable);
                    msgRunnable = null;
                    flag = false;
                    mSend.setText("发送数据");
                }


                break;
            default:
                break;
        }
    }

    /**
     * 连接socket
     */
    public void startSocket() {
        String host = mEditHost.getText().toString().trim();
        String port = mEditPort.getText().toString().trim();
        if (host.equals("")) {
            Toast.makeText(TcpActivity.this, "请输入server Ip", Toast.LENGTH_SHORT).show();
            return;
        } else if (port.equals("")) {
            Toast.makeText(TcpActivity.this, "请输入server 端口号", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mClient == null || mClient.isClosed() || !mClient.isConnected()) {
            connect(host, Integer.parseInt(port));
        }
    }

    private void connect(String host, int port) {
        new ConnectThread(host, port).start();
    }

    /**
     * 连接socket的线程
     */
    private class ConnectThread extends Thread {

        private String host;
        private int port;

        public ConnectThread(String host, int port) {
            this.host = host;
            this.port = port;
        }

        @Override
        public void run() {
            try {
                mClient = new Socket(host, port);
                //修改按钮的状态

                input = mClient.getInputStream();
                OutputStream output = mClient.getOutputStream();
                reader = new DataInputStream(input);
                writer = new DataOutputStream(output);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mStart.setText("断开连接");
                        Toast.makeText(TcpActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                    }
                });
                //保存host和port
                sp.setHost(host);
                sp.setPort(port);
                new ReceivedThread().start();
                //连接成功后发送一条消息
                writer.writeBytes("hello server");
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(TcpActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    /**
     * 接受消息的线程
     */
    private class ReceivedThread extends Thread {
        @Override
        public void run() {
            String showText = show.getText().toString();
            final StringBuilder builder = new StringBuilder(showText);
            try {
                byte[] bytes = new byte[1024 * 10];
                while (mClient.isConnected()) {

                    int length = input.read(bytes);
                    if (length < 0) {
                        Log.i(TAG, "run: socket disConnected");
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mStart.setText("连接已断开");
                            }
                        });
                        break;
                    }
                    final String s = new String(bytes, 0, length, "utf-8");
                    builder.append("\n");
                    builder.append(DateUtil.formatTime());
                    builder.append("=>\r\r");
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            builder.append(s);
                            show.setText(builder.toString());
                        }
                    });
                }
            } catch (final IOException e) {
                e.printStackTrace();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        builder.append("\n");
                        builder.append(DateUtil.formatTime());
                        builder.append("=>\r\r");
                        builder.append(e.toString());
                        show.setText(builder.toString());
                    }
                });
            }
        }
    }

    /**
     * 发送字符串
     *
     * @param msg
     */
    private void sendString(String msg) {
        if (mClient != null && mClient.isConnected() && writer != null) {
            try {
                writer.writeBytes(msg);
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {

        }
    }

    /**
     * 连续请求的消息
     * 每过一个周期时间就执行一次自身
     */
    class MsgRunnable implements Runnable {
        String msg;
        int period;

        public MsgRunnable(String msg, int period) {
            this.msg = msg;
            this.period = period;
        }

        @Override
        public void run() {
            sendString(msg);
            mHandler.postDelayed(msgRunnable, period);
        }
    }

    /**
     * 断开scoket连接
     */
    private void stopSocket() {
        try {
            if (msgRunnable != null) {
                mHandler.removeCallbacks(msgRunnable);//msgRunnable != null
                msgRunnable = null;
                flag = false;
                mSend.setText("发送数据");
            }
            if (reader != null)
                reader.close();
            if (writer != null)
                writer.close();
            if (mClient != null) {
                mClient.close();
                mClient = null;
            }
            //修改按钮状态
            Toast.makeText(this, "连接已断开", Toast.LENGTH_SHORT).show();
            mStart.setText("连接");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.hide:
                showEditView(false);
                break;
            case R.id.show:
                showEditView(true);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showEditView(boolean show) {
        mRequestData.setVisibility(show ? View.VISIBLE : View.GONE);
        mRequestCycle.setVisibility(show ? View.VISIBLE : View.GONE);
        mEditHost.setVisibility(show ? View.VISIBLE : View.GONE);
        mEditPort.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
