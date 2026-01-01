# Deployment Guide for PrivacyRisk.io

Your project is split into two repositories:
1. **Frontend**: `RISKSCORE_frontend` (React source code)
2. **Backend**: `RISKSCORE_backend` (Java Spring Boot + Bundled Frontend)

Since we bundled the frontend into the backend (via `prepare_for_deploy.ps1`), you only need to deploy the **Backend** repository to get the full website running.

## Deploying to Render.com (Recommended)

Render is free and easiest for Docker/Java apps.

### Step 1: Create a Database
1. Log in to [dashboard.render.com](https://dashboard.render.com).
2. Click **New +** -> **PostgreSQL**.
3. Name it `privacyrisk-db`.
4. Choose the **Free** plan.
5. Click **Create Database**.
6. Wait for it to start, then copy the **Internal Database URL** (it starts with `postgres://...`).

### Step 2: Deploy the App
1. Click **New +** -> **Web Service**.
2. Connect your GitHub repository: `RISKSCORE_backend`.
3. **Runtime**: Select `Docker`.
4. **Region**: Choose the same region as your database.
5. Scroll down to **Environment Variables** and add these:
   - `DB_URL`: Paste the Internal Database URL you copied.
   - `DB_USERNAME`: `privacyrisk_user` (or whatever Render shows in the DB details)
   - `DB_PASSWORD`: (Copy from the DB details)
   - `PORT`: `8080` (Rentder usually sets this automatically, but good to be safe)

   *Note: Render might provide a single `DATABASE_URL` variable. If so, you can use that for `DB_URL`.*

6. Click **Create Web Service**.

### Step 3: Success!
Render will build your Docker container (which builds the Java app) and launch it.
Once live, you will get a URL like `https://privacyrisk-backend.onrender.com`.
Opening this URL will show your **React Frontend**, which communicates with the backend API perfectly.

---

## Updating the Site
1. **Frontend Changes**:
   - Make changes in `frontend/` folder locally.
   - Run `../backend/prepare_for_deploy.ps1` (to update the resources in backend).
   - Commit and push `RISKSCORE_backend`.
   - Render will auto-deploy.
   *Optional: You can also push `RISKSCORE_frontend` just to keep the source code safe.*

2. **Backend Changes**:
   - Make changes in `backend/` folder.
   - Commit and push `RISKSCORE_backend`.
   - Render will auto-deploy.
