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
ORCHESTRATOR_REDIRECT_URL | The redirect url for the callback from IPV engine | `http://localhost:8081/orchestrator/callback` |
ORCHESTRATOR_PORT | The port number the orchestrator should run on | `8081` |

## Running locally

To run locally, you can run the startup script.
```shell
./startup.sh
```
This will first build the project with gradle,
and after it will attempt to run it.

Once started, visit [http://localhost:8081/](http://localhost:8081/).
By clicking on the _Prove your identity_ button, you will be redirected
to the IPV engine, which will redirect back to the callback URL.

After, the orchestrator sends some HTTP requests to exchange the code
for a token and to fetch the user info via back-channels.