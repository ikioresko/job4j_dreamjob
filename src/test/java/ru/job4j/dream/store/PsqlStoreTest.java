package ru.job4j.dream.store;

import org.junit.Test;
import ru.job4j.dream.model.Candidate;
import ru.job4j.dream.model.Post;

import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;

public class PsqlStoreTest {
    @Test
    public void whenCreatePost() {
        Store store = PsqlStore.instOf();
        Post post = new Post(0, "Java Job", "Java Job Desc", new Date());
        store.save(post);
        Post postInDb = store.findPostById(post.getId());
        assertThat(postInDb.getName(), is(post.getName()));
    }

    @Test
    public void whenCreateCandidate() {
        Store store = PsqlStore.instOf();
        Candidate candidate = new Candidate(0, "Java Junior", 1, new Date());
        store.save(candidate);
        Candidate candidateInDb = store.findCandidateById(candidate.getId());
        assertThat(candidateInDb.getName(), is(candidate.getName()));
    }

    @Test
    public void whenDeletePost() {
        Store store = PsqlStore.instOf();
        Post post = new Post(0, "Java Job", "Java Job Desc", new Date());
        store.save(post);
        int id = post.getId();
        store.deletePost(id);
        Post postInDb = store.findPostById(id);
        assertNull(postInDb);
    }

    @Test
    public void whenDeleteCandidate() {
        Store store = PsqlStore.instOf();
        Candidate candidate = new Candidate(0, "Java Junior", 1, new Date());
        store.save(candidate);
        int id = candidate.getId();
        store.deleteCandidate(id);
        Candidate candidateInDb = store.findCandidateById(id);
        assertNull(candidateInDb);
    }

    @Test
    public void whenFindAllPost() {
        Store store = PsqlStore.instOf();
        Post post = new Post(0, "Java Job", "Java Job Desc", new Date());
        store.save(post);
        List<Post> postInDb = (List<Post>) store.findAllPosts();
        assertThat(postInDb.size(), greaterThan(0));
    }

    @Test
    public void whenFindAllCandidate() {
        Store store = PsqlStore.instOf();
        Candidate candidate = new Candidate(0, "Java Junior", 1, new Date());
        store.save(candidate);
        List<Candidate> candidateInDb = (List<Candidate>) store.findAllCandidates();
        assertThat(candidateInDb.size(), greaterThan(0));
    }
}