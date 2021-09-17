package ru.job4j.dream.store;

import ru.job4j.dream.model.Candidate;
import ru.job4j.dream.model.Post;
import ru.job4j.dream.model.User;

import java.util.Date;

public class PsqlMain {
    public static void main(String[] args) {
        Store store = PsqlStore.instOf();
        store.save(new Candidate(0, "Candidate 1", 1, new Date()));
        store.save(new Candidate(0, "Candidate 2", 2, new Date()));
        store.save(new Candidate(0, "Candidate 3", 3, new Date()));
        store.save(new Post(0, "Java Job 1", "Some Desc 1", new Date()));
        store.save(new Post(0, "Java Job 2", "Some Desc 2", new Date()));
        store.save(new Post(0, "Java Job 3", "Some Desc 3", new Date()));
        store.deleteCandidate(1);
        System.out.println(store.findCandidateById(3));
        for (Candidate can : store.findAllCandidates()) {
            System.out.println(can.getId() + " " + can.getName());
        }
        store.deletePost(1);
        System.out.println(store.findPostById(2));
        for (Post post : store.findAllPosts()) {
            System.out.println(post.getId()
                    + " " + post.getName()
                    + " " + post.getDesc()
                    + " " + post.getCreated());
        }
        store.save(new User().userOf(0, "User", "root@local", "root"));
        store.save(new User().userOf(0, "User", "user@local", "user"));
        System.out.println(store.findUserByEmail("root@local"));
    }
}
