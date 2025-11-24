# Action Plan: Poker Game Flow Improvements

## Executive Summary

Based on testing, the poker game has several critical issues that prevent proper gameplay:
1. **Duplicate cards bug** - Same cards appearing multiple times
2. **No betting round enforcement** - Can skip directly to river
3. **No lobby admin** - Anyone can control the game
4. **No player visibility** - Can't see your own cards or game state
5. **No turn management** - Players can act out of order
6. **Manual phase progression** - Should be automatic after betting

## Critical Path (Must Fix First)

### Issue #1: Duplicate Cards Bug 🔴 CRITICAL
**Impact:** Game integrity compromised  
**Root Cause:** Likely deck state persistence issue  
**Time:** 2 hours  

**Fix:**
- Ensure deck is always fresh on game start
- Don't persist/restore deck state from database
- Let deck regenerate on game load

**Files to Modify:**
- `Game.java` - `reconstitute()` method
- `SQLiteGameRepository.java` - Don't save/load deck

### Issue #2: Betting Round Enforcement 🔴 CRITICAL
**Impact:** Can bypass betting, unfair gameplay  
**Time:** 4 hours  

**Fix:**
- Add `bettingComplete` flag to Game state
- Validate betting before allowing DEAL_FLOP/TURN/RIVER
- Track player actions per betting round

**Files to Modify:**
- `Game.java` - Add betting validation
- `DealCardsUseCase.java` - Add checks
- `BettingRound.java` - Track completion

### Issue #3: Player Turn Management 🟡 HIGH PRIORITY
**Impact:** Chaos in multiplayer games  
**Time:** 6 hours  

**Fix:**
- Add `currentPlayerToAct` to Game
- Validate player can act on their turn
- Auto-advance turn after each action

**Files to Modify:**
- `Game.java` - Add turn tracking
- `PlayerActionUseCase.java` - Validate turn
- New: `TurnManager.java` domain service

## Medium Priority (Core Gameplay)

### Issue #4: Lobby Admin Control 🟡
**Impact:** No game organization  
**Time:** 4 hours  

**Fix:**
- Add `adminPlayerId` to Lobby
- Only admin can start game
- Admin auto-joins on creation

**Files to Modify:**
- `Lobby.java` - Add admin field
- `CreateLobbyUseCase.java` - Set admin
- `JoinLobbyUseCase.java` - Auto-add admin
- `SQLiteLobbyRepository.java` - Persist admin
- `ProtocolHandler.java` - Update commands

### Issue #5: Player Card Visibility 🟡
**Impact:** Players can't see their cards  
**Time:** 3 hours  

**Fix:**
- Add GET_MY_CARDS command
- Add GET_GAME_STATE command
- Show relevant info only to requesting player

**Files to Create:**
- `GetPlayerCardsUseCase.java`
- `GetGameStateUseCase.java`

**Files to Modify:**
- `ProtocolHandler.java` - Add handlers
- `MessageFormatter.java` - Add formatters

## Lower Priority (UX Improvements)

### Issue #6: Game End Flow 🟢
**Impact:** Awkward post-game experience  
**Time:** 4 hours  

**Fix:**
- Return to lobby after game
- Add READY_FOR_NEXT_HAND command
- Add LEAVE_GAME command

### Issue #7: Dealer Button Mini-Game 🟢
**Impact:** Missing traditional poker element  
**Time:** 3 hours  

**Fix:**
- High card draw before first hand
- Rotate dealer each hand
- Display dealer position

### Issue #8: Real-Time Notifications 🟢
**Impact:** Players don't see game updates  
**Time:** 12 hours (significant)  

**Fix:**
- Implement event broadcasting
- Maintain connected clients registry
- Push notifications to all players in game

## Implementation Schedule

### Week 1: Critical Fixes
**Mon-Tue:** Duplicate cards + Betting enforcement  
**Wed-Thu:** Turn management  
**Fri:** Testing & bug fixes  

### Week 2: Core Features
**Mon:** Lobby admin  
**Tue:** Player card visibility  
**Wed-Thu:** Game end flow  
**Fri:** Testing & integration  

### Week 3: Polish
**Mon-Tue:** Dealer button system  
**Wed-Fri:** Real-time notifications  

### Week 4: Testing & Documentation
**Mon-Tue:** End-to-end testing  
**Wed:** Performance testing  
**Thu:** Documentation  
**Fri:** Deployment preparation  

## Testing Strategy

### Unit Tests to Add
```java
// GameTest.java
@Test void testBettingMustCompleteBeforeDealingFlop()
@Test void testOnlyCurrentPlayerCanAct()
@Test void testTurnAdvancesAfterAction()
@Test void testDealerRotatesEachHand()

// LobbyTest.java
@Test void testOnlyAdminCanStartGame()
@Test void testAdminAutoJoinsOnCreation()

// DeckTest.java
@Test void testNoDuplicateCardsDealt()
@Test void testDeckHas52UniqueCards()
```

