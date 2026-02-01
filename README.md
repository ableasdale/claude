# claude

Experiments with Claude Code

## Setup

Docker / Docker Model Runner (TODO)

NVidia Driver (TODO)

### NVidia Linux Drivers

Find the best driver for your GPU:

```bash
sudo ubuntu-drivers devices
```

You should see something like:

```bash
vendor   : NVIDIA Corporation
model    : GA107BM / GN20-P0-R-K2 [GeForce RTX 3050 6GB Laptop GPU]
driver   : nvidia-driver-590-open - distro non-free recommended
```

Install the driver:

```bash
sudo apt install nvidia-driver-590-open
```



### Install supporting packages for Docker and NVidia GPUs

```bash
sudo apt-get install -y nvidia-container-toolkit
sudo apt install nvidia-docker2
```

### Install and Configure CUDA Drivers to work with Docker

```bash
sudo apt-get -y install cuda-drivers
sudo nvidia-ctk runtime configure --runtime=docker
sudo systemctl restart docker
```

### Confirm that everything is set up for the GPU

```bash
nvidia-smi
```

You should see something like this:

```bash
NVIDIA-SMI 590.48.01              Driver Version: 590.48.01      CUDA Version: 13.1 
```

### Install Claude Code

```bash
curl -fsSL https://claude.ai/install.sh | bash
```

### Get the Model Runner Started

```bash
docker model install-runner
docker desktop enable model-runner
```

View the API Endpoint on: <http://localhost:12434/>

You should see the message:

```bash
Docker Model Runner is running
```

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


## Testing with Ollama

### Intall it

```bash
curl -fsSL https://ollama.com/install.sh | sh
```

```bash
ollama run gemma3:1b
```

```bash
ollama serve
```

Test with Claude Code

```bash
ANTHROPIC_BASE_URL=http://localhost:11434 claude --model gemma3:1b
```

This fails because the model doesn't support tools, so let's try this:

```bash
ollama run gpt-oss:20b
```

Not enough RAM in my cheap laptop for that model to run :(

```bash
ollama run qwen2.5-coder:7b
```

This runs for me

Now we can test with Claude Code:

```bash
ANTHROPIC_BASE_URL=http://localhost:11434 claude --model qwen2.5-coder:7b
```


## Debugging notes

```bash
curl -s http://localhost:11434/api/chat -H "Content-Type: application/json" -d '{ "model": "qwen2.5-coder:7b", "stream": false, "messages": [{"role":"user","content":"What is the temperature in New York? Use the tool."}], "tools": [{ "type":"function", "function":{ "name":"get_temperature", "description":"Get the current temperature for a city", "parameters":{ "type":"object", "required":["city"], "properties":{"city":{"type":"string"}} } } }] }' | jq
```

The response doesn't appear to be showing any tool calls, see: https://www.reddit.com/r/LocalLLaMA/comments/1pqquuf/qwen_25_coder_ollama_litellm_claude_code/ for more details.


ollama run SimonPu/Qwen3-Coder:30B-Instruct_Q4_K_XL (needs 14GB RAM :()

try also glm-4.7-flash (apparently needs 16GB RAM :()

### Putting it all together

This set of steps actually works for me!

```bash
// OLLAMA_CONTEXT_LENGTH=65535 ollama run gpt-oss:20b
OLLAMA_CONTEXT_LENGTH=65535 ollama serve
ANTHROPIC_AUTH_TOKEN=ollama
ANTHROPIC_BASE_URL=http://localhost:11434 claude --model gpt-oss:20b
```

Testing

```bash
curl -s http://localhost:11434/v1/chat/completions -H "Content-Type: application/json" -d '{ "model": "qwen2.5-coder:7b", "stream": false, "messages": [{"role":"user","content":"What is the temperature in New York? Use the tool."}], "tools": [{ "type":"function", "function":{ "name":"get_temperature", "description":"Get the current temperature for a city", "parameters":{ "type":"object", "required":["city"], "properties":{"city":{"type":"string"}} } } }] }' | jq
```

```bash
curl -s http://localhost:11434/api/chat -H "Content-Type: application/json" -d '{ "model": "gpt-oss:20b", "stream": false, "messages": [{"role":"user","content":"What is the temperature in New York? Use the tool."}], "tools": [{ "type":"function", "function":{ "name":"get_temperature", "description":"Get the current temperature for a city", "parameters":{ "type":"object", "required":["city"], "properties":{"city":{"type":"string"}} } } }] }' | jq
```

GPT-OSS 20 does seem to respond correctly:

```bash
{
  "model": "gpt-oss:20b",
  "created_at": "2026-02-01T16:17:38.855646186Z",
  "message": {
    "role": "assistant",
    "content": "",
    "thinking": "User wants temperature in New York, we need to call the function get_temperature with city \"New York\".",
    "tool_calls": [
      {
        "id": "call_28nao01m",
        "function": {
          "index": 0,
          "name": "get_temperature",
          "arguments": {
            "city": "New York"
          }
        }
      }
    ]
  },
  "done": true,
  "done_reason": "stop",
  "total_duration": 3766415882,
  "load_duration": 160548370,
  "prompt_eval_count": 134,
  "prompt_eval_duration": 646967493,
  "eval_count": 46,
  "eval_duration": 2937109230
}
```

See what models you have available 

```bash
ollama list
```

## How can we confirm that the GPU is also being used?

Open a separate terminal session and run:

```bash
ollama ps
```

You should see:

```bash
NAME           ID              SIZE     PROCESSOR          CONTEXT    UNTIL              
gpt-oss:20b    17052f91a42e    14 GB    64%/36% CPU/GPU    4096       2 minutes from now  
```

Note that the output from `nvidia-smi` should also give you some hints that Ollama is making use of your GPU:

```bash
+-----------------------------------------------------------------------------------------+
| Processes:                                                                              |
|  GPU   GI   CI              PID   Type   Process name                        GPU Memory |
|        ID   ID                                                               Usage      |
|=========================================================================================|
|    0   N/A  N/A            4462      G   /usr/bin/gnome-shell                      1MiB |
|    0   N/A  N/A           78525      C   /usr/local/bin/ollama                  5014MiB |
+-----------------------------------------------------------------------------------------+
```

## Skills

```bash
/explain-code src/main/java/OllamaTest.java
```


## Further reading

- https://www.docker.com/blog/run-claude-code-locally-docker-model-runner/
- https://www.docker.com/blog/run-llms-locally/
- https://ollama.com/blog/openai-compatibility
- https://dzone.com/articles/ollama-ubuntu-local-llm-setup
- https://code.claude.com/docs/en/sub-agents
