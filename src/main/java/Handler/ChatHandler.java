package main.java.Handler;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ChatHandler extends Thread {
    protected Socket socket;
    protected DataInputStream dataInputStream;
    protected DataOutputStream dataOutputStream;
    protected boolean isOn;

    protected static List<ChatHandler> handlers = Collections.synchronizedList(new ArrayList<ChatHandler>());

    public ChatHandler(Socket s) throws IOException{
        socket = s;
        dataInputStream = new DataInputStream(new BufferedInputStream(s.getInputStream()));
        dataOutputStream = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
    }

    public void run(){
        isOn = true;
        try {
            handlers.add(this);
            while (isOn) {
                String msg = dataInputStream.readUTF();
                broadcast(msg);
            }
        }catch (IOException ex){
            ex.printStackTrace();
        }finally{
            handlers.remove(this);
            try{
                dataOutputStream.close();
            }catch (IOException ex){
                ex.printStackTrace();
            }
            try{
                socket.close();
            }catch (IOException ex){
                ex.printStackTrace();
            }
        }
    }

    protected static void broadcast(String messade){
        synchronized (handlers){
            Iterator<ChatHandler> iterator = handlers.iterator();
            while (iterator.hasNext()){
                ChatHandler chatHandler = iterator.next();
                try{
                    synchronized (chatHandler.dataOutputStream){
                        chatHandler.dataOutputStream.writeUTF(messade);
                    }
                    chatHandler.dataOutputStream.flush();
                }catch (IOException ex){
                    ex.printStackTrace();
                    chatHandler.isOn = false;
                }
            }
        }
    }
}
