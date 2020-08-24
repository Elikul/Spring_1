package Chat.Server;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

// класс для работы к клиентами
public class ClientHandler {
    private Socket socket;
    private DataInputStream in;
    private  DataOutputStream out;
    private MainServ serv;
    private String nick;

    List<String> blackList; //чёрный список

    //метод получения ника
    public String getNick() {

        return nick;
    }

    public ClientHandler(MainServ serv, Socket socket){
        try {
            this.socket = socket;
            this.serv = serv;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.blackList = new ArrayList<>();

            new Thread(() ->  {
                    try {
                        // цикл для авторизации
                        while (true) {
                            String msg = in.readUTF();
                            // если приходит сообщение начинающееся с /auth значит пользователь хочет авторизоваться
                            if (msg.startsWith("/auth")) {
                                String[] tokens = msg.split(" ");
                                String newNick = AuthService.getNickByLoginAndPass(tokens[1], tokens[2]);
                                // если ответ не равен null отправляем ответ клиенту о том, что авторизация прошла успешно
                                if (newNick != null) {
                                    //Чтобы логины не повторялись
                                    if (!serv.isNickBusy(newNick)) {
                                        sendMsg("/authok");
                                        nick = newNick;
                                        serv.subscribe(this);
                                        break;
                                    } else {
                                        sendMsg("Учетная запись уже используется");
                                    }
                                }
                                else {
                                    sendMsg("Неверный логин/пароль");
                                }
                            }
                        }


                        // цикл для работы
                        while (true) {
                            String msg = in.readUTF();
                            if (msg.startsWith("/")) {
                                if (msg.equals("/end")) {
                                    out.writeUTF("/serverclosed");
                                    break;
                                }
                                if (msg.startsWith("/w ")) { // /w nick3 lsdfhldf sdkfjhsdf wkerhwr
                                    String[] tokens = msg.split(" ", 3);
                                    String m = msg.substring(tokens[1].length() + 4);
                                    serv.sendPersonalMsg(this, tokens[1], tokens[2]);
                                }
                                if (msg.startsWith("/blacklist ")) { // /blacklist nick3
                                    String[] tokens = msg.split(" ");
                                    blackList.add(tokens[1]);
                                    sendMsg("Вы добавили пользователя " + tokens[1] + " в черный список");
                                }
                            } else {
                                serv.broadcastMsg(this, nick + ": " + msg);
                            }
                            System.out.println("Client: " + msg);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        serv.unsubscribe(this);
                    }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //проверка находится клиент в чёрном списке у другого
    public boolean checkBlackList(String nick) {

        return blackList.contains(nick);
    }

    // метод для оправки сообщения клиенту
    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
