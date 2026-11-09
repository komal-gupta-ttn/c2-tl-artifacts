# MCP Servers — Deterministic Multi-Tool Chain (Option B)

> Alternative to RAG assistant. Build 2+ MCP servers, chain tools in a fixed sequence.

## Servers

| Server | Tools | Purpose |
|--------|-------|---------|
| `server_a.py` | `lookup`, `validate` | Look up records, validate input |
| `server_b.py` | `transform`, `enrich` | Transform data, enrich from external |
| `server_c.py` | `notify`, `audit` | Send notifications, write audit log |

## Chain (fixed sequence — NOT autonomous)

```
request → server_a.lookup → server_a.validate → server_b.transform → server_b.enrich → server_c.notify → server_c.audit → result
```

## Tech

- Spring AI `@Tool` (Java) **or** FastMCP (Python)
- Error handling: fallback when any tool fails mid-chain
- Debugging: inspect tool request/response, diagnose schema mismatches

## Not

- ❌ No autonomous agent loop
- ❌ No multi-agent orchestration
- ✅ Deterministic, fixed sequence
