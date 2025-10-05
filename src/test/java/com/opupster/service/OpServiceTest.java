package com.opupster.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class OpServiceTest {
    @Test
    void execute_shouldRunWithoutException() {
        var svc = new OpService();
        assertDoesNotThrow(svc::execute);
    }
}
