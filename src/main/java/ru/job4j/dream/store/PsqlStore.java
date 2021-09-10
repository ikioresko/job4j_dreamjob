package ru.job4j.dream.store;

import org.apache.commons.dbcp2.BasicDataSource;
import ru.job4j.dream.model.Candidate;
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
                            it.getString("description")));
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
                            it.getString("name")));
                }
            }
        } catch (Exception e) {
            LOG.error("Exception: ", e);
        }
        return candidates;
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

    private Candidate create(Candidate candidate) {
        try (PreparedStatement ps = getCn().prepareStatement(
                "INSERT INTO candidates(name) VALUES (?)",
                PreparedStatement.RETURN_GENERATED_KEYS)
        ) {
            ps.setString(1, candidate.getName());
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
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
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

    private void update(Candidate candidate) {
        try (PreparedStatement ps = getCn().prepareStatement(
                "UPDATE candidates SET name = ? WHERE id = ?")) {
            ps.setString(1, candidate.getName());
            ps.setInt(2, candidate.getId());
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
                            rs.getString("name"));
                }
            }
        } catch (Exception e) {
            LOG.error("Exception: ", e);
        }
        return candidate;
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
