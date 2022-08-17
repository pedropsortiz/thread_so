package br.ufsm.csi.so;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import lombok.SneakyThrows;

public class Server {

    public static Map<Integer, Assento> assentos = new HashMap<>();
    public static Semaphore mutex = new Semaphore(1);
    public static Logger logger = new Logger();

    @SneakyThrows
    public static void main(String[] args) {
        for (int id = 1; id < 43; id++) {
            assentos.put(id, new Assento(id));
        }

        try (ServerSocket server = new ServerSocket(8080)) {
            System.out.println("Ouvindo porta 8080");

            while (true) {
                // Esperando uma conexão
                Socket socket = server.accept();
                //Criando a Thread e iniciando a conexão do server
                Connection connection = new Connection(socket);
                Thread thread = new Thread(connection);
                thread.start();
            }
        }
    }
}
