# Bug Fix: SideConfigurationCard - Configuration Clear Issue

## Problem Description

When testing the SideConfigurationCard item, a bug was discovered where:
1. Right-clicking a machine would save its configuration to the card
2. But the saved configuration would immediately be cleared afterwards

## Root Cause

The issue was caused by **conflicting logic** between two methods:

1. **`useOn()` method** - Handled machine interactions (read/apply config)
2. **`use()` method** - Handled air interactions (clear config)

### Execution Flow Issue

When the player **Shift+Right-clicks a machine**, the following happened:

```
1. Player presses Shift + Right-click on machine
2. Client sends useOn() request
3. Server: useOn() processes and SAVES config to card
4. Server: use() is ALSO called (separate request)
5. Server: use() sees Shift is pressed and CLEARS the config
6. Result: Config saved then immediately cleared!
```

The problem: Both `use()` and `useOn()` were being triggered, and `use()` would clear what `useOn()` just saved.

## Solution

Modified the interaction logic to use **mutual exclusion**:

### Key Changes

1. **`useOn()` returns `InteractionResult.SUCCESS`** when processing machine interactions
   - This prevents `use()` from being called
   
2. **`use()` only processes** when `useOn()` returns `InteractionResult.PASS`
   - This happens when right-clicking air (no block)

3. **Simplified flow:**
   - **Shift+Right-click Machine** → `useOn()` handles, returns SUCCESS → `use()` NOT called
   - **Right-click Machine** → `useOn()` handles, returns SUCCESS → `use()` NOT called
   - **Shift+Right-click Air** → `useOn()` returns PASS → `use()` handles clear

### Code Structure

```java
@Override
public @NotNull InteractionResult useOn(@NotNull UseOnContext ctx) {
    // If ITileIO machine:
    //   - Shift+Right: READ config → return SUCCESS
    //   - Right: APPLY config → return SUCCESS
    // If air or non-machine:
    //   - Return PASS (let use() handle if Shift)
    return InteractionResult.PASS;
}

@Override
public @NotNull InteractionResultHolder<ItemStack> use(...) {
    // Only called when useOn() returned PASS
    // If Shift pressed: CLEAR config
    // Otherwise: PASS
    return InteractionResultHolder.pass(stack);
}
```

## Testing Results

### ✅ Test Cases

1. **Shift+Right-click Machine**
   - Expected: Config saved
   - Result: ✅ Config saved successfully
   - use() NOT called: ✅

2. **Right-click Machine (with saved config)**
   - Expected: Config applied
   - Result: ✅ Config applied successfully
   - use() NOT called: ✅

3. **Shift+Right-click Air**
   - Expected: Config cleared
   - Result: ✅ Config cleared successfully
   - use() called: ✅

4. **Right-click Air (no machine)**
   - Expected: No action
   - Result: ✅ No action taken
   - use() returned PASS: ✅

## Implementation Date
- **Bug Discovered:** November 14, 2025
- **Fix Applied:** November 14, 2025
- **Status:** ✅ RESOLVED

## Files Modified
- `src/main/java/committee/nova/mods/avaritia/common/item/misc/SideConfigurationCardItem.java`

## Prevention
This issue highlights the importance of understanding Minecraft's event system:
- `use()` and `useOn()` are separate events
- Both can be triggered by the same player action
- Return values determine if subsequent events fire
- Always design item interactions with this in mind
