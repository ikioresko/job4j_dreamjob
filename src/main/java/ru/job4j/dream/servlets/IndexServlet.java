package ru.job4j.dream.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.job4j.dream.store.PsqlStore;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class IndexServlet extends HttpServlet {
    private final static Gson GSON = new GsonBuilder().create();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setContentType("application/json; charset=utf-8");
        OutputStream output = resp.getOutputStream();
        String json = GSON.toJson(PsqlStore.instOf().todayPosts());
        output.write(json.getBytes(StandardCharsets.UTF_8));
        output.flush();
        output.close();
        req.getRequestDispatcher("index.jsp").forward(req, resp);
    }
}
