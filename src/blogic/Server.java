package blogic;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.Executors;

public class Server {

    private final static String PATH_FILE = "C:\\Users\\User\\IdeaProjects\\TravelAgencyV2\\static\\";
    private static PersonDaoMySql personDaoMySql;
    private static ArrayList<Person> pp;
    static HashMap<String, String> mapLoginDetails = new HashMap<>();
    static ArrayList<String> arrayListUniqueKeys = new ArrayList<>();

    public Server() {
        personDaoMySql = new PersonDaoMySql();
        starting();
    }

    private void starting() {
        System.out.println("server travel agency starting!");

        InetSocketAddress inetSocketAddress = new InetSocketAddress(1080);
        HttpServer server = null;
        try {
            server = HttpServer.create(inetSocketAddress, 5);
            server.createContext("/api/post-client", new PostClientHandler());
            server.createContext("/api/get-client", new GetClientHandler());
            server.createContext("/login", new RegistrationClient());
            server.createContext("/", new StaticHandler());

            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class PostClientHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {

            String query = exchange.getRequestURI().getQuery();
            System.out.println("PostClientHandler");
            if (query != null) {
                System.out.println("Key post: " + exchange.getRequestHeaders().get("Key"));
                if (arrayListUniqueKeys.contains(exchange.getRequestHeaders().get("Key").toString()) == true) {
                    System.out.println("Порядок! Запрос прислал наш человек");
                    String[] arrDataPerson = query.split("id=|&fname=|&lname=|&age=|&operation=");
                    System.out.println(Arrays.toString(arrDataPerson));
                    Person person = splitArrayIntoPerson(arrDataPerson);
                    String operation = arrDataPerson[5];
                    performingAnOperation(operation, person);

                } else {
                    System.out.println(" Запрос отправил неизвестный сервуру человек!\n Отправляю на страницу регистрации! ");
                    String nameFile = "login.html";
                    String answer = readFile(nameFile);

                    if (answer.equals("file not found")) {
                        sendResponseHeaders(404, "", exchange);
                    } else {
                        sendResponseHeaders(403, answer, exchange);
                    }
                    return;
                }

            }
            addCors(exchange);
            exchange.sendResponseHeaders(200, -1);
            OutputStream os = exchange.getResponseBody();
            os.close();
        }

        private Person splitArrayIntoPerson(String[] arrDataPerson) {

            int id = Integer.valueOf(arrDataPerson[1]);
            String fname = arrDataPerson[2];
            String lname = arrDataPerson[3];
            int age = Integer.valueOf(arrDataPerson[4]);

            return new Person(id, fname, lname, age);
        }

        private void performingAnOperation(String operation, Person person) {

            switch (operation) {
                case "delete":
                    try {
                        personDaoMySql.delete(person.id);
                        System.out.println("operation: delete, person: " + person.toString());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    break;
                case "update":
                    try {
                        personDaoMySql.update(person);
                        System.out.println("operation: update, person: " + person.toString());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    break;
                case "create":
                    try {
                        personDaoMySql.create(person);
                        System.out.println("operation: create, person: " + person.toString());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    try {
                        personDaoMySql.read();
                        System.out.println("default oper: read, operation: " + operation);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    static class GetClientHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
//            System.out.println("GetClientHandler");
//            System.out.println(
//                "RequestMethod: "+exchange.getRequestMethod()+'\n'
//                    +"getHttpContext():"+exchange.getHttpContext()+"\n"
//                    +"Protocol: "+exchange.getProtocol()+"\n"
//                    +"getPrincipal(): "+exchange.getPrincipal()+"\n"
//                    +"getRequestHeaders() User-Agent: "+exchange.getRequestHeaders().get("User-Agent")+"\n"
//                    +"getLocalAddress(): "+exchange.getLocalAddress()+"\n"
//                    +"getResponseHeaders(): "+exchange.getResponseHeaders()+"\n"
//                    +"getHttpContext(): "+exchange.getHttpContext()+"\n"
//                    +"getHttpContext(): "+exchange.getRequestHeaders()+"\n"
//            );

            String persons = "";

            try {
                pp = personDaoMySql.read();
                for (Person p : pp) {
                    persons += p.id + " " + p.fname + " " + p.lname + " " + p.age + "\n";
                }
//                System.out.println(persons);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            addCors(exchange);
            exchange.sendResponseHeaders(200, persons.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(persons.getBytes());
            os.close();

        }
    }

    static class RegistrationClient implements HttpHandler {
        private String answer = "";
        private String login = "";
        private String password = "";
        private String uniqueKey = "";

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            System.out.println("RequestMethod: " + exchange.getRequestMethod() + '\n'
                    + "getRequestHeaders() User-Agent: " + exchange.getRequestHeaders().get("User-Agent") + "\n"
            );
            System.out.println("Key: " + exchange.getRequestHeaders().get("Key"));
            String query = exchange.getRequestURI().getQuery();

            if (query != null) {
                String[] arrQuery = query.contains("password")==true?query.split("&password=|login=|&id="):query.split("operation=");
                String id = "";
                System.out.println("ArrQuery: " + Arrays.toString(arrQuery));
                if (arrQuery.length > 2) {
                    id = arrQuery[3];
                    login = arrQuery[1];
                    password = arrQuery[2];
                } else {
                    id = arrQuery[1];
                }
                uniqueKey = exchange.getRequestHeaders().get("Key").toString();
                System.out.println("Registration data: login: " + login + " pass: " + password + " key: " + uniqueKey + " id: " + id);
//                System.out.println("Registration data: " + id + " " + login + " " + password);
                checkID(id);
            }

            System.out.println("answer about login: " + answer);
            Server.addCors(exchange);
            exchange.sendResponseHeaders(200, answer.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(answer.getBytes());
            os.close();
        }

        private void logIn() {
            if ((login == null && login.isEmpty()) && (password == null && password.isEmpty())) {
                answer = "Enter password or login";
                return;
            }
            if (mapLoginDetails.get(login) != null) {
                String[] arr = mapLoginDetails.get(login).split("login:| password:");//данные из хранилища
                if (login.equals(arr[1]) && password.equals(arr[2]) && arrayListUniqueKeys.contains(uniqueKey) == false) {
                    arrayListUniqueKeys.add(uniqueKey);
                    answer = "login successful";
                } else {
                    answer = "Wrong login or password";
                }
            } else {
                answer = "Customer with such data does not exist, register!";
            }
        }

        private void registration() {
            String value = "login:" + login + " password:" + password;
            if (mapLoginDetails.get(login) == null && arrayListUniqueKeys.contains(uniqueKey) == false) {
                mapLoginDetails.put(login, value);
                arrayListUniqueKeys.add(uniqueKey);
                answer = "Registration completed successfully!";
            } else {
                answer = "A user with this login already exists, select something else.";
            }
            System.out.println(answer);
        }

        private void logOut(){
            if(arrayListUniqueKeys.contains(uniqueKey)==true){
                arrayListUniqueKeys.remove(uniqueKey);
                answer="logout was successful";
            }else{
                answer="logout is not possible since you are not registered to the registration page !!!";
            }
        }

        private void checkID(String id) {
            switch (id) {
                case "registration":
                    registration();
                    break;
                case "logIn":
                    logIn();
                    break;
                case "LogOut":
                    logOut();
                    break;
                default:
                    System.out.println("ID - not found!");
                    break;
            }

        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RegistrationClient that = (RegistrationClient) o;
            return answer.equals(that.answer) &&
                    login.equals(that.login);
        }

        @Override
        public int hashCode() {
            return Objects.hash(answer, login);
        }
    }

    static class StaticHandler implements HttpHandler {

        String answer = "";
        String nameFile = "";
        String query = "";

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StaticHandler that = (StaticHandler) o;
            return Objects.equals(query, that.query);
        }

        @Override
        public int hashCode() {
            return Objects.hash(query);
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            query = exchange.getRequestURI().toString();
            System.out.println("query: " + query);

            if (!query.equals("/")) {
                System.out.println("query != null");
                String[] strQuery = query.split("/");
                nameFile = strQuery[1];
                answer = readFile(nameFile);

            } else if (query.equals("/")) {
                System.out.println("query = null");
                nameFile = "login.html";
                answer = readFile(nameFile);
            }

            if (answer.equals("file not found")) {
                sendResponseHeaders(404, "", exchange);
            } else {
                sendResponseHeaders(200, answer, exchange);
            }
            addCors(exchange);
        }


    }

    static void addCors(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET");
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
    }

    private static String readFile(String nameFile) {
        String fileСontents = "";

        File f = new File(PATH_FILE + nameFile);
        if (!(f.exists() && !f.isDirectory())) {
            fileСontents = fileСontents = "file not found";
            return fileСontents;
        }

        try (FileReader reader = new FileReader(PATH_FILE + nameFile)) {
            int c;
            while ((c = reader.read()) != -1) {
                fileСontents += (char) c;
            }

        } catch (IOException ex) {
            System.out.println(ex.getMessage());

        }
        return fileСontents;
    }

    private static void sendResponseHeaders(int codeAnswer, String answer, HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(codeAnswer, answer.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(answer.getBytes());
        os.close();
    }
}
