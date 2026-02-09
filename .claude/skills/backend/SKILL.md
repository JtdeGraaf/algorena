Helps with Spring Boot backend development following Algorena's domain-driven architecture.

**Usage:** `/spring-boot <task description>`

**Examples:**
- `/spring-boot add tournaments domain`
- `/spring-boot add endpoint to get bot by game type`
- `/spring-boot add validation to match creation`

**What this skill does:**

## Project Understanding
- Follows the domain-driven design pattern: `domain/` → `controllers/` → `application/` → `data/`
- Uses the established package structure: `com.algorena.{domain}.{layer}`
- Understands the three main domains: `bots/`, `games/`, `users/`
- Follows shared patterns from `common/` (exceptions, base entities, config)

## Encapsulation Philosophy

**CRITICAL: Never use `@Setter` on domain entities or value objects.**

Entities should protect their invariants through explicit business methods:

```java
// WRONG - exposes internal state
@Setter
private boolean active;

// RIGHT - encapsulated business operation
public void activate() {
    this.active = true;
}

public void deactivate() {
    this.active = false;
}
```

Business methods should:
- Have meaningful names that describe the business operation (`markAsDeleted()` not `setDeleted(true)`)
- Validate inputs and enforce invariants
- Throw `IllegalArgumentException` for invalid operations
- Combine related state changes (e.g., `markAsDeleted()` deactivates AND clears sensitive data)

The only exception is `BaseEntity`, which uses `@Setter` because Spring Data JPA auditing requires it for timestamp fields.

## Code Generation Patterns

**Entities:**
- Extends `BaseEntity` for automatic `created` and `lastUpdated` timestamps
- Uses Lombok: `@Getter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`
- **NEVER use `@Setter`** - entities are encapsulated; state changes happen through explicit business methods (e.g., `activate()`, `updateDetails()`, `markAsDeleted()`)
- Uses `@Entity` and `@Table` with explicit table names
- Uses JSpecify `@Nullable` for nullable fields
- Implements business logic methods (activate/deactivate, update operations)
- Uses NullAway null-safety annotations
- For DDD aggregates, restrict constructor access: `@NoArgsConstructor(access = AccessLevel.PROTECTED)`, `@AllArgsConstructor(access = AccessLevel.PRIVATE)`, `@Builder(access = AccessLevel.PACKAGE)`
- Use static factory methods for complex creation (e.g., `User.createFromOAuth2(userInfo, username)`)

**BaseEntity:**
- Located at `com.algorena.common.domain.BaseEntity`
- Provides `created` and `lastUpdated` fields (Java `LocalDateTime`)
- Database columns are named `created` and `last_updated` (snake_case) - **never** use `created_at`, `created_timestamp`, or other variations
- Uses Spring Data JPA auditing: `@CreatedDate`, `@LastModifiedDate`, `@EntityListeners(AuditingEntityListener.class)`
- Fields are auto-populated by JPA - no manual setting needed

**Value Objects:**
- Use `@Embeddable` for value objects embedded in entities
- Mark with `@Immutable` (Hibernate annotation) for true immutability
- Use `@Getter` only, **never `@Setter`**
- Use `@NoArgsConstructor(access = AccessLevel.PROTECTED)` for JPA, `@AllArgsConstructor` for construction
- Suppress NullAway warnings with `@SuppressWarnings(NULL_AWAY_INIT)` (constant from `com.algorena.common.config.SuppressedWarnings`)
- Example: `OAuthIdentity` embeds provider + providerId in User entity

**DTOs:**
- Uses Java records for immutability
- Response DTOs contain all public fields (id, `created`, `lastUpdated`, etc.)
- Request DTOs use Jakarta validation: `@NotBlank`, `@NotNull`, `@Size`
- Uses `@Nullable` for optional fields
- Validation messages should be user-friendly: `@NotBlank(message = "Bot name is required")`
- For sensitive data, create separate DTO variants: `toPrivateDTO()` includes sensitive fields (API keys), `toPublicDTO()` excludes them

**Repositories:**
- Extends `JpaRepository<Entity, Long>` - always use `Long` for ID type, never `UUID`
- Uses method naming conventions: `findByField`, `findByIdAndUserId`
- Uses `@Query` with JPQL for complex queries with `@Nullable` parameters
- Parameters do NOT need `@Param` annotation (Spring Data JPA infers them)
- Annotated with `@Repository`

