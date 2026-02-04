# Greedy Bot Dockerfile
FROM python:3.12-alpine

WORKDIR /app

# Copy requirements
COPY requirements.txt ./

# Install dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Copy source code
COPY . .

# Expose port
EXPOSE 3001

# Run the bot
CMD ["python", "main.py"]
