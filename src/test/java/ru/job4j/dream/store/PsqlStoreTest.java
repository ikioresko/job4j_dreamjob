package ru.job4j.dream.store;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.job4j.dream.model.Candidate;
import ru.job4j.dream.model.Post;
import ru.job4j.dream.servlets.PostServletTest;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class PsqlStoreTest {
    private static Connection connection;
    private final Store store = PsqlStore.instOf();

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
        try (PreparedStatement statement = connection.prepareStatement("delete from post;"
                + System.lineSeparator()
                + "delete from candidates;"
                + System.lineSeparator()
                + "ALTER TABLE post ALTER COLUMN id RESTART WITH 1;"
                + System.lineSeparator()
                + "ALTER TABLE candidates ALTER COLUMN id RESTART WITH 1;")) {
            statement.execute();
        }
    }

    @Test
    public void whenCreatePostAndGetByID() {
        store.save(new Post(0, "postName", "postDesc", new Date(System.currentTimeMillis())));
        Post post = store.findPostById(1);
        assertThat(post.getName(), is("postName"));
    }

    @Test
    public void whenCreatePostAndEdit() {
        store.save(new Post(0, "postName", "postDesc", new Date(System.currentTimeMillis())));
        store.save(new Post(1, "newName", "newPostDesc", new Date(System.currentTimeMillis())));
        Post post = store.findPostById(1);
        assertThat(post.getName(), is("newName"));
        assertThat(post.getDesc(), is("newPostDesc"));
    }

    @Test
    public void whenCreatePostAndDelete() {
        store.save(new Post(0, "postName", "postDesc", new Date(System.currentTimeMillis())));
        store.deletePost(1);
        List<Post> list = (List<Post>) store.findAllPosts();
        assertThat(list.size(), is(0));
    }

    @Test
    public void whenCreateCandidateAndGetById() {
        store.save(new Candidate(0, "name", 1, new Date(System.currentTimeMillis())));
        Candidate candidate = store.findCandidateById(1);
        assertThat(candidate.getName(), is("name"));
    }

    @Test
    public void whenCreateCandidateAndEdit() {
        store.save(new Candidate(0, "name", 1, new Date(System.currentTimeMillis())));
        store.save(new Candidate(1, "newName", 1, new Date(System.currentTimeMillis())));
        Candidate candidate = store.findCandidateById(1);
        assertThat(candidate.getName(), is("newName"));
    }

    @Test
    public void whenCreateCandidateAndDelete() {
        store.save(new Candidate(0, "name", 1, new Date(System.currentTimeMillis())));
        store.deleteCandidate(1);
        List<Candidate> list = (List<Candidate>) store.findAllCandidates();
        assertThat(list.size(), is(0));
    }
}