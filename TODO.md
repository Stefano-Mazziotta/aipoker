✅ ~~1. update players list when new user join to the lobby~~ (COMPLETED)
✅ ~~2. start game without popup because the list of players exist in the context. Don't require a list of players ids from input user.~~ (COMPLETED)
✅ ~~3. disabled ui buttons based on the state of the game.~~ (COMPLETED)
4. when game is started the users must see others players in the table.
5. The table must be the center of ui.
6. first screen is onboarding and join a lobby
7. when game is started render the table with others players
8. show game flow and real time update of the table state.
4. test game flow




# 📋 TODO - Feature Roadmap

> Planned features and improvements for the Texas Hold'em Poker Server

---

## 🎯 Priority Levels

- 🔴 **Critical** - Core functionality, security, or blocking issues
- 🟡 **High** - Important features that enhance user experience
- 🟢 **Medium** - Nice-to-have improvements
- 🔵 **Low** - Future enhancements, optimizations

---

## 🔴 Critical Priority

### Security & Authentication

- [ ] **JWT Authentication for WebSocket**
  - Add token-based authentication
  - Validate JWT on WebSocket connection
  - Refresh token mechanism
  - Status: Not started
  
- [ ] **Rate Limiting**
  - Prevent command spam
  - Limit connections per IP
  - Protect against DoS attacks
  - Status: Not started

- [ ] **Input Validation & Sanitization**
  - Validate all command parameters
  - Prevent injection attacks
  - Sanitize player names and messages
  - Status: Partial (basic validation exists)

### Core Game Features

- [ ] **Side Pot Management**
  - Handle all-in scenarios with multiple side pots
  - Proper pot distribution to eligible players
  - Status: Not implemented

- [ ] **Betting Round Completion Logic**
  - Detect when all active players have acted
  - Handle when everyone checks
  - Auto-advance to next phase
  - Status: Partial (basic logic exists)

---

## 🟡 High Priority

### Multiplayer Experience

- [ ] **Game Reconnection**
  - Allow players to reconnect after disconnect
  - Restore game state for reconnected player
  - Timeout inactive players
  - Status: Not started

- [ ] **Spectator Mode**
  - Allow observers to watch games
  - Read-only WebSocket subscription
  - Don't reveal hidden cards to spectators
  - Status: Not started

- [ ] **Chat System**
  - In-game chat messages
  - Lobby chat
  - Chat history
  - Profanity filter
  - Status: Not started

### Lobby & Matchmaking

- [ ] **Advanced Lobby Features**
  - Password-protected lobbies
  - Private vs public games
  - Min/max player limits
  - Configurable blind levels
  - Status: Basic lobby exists, needs enhancement

- [ ] **Matchmaking System**
  - Quick match (auto-join available game)
  - Skill-based matchmaking
  - Rating/ELO system
  - Status: Not started

### Player Management

- [ ] **Player Profiles**
  - Avatar/profile picture
  - Statistics (hands played, win rate, etc.)
  - Achievement system
  - Badge collection
  - Status: Basic player model exists

- [ ] **Friends System**
  - Add/remove friends
  - Invite friends to games
  - Private lobbies for friends
  - Status: Not started

---

## 🟢 Medium Priority

### Game Variants

- [ ] **Tournament Mode**
  - Multi-table tournaments
  - Elimination brackets
  - Prize pool distribution
  - Blind level increases
  - Status: Not started

- [ ] **Sit-and-Go**
  - Fixed number of players
  - Start when full
  - Winner-takes-all or tiered prizes
  - Status: Not started

- [ ] **Cash Game Mode**
  - Buy-in and cash-out
  - Join/leave anytime
  - Persistent chip stacks
  - Status: Current implementation is close to this

### UI/UX Improvements

- [ ] **Rich Web Client**
  - Replace `websocket-client.html` with full-featured UI
  - Card animations
  - Chip stack visualization
  - Player action indicators
  - Timer countdown
  - Status: Basic test client exists

- [ ] **Mobile Client**
  - Native iOS app
  - Native Android app
  - Or responsive web design
  - Status: Not started

### Analytics & Monitoring

- [ ] **Game Statistics Dashboard**
  - Active games count
  - Player count
  - Average game duration
  - Popular game types
  - Status: Not started

- [ ] **Player Analytics**
  - Hands played
  - Win/loss statistics
  - Most common actions
  - Profitable hands
  - Status: Not started

- [ ] **Logging & Monitoring**
  - Structured logging (JSON logs)
  - Metrics collection (Prometheus)
  - APM integration (e.g., New Relic, DataDog)
  - Error tracking (Sentry)
  - Status: Basic logging exists

---

## 🔵 Low Priority

### Performance Optimizations

- [ ] **Caching Layer**
  - Redis for session management
  - Cache frequent database queries
  - Leaderboard caching
  - Status: Not started

