import os
import requests

def test_healthcheck():
    base_url = os.getenv("DEV_URL", "http://localhost:8080")
    response = requests.get(f"{base_url}/health")
    assert response.status_code == 200
