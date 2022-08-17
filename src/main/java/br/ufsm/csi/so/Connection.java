package br.ufsm.csi.so;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import lombok.SneakyThrows;

//Iniciando o Runnable a cada nova requisição
public class Connection implements Runnable {
    private Socket socket;

    public Connection(Socket socket) {
        this.socket = socket;
    }

    @Override
    @SneakyThrows
    public void run() {
        InputStream in = this.socket.getInputStream();
        OutputStream out = this.socket.getOutputStream();

        //Scanner lendo o InputStream
        Scanner scanner = new Scanner(in);

        if (!scanner.hasNext()) {
            scanner.close();
            return;
        }

        String method = scanner.next();
        String path = scanner.next();

        //Exibição do caminho do método
        System.out.println(method + " " + path);

        String[] dirAndParams = path.split("\\?");
        String recurso = dirAndParams[0];

        //Queries sendo interpretadas na URL
        Map<String, String> query = this.parseQuery(dirAndParams.length > 1
                ? dirAndParams[1].split("&")
                : null);

        byte[] contentBytes = null;

        //Cabeçalho
        String header = """
                HTTP/1.1 200 OK
                Content-Type: text/html; charset=UTF-8
                """;

        if (recurso.equals("/")) {
            contentBytes = this.getBytes("index.html");

            String html = new String(contentBytes);
            String elementos = "";
            int number = 1;

            for (Assento assento : Server.assentos.values()) {

                String elemento = "";

                //Se o assento estiver com ocupado = True
                //Ou seja assento ocupado, ele desabilita o botão
                //E deixa ele com a cor vermelha
                if (assento.isOcupado()){
                    elemento += "<a href=\"#\">";
                    elemento += "<abbr title=\"" +
                            "Nome:" + assento.getNome();
                    elemento += "  " +
                            "Data: " + assento.getData();
                    elemento += "  " +
                            "Hora: " + assento.getHora();
                    elemento += "\">";
                    elemento += "<button type=\"button\" class=\"btn btn-danger disabled\">";
                    elemento += assento.getId() + "</button></abbr></a>";
                }
                else { //Caso não esteja ocupado ele escreve o link com o ID do assento da vez
                    elemento += "<a class=\"assento\"";
                    elemento += " href=\"/reservar?id=" + assento.getId() + "\"";
                    elemento += "><button type=\"button\" class=\"btn btn-success\">" + assento.getId() + "</button></a>";
                }

                if (number % 4 ==0){
                    elemento += "<br/>";
                    //Se o numero for divísivel por 4 ele quebra a linha para permitir
                    //a organização em formato de ônibus
                }

                elementos += elemento + "\n";
                number++;

            }

            //Substituindo os assentos gerados na tag assentos no html
            html = html.replace("<assentos />", elementos);

            contentBytes = html.getBytes();
        }

        if (recurso.equals("/reservar")) {
            contentBytes = this.getBytes("reservar.html");

            String html = new String(contentBytes);
            html = html.replace("{{id}}", query.get("id"));

            contentBytes = html.getBytes();
        }
        if (recurso.equals("/confirmar")) {
            //Header de redirecionamento
            header = """
                    HTTP/1.1 302 Found
                    Content-Type: text/html; charset=UTF-8
                    Location: /
                    """;

            //Fechar entrada ao acessar região crítica
            Server.mutex.acquire();

            int id = Integer.parseInt(query.get("id"));
            Assento assento = Server.assentos.get(id);

            // TODO: Verificar se o assento está vago
            if (assento != null) {
                String nome = query.get("nome");
                String dataHora[] = query.get("data_hora").split("T");
                String data = dataHora[0];
                String hora = dataHora[1];

                assento.setNome(nome);
                assento.setData(data);
                assento.setHora(hora);
                assento.setOcupado(true);

                Server.logger.log(socket, assento);

                System.out.println("LOG Nova reserva adicionada: " + assento.getId() + " | " + assento.getNome());
            }
            //Liberando mutex
            Server.mutex.release();
        }

        //Escrevendo o cabeçalho
        out.write(header.toString().getBytes());
        out.write(contentBytes);

        //Fechando as streams
        in.close();
        out.close();

        scanner.close();

        //Encerrando a conexão
        this.socket.close();
    }

    @SneakyThrows
    private Map<String, String> parseQuery(String[] query) {

        if (query == null)
            return null;

        Map<String, String> queries = new HashMap<>();

        for (String s : query) {

            String[] kvPair = s.split("=");

            if (kvPair.length == 1) {
                queries.put(kvPair[0], null);
            } else {
                queries.put(kvPair[0], URLDecoder.decode(kvPair[1], "UTF-8"));
            }
        }

        return queries;
    }

    @SneakyThrows
    private byte[] getBytes(String recurso) {
        //Removendo o / do recurso recebido
        if (recurso.startsWith("/"))
            recurso = recurso.substring(1);

        InputStream is = this.getClass().getClassLoader().getResourceAsStream(recurso);

        if (is != null)
            return is.readAllBytes();

        return null;
    }
}
