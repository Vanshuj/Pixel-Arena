# 📸 Vibe Snapshot

> **The Ultimate Pixel-Perfect Frontend Competition Platform**
>
> Upload a golden design, let teams recreate it, compare screenshots with pixel-level image diffing, and rank participants by similarity score.
> Live at — https://pixel-arena-1.onrender.com/

![Vibe Snapshot](https://img.shields.io/badge/Stack-React%20%2B%20Spring%20Boot%20%2B%20MongoDB-7c3aed?style=flat-square)
![Java](https://img.shields.io/badge/Java-17%2B-orange?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-00f5c4?style=flat-square)

---

## ✨ Features

| Feature | Description |
|---|---|
| 🖼️ Golden Image | Admin uploads the reference design screenshot |
| ⏱️ Round Timer | Study phase (analysis) → Build phase → Locked |
| 📤 Team Submissions | Drag & drop screenshot upload with GitHub link |
| 🔬 Image Comparison | Pixel-level `pixelmatch` diff engine (Java & Node.js) |
| 📊 Similarity Score | Percentage match with matched/diff pixel counts |
| 🎯 Diff Visualizer | Side-by-side, diff-only, and opacity overlay modes |
| 🏆 Live Leaderboard | Auto-sorted by highest match %, auto-refreshes |
| 🛡️ Admin Dashboard | Upload, round control, submission management |
| 🗄️ MongoDB Persistence | Stores round state, golden metadata, and submissions in MongoDB |
| 🌑 Dark UI | Cyberpunk-inspired dark theme with animations |

---

## 🗂️ Project Structure

```
Pixel-Arena-main/
├── backend-java/                  # Java Spring Boot + MongoDB API (primary)
│   ├── src/main/java/com/vibesnapshot/
│   │   ├── VibeSnapshotApplication.java
│   │   ├── config/
│   │   │   ├── WebConfig.java               # CORS + /uploads/** static serving
│   │   │   └── GlobalExceptionHandler.java  # JSON error responses & upload limits
│   │   ├── controller/
│   │   │   ├── AdminController.java         # Golden image upload/delete, submission mgmt, reset
│   │   │   ├── RoundController.java         # Round timer and phase management
│   │   │   ├── SubmissionController.java    # Team submissions + leaderboard + comparison
│   │   │   └── HealthController.java        # Health check (/api/health)
│   │   ├── dto/
│   │   │   └── LeaderboardEntry.java        # Ranked leaderboard response DTO
│   │   ├── model/
│   │   │   ├── ArenaState.java              # MongoDB arena_state document
│   │   │   ├── GoldenImage.java             # Golden reference metadata
│   │   │   ├── RoundState.java              # Active round timing & phase state
│   │   │   └── Submission.java              # MongoDB submissions document
│   │   ├── repository/
│   │   │   ├── ArenaStateRepository.java
│   │   │   └── SubmissionRepository.java
│   │   └── service/
│   │       ├── ArenaService.java            # Synchronized round & arena state service
│   │       ├── ImageCompareService.java     # Java pixelmatch comparison engine
│   │       └── StorageService.java          # Local disk storage for golden/submissions/diffs
│   ├── src/main/resources/
│   │   └── application.properties           # Port, MongoDB URI, multipart & upload config
│   ├── src/test/java/com/vibesnapshot/      # Unit & MockMvc API contract tests
│   ├── uploads/                             # Stored images (submissions/ & diffs/ gitignored)
│   ├── .env.example
│   ├── README.md
│   └── pom.xml
│
├── backend/                       # Legacy Node.js + Express backend (preserved)
│   ├── routes/
│   ├── middleware/
│   └── server.js
│
├── frontend/                      # React + Vite + Tailwind
│   ├── src/
│   │   ├── components/
│   │   │   ├── Navbar.jsx
│   │   │   ├── DropZone.jsx                 # Drag & drop upload
│   │   │   ├── ScoreCircle.jsx              # SVG animated score ring
│   │   │   ├── RoundTimer.jsx               # Countdown timer with phases
│   │   │   ├── ProtectedImage.jsx           # Watermarked study-phase viewer
│   │   │   └── DiffViewer.jsx               # 3-mode image comparison UI
│   │   ├── pages/
│   │   │   ├── HomePage.jsx
│   │   │   ├── SubmitPage.jsx
│   │   │   ├── LeaderboardPage.jsx
│   │   │   └── AdminPage.jsx
│   │   └── utils/api.js                     # Axios API client
│   └── ...config files
│
├── package.json                   # Root scripts (Node backend + frontend helper)
└── README.md
```

---

## 🚀 Running Locally (Java Spring Boot Backend)

### Prerequisites

- **Java 17+** (tested with JDK 23)
- **Maven 3.9+** (or cached Maven wrapper)
- **Node.js 18+** & **npm 9+** — [Download](https://nodejs.org)
- **MongoDB** (configure `MONGODB_URI` in `backend-java/.env` or environment variables)

### Step 1 — Clone

```bash
git clone https://github.com/YOUR_USERNAME/vibe-snapshot.git
cd vibe-snapshot
```

### Step 2 — Configure Environment (optional)

```bash
# Backend (Java)
cp backend-java/.env.example backend-java/.env
# Set PORT (default: 3001) and MONGODB_URI if overriding application.properties

# Frontend
cp frontend/.env.example frontend/.env.local
# Edit VITE_ADMIN_PASSWORD to your desired password (default: admin123)
```

### Step 3 — Run the Java Backend (port `3001`)

```bash
cd backend-java
mvn spring-boot:run
```

Or build the executable JAR and run it:

```bash
cd backend-java
mvn clean package
java -jar target/vibe-snapshot-backend-1.0.0.jar
```

### Step 4 — Install & Run the Frontend (port `5173`)

In a second terminal:

```bash
cd frontend
npm install
npm run dev
```

Then open: **http://localhost:5173**

*(The Vite dev server automatically proxies `/api` and `/uploads` requests to `http://localhost:3001`, so the frontend works identically with the Java backend without any UI changes.)*

<details>
<summary>Alternative: Running the legacy Node.js backend</summary>

```bash
npm run install:all
npm run dev
```

</details>

---

## 🧪 Running Backend Tests

```bash
cd backend-java
mvn test
```

Includes unit tests for `ImageCompareService` (identical, contrasting, and oversized image resizing/diffing) and `MockMvc` contract tests for `/api/health`, `/api/admin/*`, `/api/round/*`, and `/api/submissions/*`.

---

## 🎮 How to Use

### As Admin

1. Go to `/admin` — default password: `admin123` (or `VITE_ADMIN_PASSWORD` in `frontend/.env.local`)
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
| Backend | **Java 17+**, **Spring Boot 3.2.5** (Spring Web, Spring Data MongoDB) |
| Database | **MongoDB** (`arena_state` and `submissions` collections) |
| Image Comparison | **Java `pixelmatch` port** (YIQ perceptual diff + anti-aliasing detection) |
| Image Processing | Java `BufferedImage` / `Graphics2D` / `ImageIO` + **TwelveMonkeys WebP** |
| Legacy Backend | Node.js, Express, `jimp`, `pngjs`, `pixelmatch`, `data.json` (in `backend/`) |

---

## 🔌 API Endpoints

Both `backend-java/` and `backend/` expose the exact same REST contract:

- `GET /api/health` — Health check
- `GET /api/admin/golden-image` — Get current golden image metadata
- `POST /api/admin/golden-image` — Upload golden image (`multipart/form-data`, field `image`)
- `DELETE /api/admin/golden-image` — Delete current golden image
- `GET /api/admin/submissions` — List all submissions sorted by similarity
- `DELETE /api/admin/submissions/:id` — Delete a single submission
- `POST /api/admin/reset` — Clear all submissions & reset round (preserves golden image)
- `GET /api/round` — Get round state
- `POST /api/round/start` — Start round (`{ buildDuration, analysisDuration }`)
- `POST /api/round/lock` — Lock submissions
- `POST /api/round/sync` — Synchronize round phase based on elapsed time
- `POST /api/submissions` — Submit team screenshot (`teamName`, `screenshot`, optional `githubUrl`)
- `GET /api/submissions/leaderboard` — Ranked leaderboard + round state
- `GET /api/submissions/team/:teamName` — Lookup a team's submission
- `GET /uploads/**` — Static serving for golden, submission, and diff images

---

## 🔧 Configuration

| Variable | Default | Description |
|---|---|---|
| `PORT` | `3001` | Backend HTTP server port |
| `MONGODB_URI` | `mongodb://localhost:27017/vibe-snapshot` | MongoDB connection string (set in `backend-java/.env`) |
| `MONGODB_DB` | `vibe-snapshot` | MongoDB database name |
| `UPLOAD_DIR` | `uploads` | Directory for `golden/`, `submissions/`, and `diffs/` |
| `VITE_ADMIN_PASSWORD` | `admin123` | Admin dashboard password (frontend) |
| `VITE_API_URL` | `` (empty) | Backend URL (empty = use Vite proxy) |

---

## 🌐 Deployment

### Backend (`backend-java`)

1. Build the JAR:
   ```bash
   cd backend-java
   mvn clean package
   ```
2. Deploy `target/vibe-snapshot-backend-1.0.0.jar` on any Java 17+ host (Render Docker/Web Service, Railway, Fly.io, or VPS) with:
   - `PORT` (e.g., `10000` or `3001`)
   - `MONGODB_URI` (your MongoDB Atlas connection string)
   - `MONGODB_DB=vibe-snapshot`
   - Persistent disk mounted at `uploads/` if images need to survive restarts

### Frontend (`frontend`)

1. Build static assets:
   ```bash
   cd frontend
   npm install && npm run build
   ```
2. Publish `frontend/dist` on Render Static Site, Vercel, or Netlify with:
   - `VITE_API_URL=https://your-java-backend-url`
   - `VITE_ADMIN_PASSWORD=your_secret`

---

## 📸 Screenshot Tips for Participants

For best comparison results:
- Use **full-page screenshots** (not just viewport)
- Match the **viewport width** to the golden image (check with admin)
- Tools: Chrome DevTools → `Ctrl+Shift+P` → "Capture full size screenshot"
- Or use browser extensions like "GoFullPage"

---

## 🛠️ Development Notes

- Round state and golden image metadata are stored in MongoDB (`arena_state` collection); team submissions are stored in `submissions` (indexed case-insensitively by `teamNameLower`).
- Uploaded and generated diff images are stored in `backend-java/uploads/` (`golden/`, `submissions/`, `diffs/`).
- Image comparison resizes both images to the golden image's dimensions (capped at `1280×800`) before diffing.
- Re-submitting with the same team name **replaces** the previous submission (and cleans up the old submission/diff image files).
- The admin password is checked on the frontend (`AdminPage.jsx`) — for hardened production deployments, add server-side authentication on `/api/admin/**` and `/api/round/**`.

---

## 📄 License

MIT — use freely for hackathons, workshops, and competitions.

---

Made with 💜 for frontend hackathons
