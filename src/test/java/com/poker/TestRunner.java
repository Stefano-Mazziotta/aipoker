package com.poker;

import com.poker.game.domain.model.GameTest;
import com.poker.integration.FullGameIntegrationTest;
import com.poker.lobby.application.LobbyUseCaseTest;
import com.poker.shared.infrastructure.socket.SocketServerTest;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/**
 * Main test suite runner for the Poker application.
 * Runs all integration and unit tests.
 */
public class TestRunner {
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("Texas Hold'em Poker - Test Suite");
        System.out.println("========================================\n");
        
        JUnitCore junit = new JUnitCore();
        
        // Run domain tests
        System.out.println("Running Domain Tests...");
        Result domainResult = junit.run(GameTest.class);
        printResults("Domain Tests", domainResult);
        
        // Run lobby tests
        System.out.println("\nRunning Lobby Tests...");
        Result lobbyResult = junit.run(LobbyUseCaseTest.class);
        printResults("Lobby Tests", lobbyResult);
        
        // Run integration tests
        System.out.println("\nRunning Integration Tests...");
        Result integrationResult = junit.run(FullGameIntegrationTest.class);
        printResults("Integration Tests", integrationResult);
        
        // Run socket server tests
        System.out.println("\nRunning Socket Server Tests...");
        Result serverResult = junit.run(SocketServerTest.class);
        printResults("Socket Server Tests", serverResult);
        
        // Summary
        int totalTests = domainResult.getRunCount() + lobbyResult.getRunCount() +
                        integrationResult.getRunCount() + serverResult.getRunCount();
        int totalFailures = domainResult.getFailureCount() + lobbyResult.getFailureCount() +
                           integrationResult.getFailureCount() + serverResult.getFailureCount();
        
        System.out.println("\n========================================");
        System.out.println("Test Summary");
        System.out.println("========================================");
        System.out.println("Total Tests: " + totalTests);
        System.out.println("Passed: " + (totalTests - totalFailures));
        System.out.println("Failed: " + totalFailures);
        
        if (totalFailures == 0) {
            System.out.println("\n✅ ALL TESTS PASSED!");
        } else {
            System.out.println("\n❌ SOME TESTS FAILED!");
            System.exit(1);
        }
    }
    
    private static void printResults(String suiteName, Result result) {
        System.out.println("  Tests run: " + result.getRunCount());
        System.out.println("  Failures: " + result.getFailureCount());
        System.out.println("  Time: " + result.getRunTime() + "ms");
        
        if (result.getFailureCount() > 0) {
            System.out.println("  Failed tests:");
            for (Failure failure : result.getFailures()) {
                System.out.println("    • " + failure.getTestHeader());
                System.out.println("      " + failure.getMessage());
            }
        } else {
            System.out.println("  ✅ All tests passed!");
        }
    }
}
