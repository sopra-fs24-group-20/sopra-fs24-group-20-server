package ch.uzh.ifi.hase.soprafs24;

import ch.uzh.ifi.hase.soprafs24.exceptions.GlobalExceptionAdvice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class GlobalExceptionAdviceTest {

    @Mock
    private Logger logger;

    @InjectMocks
    private GlobalExceptionAdvice exceptionAdvice;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void handleConflictTest() {
        // Setup
        RuntimeException ex = new IllegalArgumentException("Test exception message");
        WebRequest request = mock(WebRequest.class);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        // Call method
        ResponseEntity<Object> responseEntity = exceptionAdvice.handleConflict(ex, request);

        // Assertions
        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
        assertEquals("This should be application specific", responseEntity.getBody());
    }
}
