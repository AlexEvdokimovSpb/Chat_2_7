package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private String nickname;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    //цикл аутентификации
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith("/auth ")) {
                            String[] token = str.split("\\s");
                            String newNick = server
                                    .getAuthService()
                                    .getNicknameByLoginAndPassword(token[1], token[2]);

                            if (newNick != null) {
                                nickname = newNick;
                                sendMsg("/authok " + nickname);
                                server.subscribe(this);
                                server.subscribeNickname(nickname, this);
                                System.out.println("Клиент " + nickname + " подключился");
                                break;
                            } else {
                                sendMsg("Неверный логин / пароль");
                            }
                        }
                    }

                    //цикл работы
                    while (true) {
                        String str = in.readUTF();

                        if (str.equals("/end")) {
                            out.writeUTF("/end");
                            break;
                        }


                        if (str.split(" ", 2)[0].equals("/w")) { // если есть префикс /w
                            String addressee = str.split(" ", 3)[1]; // сохраняем "кому"
                            String message = str.split(" ", 3)[2]; // сохраняем сообщение

                            if (server.thisNicknameInChat(addressee)) {
                                out.writeUTF(nickname + ": " + message); // дублируем сообщение отправителю
                                server.addressMsg(this, addressee, message); // отправляем по нику
                            } else {
                                out.writeUTF("Пользователя: " +addressee+ " нет в чате"); // если получателя нет
                            }


                        } else { // иначе отправляем всем
                            server.broadcastMsg(this, str);
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("Клиент отключился");
                    server.unsubscribe(this);
                    server.unsubscribeNickname(nickname);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNickname() {
        return nickname;
    }


}
