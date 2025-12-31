import json
import urllib.request
import math

SERVER_URL = "http://localhost:8091/api/scan/analyze"

# Format: Target: {data}
KNOWN_APPS = {
    "TikTok": {"exposure": 100, "consent": 75, "sensitivity": 75, "retention": 100, "tracking": 100, "permission": 90, "network": 80},
    "Facebook": {"exposure": 100, "consent": 75, "sensitivity": 75, "retention": 100, "tracking": 100, "permission": 85, "network": 80},
    "Instagram": {"exposure": 75, "consent": 75, "sensitivity": 75, "retention": 100, "tracking": 90, "permission": 80, "network": 75},
    "X": {"exposure": 100, "consent": 25, "sensitivity": 25, "retention": 100, "tracking": 80, "permission": 60, "network": 50},
    "WhatsApp": {"exposure": 75, "consent": 75, "sensitivity": 25, "retention": 75, "tracking": 50, "permission": 60, "network": 20},
    "Signal": {"exposure": 0, "consent": 0, "sensitivity": 0, "retention": 0, "tracking": 0, "permission": 10, "network": 0},
    "Amazon": {"exposure": 75, "consent": 50, "sensitivity": 75, "retention": 100, "tracking": 80, "permission": 90, "network": 40},
    "Temu": {"exposure": 100, "consent": 75, "sensitivity": 75, "retention": 100, "tracking": 100, "permission": 95, "network": 90},
    "Google": {"exposure": 60, "consent": 50, "sensitivity": 50, "retention": 100, "tracking": 90, "permission": 20, "network": 20}
}

def calculate_expected_score(data):
    total = (data["exposure"] * 1.5 +
             data["consent"] * 1.0 +
             data["sensitivity"] * 1.2 +
             data["retention"] * 0.8 +
             data["tracking"] * 1.5 +
             data["permission"] * 1.2 +
             data["network"] * 1.0)
    
    divider = 1.5 + 1.0 + 1.2 + 0.8 + 1.5 + 1.2 + 1.0
    final_score = total / divider
    return min(100.0, final_score)

def test_app(app_name, expected_data):
    expected_score = calculate_expected_score(expected_data)
    
    req = urllib.request.Request(
        SERVER_URL, 
        data=json.dumps({"target": app_name}).encode('utf-8'),
        headers={'Content-Type': 'application/json'}
    )
    
    try:
        with urllib.request.urlopen(req) as response:
            result = json.loads(response.read().decode('utf-8'))
            actual_score = result.get("finalRiskScore")
            
            # Allow small float error
            if abs(actual_score - expected_score) < 0.01:
                return True, actual_score, expected_score
            else:
                return False, actual_score, expected_score
    except Exception as e:
        print(f"Error testing {app_name}: {e}")
        return False, None, expected_score

def main():
    print("Starting Accuracy Evaluation...")
    correct = 0
    total = 0
    
    for app, data in KNOWN_APPS.items():
        total += 1
        success, actual, expected = test_app(app, data)
        if success:
            print(f"[PASS] {app}: {actual:.2f}")
            correct += 1
        else:
            print(f"[FAIL] {app}: Actual={actual}, Expected={expected:.2f}")

    accuracy = (correct / total) * 100 if total > 0 else 0
    print(f"\nTotal Accuracy: {accuracy:.2f}% ({correct}/{total})")

if __name__ == "__main__":
    main()
