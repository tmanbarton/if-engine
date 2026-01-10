# IFEngine API - Complexity Assessment

This document tracks what the IFEngine API supports for complex games (like vots-adventure) and identifies gaps that may need addressing.

---

## Supported Well

- **Multiple locations with complex connections** - GameContent.setupConnections() handles any topology
- **Items with aliases** - Item constructor accepts alias sets
- **SceneryObjects with interaction responses** - Builder pattern with InteractionType responses
- **SceneryContainers** - Scenery that can contain items (barrels, shelves, etc.)
- **OpenableLocation** - Abstract base class for locked doors, sheds, etc. (games subclass with key-based unlocking)
- **OpenableItem** - Abstract base class for lockable items (chests, lockboxes, cryptexes) with flexible unlock logic
- **Code/word-based unlocking** - Items can require codes ("1, 2, 3, 4") or words ("plugh") via `tryUnlock()`/`tryOpen()`
- **Prompt flow for codes** - `WAITING_FOR_UNLOCK_CODE`/`WAITING_FOR_OPEN_CODE` states handle interactive code entry
- **Custom unlock logic** - `Openable` interface allows full control; games override `tryUnlock()`/`tryOpen()` with any logic
- **Custom response text** - ResponseProvider interface for full narrator customization
- **Session management** - Multi-player support with ConcurrentHashMap
- **Restart/quit flow** - Confirmation dialogs, state reset
- **Readable/Edible items** - Constants in ReadHandler/EatHandler; games can extend handlers to customize

---

## Potential Gaps for Complex Games

| Feature | Current State | Concern |
|---------|---------------|---------|
| Custom commands | Handlers hardcoded in GameEngine | Can't add game-specific verbs easily |
| Item subclasses | Supported (Item is a class) | Works, but no examples in ExampleGameContent |
| Game flags/events | Not supported | No way to track "met_npc", "visited_cave", etc. beyond inventory |
| NPC system | Not supported | No infrastructure for characters with dialogue, triggers, or conditional responses |
| State-dependent descriptions | Not supported | Items/locations can't show different text based on game flags |
| Side effect hooks | Not supported | No hooks for "when X opens, reveal Y" or "when taken, trigger trap" |
| Wearable items | Not supported | No "wear/remove" commands or worn vs carried distinction |
| Score system | Not supported | No built-in point tracking or achievement system |
| Combinable items | Not supported | No "use X with Y" or "combine X and Y" to create new items |
| Turn counter | Not supported | No tracking of moves/turns for timed puzzles or events |

---

## Recommended Fixes

### Priority 1: Allow custom command handlers
**Problem:** CommandDispatcher handlers are hardcoded in GameEngine constructor.

**Options:**
- Add `getCustomHandlers()` to GameContent interface
- GameEngine registers content-provided handlers after built-in ones
- Example: game adds "use" command for "use key on door"

---

## Improvement considerations

- **Multi-section intros** - Progressive messages, e.g. Some message. (Press enter/enter anything to continue) -> next message -> etc.

---

## Examples Needed

When these gaps are addressed, ExampleGameContent should demonstrate:
- Custom Item subclass with special behavior
- OpenableLocation subclass (e.g., a locked door requiring a key)
- OpenableItem subclass (e.g., a lockbox requiring a code)
- Custom command handler registration