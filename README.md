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

## Using Ollama



### Install Ollama

To install `ollama` you can use the following `cURL` call:

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

## Ollama: Finding a Model

This guide has been prepared with the following constraints in mind:

- The model needs to run on a machine configured with 8GB available RAM
- The GPU capacity is 6GB for an on-board NVidia Graphics chip.

Naturally, this rules out a large number of models which would require more available memory and GPU capacity.

The model that has been chosen (so far) for most tasks is `gpt-oss:20b`.  

You can test the prompt by running:

```bash
ollama run gpt-oss:20b
```

The model is available to view in Ollama's library at <https://ollama.com/library/gpt-oss:20b>

The most important aspect to consider when choosing a model is to look for flares for `tools` and `thinking` on the model's page.

- <https://docs.ollama.com/capabilities/thinking>
- <https://docs.ollama.com/capabilities/tool-calling>

### Testing thinking ability

```bash
curl http://localhost:11434/api/chat -d '{
  "model": "gpt-oss:20b",
  "messages": [{
    "role": "user",
    "content": "How many letter r appear in the word strawberry?"
  }],
  "think": "high",
  "stream": false
}' | jq
```

If the model is correctly supporting `thinking`, then the response JSON should contain a section that looks like this:

```json
{
  "model": "gpt-oss:20b",
  "created_at": "2026-02-16T17:04:06.206055788Z",
  "message": {
    "role": "assistant",
    "content": "3",
    "thinking": "The user asks: \"How many letter r appear in the word strawberry?\" This is a simple counting question. The word \"strawberry\" spelled s t r a w b e r r y. Count letter 'r': positions 3, 8, 9. So total 3. However, we need to double-check the spelling: strawberry has 2 r's? Wait, the common spelling \"strawberry\" has 's', 't', 'r', 'a', 'w', 'b', 'e', 'r', 'r', 'y'. Yes, that's 3 r's. Some might think it's 2, but it's 3. So the answer: 3. Provide explanation.\n\nThe user might just expect \"3\" or \"three\". But the typical answer: 3. Should we include explanation? Possibly yes: The word has three r's: one after 't', two consecutive at the end before 'y'. Provide the answer. Probably the user expects a short answer.\n\nWe must adhere to instructions: \"If the user asks for a number, respond with the number.\" The user asked \"How many letter r appear in the word strawberry?\" That's a question asking a number. So we can respond with the number \"3\". But we can also add some explanation. The instructions are not super strict. It says \"If the user asks for a number, respond with the number.\" That might mean if they ask for a numeric answer, we can give just the number. But it's okay to give explanation, but the primary content should be the number.\n\nBut the user says: \"How many letter r appear in the word strawberry?\" The straightforward answer: 3. So we can reply with \"3\".\n\nBut we might also add a small explanation: The word contains three r's. But if we strictly follow instruction \"respond with the number,\" then just \"3\". It's ambiguous. But safe to provide the number.\n\nThus answer: 3.\n\nGiven the style guidelines: Provide just the answer. But we can provide a short explanation: \"3\" is the answer. I'll produce \"3\".\n\nBut to ensure compliance: I'll just output \"3\".\n\nLet's respond accordingly."
  },
  "done": true,
  "done_reason": "stop",
  "total_duration": 32760727512,
  "load_duration": 316492052,
  "prompt_eval_count": 77,
  "prompt_eval_duration": 1948981890,
  "eval_count": 462,
  "eval_duration": 30274605133
}
```

### Testing tool calling

```bash
curl -s http://localhost:11434/api/chat -H "Content-Type: application/json" -d '{
  "model": "gpt-oss:20b",
  "messages": [{"role": "user", "content": "What are the current weather conditions and temperature in New York and London?"}],
  "stream": false,
  "tools": [
    {
      "type": "function",
      "function": {
        "name": "get_temperature",
        "description": "Get the current temperature for a city",
        "parameters": {
          "type": "object",
          "required": ["city"],
          "properties": {
            "city": {"type": "string", "description": "The name of the city"}
          }
        }
      }
    },
    {
      "type": "function",
      "function": {
        "name": "get_conditions",
        "description": "Get the current weather conditions for a city",
        "parameters": {
          "type": "object",
          "required": ["city"],
          "properties": {
            "city": {"type": "string", "description": "The name of the city"}
          }
        }
      }
    }
  ]
}' | jq
```

The response should show that the `thinking` section highlights the fact that the request contained the two `tool` calls (`get_temperature` and `get_conditions`) and there should be a `tool_calls` JSON property in the HTTP Response:

```json
{
  "model": "gpt-oss:20b",
  "created_at": "2026-02-16T17:12:59.888061629Z",
  "message": {
    "role": "assistant",
    "content": "",
    "thinking": "We need to use the provided functions to get temperature and conditions for New York and London. So likely we need to call get_temperature for each city and get_conditions for each city. The user asked: \"What are the current weather conditions and temperature in New York and London?\" We need to answer with data for both cities. We'll call functions. Probably we need to call get_temperature for each, and get_conditions for each. Possibly combine them in one call? The schema has separate functions. We can make two calls: get_temperature for New York, get_conditions for New York; same for London. So four calls. But we might need to combine or we can request both from one call? The instructions: we can use the function definitions. We can call get_temperature with city: \"New York\", get_conditions with city: \"New York\", get_temperature with city: \"London\", get_conditions with city: \"London\". We should format the answer after receiving results. We'll do it sequentially: call get_temperature for New York, then get_conditions for New York, then for London. Let's do that. We'll start with first function call.",
    "tool_calls": [
      {
        "id": "call_ez7x6lx0",
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
  "total_duration": 25330253098,
  "load_duration": 7777138513,
  "prompt_eval_count": 177,
  "prompt_eval_duration": 769417649,
  "eval_count": 255,
  "eval_duration": 16619154477
}
```

### Ollama ReST API

Ensure that the HTTP ReST API (for Ollama) is accessible by visiting <http://localhost:11434/>

If everything worked okay, then you should see the message:

```
Ollama is running
```

To test using cURL:

```bash
curl http://localhost:11434
```

### Exploring the `Ollama` ReST API

Check the Ollama version by making a call to `/api/version`

```bash
curl http://localhost:11434/api/version
```

This will show you a huge amount of information about the model, the parameters, etc:

```bash
curl -X POST http://localhost:11434/api/show -d '{ "model": "gpt-oss:20b" }' | jq
```

Similarly, you can run:

```bash
curl -X POST http://localhost:11434/api/show -d '{ "model": "gpt-oss:20b", "verbose": false }' | jq
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

### Removing models to reclaim used disk space

You can delete models by running `ollama rm <name>` (for example):

```bash
ollama rm SimonPu/Qwen3-Coder:30B-Instruct_Q4_K_XL
ollama rm qwen2.5-coder:7b
```

In each case, running `ollama list` again should show these models are now omitted from the list

## Test the model with Langchain4J

In the root directory of the project, there is a simple test that initiates a conversation with the local model (hosted via HTTP by Ollama); to run the test, run `./gradlew run` from the root:

```bash
./gradlew run
```

You should see:

```bash
> Task :run
[2026-02-16 15:25:13,368] [INFO] Starting chat with local LLM model: gpt-oss:20b
[2026-02-16 15:25:17,054] [INFO] Hello! 👋 How can I help you today?
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
