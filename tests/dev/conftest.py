def pytest_addoption(parser):
    parser.addoption("--dev-url", action="store", default="http://localhost:8080", help="Dev server base URL")

import pytest

@pytest.fixture
def dev_url(request):
    return request.config.getoption("--dev-url")
