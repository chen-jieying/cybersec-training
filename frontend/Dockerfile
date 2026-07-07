FROM node:18 AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN node ./node_modules/vite/bin/vite.js build

FROM nginx:1.25-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist /usr/share/nginx/html/cybersec-training
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
