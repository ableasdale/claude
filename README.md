# Using Claude Code (and langchain4j) to work with Local LLMs

Experiments working with Claude Code with locally hosted LLMs, including Docker Model Runner and Ollama

## Requirements

- Laptop with at least 8GB RAM and some GPU capacity
- Ubuntu 25.10 (steps should be similar with other recent Ubuntu releases)
- Ollama (steps tested with 0.15.2)

## Setup

Docker / Docker Model Runner (TODO - note: at the time this guide was written there were still some issues with Docker Model Runner on Linux)

### NVidia Linux Drivers and Cuda

The first step is going to be to find the best driver for your GPU:

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

Note that the Docker packages (I think?) are required in either case for Docker or 

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

You can verify the outcome of all prior steps by running `nvidia-smi`:

```bash
nvidia-smi
```

You should see something like this in the terminal output:

```bash
❯ nvidia-smi
Mon Feb 16 14:22:53 2026       
+-----------------------------------------------------------------------------------------+
| NVIDIA-SMI 590.48.01              Driver Version: 590.48.01      CUDA Version: 13.1     |
+-----------------------------------------+------------------------+----------------------+
| GPU  Name                 Persistence-M | Bus-Id          Disp.A | Volatile Uncorr. ECC |
| Fan  Temp   Perf          Pwr:Usage/Cap |           Memory-Usage | GPU-Util  Compute M. |
|                                         |                        |               MIG M. |
|=========================================+========================+======================|
|   0  NVIDIA GeForce RTX 3050 ...    Off |   00000000:01:00.0 Off |                  N/A |
| N/A   41C    P0             11W /   75W |       8MiB /   6144MiB |      6%      Default |
|                                         |                        |                  N/A |
+-----------------------------------------+------------------------+----------------------+

+-----------------------------------------------------------------------------------------+
| Processes:                                                                              |
|  GPU   GI   CI              PID   Type   Process name                        GPU Memory |
|        ID   ID                                                               Usage      |
|=========================================================================================|
|    0   N/A  N/A            9825      G   /usr/bin/gnome-shell                      1MiB |
+-----------------------------------------------------------------------------------------+
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

### Install it

```bash
curl -fsSL https://ollama.com/install.sh | sh
```


Verify the installation by running `ollama -v`:

```bash
ollama -v
```

You'll see output like this:

```bash
ollama version is 0.15.2
```



### Run an LLM locally

```bash
ollama run gemma3:1b
```

```bash
ollama serve
```

### Managing your LLMs in `Ollama`

Run `ollama list` to get a full list of all available models:

```bash
ollama list
```

You will see a list like this:

```
NAME                                        ID              SIZE      MODIFIED    
SimonPu/Qwen3-Coder:30B-Instruct_Q4_K_XL    fa6d1415a672    17 GB     2 weeks ago    
qwen2.5-coder:7b                            dae161e27b0e    4.7 GB    2 weeks ago    
gpt-oss:20b                                 17052f91a42e    13 GB     2 weeks ago    
gemma3:1b                                   8648f39daa8f    815 MB    2 weeks ago  
```

## Test with Claude Code

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

Tried and failed:

TODO - would be interesting to test this model when more RAM is available to me

```
ollama run SimonPu/Qwen3-Coder:30B-Instruct_Q4_K_XL (needs 14GB RAM :()
```

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

Doesn't seem to be working...


## Jvm / Gradle

```bash
./gradlew wrapper --gradle-version 8.1.1 
```

## Further reading

- https://www.docker.com/blog/run-claude-code-locally-docker-model-runner/
- https://www.docker.com/blog/run-llms-locally/
- https://ollama.com/blog/openai-compatibility
- https://dzone.com/articles/ollama-ubuntu-local-llm-setup
- https://code.claude.com/docs/en/sub-agents
- https://artificialanalysis.ai/models/open-source