- [ ] **Database Optimization**
  - Migrate to PostgreSQL for production
  - Add database indexes
  - Connection pooling
  - Query optimization
  - Status: SQLite currently used

- [ ] **Horizontal Scaling**
  - Redis Pub/Sub for multi-instance event broadcasting
  - Load balancer support
  - Stateless application design
  - Status: Single instance currently

### Advanced Features

- [ ] **Hand History**
  - Record and replay hands
  - Export hand history
  - Share hands with others
  - Status: Not started

- [ ] **AI Opponents**
  - Computer-controlled players
  - Multiple difficulty levels
  - Practice mode against AI
  - Status: Not started

- [ ] **Video Streaming**
  - Stream games to Twitch/YouTube
  - Delay for hidden information
  - Commentator mode
  - Status: Not started

### DevOps & Infrastructure

- [ ] **CI/CD Pipeline**
  - GitHub Actions for automated testing
  - Automated deployment
  - Docker container publishing
  - Status: Not started

- [ ] **Docker Compose Production Setup**
  - Multi-container setup (app + PostgreSQL + Redis)
  - Volume management
  - Environment configuration
  - Status: Basic Dockerfile exists

- [ ] **Kubernetes Deployment**
  - K8s manifests
  - Auto-scaling
  - Rolling updates
  - Status: Not started

---

## 🛠️ Technical Debt

### Code Quality

- [ ] **Increase Test Coverage**
  - Target: 90%+ coverage
  - Add more edge case tests
  - Integration tests for all use cases
  - Current: ~85% coverage

- [ ] **Add End-to-End Tests**
  - WebSocket client simulation
  - Full game flow testing
  - Multiple concurrent games
  - Status: Manual testing only

- [ ] **Refactor Hand Evaluator**
  - Current algorithm works but could be more elegant
  - Consider lookup tables for performance
  - Status: Works, optimization possible

### Documentation

- [ ] **API Documentation**
  - OpenAPI/Swagger for REST endpoints (if added)
  - WebSocket protocol specification
  - Message format examples
  - Status: Basic docs in README

- [ ] **Developer Guide**
  - Contributing guidelines
  - Code style guide
  - Architecture decision records (ADRs)
  - Status: Not started

- [ ] **Deployment Guide**
  - Production deployment steps
  - Environment configuration
  - Scaling guidelines
  - Monitoring setup
  - Status: Basic setup instructions exist

---

## 📊 Metrics & Success Criteria

### Performance
- [ ] Response time < 100ms for 95% of requests
- [ ] Support 1000+ concurrent WebSocket connections
- [ ] Event broadcast latency < 50ms

### Reliability
- [ ] 99.9% uptime
- [ ] Graceful degradation on failures
- [ ] Zero data loss on crashes

### User Experience
- [ ] < 2 second game join time
- [ ] Real-time updates with no perceivable lag
- [ ] Mobile-friendly UI

---

## 🚀 Recently Completed

### ✅ Phase 1: Foundation (Completed)
- ✅ Hexagonal Architecture setup
- ✅ Domain-Driven Design implementation
- ✅ Complete Texas Hold'em rules
- ✅ Hand evaluation algorithm
- ✅ SQLite persistence
- ✅ Comprehensive test suite (56 tests)

### ✅ Phase 2: Real-Time Communication (Completed)
- ✅ WebSocket server (Jakarta + Tyrus)
- ✅ Event-Driven Architecture
- ✅ GameEventPublisher with pub/sub
- ✅ Real-time event broadcasting
- ✅ WebSocket test client
- ✅ JSON message protocol

---

## 📅 Roadmap

### Q1 2026: Multiplayer Stability
- Security (JWT, rate limiting)
- Side pot management
- Reconnection support
- Advanced lobby features

### Q2 2026: Enhanced Experience
- Rich web client
- Chat system
- Spectator mode
- Player profiles

### Q3 2026: Competitive Play
- Tournament mode
- Matchmaking system
- Friends system
- Leaderboard enhancements

### Q4 2026: Scale & Polish
- Performance optimizations
- Horizontal scaling
- Mobile clients
- Advanced analytics

---

## 🤝 Contributing

Want to work on a feature? Here's how:

1. **Pick a task** from this TODO list
2. **Create an issue** on GitHub describing your approach
3. **Fork the repository** and create a feature branch
4. **Implement the feature** following architecture guidelines
5. **Add tests** (aim for 80%+ coverage)
6. **Submit a Pull Request** with clear description

### Good First Issues
- 🟢 Add input validation for all commands
- 🟢 Implement chat message filtering
- 🟢 Add more unit tests for edge cases
- 🟢 Improve error messages
- 🟢 Add player statistics to leaderboard

---

## 📝 Notes

- This is a living document - priorities may change
- Features marked "Not started" are open for contribution
- Security features should be implemented before public deployment
- Performance targets are goals, not current state

---

**Last Updated**: November 24, 2025
