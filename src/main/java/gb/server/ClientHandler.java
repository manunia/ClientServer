package gb.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.*;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    String nick;
    private static final Logger logger = Logger.getLogger("");

    public ClientHandler(Server server, Socket socket) {
        try {
            this.socket = socket;
            this.server = server;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                try {
                    while (true) {
                        String str = in.readUTF();

                        if(str.startsWith("/addUser")) {
                            String[] tokens = str.split(" ");
                            AuthService.addNewUser(tokens[1], tokens[3], tokens[2]);
                            createLog("addNewUser", tokens[1],"newUserLog");
                            sendMsg("Добавлен новый пользователь!");
                        }

                        if(str.startsWith("/auth")) {
                            String[] tokens = str.split(" ");
                            String newNick = AuthService.getNickByLoginAndPass(tokens[1], tokens[2]);

                            if(newNick != null) {
                                sendMsg("/authok");
                                nick = newNick;
                                server.subscribe(this);
                                break;
                            } else {
                                sendMsg("Неверный логин/пароль");
                            }
                        }
                    }
                    while (true) {
                        String str = in.readUTF();
                        if (str.equals("/end")) {
                            out.writeUTF("/serverclosed");
                            break;
                        }
                        server.broadcastMsg(nick + " " + str);
                        System.out.println("Client: " + str);
                        createLog("New secret message from ",nick,"messagesLog");
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
                    server.unsubscribe(this);
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void createLog(String event, String nick, String logFileName) throws IOException {
        logger.setLevel(Level.SEVERE);
        logger.getHandlers()[0].setLevel(Level.SEVERE);
        logger.getHandlers()[0].setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                String msg = record.getLevel() + "\t" +record.getMessage() + " "
                        + record.getMillis();
                return msg;
            }
        });

        Handler handler = new FileHandler(logFileName);
        handler.setLevel(Level.ALL);
        handler.setFormatter(new SimpleFormatter());
        logger.addHandler(handler);
        logger.log(Level.SEVERE, event + " " + nick);
    }
}
