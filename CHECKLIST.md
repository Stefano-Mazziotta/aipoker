# Implementation Checklist

Use this checklist to track progress on fixing the poker game flow issues.

## 🔴 Phase 1: Critical Fixes (Week 1)

### Issue #1: Duplicate Cards Bug
- [ ] Investigate current deck management in `Game.java`
- [ ] Review `Deck.dealCard()` implementation
- [ ] Check `Game.reconstitute()` for deck restoration issues
- [ ] Ensure deck is NOT persisted to database
- [ ] Add unit test: `testDeckDealsUniqueCards()`
- [ ] Add unit test: `testDeckHas52UniqueCards()`
- [ ] Run 100 test games and verify no duplicates
- [ ] Document fix in code comments

**Files to Modify:**
- `Game.java` - Fix reconstitute method
- `SQLiteGameRepository.java` - Don't save/load deck
- `GameTest.java` - Add tests

### Issue #2: Betting Round Enforcement
- [ ] Add `bettingRoundComplete` field to `Game`
- [ ] Create `BettingRoundValidator.java` domain service
- [ ] Implement `isBettingRoundComplete()` method
- [ ] Track player actions per betting round
- [ ] Modify `dealFlop()` to check betting complete
- [ ] Modify `dealTurn()` to check betting complete
- [ ] Modify `dealRiver()` to check betting complete
- [ ] Add unit test: `testCannotDealFlopWithoutBetting()`
- [ ] Add unit test: `testCannotDealTurnWithoutBetting()`
- [ ] Add unit test: `testCannotDealRiverWithoutBetting()`
- [ ] Add integration test: `testCompleteBettingFlow()`

**Files to Create:**
- `BettingRoundValidator.java`

**Files to Modify:**
- `Game.java` - Add validation
- `DealCardsUseCase.java` - Check before dealing
- `Round.java` - Track player actions
- `GameTest.java` - Add tests

### Issue #3: Turn Management
- [ ] Add `currentPlayerIndex` to `Game`
- [ ] Add `playersActedThisRound` set to `Game`
- [ ] Create `TurnManager.java` domain service
- [ ] Implement `isPlayerTurn()` method
- [ ] Implement `advanceTurn()` method
- [ ] Skip folded players automatically
- [ ] Modify `PlayerActionUseCase` to validate turn
- [ ] Add unit test: `testOnlyCurrentPlayerCanAct()`
- [ ] Add unit test: `testTurnAdvancesAfterAction()`
- [ ] Add unit test: `testFoldedPlayersSkipped()`
- [ ] Add integration test: `testTurnOrderEnforcement()`

**Files to Create:**
- `TurnManager.java`

**Files to Modify:**
- `Game.java` - Add turn tracking
- `PlayerActionUseCase.java` - Validate turn
- `GameTest.java` - Add tests

### Phase 1 Testing
- [ ] Run all existing unit tests
- [ ] Run all integration tests
- [ ] Manual test: 2-player game with betting
- [ ] Manual test: 3-player game with turn order
- [ ] Verify no duplicate cards in 100 games
- [ ] Verify betting must complete before dealing
- [ ] Verify turn order is enforced

---

## 🟡 Phase 2: Core Features (Week 2)

### Issue #4: Lobby Admin
- [ ] Add `adminPlayerId` field to `Lobby`
- [ ] Update `Lobby` constructor
- [ ] Add `isAdmin()` method
- [ ] Modify `start()` to check admin
- [ ] Update `CreateLobbyUseCase` - add admin parameter
- [ ] Update `JoinLobbyUseCase` - auto-join admin
- [ ] Update database schema - add admin column
- [ ] Create migration script
- [ ] Update `SQLiteLobbyRepository` - save/load admin
- [ ] Update `ProtocolHandler` - update CREATE_LOBBY command
- [ ] Update `MessageFormatter` - include admin in response
- [ ] Add unit test: `testOnlyAdminCanStartGame()`
- [ ] Add unit test: `testAdminAutoJoinsOnCreation()`
- [ ] Add integration test: `testLobbyAdminFlow()`

