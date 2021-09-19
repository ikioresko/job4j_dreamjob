package ru.job4j.dream.store;

import org.apache.commons.dbcp2.BasicDataSource;
import ru.job4j.dream.model.Candidate;
import ru.job4j.dream.model.City;
import ru.job4j.dream.model.Post;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.job4j.dream.model.User;

public class PsqlStore implements Store {
    private static final Logger LOG = LoggerFactory.getLogger(PsqlStore.class.getName());
    private final BasicDataSource pool = new BasicDataSource();

    private PsqlStore() {
        Properties cfg = new Properties();
        try (BufferedReader io = new BufferedReader(
                new FileReader("db.properties")
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
        pool.setDriverClassName(cfg.getProperty("jdbc.driver"));
        pool.setUrl(cfg.getProperty("jdbc.url"));
        pool.setUsername(cfg.getProperty("jdbc.username"));
        pool.setPassword(cfg.getProperty("jdbc.password"));
        pool.setMinIdle(5);
        pool.setMaxIdle(10);
        pool.setMaxOpenPreparedStatements(100);
    }

    private static final class Lazy {
        private static final Store INST = new PsqlStore();
    }

    public static Store instOf() {
        return Lazy.INST;
    }

    private Connection getCn() throws SQLException {
        return pool.getConnection();
    }

    @Override
    public Collection<Post> findAllPosts() {
        List<Post> posts = new ArrayList<>();
        try (PreparedStatement ps = getCn().prepareStatement("SELECT * FROM post")
        ) {
            try (ResultSet it = ps.executeQuery()) {
                while (it.next()) {
                    posts.add(new Post(it.getInt("id"),
                            it.getString("name"),
                            it.getString("description"),
                            it.getTimestamp("created")));
                }
            }
        } catch (Exception e) {
            LOG.error("Exception: ", e);
        }
        return posts;
    }

    @Override
    public Collection<Candidate> findAllCandidates() {
        List<Candidate> candidates = new ArrayList<>();
        try (PreparedStatement ps = getCn().prepareStatement("SELECT * FROM candidates")
        ) {
            try (ResultSet it = ps.executeQuery()) {
                while (it.next()) {
                    candidates.add(new Candidate(it.getInt("id"),
                            it.getString("name"),
                            it.getInt("city_id"),
                            it.getTimestamp("created")));
                }
            }
        } catch (Exception e) {
            LOG.error("Exception: ", e);
        }
        return candidates;
    }

    @Override
    public Collection<Post> todayPosts() {
        List<Post> posts = new ArrayList<>();
        try (PreparedStatement ps = getCn().prepareStatement(
                "SELECT * FROM post WHERE created BETWEEN NOW() - INTERVAL '24 HOUR' and NOW()")
        ) {
            try (ResultSet it = ps.executeQuery()) {
                while (it.next()) {
                    posts.add(new Post(it.getInt("id"),
                            it.getString("name"),
                            it.getString("description"),
                            it.getTimestamp("created")));
                }
            }
        } catch (Exception e) {
            LOG.error("Exception: ", e);
        }
        return posts;
    }

    @Override
    public Collection<Candidate> todayCandidates() {
        List<Candidate> candidates = new ArrayList<>();
        try (PreparedStatement ps = getCn().prepareStatement(
                "SELECT * FROM candidates "
                        + "WHERE created BETWEEN NOW() - INTERVAL '24 HOUR' and NOW()")
        ) {
            try (ResultSet it = ps.executeQuery()) {
                while (it.next()) {
                    candidates.add(new Candidate(it.getInt("id"),
                            it.getString("name"),
                            it.getInt("city_id"),
                            it.getTimestamp("created")));
                }
            }
        } catch (Exception e) {
            LOG.error("Exception: ", e);
        }
        return candidates;
    }

    @Override
    public Collection<City> findAllCities() {
        List<City> cities = new ArrayList<>();
        try (PreparedStatement ps = getCn().prepareStatement("SELECT * FROM city")
        ) {
            try (ResultSet it = ps.executeQuery()) {
                while (it.next()) {
                    cities.add(new City(it.getInt("id"),
                            it.getString("name")));
                }
            }
        } catch (Exception e) {
            LOG.error("Exception: ", e);
        }
        return cities;
    }

    @Override
    public void save(Post post) {
        if (post.getId() == 0) {
            create(post);
        } else {
            update(post);
        }
    }

    @Override
    public void save(Candidate candidate) {
        if (candidate.getId() == 0) {
            create(candidate);
        } else {
            update(candidate);
        }
    }

    @Override
    public void save(User user) {
        if (user.getId() == 0) {
            create(user);
        } else {
            update(user);
        }
    }

    private Candidate create(Candidate candidate) {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String text = formatter.format(candidate.getCreated());
        try (PreparedStatement ps = getCn().prepareStatement(
                "INSERT INTO candidates(name, city_id, created) VALUES (? , ? , ?)",
                PreparedStatement.RETURN_GENERATED_KEYS)
        ) {
            ps.setString(1, candidate.getName());
            ps.setInt(2, candidate.getCityId());
            ps.setTimestamp(3, Timestamp.valueOf(text));
            ps.execute();
            try (ResultSet id = ps.getGeneratedKeys()) {
                if (id.next()) {
                    candidate.setId(id.getInt(1));
                }
            }
        } catch (Exception e) {
            LOG.error("Exception: ", e);
        }
        return candidate;
    }

    private Post create(Post post) {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String text = formatter.format(post.getCreated());
        try (PreparedStatement ps = getCn().prepareStatement(
                "INSERT INTO post(name, description, created) VALUES (? , ? , ?)",
                PreparedStatement.RETURN_GENERATED_KEYS)
        ) {
            ps.setString(1, post.getName());
            ps.setString(2, post.getDesc());
            ps.setTimestamp(3, Timestamp.valueOf(text));
            ps.execute();
            try (ResultSet id = ps.getGeneratedKeys()) {
                if (id.next()) {
                    post.setId(id.getInt(1));
                }
            }
        } catch (Exception e) {
            LOG.error("Exception: ", e);
        }
        return post;
    }

    private User create(User user) {
        try (PreparedStatement ps = getCn().prepareStatement(
                "INSERT INTO users(name, email, password) VALUES (? , ? , ?) "
                        + "ON CONFLICT DO NOTHING",
                PreparedStatement.RETURN_GENERATED_KEYS)
        ) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.execute();
            try (ResultSet id = ps.getGeneratedKeys()) {
                if (id.next()) {
                    user.setId(id.getInt(1));
                }
            }
        } catch (Exception e) {
            LOG.error("Exception: ", e);
        }
        return user;
    }

    private void update(Candidate candidate) {
        try (PreparedStatement ps = getCn().prepareStatement(
                "UPDATE candidates SET name = ?, city_id = ? WHERE id = ?")) {
            ps.setString(1, candidate.getName());
            ps.setInt(2, candidate.getCityId());
            ps.setInt(3, candidate.getId());
            ps.execute();
        } catch (Exception e) {
            LOG.error("Exception: ", e);
        }
    }

    private void update(Post post) {
        try (PreparedStatement ps = getCn().prepareStatement(
                "UPDATE post SET name = ?, description = ? WHERE id = ?")) {
            ps.setString(1, post.getName());
            ps.setString(2, post.getDesc());
            ps.setInt(3, post.getId());
            ps.execute();
        } catch (Exception e) {
            LOG.error("Exception: ", e);
        }
    }

    private void update(User user) {
        try (PreparedStatement ps = getCn().prepareStatement(
                "UPDATE users SET name = ?, email = ?, password = ? WHERE id = ?")) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setInt(4, user.getId());
            ps.execute();
        } catch (Exception e) {
            LOG.error("Exception: ", e);
        }
    }

