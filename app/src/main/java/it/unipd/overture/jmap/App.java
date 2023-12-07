package it.unipd.overture.jmap;

import static spark.Spark.*;

public class App {
    public String getGreeting() {
        return "Hello World!";
    }

    public static void main(String[] args) {
        get("/jmap", (q, a) -> {
            q.session(true);
            String account = q.session().attribute("account");
            if (account == null) {
                halt(401, "Login first!");
            }

            a.type("application/json");
            return "{\"capabilities\": { \"urn:ietf:params:jmap:core\": {}, \"urn:ietf:params:jmap:submission\": {}, \"urn:ietf:params:jmap:mail\": {}, }, \"accounts\": {\"" + account + "\": {} } }";
        });

        post("/jmap", (q, a) -> {
            return "";
        });

        post("/insertMail", (q, a) -> {
            new Database().insertMail(q.body());
            return "";
        });

        get("/getMail", (q, a) -> {
            a.type("application/json");
            return new Database().getMail(q.headers("Authorization"), q.queryParams("id"));
        });

        get("/getAll", (q, a) -> {
            a.type("application/json");
            return new Database().getAccountMails(q.headers("Authorization"));
        });

        get("/", (q, a) -> {
            q.session(true);
            String bearer = q.session().attribute("bearer");
            if (bearer == null) {
                return """
	<html>
                <body>
                    <form action="/login" method="post">
                        <label for="username">Username</label>
                        <input type="text" id="username" name="username"><br><br>
                        <label for="password">Password</label>
                        <input type="password" id="password" name="password"><br><br>
                        <input type="submit" value="Submit">
                    </form>
                </body>
                </html>""";
            } else {
                return "<html> <body> <p>Your bearer is: " + bearer + "</p> <p> <a href=\"/logout\">Logout</a> </p> <p> <a href=\"/reset\">Reset</a> </p> </body> </html>";
            } 
        });

        post("/login", (q, a) -> {
            String username = q.queryParams("username");
            String password = q.queryParams("password");
            String bearer = new Database().login(username, password);
            if (bearer != null) {
                q.session().attribute("bearer", bearer);
            }
            a.redirect("/");
            return "";
        });

        get("/logout", (q, a) -> {
            q.session(true);
            q.session().removeAttribute("bearer");
            a.redirect("/");
            return "";
        });

        get("/reset", (q, a) -> {
            q.session(true);
            if (q.session().attribute("bearer") != new Database().getAdminBearer()) {
                halt(401, "Login as admin!");
            }
            new Database().reset();
            a.redirect("/logout");
            return "Done.";
        });
    }
}
