import requests
import os

def test_register_and_search_doctor():
    base_url = os.getenv("DEV_URL", "http://localhost:8080")

    doctor_data = {
        "regNo": "D123",
        "name": "John Doe",
        "specialization": "Cardiology"
    }

    # Step 1: Register the doctor
    register_response = requests.post(f"{base_url}/registerDoctor", json=doctor_data)
    assert register_response.status_code == 200
    assert "Doctor registered successfully" in register_response.text

    # Step 2: Search for the doctor
    search_response = requests.get(f"{base_url}/searchDoctor/{doctor_data['name']}")
    assert search_response.status_code == 200
    results = search_response.json()

    # Ensure at least one matching doctor with correct regNo is returned
    matching_doctors = [doc for doc in results if doc.get("regNo") == doctor_data["regNo"]]
    assert len(matching_doctors) > 0