**SQL Migration:**
```sql
ALTER TABLE lobbies ADD COLUMN admin_player_id TEXT;
UPDATE lobbies SET admin_player_id = (
    SELECT player_id FROM lobby_players 
    WHERE lobby_id = lobbies.id LIMIT 1
);
```

**Files to Modify:**
- `Lobby.java`
- `CreateLobbyUseCase.java`
- `JoinLobbyUseCase.java`
- `SQLiteLobbyRepository.java`
- `ProtocolHandler.java`
- `MessageFormatter.java`
- `LobbyUseCaseTest.java`

### Issue #5: Player Card Visibility
- [ ] Create `GetPlayerCardsUseCase.java`
- [ ] Create `GetGameStateUseCase.java`
- [ ] Add GET_MY_CARDS handler to `ProtocolHandler`
- [ ] Add GET_GAME_STATE handler to `ProtocolHandler`
- [ ] Add `formatPlayerCards()` to `MessageFormatter`
- [ ] Add `formatGameState()` to `MessageFormatter`
- [ ] Ensure only requesting player sees their cards
- [ ] Add unit test: `testGetPlayerCards()`
- [ ] Add unit test: `testGetGameState()`
- [ ] Manual test: View cards during game

**Files to Create:**
- `GetPlayerCardsUseCase.java`
- `GetGameStateUseCase.java`

**Files to Modify:**
- `ProtocolHandler.java`
- `MessageFormatter.java`

### Phase 2 Testing
- [ ] Test admin-only game start
- [ ] Test non-admin cannot start game
- [ ] Test viewing own cards
- [ ] Test viewing game state
- [ ] Test admin shown in lobby info
- [ ] Run all tests from Phase 1 again

---

## 🟢 Phase 3: Nice-to-Have (Week 3)

### Issue #6: Game End Flow
- [ ] Add READY_NEXT_HAND command
- [ ] Add LEAVE_GAME command
- [ ] Track ready players after game ends
- [ ] Start new hand when all ready
- [ ] Return to lobby on leave
- [ ] Add unit test: `testReadyForNextHand()`
- [ ] Add integration test: `testMultiHandGame()`

**Files to Modify:**
- `Game.java` - Add ready tracking
- `ProtocolHandler.java` - Add commands
- `MessageFormatter.java` - Add formatters

### Issue #7: Dealer Button System
- [ ] Add `determineInitialDealer()` to `Game`
- [ ] Implement high-card dealer selection
- [ ] Add `advanceDealer()` method (already exists?)
- [ ] Display dealer position to players
- [ ] Add unit test: `testDealerRotation()`
- [ ] Add unit test: `testInitialDealerSelection()`

**Files to Modify:**
- `Game.java`
- `GameTest.java`

### Issue #8: Real-Time Notifications
- [ ] Create `GameEvent` interface
- [ ] Create `GameEventPublisher` service
- [ ] Create `GameEventListener` interface
- [ ] Create `ConnectedClientsRegistry`
- [ ] Implement event types:
  - [ ] PlayerActed
  - [ ] CardsDealt
  - [ ] PotUpdated
  - [ ] TurnChanged
  - [ ] PhaseChanged
  - [ ] WinnerDetermined
- [ ] Modify `ClientHandler` to register as listener
- [ ] Broadcast events to all game participants
- [ ] Add integration test: `testEventBroadcasting()`

**Files to Create:**
- `GameEvent.java`
- `GameEventPublisher.java`
- `GameEventListener.java`
- `ConnectedClientsRegistry.java`
- Event types (PlayerActedEvent, etc.)

**Files to Modify:**
- `Game.java` - Publish events
- `ClientHandler.java` - Listen for events
- `SocketServer.java` - Manage registry
- `ProtocolHandler.java` - Publish on commands

### Phase 3 Testing
- [ ] Test dealer rotation over 10 hands
- [ ] Test initial dealer selection
- [ ] Test ready/leave after game
- [ ] Test event notifications to all players
- [ ] Test player sees all actions
- [ ] Performance test: 10 concurrent games

---

## 📋 Final Checklist

