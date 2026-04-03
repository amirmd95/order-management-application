# Frontend

This Angular app is the control room for the Order Management Application. It talks to the product, inventory, and order microservices through `proxy.conf.json`.

## Run on your host

Use a supported Node release first. Angular `21.x` supports Node `20.19+`, `22.12+`, or `24+`.

```bash
nvm use 24
npm install
npm start
```

The app runs on `http://localhost:4200`.

## Run in Docker

From the repository root:

```bash
docker compose up --build frontend
```

That uses the Node `24` development container and keeps frontend dependencies inside Docker instead of your host `node_modules`.

## Build

```bash
npm run build
```

If your host Node version is unsupported, use the Docker workflow instead.
