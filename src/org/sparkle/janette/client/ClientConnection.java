package org.sparkle.janette.client;

import org.sparkle.jbind.JBinD;
import org.sparkle.jbind.Reader;
import org.sparkle.jbind.Writer;

import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author yew_mentzaki
 */
public final class ClientConnection {

    Socket client;
    ClientHandler clienthandler;
    Timer timer = new Timer();
    private void send() {
        try {
            JBinD b = clienthandler.out();
            if (b == null) return;
            byte[] bytes = Writer.write(b);
            DataOutputStream dos = new DataOutputStream(client.getOutputStream());
            dos.writeInt(bytes.length);
            dos.write(bytes, 0, bytes.length);
        } catch (SocketException ex){
            System.err.println("Server has been closed");
            disconnect();
        } catch (IOException ex) {
            Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    TimerTask get = new TimerTask(){
        @Override
        public void run() {
            InputStream inputStream = null;
            try {
                inputStream = client.getInputStream();
                DataInputStream dis = new DataInputStream(inputStream);
                while (true) {
                    int length = dis.readInt();
                    if (length <= 0) {
                        continue;
                    }
                    byte[] message = new byte[length];
                    dis.read(message);
                    JBinD bind = Reader.read(message);
                    clienthandler.in(bind);
                }
            } catch (IOException ex) {

            } finally {

            }
        }
    };

    public void disconnect() {
        try {
            timer.cancel();
            timer.purge();
            get.cancel();
            client.close();
        } catch (IOException ex) {
            Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
        }catch (NullPointerException ignored){
            System.out.print("Nothing to disconnect");
        }
    }

    String server;
    int port;
    Class handler;
    int sendingDelay = 350;

    public ClientConnection(String server, int port, Class handler, int sendingDelay) {
        this.server = server;
        this.port = port;
        this.handler = handler;
        this.sendingDelay = sendingDelay;
    }


    public void connect() throws InstantiationException, IllegalAccessException, IOException {
        try {
            this.client = new Socket(server, port);
            clienthandler = (ClientHandler) handler.newInstance();
            startTimer();
            get.run();

        } catch (InstantiationException ex) {
            Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void startTimer(){
        timer.schedule(new TimerTask() {
            public void run(){
                send();
            }
        },0,sendingDelay);
    }
}
