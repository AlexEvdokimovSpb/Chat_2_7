package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

public class Server {
    private List<ClientHandler> clients;
    private AuthService authService;
    private HashMap<String, ClientHandler> hashClients; // ключ - ник, значение - клиент

    private int PORT = 8189;
    ServerSocket server = null;
    Socket socket = null;

    public Server(){
        clients = new Vector<>();
        authService = new SimpleAuthService();
        hashClients = new HashMap<>();

        try {
            server = new ServerSocket(PORT);
            System.out.println("Сервер запущен");

            while (true) {
                socket = server.accept();
                System.out.println("Клиент подключился");
                new ClientHandler(this, socket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public AuthService getAuthService() {
        return authService;
    }

    public void broadcastMsg(ClientHandler sender, String msg){
        String message = String.format("%s : %s", sender.getNickname(), msg);
        for (ClientHandler c : clients) {
            c.sendMsg(message);
        }
    }

    public void addressMsg(ClientHandler sender, String addressNickname, String msg){ // метод отправки оп адресу
        ClientHandler address = hashClients.get(addressNickname);
        String message = String.format("%s : %s", sender.getNickname(), msg);
        address.sendMsg(message);
    }

    public boolean thisNicknameInChat(String addressNickname){ // метод проверки присутствия в чате
        return (hashClients.containsKey(addressNickname));
    }


    public void subscribe(ClientHandler clientHandler){
        clients.add(clientHandler);
    }

    public void unsubscribe(ClientHandler clientHandler){
        clients.remove(clientHandler);
    }

    public void subscribeNickname(String nickname, ClientHandler clientHandle)
    { hashClients.put (nickname, clientHandle); }

    public void unsubscribeNickname(String nickname){
        hashClients.remove(nickname);
    }

}