**Services:**
- Interface + Implementation pattern (`FooService` + `FooServiceImpl`)
- Implementation uses `@Service`, `@AllArgsConstructor`
- Uses `@Transactional` (defaults to read-write, `readOnly = true` for queries)
- Injects `CurrentUser` for user context via `currentUser.id()`
- Throws `DataNotFoundException` when entities not found
- Implements private `toDTO` methods for entity → DTO conversion
- Use business methods on entities, not direct field manipulation: `bot.updateDetails(name, description)` not `bot.setName(name)`
- For public/private views, implement separate toDTO methods: `toPrivateDTO()` and `toPublicDTO()`
- Service interfaces are minimal - just method signatures, no JavaDoc needed for obvious CRUD methods

**Controllers:**
- Uses `@RestController`, `@RequestMapping("/api/v1/{resource}")`
- Uses `@PreAuthorize("hasRole('USER')")` at class level for authentication
- Uses `@AllArgsConstructor` for dependency injection
- Uses OpenAPI annotations: `@Tag`, `@Operation`, `@Parameter`
- Follows REST conventions: POST (201), GET (200), PATCH (200), DELETE (204)
- Use `@PatchMapping` for partial updates (not PUT) - aligns with how entities use business methods for targeted updates
- Uses `@Valid` for request body validation
- Returns `ResponseEntity<T>` for type safety
- Uses `@PathVariable` for IDs, `@RequestParam` for filters
- Uses `@PageableDefault(size = 20, sort = "created")` and `@ParameterObject` for pagination

**Testing:**
- Unit tests: `*Test.java` - Pure logic tests with AssertJ
- Integration tests: `*IntegrationTest.java` - Extends `AbstractIntegrationTest`
- Uses AssertJ for assertions: `assertThat()`, `assertThatThrownBy()`
- Follows Given/When/Then structure with comments
- Integration tests use real database via Testcontainers

**Common Patterns:**
- Use `CurrentUser` service for getting authenticated user ID: `currentUser.id()`
- Security: Verify user owns resource via `findByIdAndUserId`
- Error handling: Throw domain exceptions from `com.algorena.common.exception` package (singular, not "exceptions"):
  - `DataNotFoundException` - 404 when entity not found
  - `BadRequestException` - 400 for invalid input
  - `ConflictException` - 409 for conflicts
  - `ForbiddenException` - 403 for access denied
  - `UnauthorizedException` - 401 for authentication failures
  - `InternalServerException` - 500 for unexpected errors
  - `BotCommunicationException` - for bot endpoint failures
- Validation: Jakarta Bean Validation in DTOs, business logic in entities (entities throw `IllegalArgumentException`)
- API keys: Use `ApiKeyConverter` for encryption (see `bots` domain)
- NullAway: Use `@SuppressWarnings(NULL_AWAY_INIT)` with constant from `com.algorena.common.config.SuppressedWarnings` when fields are initialized by JPA/framework

**Database Migrations:**
- Located at `backend/src/main/resources/db/migration/`
- Naming: `V{major}_{minor}_{patch}__{description}.sql` (e.g., `V1_0_7__add_bot_soft_delete.sql`)
- Column naming conventions (snake_case):
  - Timestamps: `created`, `last_updated` - **never** `created_at`, `created_timestamp`, `updated_at`
  - Foreign keys: `user_id`, `bot_id`, `match_id`
  - Booleans: `active`, `deleted` - **never** `is_active`, `is_deleted`
- All tables with entities extending `BaseEntity` must have `created TIMESTAMP NOT NULL` and `last_updated TIMESTAMP NOT NULL`
- **ID Convention**: Use `BIGSERIAL` (Long) for all primary keys - **do not use UUID**
  - In Java entities: `@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;`
  - In SQL: `id BIGSERIAL PRIMARY KEY`
  - For child tables (joined inheritance): `id BIGINT PRIMARY KEY REFERENCES parent_table(id)`
- Always include proper foreign key constraints with `ON DELETE CASCADE` where appropriate

