import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        BooleanSearchEngine engine = new BooleanSearchEngine(new File("pdfs"));
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();


        final int port = 8989;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                System.out.println("Установлено новое соединение");
                final String inputText = in.readLine().toLowerCase();
                System.out.println("Запрос по слову: " + inputText);

                List<PageEntry> foundPageEntries = engine.search(inputText);
                String jsonResult = gson.toJson(foundPageEntries);

                out.println(jsonResult);

            }
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
        }
    }
}