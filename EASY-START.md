# Running This Project — The Easy Way

You do **not** need to install Java, Maven, or MySQL. You only need one
program: **Docker Desktop**. It handles everything else for you.

## Step 1 — Install Docker Desktop

Go to https://www.docker.com/products/docker-desktop and download it for
your computer (Windows, Mac, or Linux). Install it like any other app,
then open it once so it's running in the background. You'll see a whale
icon in your menu bar / system tray when it's ready.

## Step 2 — Open a terminal in this project folder

- **Mac**: open the "Terminal" app, type `cd ` (with a space), then drag
  the unzipped `streamflix` folder into the terminal window, and press Enter.
- **Windows**: open "Command Prompt" or "PowerShell", type `cd ` (with a
  space), then drag the `streamflix` folder into the window, and press Enter.

You should now see something like `.../streamflix $` — that means you're
in the right place.

## Step 3 — Type one command

```
docker compose up
```

Press Enter and wait. The first time will take a few minutes — it's
downloading and building everything. You'll see a lot of text scroll by;
that's normal. When it slows down and you see a line like:

```
Started StreamflixApplication in ... seconds
```

...it's ready.

## Step 4 — See it working

Open your web browser and go to:

```
http://localhost:8080/swagger-ui.html
```

This gives you a page where you can click buttons to try out every
feature of the app (register an account, search movies, add to your
watchlist, etc.) without needing to know how to code.

## Step 5 — Stop it when you're done

Go back to the terminal window and press `Ctrl + C`. To fully shut
everything down, you can also type:

```
docker compose down
```

## If something goes wrong

- **"docker: command not found"** → Docker Desktop isn't installed or
  isn't running. Open the Docker Desktop app and wait for the whale icon
  to say it's ready, then try again.
- **Port already in use** → something else on your computer is already
  using port 8080 or 3306. Quit that other program, or ask me and I'll
  show you how to change the port.
- **Anything else** → copy the error text from your terminal and send it
  to me — I'll tell you exactly what it means and what to do.

## What just happened, in plain terms

`docker compose up` read a small instruction file (`docker-compose.yml`)
that told your computer to:
1. Set up a MySQL database (the "filing cabinet" that stores users, movies, etc.)
2. Build your Java project into a running program
3. Connect the two together and start them

Everything runs inside isolated little boxes ("containers") so it can't
mess up anything else on your computer, and it's easy to delete later if
you want a clean slate.