### Code Quality
- [ ] All new code has unit tests
- [ ] All new code has JavaDoc comments
- [ ] No code duplication
- [ ] Follow existing code style
- [ ] No compiler warnings
- [ ] All edge cases handled

### Testing
- [ ] All unit tests pass (100%)
- [ ] All integration tests pass (100%)
- [ ] Manual end-to-end test passes
- [ ] Performance test passes (<100ms response)
- [ ] Load test: 10 concurrent games
- [ ] Test with 2, 3, 4, 6, 9 players

### Documentation
- [ ] Update README.md with new commands
- [ ] Update ARCHITECTURE.md with new components
- [ ] Create GAME_RULES.md
- [ ] Update API documentation
- [ ] Add troubleshooting guide
- [ ] Document known limitations

### Database
- [ ] Run migration scripts
- [ ] Backup database before migration
- [ ] Test migration rollback
- [ ] Verify data integrity after migration

### Deployment
- [ ] Build succeeds without errors
- [ ] Package creates correct JAR
- [ ] Docker image builds successfully
- [ ] Server starts without errors
- [ ] Can connect via telnet
- [ ] Logs show expected messages

---

## 📊 Progress Tracking

### Overall Progress

```
Phase 1 (Critical):  [░░░░░░░░░░] 0/10 tasks (0%)
Phase 2 (Core):      [░░░░░░░░░░] 0/10 tasks (0%)
Phase 3 (Polish):    [░░░░░░░░░░] 0/10 tasks (0%)
Final:               [░░░░░░░░░░] 0/10 tasks (0%)
```

Update this as you complete tasks:
- [█████░░░░░] 50%
- [██████████] 100%

### Time Tracking

| Phase | Estimated | Actual | Notes |
|-------|-----------|--------|-------|
| Phase 1 | 12h | - | |
| Phase 2 | 7h | - | |
| Phase 3 | 19h | - | |
| Testing | 16h | - | |
| Docs | 8h | - | |
| **Total** | **62h** | **-** | |

---

## 🎯 Quick Start

**Day 1:**
- [ ] Fix duplicate cards (2h)
- [ ] Add betting validation (2h)
- [ ] Add basic turn management (2h)

**Day 2:**
- [ ] Complete turn management (2h)
- [ ] Write tests for Phase 1 (2h)
- [ ] Manual testing (2h)

**Day 3:**
- [ ] Add lobby admin (2h)
- [ ] Add card visibility (2h)
- [ ] Write tests for Phase 2 (2h)

**Days 4-5:**
- [ ] Nice-to-have features
- [ ] Final testing
- [ ] Documentation

---

## 🚨 Red Flags to Watch For

- [ ] Tests start failing after changes
- [ ] Performance degrades (>100ms response time)
- [ ] Duplicate cards still appearing
- [ ] Betting can be bypassed
- [ ] Players acting out of turn
- [ ] Database corruption
- [ ] Memory leaks in long-running server
- [ ] Concurrent modification exceptions

---

## ✅ Definition of Done

A task is complete when:
1. ✅ Code is written and compiles
2. ✅ Unit tests written and passing
3. ✅ Integration test passes
4. ✅ Manual test passes
5. ✅ Code reviewed (self or peer)
6. ✅ Documentation updated
7. ✅ Committed to version control

---

## 📞 Getting Help

**Stuck on something?**
1. Check relevant `.md` file:
   - Technical details → `IMPROVEMENTS.md`
   - Quick fixes → `QUICK_FIXES.md`
   - Planning → `ACTION_PLAN.md`
   - Visual aids → `FLOW_DIAGRAMS.md`
2. Review existing tests for examples
3. Check `ARCHITECTURE.md` for design patterns
4. Look at similar existing code

**Found a bug?**
1. Write a failing test
2. Fix the bug
3. Verify test passes
4. Check if other tests broke

**Need to add a feature?**
1. Write test first (TDD)
2. Implement feature
3. Make test pass
4. Refactor if needed

---

*Last Updated: Nov 24, 2025*  
*Start Date: [Fill in when starting]*  
*Target Completion: [Fill in target date]*
