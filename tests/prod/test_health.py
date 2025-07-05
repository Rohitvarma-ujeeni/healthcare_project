import requests
import os

BASE_URL = os.getenv("PRO_URL", "http://localhost:8080")

def test_register_doctor():
    payload = {
        "regNo": "D1001",
        "name": "Dr Rohit",
        "specialization": "Cardiology"
    }
    response = requests.post(f"{BASE_URL}/api/registerDoctor", json=payload)
    assert response.status_code == 200
    assert "Doctor registered successfully" in response.text

def test_search_doctor():
    response = requests.get(f"{BASE_URL}/api/searchDoctor/Dr%20Rohit")
    assert response.status_code == 200
    doctors = response.json()
    assert isinstance(doctors, list)
    assert any(doc["regNo"] == "D1001" for doc in doctors)
