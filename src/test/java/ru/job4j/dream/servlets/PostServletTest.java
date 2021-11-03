package ru.job4j.dream.servlets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.job4j.dream.model.Post;
import ru.job4j.dream.store.PsqlStore;
import ru.job4j.dream.store.Store;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class PostServletTest {
    private static final BasicDataSource POOL = new BasicDataSource();
    private final Store store = new PsqlStore(POOL);

    @BeforeClass
    public static void initConnection() {
        Properties cfg = new Properties();
        try (BufferedReader io = new BufferedReader(
                new InputStreamReader(PsqlStore.class.getClassLoader()
                        .getResourceAsStream("test.properties")
                )
        )) {
            cfg.load(io);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        try {
            Class.forName(cfg.getProperty("jdbc.driver"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        POOL.setDriverClassName(cfg.getProperty("jdbc.driver"));
        POOL.setUrl(cfg.getProperty("jdbc.url"));
        POOL.setUsername(cfg.getProperty("jdbc.username"));
        POOL.setPassword(cfg.getProperty("jdbc.password"));
        POOL.setMinIdle(5);
        POOL.setMaxIdle(10);
        POOL.setMaxOpenPreparedStatements(100);
    }

    @AfterClass
    public static void closeConnection() throws SQLException {
        POOL.close();
    }

    @After
    public void wipeTable() throws SQLException {
        try (Connection cn = POOL.getConnection();
             PreparedStatement statement = cn
                     .prepareStatement("delete from post;"
                             + System.lineSeparator()
                             + "ALTER TABLE post ALTER COLUMN id RESTART WITH 1;")) {
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

}