---
format: "v2"
name: "mcp-python-server-fastapi"
title: "FastAPI MCP Python Server Generator"
title_fr: "Générateur de serveur MCP Python avec FastAPI"
description: "Template and instructions for deploying a custom Model Context Protocol (MCP) server using Python and FastAPI."
description_fr: "Modèle et instructions pour déployer un serveur Model Context Protocol (MCP) personnalisé en Python avec FastAPI."
domain: "skills"
tags: [cybersecurity, engineering, best-practices]
maturity: "stable"
audience: ["backend-engineer", "security-engineer", "coding-agent"]
requires: ["bash", "git"]
updated: "2026-08-08"
---

## Prerequisites
- Repository codebase checked out locally.
- Access to Java 17+, Spring Boot 3+, or target framework environment.
- Required build tools (Maven/Gradle) installed.

## Usage
The Model Context Protocol (MCP) allows AI agents to securely interact with local resources and external APIs. This skill provides a standardized blueprint for spinning up a custom MCP server in Python using FastAPI, leveraging SSE (Server-Sent Events) for bi-directional communication.

#### Prerequisites

- Python 3.10 or higher installed on the host machine.
- The `mcp` and `fastapi` Python packages installed.
- Basic understanding of JSON-Schema for tool definition.

#### Usage

1. Create a new directory for the MCP server.
2. Initialize a virtual environment and install dependencies: `pip install mcp fastapi uvicorn`.
3. Create a `server.py` file based on the standard MCP lifecycle hooks.
4. Define your custom tools using Python decorators provided by the MCP SDK.
5. Launch the server using Uvicorn on a specific port.
6. Configure the agent's MCP client configuration to point to the SSE endpoint of this server.

#### Inputs

- `port` (Integer): The port on which to run the FastAPI server (e.g., 8000).
- `tools_definition` (JSON): The specification of the custom tools the server will expose.

#### Outputs

- A running FastAPI server process listening for MCP connections.
- The server will expose two primary endpoints: `/sse` for establishing the event stream and `/messages` for receiving JSON-RPC tool invocation requests from the agent.

## Inputs
- Source code diff or repository path under evaluation.
- Relevant documentation, configuration files, or issue description.

## Outputs
- Structured review findings, action items, or generated markdown artifacts.