**Documentation:**
- Large methods or methods that do complex operations MUST have JavaDoc comments
- Methods that are not self-explanatory MUST have JavaDoc explaining their purpose
- Small, self-explanatory methods (simple getters, setters, CRUD operations) do NOT need JavaDoc but can have them
- When in doubt, add documentation - it's better to over-document than under-document
- JavaDoc should explain the "why" and "what", not just repeat the method name
- Include `@param`, `@return`, and `@throws` tags for non-trivial methods

## Commands
The skill knows these backend commands (from `backend/` directory):
- `./mvnw spring-boot:run` - Start dev server (port 8080)
- `./mvnw test` - Run unit tests only
- `./mvnw verify` - Run unit + integration tests
- `./mvnw test -Dtest=ClassName` - Run single test class
- `./mvnw test -Dtest=ClassName#method` - Run single test method

## File Structure Template
When creating a new domain, follow this structure:
```
backend/src/main/java/com/algorena/{domain}/
├── controllers/          # REST endpoints
│   └── {Domain}Controller.java
├── application/          # Business logic
│   ├── {Domain}Service.java
│   └── {Domain}ServiceImpl.java
├── domain/              # Entities & value objects
│   └── {Domain}.java
├── data/                # Repositories
│   └── {Domain}Repository.java
└── dto/                 # Data transfer objects
    ├── {Domain}DTO.java
    ├── Create{Domain}Request.java
    └── Update{Domain}Request.java
```

## What to Ask
Before generating code, the skill should:
1. Clarify which domain the feature belongs to (create new domain vs extend existing)
2. Identify required fields and validation rules
3. Determine security requirements (user-owned resources vs public)
4. Ask about pagination needs for list endpoints
5. Confirm if tests are needed (default: yes for new features)

## Adding a New Game

To add a new game type (e.g., Tic-Tac-Toe, Checkers), follow this comprehensive checklist:

### 1. Update Game Enum
Add the new game to `backend/src/main/java/com/algorena/bots/domain/Game.java`:
```java
public enum Game {
    CHESS,
    CONNECT_FOUR,
    TIC_TAC_TOE  // Add new game
}
```

### 2. Backend Game Package Structure
Create `backend/src/main/java/com/algorena/games/{newgame}/` with subdirectories:
```
{newgame}/
├── domain/              # Game-specific entities
│   ├── {Game}GameState.java      # Extends AbstractGameState
│   └── {Game}MatchMove.java      # Optional: game-specific move data
├── engine/              # Game logic
│   └── {Game}GameEngine.java     # Implements GameEngine<S, M>
├── application/         # Match execution
│   └── {Game}MatchExecutor.java  # Implements GameMatchExecutor
└── data/               # Repositories
    ├── {Game}GameStateRepository.java
    └── {Game}MatchMoveRepository.java  # Optional
```

### 3. Implement Game Engine
Create `{Game}GameEngine.java` implementing `GameEngine<S, M>`:
- `S` = Game state type (e.g., `ChessGameState`)
- `M` = Move type (e.g., `String` for chess notation, `Integer` for Connect4 column)
- Implement three methods:
  - `startNewGame()` - Initialize game state
  - `applyMove(S state, M move, int playerIndex)` - Validate and apply move
  - `checkResult(S state)` - Return `GameResult` if game over, null if ongoing
- Annotate with `@Component` for Spring auto-detection
- Use `GameResult.winner(winnerIndex, loserIndex)` or `GameResult.draw()`
- Validate player turns and throw `IllegalArgumentException` for illegal moves

### 4. Implement Match Executor
Create `{Game}MatchExecutor.java` implementing `GameMatchExecutor`:
- Inject: `{Game}GameStateRepository`, `MatchMoveRepository`, `{Game}GameEngine`, `BotClientService`
- Implement `getGameType()` to return the correct `Game` enum value
- Implement `executeSingleMove(Match match)`:
  - Load current game state from repository
  - Check if game is already over via `gameEngine.checkResult(state)`
  - Determine current player via `getCurrentPlayerIndex(match)`
  - Call bot endpoint via `botClientService.requestMove(bot, request)`
  - Validate and apply move via `gameEngine.applyMove(state, move, playerIndex)`
  - Save move to database via `matchMoveRepository.save(...)`
  - Update and save game state
  - Return result if game ended
