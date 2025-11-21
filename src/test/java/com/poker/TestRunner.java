package com.poker;

import com.poker.game.domain.model.GameTest;
import com.poker.integration.FullGameIntegrationTest;
import com.poker.lobby.application.LobbyUseCaseTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Main test suite runner for the Poker application. Runs all integration and
 * unit tests using JUnit 5.
 */
@Suite
@SuiteDisplayName("Texas Hold'em Poker - Test Suite")
@SelectClasses({
    GameTest.class,
    LobbyUseCaseTest.class,
    FullGameIntegrationTest.class
})
public class TestRunner {
    // This class remains empty, used only as a holder for the suite configuration
}
