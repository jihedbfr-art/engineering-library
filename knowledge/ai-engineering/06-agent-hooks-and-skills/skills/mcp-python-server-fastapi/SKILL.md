---
name: "mcp-python-server-fastapi"
description: "Template and instructions for deploying a custom Model Context Protocol (MCP) server using Python and FastAPI."
format: "v2"
---

# FastAPI MCP Python Server

The Model Context Protocol (MCP) allows AI agents to securely interact with local resources and external APIs. This skill provides a standardized blueprint for spinning up a custom MCP server in Python using FastAPI, leveraging SSE (Server-Sent Events) for bi-directional communication.

## Prerequisites

- Python 3.10 or higher installed on the host machine.
- The `mcp` and `fastapi` Python packages installed.
- Basic understanding of JSON-Schema for tool definition.

## Usage

1. Create a new directory for the MCP server.
2. Initialize a virtual environment and install dependencies: `pip install mcp fastapi uvicorn`.
3. Create a `server.py` file based on the standard MCP lifecycle hooks.
4. Define your custom tools using Python decorators provided by the MCP SDK.
5. Launch the server using Uvicorn on a specific port.
6. Configure the agent's MCP client configuration to point to the SSE endpoint of this server.

## Inputs

- `port` (Integer): The port on which to run the FastAPI server (e.g., 8000).
- `tools_definition` (JSON): The specification of the custom tools the server will expose.

## Outputs

- A running FastAPI server process listening for MCP connections.
- The server will expose two primary endpoints: `/sse` for establishing the event stream and `/messages` for receiving JSON-RPC tool invocation requests from the agent.
