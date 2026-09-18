package com.btxtech.server.user;

import com.btxtech.server.model.UserEntity;
import com.btxtech.server.repository.UserRepository;
import com.btxtech.server.service.engine.LevelCrudService;
import com.btxtech.server.service.tracking.UserActivityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The game page fires its first requests in parallel, before its http session has a player. Each
 * one created an anonymous player of its own, and the client ended up knowing itself under a
 * different id than its base - a yellow builder and no quest. 37 of 1180 new players on PROD,
 * 2026-09-11..18, reported from a phone in the Meta browser on 2026-09-18.
 */
class AnonymousUserPerSessionTest {
    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserService(mock(LevelCrudService.class), userRepository, null, null, null, null,
                mock(UserActivityService.class), null);
        // Slow enough that the requests overlap, as they do on a phone.
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            Thread.sleep(50);
            return invocation.getArgument(0);
        });
        // The inserting transaction has not committed: nobody else sees the row yet.
        when(userRepository.findByUserId(anyString())).thenReturn(Optional.empty());
    }

    @Test
    void parallelRequestsOfOneSessionGetOnePlayer() throws Exception {
        int requests = 3;
        ExecutorService executor = Executors.newFixedThreadPool(requests);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<String>> results = new ArrayList<>();
        for (int i = 0; i < requests; i++) {
            Callable<String> request = () -> {
                start.await();
                return userService.getOrCreateUserId(null, "http-session-1");
            };
            results.add(executor.submit(request));
        }
        start.countDown();
        String first = results.get(0).get();
        for (Future<String> result : results) {
            assertEquals(first, result.get(), "every request of the page has to see the same player");
        }
        executor.shutdown();
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void differentSessionsGetDifferentPlayers() {
        String one = userService.getOrCreateUserId(null, "http-session-1");
        String other = userService.getOrCreateUserId(null, "http-session-2");

        assertNotEquals(one, other);
    }
}
