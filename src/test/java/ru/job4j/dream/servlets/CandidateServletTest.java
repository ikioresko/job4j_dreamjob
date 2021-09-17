package ru.job4j.dream.servlets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import ru.job4j.dream.model.Candidate;
import ru.job4j.dream.store.MemStore;
import ru.job4j.dream.store.PsqlStore;
import ru.job4j.dream.store.Store;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RunWith(PowerMockRunner.class)
@PrepareForTest(PsqlStore.class)
public class CandidateServletTest {
    private final Store store = MemStore.instOf();

    @Test
    public void whenCreateCandidate() throws IOException {
        PowerMockito.mockStatic(PsqlStore.class);
        PowerMockito.when(PsqlStore.instOf()).thenReturn(store);
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PowerMockito.when(req.getParameter("id")).thenReturn("0");
        PowerMockito.when(req.getParameter("name")).thenReturn("n");
        PowerMockito.when(req.getParameter("city")).thenReturn("1");
        new CandidateServlet().doPost(req, resp);
        Candidate result = store.findAllCandidates().iterator().next();
        assertThat(result.getName(), is("n"));
    }

    @Test
    public void whenGetCandidates() throws IOException, ServletException {
        String path = "candidate/candidates.jsp";
        PowerMockito.mockStatic(PsqlStore.class);
        PowerMockito.when(PsqlStore.instOf()).thenReturn(store);
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        PowerMockito.when(req.getRequestDispatcher(path)).thenReturn(dispatcher);
        new CandidateServlet().doGet(req, resp);
        verify(req, times(1)).getRequestDispatcher(path);
        verify(req, never()).getSession();
        verify(dispatcher).forward(req, resp);
    }
}