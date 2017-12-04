package com.yaxi.tcphttp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 *
 * Created by yaxi on 2017/11/28.
 */

public class TcpSocket {

    Socket socket;
    DataInputStream dataInputStream;
    DataOutputStream dataOutputStream;

    public TcpSocket() {
        socket = new Socket();
    }

    public boolean connect(String host, int port) {
        boolean connected = false;
        SocketAddress socketAddress = new InetSocketAddress(host, port);
        try {
            socket.connect(socketAddress, 5000);
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            connected = true;
        } catch (IOException e) {
            e.printStackTrace();
            connected = false;
        }
        return connected;
    }

    public boolean setSoTimeout(int ms) {
        boolean boo = false;
        try {
            socket.setSoTimeout(ms);
            boo = true;
        } catch (Exception e) {
            // TODO: handle exception
        }
        return boo;
    }

    public boolean sendStr(String str) {
        boolean success = false;
        try {
            dataOutputStream.writeBytes(str);
            dataOutputStream.flush();
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return success;
    }

    public byte[] read() {
        byte[] data = null;

        try {
            int num = dataInputStream.available();
            if (num > 0) {
                data = new byte[num];
                dataInputStream.read(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }

    public boolean closed() {
        boolean closed = false;
        try {
            dataInputStream.close();
            dataOutputStream.close();
            socket.close();
            socket = null;
            closed = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return closed;
    }
}
