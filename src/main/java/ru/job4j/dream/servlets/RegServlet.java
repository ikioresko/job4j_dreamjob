package ru.job4j.dream.servlets;

import ru.job4j.dream.model.User;
import ru.job4j.dream.store.PsqlStore;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RegServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String email = req.getParameter("email");
        if (PsqlStore.instOf().findUserByEmail(email) == null) {
            User user = new User();
            user.setName(req.getParameter("name"));
            user.setEmail(email);
            user.setPassword(req.getParameter("password"));
            PsqlStore.instOf().save(user);
            req.setAttribute("error", "Регистрация завершена");
        } else {
            req.setAttribute("error", "Пользователь с таким email уже существует");
        }
        req.getRequestDispatcher("register/reg.jsp").forward(req, resp);
    }
}
