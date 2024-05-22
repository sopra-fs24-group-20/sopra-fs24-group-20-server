package ch.uzh.ifi.hase.soprafs24.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class APITest {

    private RoundService roundService;

    @BeforeEach
    void setUp() {
        roundService = new RoundService();
    }

    @Test
    void checkWordExists_ExistingWord_ShouldReturnTrue() {
        // Directly testing the method
        assertTrue(roundService.checkWordExists("Tree"), "The word 'Tree' should exist.");
    }

    @Test
    void checkWordExists_Not_ExistingWord_ShouldReturnFalse() {
        // Directly testing the method
        assertFalse(roundService.checkWordExists("Treeeeeeeeee"), "The word 'Treeeeeeeeee' should not exist.");
    }
}