- Implement `getCurrentPlayerIndex(Match match)` to determine whose turn it is
- Annotate with `@Component` for auto-registration in `MatchExecutorService`
- Add JavaDoc explaining game-specific logic

### 5. Create Database Migrations
Create Flyway migration `backend/src/main/resources/db/migration/V{X}_{Y}__{description}.sql`:
- Add table for game state (e.g., `tic_tac_toe_game_states`)
  - Must reference `game_states(id)` as primary key
  - Add game-specific columns (board state, turn counter, etc.)
- Optionally add table for game-specific move data (e.g., `tic_tac_toe_match_moves`)
  - Must reference `match_moves(id)` as primary key
  - Add move-specific columns (coordinates, piece type, etc.)
- Add indexes for `match_id` foreign keys

### 6. Create Domain Entities

**Game State Entity** - Extends `AbstractGameState`:
```java
@Entity
@Table(name = "{game}_game_states")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class {Game}GameState extends AbstractGameState {
    // Add game-specific fields (board state, etc.)
    // Add business logic methods for state updates
}
```

**Match Move Entity** (optional) - Extends base move:
```java
@Entity
@Table(name = "{game}_match_moves")
public class {Game}MatchMove {
    @Id
    private Long id;  // References match_moves(id)
    // Add game-specific move fields
}
```

### 7. Create Repositories
```java
@Repository
public interface {Game}GameStateRepository extends JpaRepository<{Game}GameState, Long> {
    Optional<{Game}GameState> findByMatchId(Long matchId);
}
```

### 8. Write Tests
Create `{Game}GameEngineTest.java`:
- Test `startNewGame()` returns valid initial state
- Test `applyMove()` with valid moves
- Test `applyMove()` rejects illegal moves with `IllegalArgumentException`
- Test `applyMove()` rejects wrong player turn
- Test `checkResult()` detects wins, draws, and ongoing games
- Use AssertJ assertions: `assertThat()`, `assertThatThrownBy()`

Create `{Game}MatchExecutorIntegrationTest.java`:
- Extend `AbstractIntegrationTest`
- Test full match execution with mock bot endpoints
- Test error handling (timeouts, invalid moves)
- Verify moves and state are saved correctly

### 9. Frontend Integration
Based on the game registry pattern (see MEMORY.md):

Create `frontend/src/components/games/{game}/`:
- `{Game}DetailsComponent.tsx` - Display match details
- `{Game}ReplayComponent.tsx` - Interactive replay with controls
- `{Game}ReplayEngine.ts` - Implements `calculatePositions(moves)` method
  - Mirror the backend `GameEngine.applyMove` logic
  - Return array of position states for each move

Update `frontend/src/components/games/registry.ts`:
```typescript
import { {Game}DetailsComponent, {Game}ReplayComponent } from './{game}'
import { {Game}ReplayEngine } from './{game}/{Game}ReplayEngine'

export const getGameComponents = (gameType: string) => {
  switch (gameType) {
    case 'TIC_TAC_TOE':
      return {
        DetailsComponent: {Game}DetailsComponent,
        ReplayComponent: {Game}ReplayComponent
      }
    // ... other games
  }
}

export const getReplayEngine = (gameType: string) => {
  switch (gameType) {
    case 'TIC_TAC_TOE':
      return new {Game}ReplayEngine()
    // ... other games
  }
}
```

Use shared hooks for replay functionality:
- `useReplayControls` - Navigation state (current move index)
- `useReplayAutoplay` - Automatic playback
- `useReplayKeyboard` - Arrow keys, Home/End, Space

### 10. Testing & Verification
- Backend: Run `./mvnw verify` to test all layers
- Frontend: Test replay engine matches backend engine behavior
- Integration: Create test bots and run a full match
- Verify OpenAPI docs at http://localhost:8080/swagger-ui.html

## Tech Stack Context
- Java 25, Spring Boot 4.0.2
- PostgreSQL with Flyway migrations
- NullAway for compile-time null safety
- Lombok for boilerplate reduction
- OpenAPI/Swagger for API documentation
- AssertJ for test assertions
- Testcontainers for integration tests
