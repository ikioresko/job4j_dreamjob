package ru.job4j.dream.store;

import ru.job4j.dream.model.Candidate;
import ru.job4j.dream.model.City;
import ru.job4j.dream.model.Post;
import ru.job4j.dream.model.User;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MemStore implements Store {
    private static final MemStore INST = new MemStore();
    private static final AtomicInteger POST_ID = new AtomicInteger(3);
    private static final AtomicInteger USER_ID = new AtomicInteger();
    private static final AtomicInteger CANDIDATE_ID = new AtomicInteger(3);
    private static final AtomicInteger CITY_ID = new AtomicInteger(0);
    private final Map<Integer, City> cities = new ConcurrentHashMap<>();
    private final Map<Integer, Post> posts = new ConcurrentHashMap<>();
    private final Map<Integer, User> users = new ConcurrentHashMap<>();
    private final Map<Integer, Candidate> candidates = new ConcurrentHashMap<>();

    private MemStore() {
//        posts.put(1, new Post(1,
//                "Junior Java Job",
//                "Java Developer", new Date()));
//        posts.put(2, new Post(2,
//                "Middle Java Job",
//                "Wanted Java Developer", new Date()));
//        posts.put(3, new Post(3,
//                "Senior Java Job",
//                "Wanted Senior Java Developer", new Date()));
//        candidates.put(1, new Candidate(1, "Junior Java", 1, new Date()));
//        candidates.put(2, new Candidate(2, "Middle Java", 2, new Date()));
//        candidates.put(3, new Candidate(3, "Senior Java", 3, new Date()));
    }

    public static Store instOf() {
        return INST;
    }

    public void save(Post post) {
        if (post.getId() == 0) {
            post.setId(POST_ID.incrementAndGet());
        }
        posts.put(post.getId(), post);
    }

    private Date getStartDateToday() {
        Date date = new Date();
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    @Override
    public void deleteCandidate(int id) {
        candidates.remove(id);
    }

    @Override
    public void deletePost(int id) {
        posts.remove(id);
    }

    @Override
    public Post findPostById(int id) {
        return posts.get(id);
    }

    @Override
    public Candidate findCandidateById(int id) {
        return candidates.get(id);
    }

    @Override
    public void save(Candidate candidate) {
        if (candidate.getId() == 0) {
            candidate.setId(CANDIDATE_ID.incrementAndGet());
        }
        candidates.put(candidate.getId(), candidate);
    }

    @Override
    public Collection<Post> findAllPosts() {
        return posts.values();
    }

    @Override
    public Collection<Post> todayPosts() {
        List<Post> list = new ArrayList<>();
        Date startDate = getStartDateToday();
        for (Post post : findAllPosts()) {
            if (startDate.compareTo(post.getCreated()) <= 0) {
                list.add(post);
            }
        }
        return list;
    }

    @Override
    public Collection<Candidate> findAllCandidates() {
        return candidates.values();
    }

    @Override
    public Collection<City> findAllCities() {
        return cities.values();
    }

    @Override
    public Collection<Candidate> todayCandidates() {
        List<Candidate> list = new ArrayList<>();
        Date startDate = getStartDateToday();
        for (Candidate can : findAllCandidates()) {
            if (startDate.compareTo(can.getCreated()) <= 0) {
                list.add(can);
            }
        }
        return list;
    }

    @Override
    public void save(User user) {
        if (user.getId() == 0) {
            user.setId(USER_ID.incrementAndGet());
        }
        users.put(user.getId(), user);
    }

    @Override
    public User findUserByEmail(String email) {
        User user = null;
        for (User us : users.values()) {
            if (us.getEmail().equals(email)) {
                user = us;
                break;
            }
        }
        return user;
    }
}