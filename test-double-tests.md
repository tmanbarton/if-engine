# Tests That Exercise Test Doubles Instead of Production Code

Tests where the handler takes the same code path as an existing test, and only the
test double's response differs. The handler just calls `tryOpen()`/`tryUnlock()` and
returns the result — it contains no logic about keys, locks, open states, or case matching.

## OpenHandlerTest

### Can remove (12 tests)

| Nested class | Test | Why it's testing the test double | Existing test that covers the same handler path |
|---|---|---|---|
| `OpenUnlockedDoor` | `testHandle_openUnlockedVault` | Same routing as "door" — target name matching is in `TestOpenableLocation.matchesOpenTarget()` | `testHandle_openUnlockedDoor` |
| `OpenLockedWithKey` | `testHandle_openLockedWithKey` | Auto-unlock logic is in `TestOpenableLocation.tryOpen()` | `testHandle_openUnlockedDoor` |
| `OpenLockedWithoutKey` | `testHandle_openLockedWithoutKey` | No-key check is in `TestOpenableLocation.tryOpen()` | `testHandle_openUnlockedDoor` |
| `OpenLockedWithoutKey` | `testHandle_openLockedVaultWithoutKey` | Redundant with above + different target name matching in test double | `testHandle_openUnlockedDoor` |
| `AlreadyOpen` | `testHandle_alreadyOpen` | Already-open check is in `TestOpenableLocation.tryOpen()` | `testHandle_openUnlockedDoor` |
| `CaseSensitivity` | `testHandle_openUppercaseTarget` | Case handling is in `TestOpenableLocation.matchesOpenTarget()` | `testHandle_openUnlockedDoor` |
| `CaseSensitivity` | `testHandle_openMixedCaseTarget` | Case handling is in `TestOpenableLocation.matchesOpenTarget()` | `testHandle_openUnlockedDoor` |
| `StateTransitions` | `testHandle_stateAfterOpenLockedWithKey` | `isUnlocked()`/`isOpen()` are set by `TestOpenableLocation.tryOpen()` | `testHandle_openUnlockedDoor` |
| `StateTransitions` | `testHandle_stateAfterOpenUnlocked` | `isOpen()` is set by `TestOpenableLocation.tryOpen()` | `testHandle_openUnlockedDoor` |
| `OpenableItemInInventory` | `testHandle_openLockedItemWithKey` | Auto-unlock logic is in `TestOpenableItem.tryOpen()` | `testHandle_openUnlockedItemInInventory` |
| `OpenableItemInInventory` | `testHandle_openLockedItemWithoutKey` | Locked check is in `TestOpenableItem.tryOpen()` | `testHandle_openUnlockedItemInInventory` |
| `OpenableItemAtLocation` | `testHandle_openLockedItemAtLocationWithoutKey` | Locked check is in `TestOpenableItem.tryOpen()` | `testHandle_openUnlockedItemAtLocation` |

### Empty nested classes after removal

These nested classes would have zero tests left and should be deleted entirely:

- `OpenLockedWithKey`
- `OpenLockedWithoutKey`
- `AlreadyOpen`
- `CaseSensitivity`
- `StateTransitions`

## UnlockHandlerTest

### Can remove (7 tests)

| Nested class | Test | Why it's testing the test double | Existing test that covers the same handler path |
|---|---|---|---|
| `UnlockWithKey` | `testHandle_unlockVaultWithKey` | Same routing as "door" — target name matching is in `TestOpenableLocation.matchesUnlockTarget()` | `testHandle_unlockWithKey` |
| `UnlockWithoutKey` | `testHandle_unlockWithoutKey` | No-key check is in `TestOpenableLocation.tryUnlock()` | `testHandle_unlockWithKey` |
| `AlreadyUnlocked` | `testHandle_alreadyUnlocked` | Already-unlocked check is in `TestOpenableLocation.tryUnlock()` | `testHandle_unlockWithKey` |
| `CaseSensitivity` | `testHandle_unlockUppercaseTarget` | Case handling is in `TestOpenableLocation.matchesUnlockTarget()` | `testHandle_unlockWithKey` |
| `CaseSensitivity` | `testHandle_unlockMixedCaseTarget` | Case handling is in `TestOpenableLocation.matchesUnlockTarget()` | `testHandle_unlockWithKey` |
| `OpenableItemInInventory` | `testHandle_unlockItemWithoutKey` | No-key check is in `TestOpenableItem.tryUnlock()` | `testHandle_unlockItemWithKey` |
| `OpenableItemAtLocation` | `testHandle_unlockItemAtLocationWithoutKey` | No-key check is in `TestOpenableItem.tryUnlock()` | `testHandle_unlockItemAtLocationWithKey` |

### Empty nested classes after removal

- `UnlockWithoutKey`
- `AlreadyUnlocked`
- `CaseSensitivity`

## Total: 19 tests

## The pattern

In every case, the handler code path is identical:

```
find object by priority → call tryOpen/tryUnlock → return result.message()
```

The handler contains no branching logic based on lock state, key presence, or case.
These tests only vary what the test double returns, not what the handler does.