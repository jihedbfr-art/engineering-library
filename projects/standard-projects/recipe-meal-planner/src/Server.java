import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Recipe & meal planner backend. Zero external dependencies.
 * Storage: flat JSON files under data/.
 */
public class Server {

    static final Path DATA_DIR = Paths.get("data");
    static final Path RECIPES_FILE = DATA_DIR.resolve("recipes.json");
    static final Path PLANNING_FILE = DATA_DIR.resolve("planning.json");
    static final Path PUBLIC_DIR = Paths.get("public");
    static final List<String> DAYS = Arrays.asList(
            "monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday");
    static final List<String> MEALS = Arrays.asList("breakfast", "lunch", "dinner");

    static final ReentrantLock lock = new ReentrantLock();

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        Files.createDirectories(DATA_DIR);
        ensureFile(RECIPES_FILE, "[]");
        ensureFile(PLANNING_FILE, emptyPlanningJson());

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/recipes", new RecipesHandler());
        server.createContext("/api/planning", new PlanningHandler());
        server.createContext("/api/shopping-list", new ShoppingListHandler());
        server.createContext("/", new StaticHandler());
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
        System.out.println("recipe-meal-planner listening on http://localhost:" + port);
    }

    static void ensureFile(Path path, String defaultContent) throws IOException {
        if (!Files.exists(path)) {
            Files.writeString(path, defaultContent, StandardCharsets.UTF_8);
        }
    }

    static String emptyPlanningJson() {
        Map<String, Object> plan = new LinkedHashMap<>();
        for (String day : DAYS) {
            Map<String, Object> meals = new LinkedHashMap<>();
            for (String meal : MEALS) meals.put(meal, null);
            plan.put(day, meals);
        }
        return Json.write(plan);
    }

    // ---------- Recipes ----------

    static class RecipesHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            try {
                String method = ex.getRequestMethod();
                String path = ex.getRequestURI().getPath();
                String[] parts = path.replaceFirst("^/api/recipes/?", "").split("/");
                String id = parts.length > 0 && !parts[0].isEmpty() ? parts[0] : null;

                lock.lock();
                try {
                    List<Object> recipes = Json.asArray(Json.parse(Files.readString(RECIPES_FILE)));

                    if ("GET".equals(method) && id == null) {
                        respond(ex, 200, Json.write(recipes));
                        return;
                    }
                    if ("GET".equals(method)) {
                        Object found = findRecipe(recipes, id);
                        if (found == null) { respond(ex, 404, err("recipe not found")); return; }
                        respond(ex, 200, Json.write(found));
                        return;
                    }
                    if ("POST".equals(method)) {
                        Map<String, Object> body = Json.asObject(Json.parse(readBody(ex)));
                        String name = Json.str(body, "name", "").trim();
                        if (name.isEmpty()) { respond(ex, 400, err("name is required")); return; }
                        Map<String, Object> recipe = new LinkedHashMap<>();
                        String newId = UUID.randomUUID().toString();
                        recipe.put("id", newId);
                        recipe.put("name", name);
                        recipe.put("ingredients", normalizeIngredients(body.get("ingredients")));
                        recipe.put("instructions", Json.str(body, "instructions", ""));
                        recipes.add(recipe);
                        Files.writeString(RECIPES_FILE, Json.write(recipes), StandardCharsets.UTF_8);
                        respond(ex, 201, Json.write(recipe));
                        return;
                    }
                    if ("PUT".equals(method) && id != null) {
                        Map<String, Object> body = Json.asObject(Json.parse(readBody(ex)));
                        Map<String, Object> existing = (Map<String, Object>) findRecipe(recipes, id);
                        if (existing == null) { respond(ex, 404, err("recipe not found")); return; }
                        String name = Json.str(body, "name", (String) existing.get("name")).trim();
                        if (name.isEmpty()) { respond(ex, 400, err("name is required")); return; }
                        existing.put("name", name);
                        if (body.containsKey("ingredients")) {
                            existing.put("ingredients", normalizeIngredients(body.get("ingredients")));
                        }
                        if (body.containsKey("instructions")) {
                            existing.put("instructions", Json.str(body, "instructions", ""));
                        }
                        Files.writeString(RECIPES_FILE, Json.write(recipes), StandardCharsets.UTF_8);
                        respond(ex, 200, Json.write(existing));
                        return;
                    }
                    if ("DELETE".equals(method) && id != null) {
                        boolean removed = recipes.removeIf(r -> id.equals(Json.asObject(r).get("id")));
                        if (!removed) { respond(ex, 404, err("recipe not found")); return; }
                        Files.writeString(RECIPES_FILE, Json.write(recipes), StandardCharsets.UTF_8);
                        // also clear this recipe from any planning slot
                        Map<String, Object> plan = Json.asObject(Json.parse(Files.readString(PLANNING_FILE)));
                        for (String day : DAYS) {
                            Map<String, Object> meals = Json.asObject(plan.get(day));
                            for (String meal : MEALS) {
                                if (id.equals(meals.get(meal))) meals.put(meal, null);
                            }
                        }
                        Files.writeString(PLANNING_FILE, Json.write(plan), StandardCharsets.UTF_8);
                        respond(ex, 204, "");
                        return;
                    }
                    respond(ex, 405, err("method not allowed"));
                } finally {
                    lock.unlock();
                }
            } catch (Exception e) {
                respond(ex, 500, err("internal error: " + e.getMessage()));
            }
        }
    }

    static Object findRecipe(List<Object> recipes, String id) {
        for (Object r : recipes) {
            if (id.equals(Json.asObject(r).get("id"))) return r;
        }
        return null;
    }

    static List<Object> normalizeIngredients(Object raw) {
        List<Object> out = new ArrayList<>();
        for (Object item : Json.asArray(raw)) {
            Map<String, Object> ing = Json.asObject(item);
            Map<String, Object> clean = new LinkedHashMap<>();
            clean.put("name", Json.str(ing, "name", "").trim());
            clean.put("quantity", Json.num(ing, "quantity", 0));
            clean.put("unit", Json.str(ing, "unit", "").trim());
            if (!((String) clean.get("name")).isEmpty()) out.add(clean);
        }
        return out;
    }

    // ---------- Planning ----------

    static class PlanningHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            try {
                String method = ex.getRequestMethod();
                lock.lock();
                try {
                    if ("GET".equals(method)) {
                        respond(ex, 200, Files.readString(PLANNING_FILE));
                        return;
                    }
                    if ("PUT".equals(method)) {
                        Map<String, Object> body = Json.asObject(Json.parse(readBody(ex)));
                        Map<String, Object> plan = Json.asObject(Json.parse(Files.readString(PLANNING_FILE)));
                        List<Object> recipes = Json.asArray(Json.parse(Files.readString(RECIPES_FILE)));
                        for (String day : DAYS) {
                            if (!body.containsKey(day)) continue;
                            Map<String, Object> incoming = Json.asObject(body.get(day));
                            Map<String, Object> current = Json.asObject(plan.get(day));
                            for (String meal : MEALS) {
                                if (!incoming.containsKey(meal)) continue;
                                Object recipeId = incoming.get(meal);
                                if (recipeId != null && findRecipe(recipes, recipeId.toString()) == null) {
                                    respond(ex, 400, err("unknown recipe id: " + recipeId));
                                    return;
                                }
                                current.put(meal, recipeId);
                            }
                            plan.put(day, current);
                        }
                        Files.writeString(PLANNING_FILE, Json.write(plan), StandardCharsets.UTF_8);
                        respond(ex, 200, Json.write(plan));
                        return;
                    }
                    respond(ex, 405, err("method not allowed"));
                } finally {
                    lock.unlock();
                }
            } catch (Exception e) {
                respond(ex, 500, err("internal error: " + e.getMessage()));
            }
        }
    }

    // ---------- Shopping list ----------

    static class ShoppingListHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            try {
                if (!"GET".equals(ex.getRequestMethod())) {
                    respond(ex, 405, err("method not allowed"));
                    return;
                }
                lock.lock();
                try {
                    Map<String, Object> plan = Json.asObject(Json.parse(Files.readString(PLANNING_FILE)));
                    List<Object> recipes = Json.asArray(Json.parse(Files.readString(RECIPES_FILE)));

                    // key = name|unit -> total quantity
                    Map<String, Double> totals = new LinkedHashMap<>();
                    for (String day : DAYS) {
                        Map<String, Object> meals = Json.asObject(plan.get(day));
                        for (String meal : MEALS) {
                            Object recipeId = meals.get(meal);
                            if (recipeId == null) continue;
                            Object recipe = findRecipe(recipes, recipeId.toString());
                            if (recipe == null) continue;
                            for (Object ingObj : Json.asArray(Json.asObject(recipe).get("ingredients"))) {
                                Map<String, Object> ing = Json.asObject(ingObj);
                                String name = Json.str(ing, "name", "").trim().toLowerCase();
                                String unit = Json.str(ing, "unit", "").trim().toLowerCase();
                                double qty = Json.num(ing, "quantity", 0);
                                String key = name + "|" + unit;
                                totals.merge(key, qty, Double::sum);
                            }
                        }
                    }

                    List<Object> list = new ArrayList<>();
                    for (Map.Entry<String, Double> e : totals.entrySet()) {
                        String[] k = e.getKey().split("\\|", -1);
                        Map<String, Object> item = new LinkedHashMap<>();
                        item.put("name", k[0]);
                        item.put("unit", k[1]);
                        item.put("quantity", e.getValue());
                        list.add(item);
                    }
                    list.sort((a, b) -> ((String) Json.asObject(a).get("name"))
                            .compareTo((String) Json.asObject(b).get("name")));
                    respond(ex, 200, Json.write(list));
                } finally {
                    lock.unlock();
                }
            } catch (Exception e) {
                respond(ex, 500, err("internal error: " + e.getMessage()));
            }
        }
    }

    // ---------- Static files ----------

    static class StaticHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            String path = ex.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";
            Path file = PUBLIC_DIR.resolve(path.substring(1)).normalize();
            if (!file.startsWith(PUBLIC_DIR) || !Files.exists(file) || Files.isDirectory(file)) {
                respondBytes(ex, 404, "not found".getBytes(StandardCharsets.UTF_8), "text/plain");
                return;
            }
            byte[] content = Files.readAllBytes(file);
            respondBytes(ex, 200, content, contentType(file.toString()));
        }
    }

    static String contentType(String path) {
        if (path.endsWith(".html")) return "text/html; charset=utf-8";
        if (path.endsWith(".css")) return "text/css; charset=utf-8";
        if (path.endsWith(".js")) return "application/javascript; charset=utf-8";
        if (path.endsWith(".json")) return "application/json; charset=utf-8";
        return "application/octet-stream";
    }

    // ---------- HTTP helpers ----------

    static String readBody(HttpExchange ex) throws IOException {
        try (InputStream is = ex.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    static void respond(HttpExchange ex, int status, String body) throws IOException {
        respondBytes(ex, status, body.getBytes(StandardCharsets.UTF_8), "application/json; charset=utf-8");
    }

    static void respondBytes(HttpExchange ex, int status, byte[] body, String contentType) throws IOException {
        ex.getResponseHeaders().set("Content-Type", contentType);
        if (body.length == 0) {
            ex.sendResponseHeaders(status, -1);
        } else {
            ex.sendResponseHeaders(status, body.length);
            try (OutputStream os = ex.getResponseBody()) {
                os.write(body);
            }
        }
    }

    static String err(String message) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("error", message);
        return Json.write(m);
    }
}
