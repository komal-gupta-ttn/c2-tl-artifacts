# MCP Client — Deterministic Chain Orchestrator

Connects to 2+ MCP servers and runs requests through a fixed tool sequence.

```
client → [server_a.lookup] → [server_a.validate] → [server_b.transform] → [server_b.enrich] → result
```

Error handling: if any tool fails mid-chain, fall back to degraded result or retry.
