def pytest_addoption(parser):
    parser.addoption("--prod-url", action="store", default="http://localhost:8080", help="prod server base URL")

import pytest

@pytest.fixture
def prod_url(request):
    return request.config.getoption("--prod-url")
