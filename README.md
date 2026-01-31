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

## Further reading

- https://www.docker.com/blog/run-claude-code-locally-docker-model-runner/
- https://www.docker.com/blog/run-llms-locally/
