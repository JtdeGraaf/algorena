# Frontend Dockerfile (Development)
FROM oven/bun:1-alpine

WORKDIR /app

# Copy package files
COPY package.json bun.lock* ./

# Install dependencies
RUN bun install

# Copy source code
COPY . .

# Expose Vite dev server port
EXPOSE 5173

# Run dev server with host flag for Docker
CMD ["bun", "run", "dev", "--host"]
