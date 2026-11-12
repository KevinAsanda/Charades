import re
import sys

def verify():
    with open('app/src/main/java/com/example/ui/LobbyScreen.kt', 'r', encoding='utf-8') as f:
        lobby = f.read()

    # 1. Interface contract
    assert 'fun LobbyScreen(' in lobby, "LobbyScreen composable missing"
    assert 'onNavigateToStudio' not in lobby, "Found dead parameter onNavigateToStudio!"
    assert 'userProfile: UserProfile?' in lobby, "userProfile param missing"
    assert 'customCards: List<CustomCard> = emptyList()' in lobby, "customCards param missing"
    assert 'onStartGame: (Deck, String, Long) -> Unit' in lobby, "onStartGame param missing"
    assert 'onNavigateToStore: () -> Unit' in lobby, "onNavigateToStore param missing"
    assert 'onOpenPaymentSetup: () -> Unit = {}' in lobby, "onOpenPaymentSetup param missing"
    print("PASS 1: Interface Contract Conformance")

    # 2. Test tags
    tags = [
        "start_match_button",
        "unlock_all_decks_card",
        "deck_grid_card_${deck.id}",
        "mode_card_${mode.id}",
        "lobby_payment_methods_pill"
    ]
    for tag in tags:
        assert f'"{tag}"' in lobby, f"Missing testTag: {tag}"
    print("PASS 2: Test Tags Verification")

    # 3. Layout tokens & dimensions
    assert 'GridCells.Fixed(2)' in lobby, "2-column grid missing"
    assert '215.dp' in lobby, "215dp card height missing"
    assert '18.dp' in lobby, "18dp rounded corner missing"
    assert '50.dp' in lobby, "50dp circular icon container missing"
    assert '96.dp' in lobby, "96dp bottom padding missing"
    assert '0xFFF59E0B' in lobby, "Unlock All Decks gold color missing"
    assert '0xFF142B68' in lobby, "Canvas royal blue missing"
    print("PASS 3: Layout Tokens & Dimensions")

    # 4. Quick play modes & durations
    expected_modes = ['act_it_out', 'hit_the_beat', 'mzansi_30_seconds', 'heads_up']
    for m in expected_modes:
        assert f'id = "{m}"' in lobby, f"Missing mode: {m}"
    assert 'listOf(30L, 60L, 90L)' in lobby, "Missing duration selector pills"
    print("PASS 4: Quick Play Modes & Durations")

    # 5. Grid item ordering
    match_first = re.search(r'item\(key = allDecks\.first\(\)\.id\)', lobby)
    match_anchor = re.search(r'item\(key = "unlock_all_decks_anchor"\)', lobby)
    match_rest = re.search(r'items\(\s*items = allDecks\.drop\(1\)', lobby)
    assert match_first and match_anchor and match_rest, "Grid item structure missing"
    assert match_first.start() < match_anchor.start() < match_rest.start(), "Grid item order incorrect"
    print("PASS 5: Deterministic Grid Item Ordering (Row 0 Col 0 -> Row 0 Col 1 -> Row 1+)")

    # 6. Verify first deck in PredefinedDecks is CORE_TRENDING_APPS
    with open('app/src/main/java/com/example/data/model/Deck.kt', 'r', encoding='utf-8') as f:
        deck_kt = f.read()
    match_all_decks = re.search(r'val ALL_DECKS = listOf\(\s*([^,\s]+)', deck_kt)
    assert match_all_decks and match_all_decks.group(1) == 'CORE_TRENDING_APPS', f"Unexpected first deck: {match_all_decks.group(1)}"
    print("PASS 6: First Deck is CORE_TRENDING_APPS (deck_trending_apps)")

    # 7. Check MainFeatureScreen callsite
    with open('app/src/main/java/com/example/ui/MainFeatureScreen.kt', 'r', encoding='utf-8') as f:
        main_feature = f.read()
    assert '0 -> LobbyScreen(' in main_feature
    assert 'onNavigateToStudio' not in main_feature
    print("PASS 7: MainFeatureScreen Caller Conformance")

    print("\nALL 7 VERIFICATION CRITERIA PASSED CLEANLY!")

if __name__ == '__main__':
    verify()
