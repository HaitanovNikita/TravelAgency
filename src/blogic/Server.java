package blogic;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import javax.swing.Timer;
import java.io.*;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.time.*;
import java.util.*;
import java.util.concurrent.Executors;

public class Server {

    private final static String PATH_FILE = "C:\\Users\\User\\IdeaProjects\\TravelAgencyV2\\static\\";
    private static PersonDaoMySql personDaoMySql;
    private static ArrayList<Person> pp;
    static HashMap<String, String> mapLoginDetails = new HashMap<>();
    static HashMap<String, Calendar> mapUnqKeyDate = new HashMap<>();

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

            refresh();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class PostClientHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {

            String query = exchange.getRequestURI().getQuery();
            if (query != null) {

                String unqKey = exchange.getRequestHeaders().get("Key").toString();
                unqKey = unqKey.replace('[', ' ').replace(']', ' ').trim();

                System.out.println("UnqKey: " + unqKey);
                if (mapUnqKeyDate.get(unqKey) != null) {
                    System.out.println("Порядок! Запрос прислал наш человек");
                    Calendar calendar = new GregorianCalendar();
                    System.out.println("Время которое сейчас записано " + mapUnqKeyDate.get(unqKey).get(Calendar.HOUR) + ":" + mapUnqKeyDate.get(unqKey).get(Calendar.MINUTE));
                    mapUnqKeyDate.get(unqKey).set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) + 10);
                    System.out.println("Новое записаное время " + mapUnqKeyDate.get(unqKey).get(Calendar.HOUR) + ":" + mapUnqKeyDate.get(unqKey).get(Calendar.MINUTE));
                    String[] arrDataPerson = query.split("id=|&fname=|&lname=|&age=|&operation=");

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
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    break;
                case "update":
                    try {
                        personDaoMySql.update(person);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    break;
                case "create":
                    try {
                        personDaoMySql.create(person);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    try {
                        personDaoMySql.read();
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
            String persons = "";

            try {
                pp = personDaoMySql.read();
                for (Person p : pp) {
                    persons += p.id + " " + p.fname + " " + p.lname + " " + p.age + "\n";
                }
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

            String query = getQuery(exchange, "");

            if (query != null) {
                String[] arrQuery = query.contains("password") == true ? query.split("&password=|login=|&id=") : query.split("operation=");
                String id = "";
                System.out.println("ArrQuery: " + Arrays.toString(arrQuery));
                if (arrQuery.length > 2) {
                    id = arrQuery[3];
                    login = arrQuery[1];
                    password = arrQuery[2];
                } else {
                    id = arrQuery[1];
                }
                uniqueKey = getRandomKey();
                System.out.println("Registration data: login: " + login + " pass: " + password + " key: " + uniqueKey + " id: " + id);
                checkID(id);
            }

            System.out.println("answer about RegistrationClient: " + answer);
            Server.addCors(exchange);
            exchange.sendResponseHeaders(200, answer.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(answer.getBytes());
            os.close();
        }

        private String getQuery(HttpExchange exchange, String query) throws IOException {
            InputStream inputstream = exchange.getRequestBody();
            InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
            BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
            String string = "";

            while ((string = bufferedreader.readLine()) != null) {
                query += string;
            }
            System.out.println("Data person: " + query);
            return query;
        }

        private void logIn() {
            if ((login == null && login.isEmpty()) && (password == null && password.isEmpty())) {
                answer = "Enter password or login";
                return;
            }
            if (mapLoginDetails.get(login) != null) {
                String[] arr = mapLoginDetails.get(login).split("login:| password:");//данные из хранилища
                if (login.equals(arr[1]) && password.equals(arr[2]) && mapUnqKeyDate.get(uniqueKey) == null) {
                    Calendar date = new GregorianCalendar();
                    printTime(date);
                    mapUnqKeyDate.put(uniqueKey, date);
                    answer = "login successful&" + uniqueKey;
                } else {
                    answer = "Wrong login or password";
                }
            } else {
                answer = "Customer with such data does not exist, register!";
            }
        }

        private void registration() {
            if (mapLoginDetails.get(login) == null && mapUnqKeyDate.get(uniqueKey) == null) {
                Calendar date = new GregorianCalendar();
                date.set(Calendar.MINUTE, date.get(Calendar.MINUTE) + 10);
                printTime(date);
                System.out.println("Добавлен ключ в систему: " + uniqueKey);
                mapUnqKeyDate.put(uniqueKey, date);
                answer = "Registration completed successfully!&" + uniqueKey;
            } else {
                answer = "A user with this login already exists, select something else.";
            }
            System.out.println(answer);
        }

        private void logOut() {
            if (mapUnqKeyDate.get(uniqueKey) != null) {
                mapUnqKeyDate.remove(uniqueKey);
                answer = "logout was successful";
            } else {
                answer = "logout is not possible since you are not registered to the registration page !!!";
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

        private void setCookie(HttpExchange exchange, String cookie) {
            List<String> valuesCookie = new ArrayList<>();
            valuesCookie.add("Key=" + cookie);

            Headers headers = exchange.getResponseHeaders();
            headers.put("Set-Cookie", valuesCookie);

            Headers headers2 = exchange.getRequestHeaders();

            String test = headers2.get("Cookie").toString();
            System.out.println("Test: " + test);
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

    static void refresh() {

        Timer t = new Timer(60000, e -> {
            refreshMapUnqKeyDate();
        });
        t.start();

    }

    static void refreshMapUnqKeyDate() {

        ArrayList<String> keysForDelete = new ArrayList<>();
        if (mapUnqKeyDate.size() != 0) {
            System.out.println("В хранилище есть данные!");
            for (Map.Entry<String, Calendar> entry : mapUnqKeyDate.entrySet()) {
                Calendar calendar = new GregorianCalendar();
                System.out.println("время проверки : " + calendar.get(Calendar.HOUR) + ":" + calendar.get(Calendar.MINUTE));
                if (calendar.after(entry.getValue())) {
                    keysForDelete.add(entry.getKey());
                    System.out.println("Время нарушено");
                    System.out.println("Текущие время : " + calendar.get(Calendar.HOUR) + ":" + calendar.get(Calendar.MINUTE));
                    System.out.println("Записаные время: " + entry.getValue().get(Calendar.HOUR) + ":" + entry.getValue().get(Calendar.MINUTE));

                } else {
                    System.out.println("Со временем порядок");
                }
            }
            System.out.println("Размер удаляемой коллекции: " + keysForDelete.size());
            for (int i = 0; i < keysForDelete.size(); i++) {
                System.out.println("Key[" + i + "] remove: " + keysForDelete.get(i));
                mapUnqKeyDate.remove(keysForDelete.get(i));
            }

        } else {
            System.out.println("Хранилище пустое!");
        }

    }

    static String getRandomKey() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    private static void printTime(Calendar date) {
        System.out.println("Time registration(hour): "
                + date.get(Calendar.HOUR) + ":(min) "
                + date.get(Calendar.MINUTE) + ":(sec) "
                + date.get(Calendar.SECOND));
    }

    private static String readFile(String nameFile) {
        String fileСontents = "";

        File f = new File(PATH_FILE + nameFile);
        if (!(f.exists() && !f.isDirectory())) {
            fileСontents = "file not found";
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
