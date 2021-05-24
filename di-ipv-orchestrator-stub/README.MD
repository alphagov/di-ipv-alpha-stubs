# di-ipv-orchestrator-stub

A Spark Java orchestrator stub.

## About

The orchestrator performs an OAuth2 authorization code grant flow
to first fetch an authorization code from the IPV engine,
exchange it for an access token, and then use that token
to fetch user information from the IPV engine.

## Environment

Variable | Description | Example Value
--- | --- | --- |
IPV_ENDPOINT | The IPV engine endpoint to call | `http://localhost:3000/` |
IPV_CLIENT_ID | IPV client id | `some_client_id` |
ORCHESTRATOR_REDIRECT_URL | The redirect url for the callback from IPV engine | `http://localhost:8081/orchestrator/codeCallback` |
ORCHESTRATOR_PORT | The port number the orchestrator should run on | `8081` |

## Running locally

To run locally, first build