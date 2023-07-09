package walerowicz.pawel.spotifyfun.playlist.service.search;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ConcurrentRequestProcessorTest {
    private ConcurrentRequestProcessor processor;

    @Captor
    ArgumentCaptor<ConcurrentSearch> concurrentSearchCaptor;

    @BeforeEach
    void reset() {
        processor = new ConcurrentRequestProcessor("");
    }

    @Test
    void shouldExecuteOneConcurrentSearchForEachQuery() {
        try (MockedStatic<Executors> executorsMockedStatic = Mockito.mockStatic(Executors.class)) {
            final var mockExecutorService = Mockito.mock(ExecutorService.class);
            executorsMockedStatic.when(() -> Executors.newFixedThreadPool(any(Integer.class)))
                    .thenReturn(mockExecutorService);
            final var queryList = List.of("query1", "query2", "query3", "query4");

            processor.sendConcurrentRequests(queryList, "test-token");

            executorsMockedStatic.verify(() -> Executors.newFixedThreadPool(queryList.size()));
            verify(mockExecutorService, times(queryList.size())).execute(any(Runnable.class));
        }
    }

    @Test
    void shouldStopAllConcurrentSearches() {
        try (MockedStatic<Executors> executorsMockedStatic = Mockito.mockStatic(Executors.class)) {
            final var mockExecutorService = Mockito.mock(ExecutorService.class);
            executorsMockedStatic.when(() -> Executors.newFixedThreadPool(any(Integer.class)))
                    .thenReturn(mockExecutorService);
            final var queryList = List.of("query1", "query2", "query3", "query4");

            processor.sendConcurrentRequests(queryList, "test-token");
            final var allSearches = concurrentSearchCaptor.getAllValues();
            allSearches.forEach(concurrentSearch -> assertTrue(concurrentSearch.isRunning()));
            processor.stopSendingRequests();
            allSearches.forEach(concurrentSearch -> assertFalse(concurrentSearch.isRunning()));
        }
    }
}