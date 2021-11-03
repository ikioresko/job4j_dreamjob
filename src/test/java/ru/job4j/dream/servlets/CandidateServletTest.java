package ru.job4j.dream.servlets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.job4j.dream.model.Candidate;
import ru.job4j.dream.store.PsqlStore;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

public class CandidateServletTest {
    private static Connection connection;

    @BeforeClass
    public static void initConnection() {
        try (InputStream in = PostServletTest.class.getClassLoader()
                .getResourceAsStream("db.properties")) {
            Properties config = new Properties();
            config.load(in);
            Class.forName(config.getProperty("jdbc.driver"));
            connection = DriverManager.getConnection(
                    config.getProperty("jdbc.url"),
                    config.getProperty("jdbc.username"),
                    config.getProperty("jdbc.password")
            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @AfterClass
    public static void closeConnection() throws SQLException {
        connection.close();
    }

    @After
    public void wipeTable() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("delete from candidates;"
                + System.lineSeparator()
                + "ALTER TABLE candidates ALTER COLUMN id RESTART WITH 1;")) {
            statement.execute();
        }
    }

    @Test
    public void whenCreateCandidate() throws IOException {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(req.getParameter("id")).thenReturn("0");
        when(req.getParameter("name")).thenReturn("name of new post");
        when(req.getParameter("city")).thenReturn("1");
        new CandidateServlet().doPost(req, resp);
        Candidate candidate = PsqlStore.instOf().findCandidateById(1);
        assertThat(candidate, notNullValue());
    }

    @Test
    public void whenGetCandidate() throws IOException, ServletException {
        String path = "candidate/candidates.jsp";
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        when(req.getRequestDispatcher(path)).thenReturn(dispatcher);
        new CandidateServlet().doGet(req, resp);
        verify(req, times(1)).getRequestDispatcher(path);
        verify(req, never()).getSession();
        verify(dispatcher).forward(req, resp);
    }
}