# ==========================================
# Stage 1: Build
# ==========================================
FROM node:20-alpine AS builder

WORKDIR /build

# Install dependencies (leverage Docker cache)
COPY package.json package-lock.json ./
RUN npm ci

# Copy source code and build
COPY . .
RUN npm run build

# ==========================================
# Stage 2: Nginx
# ==========================================
FROM nginx:1.25-alpine

LABEL maintainer="RiverFlow Team"
LABEL description="RiverFlow UI - Vue 3 Frontend"

# Copy built assets
COPY --from=builder /build/dist /usr/share/nginx/html

# Nginx config: serve static files + proxy API to backend
RUN printf '%s\n' \
    'server {' \
    '    listen 80;' \
    '    server_name localhost;' \
    '' \
    '    root /usr/share/nginx/html;' \
    '    index index.html;' \
    '' \
    '    # Frontend routes (Vue Router history mode)' \
    '    location / {' \
    '        try_files $uri $uri/ /index.html;' \
    '    }' \
    '' \
    '    # API proxy to riverflow-admin service' \
    '    location /api/ {' \
    '        proxy_pass http://riverflow-admin:8080/;' \
    '        proxy_set_header Host $host;' \
    '        proxy_set_header X-Real-IP $remote_addr;' \
    '        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;' \
    '        proxy_set_header X-Forwarded-Proto $scheme;' \
    '    }' \
    '}' \
    > /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