    @Override
    public Post findPostById(int id) {
        Post post = null;
        try (PreparedStatement ps = getCn().prepareStatement(
                "SELECT * FROM post WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    post = new Post(rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getTimestamp("created"));
                }
            }
        } catch (Exception e) {
            LOG.error("Exception: ", e);
        }
        return post;
    }

    @Override
    public Candidate findCandidateById(int id) {
        Candidate candidate = null;
        try (PreparedStatement ps = getCn().prepareStatement(
                "SELECT * FROM candidates WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    candidate = new Candidate(rs.getInt("id"),
                            rs.getString("name"),
                            rs.getInt("city_id"),
                            rs.getTimestamp("created"));
                }
            }
        } catch (Exception e) {
            LOG.error("Exception: ", e);
        }
        return candidate;
    }

    @Override
    public User findUserByEmail(String email) {
        User user = null;
        try (PreparedStatement ps = getCn().prepareStatement(
                "SELECT * FROM users WHERE email = ?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    user = new User().userOf(rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("password"));
                }
            }
        } catch (Exception e) {
            LOG.error("Exception: ", e);
        }
        return user;
    }

    @Override
    public void deleteCandidate(int id) {
        try (PreparedStatement ps = getCn().prepareStatement(
                "DELETE FROM candidates WHERE id = ?")) {
            ps.setInt(1, id);
            ps.execute();
        } catch (Exception e) {
            LOG.error("Exception: ", e);
        }
    }

    @Override
    public void deletePost(int id) {
        try (PreparedStatement ps = getCn().prepareStatement(
                "DELETE FROM post WHERE id = ?")) {
            ps.setInt(1, id);
            ps.execute();
        } catch (Exception e) {
            LOG.error("Exception: ", e);
        }
    }
}