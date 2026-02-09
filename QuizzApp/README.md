# NexQuiz – Quiz Battle

**Multiplayer quiz app** in Java: TCP client–server with per-client threads, object serialization over sockets, and synchronized leaderboard (all players must finish before results). Swing GUI with text, image, and audio questions plus local sound feedback—demonstrates concurrency, I/O, and desktop UI in one project.

---

A **multiplayer quiz application** built in Java with a client–server architecture. Players connect to a central server, answer a series of questions (including text, image, and audio), and see a final leaderboard when all players have finished.

---

## Features

- **Multiplayer**: Up to **3 players** connect to the same server; the game waits for all to finish before showing the leaderboard.
- **Question types**:
  - **Text**: Multiple-choice (e.g. “5 + 3 = ?”, “Which one is a fruit?”).
  - **Image choice**: “Click on the capital of France” — options are images (Berlin, Madrid, Paris, Rome).
  - **Audio**: “Who sang this song?” — play a clip and choose from text options.
  - **Image-in-question**: “Who is in this image?” — question includes an image (e.g. Messi), options are text.
- **Feedback**: Correct/wrong sounds (`correct.wav`, `wrong.wav`) and on-screen result messages.
- **Leaderboard**: After all players complete the quiz, each client sees a final leaderboard with names and scores.

---

## Project Structure

```
QuizzApp/
├── client/                    # Quiz client
│   ├── QuizClient.java        # Connects to server, launches GUI
│   ├── QuizClientGUI.java     # Swing UI, game loop, sound/image handling
│   ├── images/                # Option images (berlin, madrid, paris, rome)
│   └── sounds/                # correct.wav, wrong.wav
├── server/                    # Quiz server
│   ├── QuizServer.java        # Listens on port 1234, spawns ClientHandlers
│   ├── ClientHandler.java     # Per-client thread: questions, scoring, leaderboard
│   ├── QuestionBank.java      # (empty – questions defined in ClientHandler)
│   ├── audio/                 # song.wav (for audio question)
│   └── images/                # messi.jpg (for image question)
├── model/                     # Shared domain model
│   ├── Question.java          # Question text, options, correct index, image/audio bytes
│   ├── Player.java            # Name, score
│   ├── Answer.java            # (empty)
│   └── TestModel.java         # Simple test for Question and Player
├── data/
│   └── questions.json         # (reserved for future question data)
└── README.md
```

---

## Requirements

- **Java** (JDK 8 or later; uses Swing and Java Sound).
- Run from the **project root** (`QuizzApp/`) so paths like `client/sounds/`, `server/audio/`, `server/images/` resolve correctly.

---

## How to Run

### 1. Start the server

From the project root:

```bash
cd /path/to/QuizzApp
javac server/*.java model/*.java
java -cp . server.QuizServer
```

You should see: `Server is listening on port 1234`.

### 2. Start clients (up to 3)

In separate terminals, from the project root:

```bash
javac client/*.java model/*.java
java -cp . client.QuizClient
```

- Enter a player name when prompted.
- Answer questions as they appear; correct/wrong feedback and sounds will play.
- When all 3 players have finished, each client sees the **Final Leaderboard** dialog.

---

## Configuration

| Item        | Location              | Value / role                          |
|------------|------------------------|----------------------------------------|
| Server port| `QuizServer.java`      | `1234`                                |
| Server host| `QuizClient.java`      | `localhost`                           |
| Max players| `QuizServer.java`      | `MAX_PLAYERS = 3`                     |
| Questions  | `ClientHandler.java`   | Hardcoded list of `Question` objects  |

---

## Communication (Protocol)

- **Transport**: TCP; one socket per client.
- **Streams**: `ObjectOutputStream` / `ObjectInputStream` for objects; `writeInt`/`readInt` for answer index.
- **Flow** (per client):
  1. Server sends `"Enter your name:"` (String).
  2. Client sends player name (String).
  3. For each question:
     - Server sends a `Question` (serialized, may include `imageBytes`/`audioBytes`).
     - Client sends answer as an `int` (0–3).
     - Server sends result String (e.g. `"Correct!"` or `"Wrong! Correct answer: ..."`).
  4. When a client finishes, it is added to `QuizServer.finishedPlayers`. The handler waits until `finishedPlayers.size() == MAX_PLAYERS`.
  5. Server builds the leaderboard (by score), sends one String (e.g. `"🏆 Final Leaderboard:\n1. ..."`) to each client when they reach that point.

---

## Model Overview

- **`Question`**  
  - `questionText`, `options` (list of 4 strings), `correctIndex` (0–3).  
  - Optional: `imageBytes`, `audioBytes` for image/audio questions.  
  - Implements `Serializable` for sending over the socket.

- **`Player`**  
  - `name`, `score`.  
  - `addPoint()` used when an answer is correct.

- **`TestModel`**  
  - Small test/demo for `Question` and `Player` (no server/client).

---

## Assets

- **Client**: `client/images/` (berlin, madrid, paris, rome for the capital question), `client/sounds/` (correct.wav, wrong.wav).
- **Server**: `server/audio/song.wav`, `server/images/messi.jpg`.  
Paths are relative to the working directory (project root).

---

## Possible Next Steps

- Load questions from `data/questions.json` (or another format) instead of hardcoding in `ClientHandler`.
- Use `QuestionBank.java` to load and serve questions.
- Make server host/port (and optionally max players) configurable via args or a config file.
- Add a timeout per question or per game.
- Support more than 3 players or configurable max players.

---

## License

This project is provided as-is for educational and personal use.
