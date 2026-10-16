# syntax=docker/dockerfile:1

# ---- Etapa 1: build de la SPA ----
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
ENV CYPRESS_INSTALL_BINARY=0
RUN npm ci --no-audit --no-fund
COPY . .
RUN npm run build

# ---- Etapa 2: nginx con TLS (NFR-SEG-1) ----
FROM nginx:1.29-alpine
RUN apk add --no-cache openssl \
    && mkdir -p /etc/nginx/certs \
    && rm /etc/nginx/conf.d/default.conf
COPY nginx/atlas.conf /etc/nginx/conf.d/atlas.conf
COPY nginx/10-certificado-tls.sh /docker-entrypoint.d/10-certificado-tls.sh
RUN chmod +x /docker-entrypoint.d/10-certificado-tls.sh
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80 443
HEALTHCHECK --interval=30s --timeout=5s --retries=3 CMD wget -qO- --no-check-certificate https://localhost/ >/dev/null || exit 1
