import re
import sys
import xml.etree.ElementTree as ET

def verify_manifest():
    print("=== Manifest Verification ===")
    tree = ET.parse("app/src/main/AndroidManifest.xml")
    root = tree.getroot()
    ns = {
        'android': 'http://schemas.android.com/apk/res/android',
        'tools': 'http://schemas.android.com/tools'
    }
    
    app = root.find("application")
    activity = app.find("activity")
    screen_orient = activity.get(f"{{{ns['android']}}}screenOrientation")
    config_changes = activity.get(f"{{{ns['android']}}}configChanges")
    tools_ignore = activity.get(f"{{{ns['tools']}}}ignore")
    
    print(f"activity name: {activity.get(f'{{{ns['android']}}}name')}")
    print(f"screenOrientation: {screen_orient}")
    print(f"configChanges: {config_changes}")
    print(f"tools:ignore: {tools_ignore}")
    
    assert screen_orient == "portrait", f"Expected portrait, got {screen_orient}"
    assert config_changes == "orientation|screenSize|screenLayout|keyboardHidden", f"Unexpected configChanges: {config_changes}"
    assert tools_ignore == "LockedOrientationActivity", f"Unexpected tools:ignore: {tools_ignore}"
    print("Manifest: PASS\n")

def verify_gameplay_screen():
    print("=== GamePlayScreen.kt Verification ===")
    with open("app/src/main/java/com/example/ui/GamePlayScreen.kt", "r", encoding="utf-8") as f:
        text = f.read()

    # Verify no initialOrientation
    assert "initialOrientation" not in text, "initialOrientation found!"
    print("Check 'no initialOrientation': PASS")

    # Verify requestedOrientation usage
    req_orient = re.findall(r'activity\?\.requestedOrientation\s*=\s*ActivityInfo\.([A-Z_]+)', text)
    print(f"requestedOrientation assignments: {req_orient}")
    assert req_orient == [
        'SCREEN_ORIENTATION_SENSOR_LANDSCAPE',
        'SCREEN_ORIENTATION_PORTRAIT',
        'SCREEN_ORIENTATION_PORTRAIT'
    ], f"Unexpected orientation assignments: {req_orient}"
    print("Check 'requestedOrientation assignments': PASS")

    # Verify WindowInsets and keepScreenOn
    assert "view.keepScreenOn = true" in text
    assert "view.keepScreenOn = false" in text
    assert "BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE" in text
    print("Check 'insets and screen on': PASS")

    # Verify sensor logic
    assert "val z = event.values[2]" in text
    assert "abs(z) < 5.0f" in text
    assert "z < -7.0f" in text
    assert "z > 7.0f" in text
    assert "1100L" in text
    print("Check 'sensor logic invariance': PASS")

    # Simple bracket matching
    clean = re.sub(r'//.*', '', text)
    clean = re.sub(r'/\*[\s\S]*?\*/', '', clean)
    clean = re.sub(r'\"\"\"[\s\S]*?\"\"\"', '""', clean)
    clean = re.sub(r'"(\\.|[^"\\])*"', '""', clean)
    clean = re.sub(r"'(\\.|[^'\\])*'", "''", clean)

    b_open = clean.count('{')
    b_close = clean.count('}')
    p_open = clean.count('(')
    p_close = clean.count(')')
    sb_open = clean.count('[')
    sb_close = clean.count(']')

    print(f"Code-only brackets: Braces {b_open}/{b_close}, Parens {p_open}/{p_close}, SBrackets {sb_open}/{sb_close}")
    assert b_open == b_close, f"Brace mismatch: {b_open} vs {b_close}"
    assert p_open == p_close, f"Paren mismatch: {p_open} vs {p_close}"
    assert sb_open == sb_close, f"Square bracket mismatch: {sb_open} vs {sb_close}"
    print("Check 'bracket matching': PASS\n")

if __name__ == "__main__":
    verify_manifest()
    verify_gameplay_screen()
    print("ALL INTEGRITY CHECKS PASSED.")
