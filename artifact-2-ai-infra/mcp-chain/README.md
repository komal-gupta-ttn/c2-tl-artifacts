# MCP Chain — Deterministic Multi-Tool Workflow

> Explicitly **NOT** autonomous looping / multi-agent. Fixed sequence orchestration with error handling & fallback.

## Servers (`servers/`)
2+ minimal MCP servers exposing domain tools (Spring AI `@Tool` or FastMCP).

## Client (`client/`)
MCP client connecting to 2+ servers, orchestrating a fixed sequence:
```
request → Tool A → Tool B → Tool C → result
```

## Error handling
Fallback when a tool call fails mid-chain.

## Debugging
Inspect tool request/response; diagnose schema mismatches.
