package br.ufsm.csi.so;

import java.io.File;
import java.io.FileWriter;
import java.net.Socket;
import java.util.concurrent.Semaphore;
import lombok.SneakyThrows;

//Classe que cria e escreve no arquivo de log
public class Logger {

    private String logString = "";
    private Semaphore vazio = new Semaphore(1000);
    private Semaphore cheio = new Semaphore(0);
    private Semaphore mutex = new Semaphore(1);

    private File file = new File("log.txt");

    private Socket socket;
    private Assento assento;

    @SneakyThrows
    public Logger() {
        if (file.createNewFile()) {
            System.out.println("Criado arquivo de log: " + this.file.getName());
        }
    }

    public void log(Socket socket, Assento assento) {
        this.socket = socket;
        this.assento = assento;

        Thread produz = new Thread(new ProduzLog());
        Thread armazena = new Thread(new ArmazenaLog());

        produz.start();
        armazena.start();
    }

    //Produtores e Consumidores
    private class ProduzLog implements Runnable {

        @Override
        @SneakyThrows
        public void run() {
            mutex.acquire();
            logString = "\n#Nova reserva\n";
            logString += "Nome: " + assento.getNome() + "\n";
            logString += "Assento: " + assento.getId() + "\n";
            logString += "Data: " + assento.getData() + " " + assento.getHora();

            vazio.acquire(logString.length());
            cheio.release();
            mutex.release();
        }
    }

    private class ArmazenaLog implements Runnable {

        @Override
        @SneakyThrows
        public void run() {
            mutex.acquire();
            cheio.acquire();

            vazio.release(logString.length());

            //Iniciando o arquivo de log no server
            FileWriter writer = new FileWriter(file.getName(), true);

            //Escrevendo o texto no arquivo de log
            writer.write(logString);

            //Fechando o arquivo de log
            writer.close();

            mutex.release();
        }
    }
}
