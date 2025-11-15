# Side Configuration Card - Testing Guide

## Pre-Testing Setup

### Requirements
1. Have NeutronCompressor or NeutronCollector placed in world
2. Configure at least one side of the machine using the IO config GUI
3. Have a Side Configuration Card in inventory

### Testing Steps

## Test 1: Save Configuration from Machine
**Objective:** Verify Shift+Right-click reads machine configuration

**Steps:**
1. Open machine's GUI and configure at least one side (e.g., set top side to "Passive Input")
2. Close GUI
3. Hold Side Configuration Card in hand
4. **Hold Shift + Right-click** on the configured machine
5. Check that:
   - ✅ Card shows "§7Configuration saved" in tooltip
   - ✅ Card has enchantment glow
   - ✅ Message: "Configuration saved to card" appears

**Expected Result:** Configuration successfully saved to card

---

## Test 2: Apply Configuration to Machine
**Objective:** Verify Right-click applies configuration from card

**Steps:**
1. Have a configured card (from Test 1)
2. Place a **different/unconfigured** machine of same type
3. Hold the configured card
4. **Right-click** (no Shift) on the new machine
5. Open the machine's GUI
6. Check that:
   - ✅ Configuration matches the saved config
   - ✅ Message: "Configuration applied to machine" appears

**Expected Result:** Configuration successfully copied to new machine

---

## Test 3: Clear Configuration
**Objective:** Verify Shift+Right-click air clears card configuration

**Steps:**
1. Have a configured card (from Test 1 or 2)
2. Hold the card
3. **Look at air (not at any block)**
4. **Hold Shift + Right-click**
5. Check that:
   - ✅ Tooltip shows "§7Empty configuration"
   - ✅ Enchantment glow disappears
   - ✅ Message: "Configuration cleared" appears

**Expected Result:** Card configuration successfully cleared

---

## Test 4: No Configuration Error
**Objective:** Verify error message when trying to apply empty card

**Steps:**
1. Have an **empty** card (from Test 3)
2. Place any compatible machine
3. Hold the empty card
4. **Right-click** (no Shift) on the machine
5. Check that:
   - ✅ Message: "No configuration to apply" appears
   - ✅ Machine configuration unchanged

**Expected Result:** Proper error message, no crash

---

## Test 5: Already Empty Error
**Objective:** Verify error message when clearing already empty card

**Steps:**
1. Have an **empty** card (from Test 3)
2. Hold the card
3. **Shift + Right-click** in air
4. Check that:
   - ✅ Message: "Configuration is already empty" appears
   - ✅ No crash or error

**Expected Result:** Proper error message, no crash

---

## Test 6: Apply to Different Tier Machine
**Objective:** Verify configuration works across different machine tiers

**Steps:**
1. Configure a NeutronCompressor (default tier)
2. Save configuration to card
3. Place a Dense/Denser/Densest NeutronCompressor
4. Apply configuration from card
5. Check that:
   - ✅ Configuration applies correctly
   - ✅ All sides configured properly

**Expected Result:** Configuration transferable across tiers

---

## Test 7: Non-ITileIO Block
**Objective:** Verify card doesn't interfere with normal blocks

**Steps:**
1. Hold configured card
2. **Right-click** on any non-machine block (e.g., chest, furnace)
3. Check that:
   - ✅ No message appears
   - ✅ Card configuration unchanged
   - ✅ Block behaves normally

**Expected Result:** No interference with non-machine blocks

---

## Bug-Specific Tests

## Test 8: [FIXED] Configuration Persistence
**Objective:** Verify saved configuration doesn't get cleared

**Steps:**
1. Configure a machine
2. **Shift + Right-click** to save to card
3. **Immediately** hover over card to check tooltip
4. Check that:
   - ✅ Tooltip shows "§7Configuration saved" (not empty)
   - ✅ Enchantment glow visible
   - ✅ Configuration persisted

**Expected Result:** Configuration persists after saving (BUG FIXED!)

---

## Test 9: [FIXED] Shift+Right Machine vs Air
**Objective:** Verify Shift+Right behaves differently on machine vs air

**Steps:**
1. Hold card
2. **Shift + Right-click** on machine → Should SAVE
3. Check message
4. Hold card
5. **Shift + Right-click** on air → Should CLEAR
6. Check message

**Expected Result:**
- On machine: "Configuration saved to card"
- On air: "Configuration cleared"

---

## Multiplayer Testing

### Server-Side Validation
1. Test all above scenarios on dedicated server
2. Verify server console has no errors
3. Test with multiple players simultaneously
4. Verify configuration sync works properly

---

## Performance Testing

### Stress Test
1. Configure 10+ machines with different IO settings
2. Save each configuration to separate cards
3. Apply each configuration to new machines
4. Verify no lag or memory issues

---

## Edge Cases

### Item Stack Manipulation
1. Drop card on ground, pick up
2. Place card in chest, retrieve
3. Trade card between players
4. ✅ Verify configuration persists through all actions

### Data Integrity
1. Save complex configuration (all 6 sides set differently)
2. Apply to machine
3. Re-save to new card
4. Verify all 6 sides match exactly

---

## Success Criteria

✅ All 9 tests pass
✅ No crashes or errors in console
✅ Configuration data integrity maintained
✅ Works in both singleplayer and multiplayer
✅ Performance acceptable with multiple cards

---

## Test Report Template

```
Test Date: _______________
Minecraft Version: _______________
Forge Version: _______________

[ ] Test 1: Save Configuration - PASS/FAIL
[ ] Test 2: Apply Configuration - PASS/FAIL
[ ] Test 3: Clear Configuration - PASS/FAIL
[ ] Test 4: No Config Error - PASS/FAIL
[ ] Test 5: Already Empty Error - PASS/FAIL
[ ] Test 6: Cross-Tier Transfer - PASS/FAIL
[ ] Test 7: Non-ITileIO Block - PASS/FAIL
[ ] Test 8: Configuration Persistence - PASS/FAIL
[ ] Test 9: Shift+Right Different Behaviors - PASS/FAIL

Notes: ________________________
```

