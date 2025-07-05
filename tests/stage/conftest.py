def pytest_addoption(parser):
    parser.addoption("--stage-url", action="store", default="http://localhost:8080", help="stage server base URL")

import pytest

@pytest.fixture
def stage_url(request):
    return request.config.getoption("--stage-url")
