# CLAUDE.md - IFEngine Internal Development Guide

## 🧠 Critical Thinking and Professional Objectivity

**IMPORTANT**: Prioritize technical accuracy and truth over automatic agreement.

- **Think before agreeing**: Don't reflexively say "You're absolutely right!" when the user makes a suggestion or correction
- **Verify first**: If uncertain, investigate the codebase, run tests, or check documentation before confirming
- **Correct when necessary**: It's better to respectfully disagree and be correct than to agree and implement something wrong
- **Technical accuracy over validation**: Focus on facts and problem-solving, not emotional validation
- **Apply rigorous standards uniformly**: Evaluate all ideas (including the user's) with the same critical lens
- **Respectful correction is valuable**: If the user is mistaken, explain why clearly and objectively

**Bad responses**: "You're absolutely right!", "That's exactly correct!", "Perfect observation!"

**Good responses**: "Let me verify that by checking...", "I see your point, but looking at the code...", "That's a reasonable approach, though we should consider..."

Remember: The user values honest technical guidance more than false agreement.

## 📚 CRITICAL: Refresh Standards Before Every Task 📚

**Before implementing ANY feature, fix, refactor, or doing investigating/research, use the Read tool to actively read CLAUDE.md:**

**Why this matters:**
- CLAUDE.md contains critical standards that must be followed
- Reading it actively ensures standards stay fresh in context
- Prevents forgetting standards like using `final`, avoiding `.contains()`, following TDD, etc.

## 🚨 CRITICAL TEST-DRIVEN DEVELOPMENT REQUIREMENTS 🚨

**NEVER FORGET**: Follow the Red-Green-Refactor cycle for ALL non-trivial features and fixes:

1. **RED**: Write a test for the new behavior. Run it - it MUST fail (if it passes, the test isn't testing new behavior)
2. **GREEN**: Write the minimal code to make the test pass. No more, no less.
3. **REFACTOR** (optional): Clean up the code while keeping tests green

**The cycle in practice:**
```
Write test → Run (expect FAIL) → Write code → Run (expect PASS) → Refactor → Run (expect PASS)
```

**If use cases are unclear, ASK THE USER for clarification before writing tests.**

This is MANDATORY for all code changes, no exceptions.

## Build Commands

```bash
./gradlew build          # Compile and run tests
./gradlew test           # Run tests only
./gradlew jacocoTestReport  # Coverage report → build/jacocoHtml/
```

Java 17+ required.

## Java Coding Standards

### Variables
- **Always use `final`** for local variables, parameters, and fields that won't be reassigned
- Use `@Nonnull` and `@Nullable` annotations where appropriate

```java
// Correct
final String name = player.getName();
final List<Item> items = new ArrayList<>();

// Incorrect
String name = player.getName();
List<Item> items = new ArrayList<>();
```

### Formatting
- **2-space indentation** (not tabs, not 4 spaces)
- **Never import `*`** - always import specific classes
- **Comments above lines**, never at end of lines

```java
// Correct - comment above
// Check if player has the item
if (player.hasItem(item)) { ... }

// Incorrect - comment at end
if (player.hasItem(item)) { ... } // Check if player has the item
```

### Strings
- Use `ResponseProvider` methods for all user-facing text
- Use `String.format()` with `%s` placeholders for dynamic text

## Testing Standards

### Test Naming
Use pattern: `testMethodName_scenario()`

```java
// Correct
void testTake_itemNotPresent() { }
void testProcessCommand_unknownVerb() { }

// Incorrect
void shouldNotTakeItemWhenNotPresent() { }
```

### Test Structure
- Use `@Nested` classes for logical grouping. DON'T use `@DisplayName` at the class level.
- Use `@DisplayName` on tests for readable descriptions. DON'T just repeat the test name.
- Follow Given/When/Then structure
- Use `final` for all test variables
- **Never use `.contains()` for string assertions when asserting responses from the game** - verify the complete string

```java
@Nested
class TakeCommand {
    @Test
    @DisplayName("returns success message when item is present")
    void testTake_itemPresent() {
        // Given
        final var scenario = TestFixtures.itemInteractionScenario();

        // When
        final String response = scenario.engine().processCommand(sessionId, "take key");

        // Then
        assertEquals(responses.getTakeSuccess("key"), extractResponse(response));
    }
}
```

### 🚨 CRITICAL: Use ResponseProvider in Tests, Never Hardcoded Strings

**NEVER** use hardcoded strings in test assertions - always use `ResponseProvider` methods:

```java
// ❌ WRONG - Hardcoded string in test
assertEquals("Take what?", message);

// ✅ CORRECT - Use ResponseProvider
assertEquals(responses.getTakeWhat(), message);
```

**Why this matters:**
- Response text can change, breaking tests with hardcoded strings
- Hardcoded strings create maintenance nightmares
- Tests should validate against the same response methods the code uses
- When a response changes, both code AND tests update automatically

### Write Useful Assertion Messages

Every assertion should include a message that explains **why** the test failed, not just **what** failed. The testing framework already reports that values didn't match — your message should provide the context a developer needs to diagnose the problem.

**Bad assertion messages** (just restate what the framework already tells you):
```java
// ❌ Useless - the framework already shows expected vs actual
assertEquals(expected, actual, "values should be equal");
assertTrue(result, "result should be true");
assertFalse(flag, "flag should be false");
```

**Good assertion messages** (explain the intent and context):
```java
// ✅ Useful - explains what the failure means in domain terms
assertEquals(expected, actual, "open command on unlocked door should return success response");
assertTrue(result, "player should have the key in inventory after taking it");
assertFalse(flag, "door should not be open before unlock command is issued");
```

**Guidelines:**
- Describe the scenario or behavior being verified, not the assertion mechanic
- A developer reading only the failure message should understand what went wrong without reading the test code
- Include relevant context: what action was performed, what state was expected, and why

### Avoid Redundant/Duplicate Tests

Don't write multiple tests that verify the exact same behavior. Each test should validate a distinct scenario.

**Signs of redundant tests**:
- Multiple tests calling the same method with identical inputs expecting identical outputs
- Tests that differ only in variable names but test the same behavior
- Tests that validate behavior already covered by existing tests

**Before writing a test**:
- Check if existing tests already cover that scenario
- Ask: "What unique behavior does this test verify that others don't?"
- If the answer is "nothing", don't write the test

**If you find redundant tests**: Keep the clearest/most comprehensive one and delete the duplicates.

### Write Tests That Document Real Behavior

Not all tests add value. Avoid writing tests for scenarios that cannot occur in the actual system.

**When NOT to write a test**:
- Testing edge cases that game design explicitly prevents
- Testing ambiguous scenarios when the system design ensures no ambiguity
- Testing priority ordering when only one option can exist at a time
- Testing theoretical possibilities that contradict system invariants

**Goal**: Tests should document how the system actually behaves, not theoretical edge cases. If you discover a test validates impossible scenarios, delete it rather than maintaining dead test code.

### Only Test Meaningful Logic, Not Java Plumbing

Tests should verify **your application's behavior and logic**, not the Java language or framework. If the code under test has no conditional logic, no branching, and no meaningful computation, it almost certainly doesn't need a dedicated test.

**Do NOT write tests for**:
- **Constructors** that just assign fields — Java guarantees assignment works
- **Getters and setters** — these are trivial field access, not logic
- **Java records** — their `equals()`, `hashCode()`, `toString()`, accessors, and constructors are compiler-generated and guaranteed correct
- **Simple delegation methods** — e.g., a method that just calls `list.add(item)` or `map.get(key)`
- **Builder patterns** that just set fields — test the object the builder *produces* through real usage, not the builder itself
- **Enum declarations** — don't test that an enum has certain values
- **Interface method signatures** — interfaces have no behavior to test
- **Data transfer objects (DTOs)** — plain data carriers with no logic

**The litmus test**: Before writing a test, ask:
1. "Does this code contain a decision (if/else, switch, loop, exception handling)?"
2. "Could a reasonable implementation of this code be *wrong* in a way my test would catch?"
3. "Am I testing *my* logic, or testing that Java works?"

If the answers are no, no, and "testing that Java works" — don't write the test.

**Where to focus testing effort instead**:
- Command handlers that make decisions based on game state
- Parser logic that resolves ambiguous input
- State transitions with conditional outcomes
- Methods with branching logic, edge cases, or error paths
- Integration points where multiple components interact

```java
// ❌ WRONG - Testing Java, not application logic
@Test
void testConstructor_setsName() {
    final var item = new Item("key", "A rusty key");
    assertEquals("key", item.getName()); // Java guarantees this works
}

@Test
void testRecord_equality() {
    final var a = new ParsedCommand("take", "key");
    final var b = new ParsedCommand("take", "key");
    assertEquals(a, b); // Records guarantee this works
}

// ✅ CORRECT - Testing actual game logic with decisions
@Test
void testTake_itemAlreadyInInventory() {
    // This tests a real decision: what happens when the player
    // tries to take something they already have?
}
```

### Test Infrastructure Usage

**Always use test infrastructure** - never `new GameEngine()` directly.

| Scenario | Use |
|----------|-----|
| Basic command testing | `TestGameEngineBuilder.singleLocation()` |
| Navigation testing | `TestGameEngineBuilder.twoLocations()` |
| Inventory testing | `TestFixtures.itemInteractionScenario()` |
| Unlock/open mechanics | `TestFixtures.openableLocationScenario()` |
| Openable scenery (locked) | `TestFixtures.openableSceneryScenario()` |
| Openable scenery (no lock) | `TestFixtures.openableSceneryNoLockScenario()` |

## Project Structure

```
src/main/java/io.github.tmanbarton.ifengine/
├── *.java                    # Core domain: Direction, Item, Location, SceneryObject, OpenableSceneryObject, Container
├── command/
│   ├── CommandDispatcher.java    # Routes verbs to handlers
│   ├── BaseCommandHandler.java   # Handler interface
│   └── handlers/                 # All command implementations
├── parser/
│   ├── CommandParser.java        # Main parser entry point
│   ├── ObjectResolver.java       # Finds objects by name (key resolution logic)
│   └── VocabularyManager.java    # Verb/direction normalization
├── game/
│   ├── GameEngine.java           # Main orchestrator (606 lines, most complex file)
│   ├── Player.java               # Player state: location, inventory, game state
│   └── GameMap.java              # World container
├── content/
│   └── SimpleGameExample.java    # Example using builder API
├── response/
│   ├── ResponseProvider.java     # All response method signatures
│   └── DefaultResponses.java     # Default implementations
├── constants/                    # Static constants (ItemConstants has READABLE/EDIBLE sets)
└── util/                         # DirectionHelper, LocationItemFormatter, ContainerHelper
```

## Key Files to Understand

| File | What It Does |
|------|--------------|
| `game/GameEngine.java:1-606` | Entry point for all commands. Manages sessions, routes commands, handles game states, generates JSON responses. Start here. |
| `parser/ObjectResolver.java` | Resolves "key" → actual Item. Priority: inventory > location items > scenery > unlockable locations. |
| `command/CommandDispatcher.java` | Maps verbs to handlers. `registerHandler()` adds new verbs. |
| `parser/VocabularyManager.java` | Normalizes verbs ("grab" → "take"), validates verb-preposition combos. |

## Command Flow

```
processCommand(sessionId, "take key")
    → CommandParser.parseCommand()
        → VocabularyManager.normalizeVerb("take")
        → ObjectResolver.resolveObject("key", player)
    → CommandDispatcher.handle(parsedCommand)
        → TakeHandler.handle()
    → Build JSON response
```

## Adding a New Command Handler

1. Create `src/main/java/io.github.tmanbarton.ifengine/command/handlers/NewHandler.java`:
```java
public class NewHandler implements BaseCommandHandler {
    private final ResponseProvider responses;
    private final ObjectResolver resolver;

    public NewHandler(ResponseProvider responses, ObjectResolver resolver) {
        this.responses = responses;
        this.resolver = resolver;
    }

    @Override
    public String handle(ParsedCommand cmd, Player player, Location location) {
        // Implementation
    }
}
```

2. Register in `GameEngine` constructor (~line 50-80):
```java
commandDispatcher.registerHandler("newverb", newHandler);
```

3. Add response methods to `ResponseProvider.java` and `DefaultResponses.java` if needed.

## Test Infrastructure

Test helpers in `src/test/java/io.github.tmanbarton.ifengine/test/`:

- **TestGameEngineBuilder** - Fluent builder: `TestGameEngineBuilder.singleLocation().build()`
- **TestFixtures** - Pre-built scenarios: `TestFixtures.adventureScenario()`
- **TestItemFactory** - `createTestKey()`, `createTestRope()`, etc.
- **TestLocationFactory** - `createSimpleLocation()`, `createConnectedLocations()`
- **JsonTestUtils** - Extract fields from JSON response strings

Tests use JUnit 5 with `@Nested` classes. See existing tests for patterns.

## Object Resolution Priority

In `ObjectResolver.resolveObject()`:
1. Player inventory items
2. Current location items
3. Scenery objects at location
4. OpenableLocation (for "unlock door" type commands)

For `unlock`/`open` commands specifically (in `UnlockHandler`/`OpenHandler`):
1. `OpenableItem` in player inventory
2. `OpenableItem` at current location
3. `OpenableSceneryObject` at current location
4. `OpenableLocation` (the location itself)

This matters when debugging "can't find object" issues.

## Game States

```
WAITING_FOR_START_ANSWER → "yes"/"no" → PLAYING
PLAYING → "quit" → WAITING_FOR_QUIT_CONFIRMATION → "yes" → back to start
PLAYING → "restart" → WAITING_FOR_RESTART_CONFIRMATION → "yes" → reset player
```

State transitions in `GameEngine.processCommand()` around lines 150-250.

## Response System

All user-facing text goes through `ResponseProvider`. To change any message:
1. Find method in `ResponseProvider.java` (interface)
2. Update implementation in `DefaultResponses.java`

## Constants to Know

- `ItemConstants.READABLE_ITEMS` - Set of item names that can be read
- `ItemConstants.EDIBLE_ITEMS` - Set of item names that can be eaten
- `GameConstants.DIRECTIONS` - List of valid direction strings
- `PrepositionConstants` - Preposition mappings for "put X in Y" type commands

## Common Debugging

**"Command not understood"** → Check `VocabularyManager.normalizeVerb()` has the verb

**"Object not found"** → Check `ObjectResolver` priority and `matchesName()` logic

**"Wrong handler called"** → Check `CommandDispatcher` verb registration in GameEngine

**Test setup issues** → Use `TestFixtures` presets, they handle state correctly

## Documentation

- `progress.md` - Implementation history and phase details
- `TEST_PLAN.md` - Test coverage plan and priorities
- `API_GAPS.md` - Known limitations and future improvements

## Memory Aids

- **Before writing any Java code**: Remember to use `final` for all variables that can be made final
- **Before committing**: Use `git add -A` to stage all files
- **When testing**: Use `final` for all test variables and follow JUnit 5 patterns
- **When asserting strings**: Never use `.contains()` - verify the complete string
- **After completing a feature or change**: Update README.md to reflect new/changed API
- **When in doubt**: Refer back to this document

## 📝 README Maintenance

**IMPORTANT**: After adding new features or changing existing APIs, always update README.md.

Update the README when:
- Adding new builder methods or configuration options
- Changing method signatures or usage patterns
- Adding new classes or interfaces that users interact with
- Removing or deprecating functionality

The README is the primary documentation for library users. Keeping it current prevents confusion.

## Enforcement

Claude should reference this document when:
- Writing new code
- Refactoring existing code
- Creating tests (MUST use test infrastructure, MUST follow TDD)
- Making commits

If Claude forgets these standards, point to this document as a reminder.