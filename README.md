# 📸 Vibe Snapshot

> **The Ultimate Pixel-Perfect Frontend Competition Platform**
>
> Upload a golden design, let teams recreate it, compare screenshots with AI-powered image diffing, and rank participants by similarity score.

![Vibe Snapshot](https://img.shields.io/badge/Stack-React%20%2B%20Node.js-7c3aed?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-00f5c4?style=flat-square)

---

## ✨ Features

| Feature | Description |
|---|---|
| 🖼️ Golden Image | Admin uploads the reference design screenshot |
| ⏱️ Round Timer | Study phase (analysis) → Build phase → Locked |
| 📤 Team Submissions | Drag & drop screenshot upload with GitHub link |
| 🔬 Image Comparison | `pixelmatch` pixel-level diff engine |
| 📊 Similarity Score | Percentage match with diff pixel count |
| 🎯 Diff Visualizer | Side-by-side, diff-only, and opacity overlay modes |
| 🏆 Live Leaderboard | Auto-sorted by highest match %, auto-refreshes |
| 🛡️ Admin Dashboard | Upload, round control, submission management |
| 🌑 Dark UI | Cyberpunk-inspired dark theme with animations |

---

## 🗂️ Project Structure

```
vibe-snapshot/
├── backend/               # Node.js + Express API
│   ├── routes/
│   │   ├── admin.js       # Golden image upload, submission mgmt
│   │   ├── submissions.js # Team submissions + image comparison
│   │   └── round.js       # Round timer and phase management
│   ├── middleware/
│   │   ├── imageCompare.js # pixelmatch comparison engine
│   │   └── store.js        # JSON file-based data persistence
│   ├── uploads/            # Stored images (gitignored)
│   │   ├── golden/         # Admin's golden reference image
│   │   ├── submissions/    # Team screenshot uploads
│   │   └── diffs/          # Generated diff images
│   └── server.js
│
├── frontend/              # React + Vite + Tailwind
│   ├── src/
│   │   ├── components/
│   │   │   ├── Navbar.jsx
│   │   │   ├── DropZone.jsx      # Drag & drop upload
│   │   │   ├── ScoreCircle.jsx   # SVG animated score ring
│   │   │   ├── RoundTimer.jsx    # Countdown timer with phases
│   │   │   └── DiffViewer.jsx    # 3-mode image comparison UI
│   │   ├── pages/
│   │   │   ├── HomePage.jsx
│   │   │   ├── SubmitPage.jsx
│   │   │   ├── LeaderboardPage.jsx
│   │   │   └── AdminPage.jsx
│   │   └── utils/api.js          # Axios API client
│   └── ...config files
│
├── package.json           # Root scripts with concurrently
└── README.md
```

---

## 🚀 Running Locally

### Prerequisites

- **Node.js 18+** — [Download](https://nodejs.org)
- **npm 9+** (comes with Node.js)

### Step 1 — Clone

```bash
git clone https://github.com/YOUR_USERNAME/vibe-snapshot.git
cd vibe-snapshot
```

### Step 2 — Install all dependencies

```bash
# From root — installs root + backend + frontend
npm run install:all
```

Or manually:

```bash
npm install
cd backend && npm install
cd ../frontend && npm install
```

### Step 3 — Configure environment (optional)

```bash
# Frontend
cp frontend/.env.example frontend/.env.local
# Edit VITE_ADMIN_PASSWORD to your desired password (default: admin123)

# Backend
cp backend/.env.example backend/.env
```

### Step 4 — Run

```bash
# From root — runs backend (port 3001) + frontend (port 5173) simultaneously
npm run dev
```

Then open: **http://localhost:5173**

---

## 🎮 How to Use

### As Admin

1. Go to `/admin` — password: `admin123` (or your custom password)
2. **Upload Golden Image** in the "Golden Image" tab
3. Set phase durations (analysis + build minutes)
4. Click **Start Round**
5. Watch submissions come in on the **Submissions** tab
6. Click any submission to view the diff comparison
7. Lock submissions when time is up

### As Participant

1. Go to `/submit`
2. During **Study Phase** — view the golden image carefully
3. During **Build Phase** — recreate the design as a webpage
4. Take a full-page screenshot of your result
5. Enter your team name, upload screenshot, optionally add GitHub URL
6. Submit → see your similarity score + visual diff instantly
7. Check `/leaderboard` to see your rank

---

## 🏗️ Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React 18, Vite, Tailwind CSS |
| Routing | React Router v6 |
| File Upload UI | react-dropzone |
| Notifications | react-hot-toast |
| Icons | lucide-react |
| Backend | Node.js, Express |
| Image Comparison | **pixelmatch** (pixel-level diff) |
| Image Processing | **Jimp** (resize/normalize) |
| PNG Processing | **pngjs** (raw pixel access) |
| File Uploads | Multer |
| Data Storage | JSON file (zero-dependency persistence) |

---

## 🌐 Deployment

### Option A — Render (Recommended, Free)

**Backend:**
1. Push code to GitHub
2. Go to [render.com](https://render.com) → New Web Service
3. Connect your repo, set root to `/backend`
4. Build command: `npm install`
5. Start command: `node server.js`
6. Add env var: `PORT=10000`
7. Note your backend URL: `https://vibe-snapshot-api.onrender.com`

**Frontend:**
1. New Static Site on Render
2. Root: `/frontend`, Build: `npm run build`, Publish: `dist`
3. Add env var: `VITE_API_URL=https://vibe-snapshot-api.onrender.com`
4. Add env var: `VITE_ADMIN_PASSWORD=your_secret`

### Option B — Railway (Backend) + Vercel (Frontend)

**Backend on Railway:**
```bash
# Install Railway CLI
npm i -g @railway/cli
cd backend
railway login
railway init
railway up
```

**Frontend on Vercel:**
```bash
cd frontend
# Set VITE_API_URL to your Railway backend URL
npx vercel --prod
```

### Option C — Netlify (Frontend only) + Railway (Backend)

**Frontend on Netlify:**
1. `cd frontend && npm run build`
2. Drag `dist/` to [netlify.com/drop](https://netlify.com/drop)
3. Or connect GitHub repo with build settings:
   - Base: `frontend`
   - Build: `npm run build`
   - Publish: `dist`
4. Set env vars in Netlify dashboard

### Option D — VPS / Self-hosted

```bash
# On your server
git clone https://github.com/YOUR_USERNAME/vibe-snapshot
cd vibe-snapshot
npm run install:all

# Build frontend
cd frontend && npm run build

# Serve frontend from backend (add this to backend/server.js)
# app.use(express.static(path.join(__dirname, '../frontend/dist')));

# Run with PM2
npm install -g pm2
cd ../backend
pm2 start server.js --name vibe-snapshot
pm2 save
```

---

## 🔧 Configuration

| Variable | Default | Description |
|---|---|---|
| `VITE_ADMIN_PASSWORD` | `admin123` | Admin dashboard password |
| `VITE_API_URL` | `` (empty) | Backend URL (empty = use Vite proxy) |
| `PORT` | `3001` | Backend server port |

---

## 📸 Screenshot Tips for Participants

For best comparison results:
- Use **full-page screenshots** (not just viewport)
- Match the **viewport width** to the golden image (check with admin)
- Tools: Chrome DevTools → `Ctrl+Shift+P` → "Capture full size screenshot"
- Or use browser extensions like "GoFullPage"

---

## 🛠️ Development Notes

- Data persists in `backend/data.json` (gitignored in production)
- Uploaded images stored in `backend/uploads/` (gitignored)
- Image comparison resizes both images to the golden image's dimensions before diffing
- Re-submitting with the same team name **replaces** the previous submission
- The admin password is frontend-only validation — for production, add server-side auth

---

## 📄 License

MIT — use freely for hackathons, workshops, and competitions.

---

Made with 💜 for frontend hackathons