### Integration Tests to Add
```java
// FullGameFlowTest.java
@Test void testCompleteTwoPlayerGame()
@Test void testBettingRoundProgression()
@Test void testMultiHandGameWithDealerRotation()

// LobbyFlowTest.java  
@Test void testCreateJoinStartLeaveFlow()
@Test void testOnlyAdminCanStartGame()
```

### Manual Testing Scenarios

#### Scenario 1: Basic Two-Player Game
```bash
# Terminal 1 (Alice - Admin)
REGISTER alice 1000
CREATE_LOBBY test-game 2 <alice-id>

# Terminal 2 (Bob)
REGISTER bob 1000  
JOIN_LOBBY <lobby-id> <bob-id>

# Terminal 1
START_GAME <alice-id> <bob-id> 10 20
GET_MY_CARDS <game-id> <alice-id>
CALL <game-id> <alice-id> 20

# Terminal 2
GET_MY_CARDS <game-id> <bob-id>
CHECK <game-id> <bob-id>

# Terminal 1 (should work now)
DEAL_FLOP <game-id>

# Continue through game...
```

#### Scenario 2: Turn Enforcement
```bash
# Player 1's turn
FOLD <game-id> <player1-id>   # ✓ Should work

# Try to act out of turn
FOLD <game-id> <player3-id>   # ✗ Should fail: "Not your turn"

# Player 2's turn  
CHECK <game-id> <player2-id>  # ✓ Should work
```

#### Scenario 3: Betting Round Enforcement
```bash
START_GAME <p1> <p2> 10 20

# Try to skip betting
DEAL_FLOP <game-id>           # ✗ Should fail: "Betting incomplete"

# Complete betting
CALL <game-id> <p1> 20
CHECK <game-id> <p2>

# Now should work
DEAL_FLOP <game-id>           # ✓ Should work
```

## Risk Assessment

| Risk | Probability | Impact | Mitigation |
|------|------------|--------|------------|
| Breaking existing tests | High | Medium | Run tests after each change |
| Database migration issues | Medium | High | Test migrations thoroughly |
| Performance degradation | Low | Medium | Profile after notification system |
| State synchronization bugs | High | High | Add comprehensive state tests |

## Success Metrics

- [ ] All existing tests pass
- [ ] No duplicate cards in 1000 game simulation
- [ ] Betting rounds enforce correctly 100% of time
- [ ] Turn management prevents out-of-order actions
- [ ] Admin controls work as expected
- [ ] Players can view their cards
- [ ] Game completes from start to finish without manual intervention
- [ ] Performance: <100ms response time for commands
- [ ] Stability: 24hr server uptime without crashes

## Rollback Plan

If critical issues arise:
1. Keep current version as `v1.0-stable` branch
2. Make changes in `feature/game-flow-improvements` branch
3. If issues found, revert to stable
4. Fix issues in feature branch
5. Re-test before merging

## Documentation Updates Needed

- [ ] Update README.md with new commands
- [ ] Update ARCHITECTURE.md with new components
- [ ] Add GAME_RULES.md explaining betting rounds
- [ ] Update API documentation
- [ ] Add troubleshooting guide
- [ ] Create video demo of complete game

## Team Coordination

**Developer 1:** Core game logic (Issues #1, #2, #3)  
**Developer 2:** Lobby & UX (Issues #4, #5, #6)  
**Developer 3:** Advanced features (Issues #7, #8)  

## Questions to Answer

1. Should we support reconnection if a player disconnects?
2. Do we want spectator mode?
3. Should we add chat functionality?
4. Do we need tournament support?
5. Should we add AI players for testing?
6. Do we want hand history/replay?
7. Should we support side pots for all-in scenarios?
8. Do we need player statistics tracking?

## Resources Needed

- [ ] Additional testing devices/terminals
- [ ] Performance monitoring tools
- [ ] Database backup before migrations
- [ ] Staging environment for testing
- [ ] Code review from senior developer

## Next Immediate Actions (Today)

1. ✅ Create improvement documentation (DONE)
2. ⬜ Fix duplicate cards bug
3. ⬜ Add unit test for deck uniqueness
4. ⬜ Add betting round validation
5. ⬜ Test with 2 players through complete game

## Review Checkpoints

- **End of Week 1:** Critical fixes complete, game playable
- **End of Week 2:** Core features added, good UX
- **End of Week 3:** Polish features complete
- **End of Week 4:** Production ready

---

**Created:** Nov 24, 2025  
**Last Updated:** Nov 24, 2025  
**Status:** Planning  
**Next Review:** After critical fixes implemented
