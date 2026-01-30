# claude

Experiments with Claude Code

## Setup

Docker / Docker Model Runner (TODO)

NVidia Driver (TODO)
CUDA (TODO)

### Check to see what models are available to you (already pulled)

```bash
docker model list
```

### Check to see what models are running

```bash
docker model ps
```

### Set up the model

```bash
docker model pull ai/gemma3
docker model configure --context-size 32768 ai/gemma3
ANTHROPIC_BASE_URL=http://localhost:12434 claude --model ai/gemma3
```

### Test the model running `docker model` and interacting with the prompt

```bash
docker model run ai/gemma3
```

### View requests

```bash
docker model requests --model ai/gemma3 | jq
```


## Further reading

- https://www.docker.com/blog/run-claude-code-locally-docker-model-runner/

