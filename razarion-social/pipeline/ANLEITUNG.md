# Anleitung

Spickzettel für den Betrieb. Das ausführliche `README.md` erklärt, *warum* die Dinge so sind —
hier steht nur, was du eintippst.

Alle Befehle laufen in diesem Verzeichnis:

```
cd C:\dev\projects\razarion\code\razarion2\razarion-social\pipeline
```

## Der übliche Ablauf

```
1. Beitrag erzeugen      generate.mjs   oder   compose.mjs
2. Lesen und freigeben   status auf "ok" in den Review-Dateien
3. Ausliefern            upload + publish
```

Zwischen Schritt 2 und 3 kann beliebig viel Zeit liegen. Nichts wird veröffentlicht, was nicht
auf `ok` steht.

## 1. Beitrag erzeugen

### Automatisch, aus den Spieldaten

```bash
node generate.mjs                 # nächste Einheit aus der Rotation
node generate.mjs --dry-run       # nur anzeigen, nichts schreiben
node generate.mjs --unit Harvester
node generate.mjs --reset         # Rotation von vorn
```

Holt Name, Beschreibung, Preis, Lebenspunkte und Spawn-Zeit vom laufenden Server, rendert eine
Karte und schreibt den Beitrag in die Review-Dateien. Braucht `RAZARION_ADMIN_USER` und
`RAZARION_ADMIN_PASSWORD` in `../.env`.

Die Rotation merkt sich in `state/generate.json`, welche Einheiten schon dran waren. Zwei Läufe am
selben Tag sind kein Problem.

Zwölf Einheiten sind darin — bei drei Beiträgen pro Woche also rund vier Wochen. Die Bot-Varianten
derselben Einheiten bleiben draußen: sie tragen denselben Namen, aber andere Werte (der Bot-Viper
kostet 100 statt 10), und ein Beitrag daraus nennte einen Preis, den kein Spieler je zahlt.

### Bessere Bilder: Studio-Szenen

Standardmäßig baut `generate.mjs` eine Karte um das gespeicherte Thumbnail — das ist nur 200×200
und wirkt entsprechend weich. Liegt für eine Einheit dagegen ein Szenen-Render bereit, nimmt es
den stattdessen:

```
data/scenes/factory.png      → wird für die Einheit "Factory" verwendet
data/scenes/harvester.png    → für "Harvester"
```

Der Dateiname ist der Einheitenname in Kleinbuchstaben, Leerzeichen als Bindestrich.

**So entsteht so ein Render:** [razarion.com/studio](https://www.razarion.com/studio) → *Scenes* →
Szene anklicken → rechts *Resolution* auf **1080 × 1080 (social square)**, *Background* auf
**Scene background** → **warten, bis der Boden nicht mehr einfarbig grün ist** → *Take screenshot*
→ *Save PNG* → Datei nach `data/scenes/<name>.png` verschieben.

Das einfarbige Grün ist kein fehlendes Terrain, sondern das Platzhalter-Material: die Geometrie
steht sofort, das echte Boden-Material wird pro Kachel nachgebaut (ein Shader-Build von rund 100 ms,
serialisiert auf eine Kachel pro Frame). Solange der Boden flach grün ist, ist der Render noch nicht
fertig — auch wenn Einheiten und Effekte längst richtig aussehen.

**`Terrain area` auf `Around camera (fast)` stehen lassen.** Das ist der Standard und baut die rund
20 Kacheln um die Kamera, also gut zwei Sekunden. **`Full map (slow)` stellt 1024 Kacheln ein und
macht es damit rund hundertmal langsamer** — der Boden bleibt dann minutenlang grün. Die Bezeichnung
ist wörtlich gemeint.

Tut ein Item in der Szene etwas, gehört das ins Bild — ein angeklicktes Item zeigt seine Aktionen in
PROPERTIES. Beim Harvester ist *Harvest target* bereits gesetzt, **Start harvest** zündet den Strahl
samt fliegender Kristallsplitter. Danach **Deselect** drücken, sonst stehen die Gizmo-Pfeile des
ausgewählten Items mit im Render.

Das Warten ist der Punkt, an dem es schiefgeht: löst man zu früh aus, kommt ein leeres Bild heraus,
und die Vorschau zeigt das auch. Deshalb bleibt dieser Schritt Handarbeit — ob eine Szene fertig
geladen und richtig gerahmt ist, sieht ein Mensch, ein Skript nicht.

Vorhanden sind bisher `factory.png` und `harvester.png`. Für die übrigen zehn Einheiten gibt es
teils Szenen im Studio (Tesla, Radar, Powerplant, Builder), teils noch keine.

### Mit eigenem Material

```bash
node compose.mjs --media pfad/zum/bild.jpg --text "Was zu sehen ist." --link "https://www.razarion.com"
node compose.mjs --text "Nur Text"                    # Instagram bekommt eine Karte
node compose.mjs --media clip.mp4 --text "..." --tags "harvester,economy"
node compose.mjs --portrait clip-portrait.mp4 --landscape clip-landscape.mp4 --text "..."
```

`--link` und `--tags` sind optional. Ohne `--media` rendert der nächste Schritt eine Textkarte,
weil Instagram keine reinen Textbeiträge annimmt:

```bash
node render_cards.mjs
```

### Clips aufnehmen, ohne dabeizusitzen

```bash
node record_studio.mjs --scene "Badger vs Radar" --both   # hochkant und quer, der Normalfall
node record_studio.mjs --scene "Badger vs Radar"
node record_studio.mjs --scene "Badger vs Radar" --seconds 12 --out data/clips/badger.mp4
node record_studio.mjs --scene "Tesla" --url https://www.razarion.com/studio/scenes
node record_studio.mjs --scene "Tesla" --head        # zusehen, statt headless
```

Startet ein eigenes Chrome ohne Fenster, meldet sich mit den Zugangsdaten aus `../.env` an (kein
Login-Formular, das Token wird vor dem Start in den `localStorage` gelegt), öffnet die Szene, wartet
auf Modelle und Boden, nimmt auf und legt die Datei ab. Mit `--both` nimmt er die Szene zweimal auf,
erst hochkant, dann quer, und legt `<name>-portrait.mp4` und `<name>-landscape.mp4` ab.

**Im Director** gibt es neben „Record" dieselbe Wahl: *portrait* oder *landscape*. Einen Plan einmal
pro Format aufnehmen.

**Was die Szene mitbringen muss:** die Kamera — und bei einer Szene, die feuert, die Angriffsschleife
als **„Loop on open"** am angreifenden Item gespeichert. Der Recorder kann kein Item im Viewport
anklicken; eine Szene, die erst nach einem Klick feuert, wird im Stillstand gefilmt.

**Der Lauf prüft sich selbst.** Kommt ein Clip mit eingebrochener Bildrate zurück, bricht er mit
Fehler ab, statt eine unbrauchbare Datei zu hinterlassen. Genau das ist hier schon passiert: eine
Aufnahme in einem Hintergrund-Tab lieferte 5 Frames in 0,17 s, weil Chrome den Render-Loop in einem
unsichtbaren Tab anhält — headless gibt es dieses Problem nicht.

Standardmäßig zielt er auf den lokalen Dev-Server (`http://localhost:4300/scenes`, Token vom
Backend auf 8080). Für die Produktion `--url https://www.razarion.com/studio/scenes`.

### Clips

**Jeder Clip am besten zweimal: hochkant und quer.** Instagram, Facebook und YouTube Shorts sind
Handy-Feeds und wollen 9:16. X wird am Desktop gelesen und bekommt 16:9: Von dort kommen 24 % der
Desktop-Besucher bis ins Spiel, am Handy 5 %.

```bash
node compose.mjs --portrait data/clips/badger-portrait.mp4 --landscape data/clips/badger-landscape.mp4 --text "..."
```

Jedes Netzwerk bekommt beim Ausliefern seine eigene Fassung. Sie wird aus der Vorlage mit dem
passenden Format abgeleitet, neben ihr als `<name>--<format>.mp4` abgelegt und beim nächsten Lauf
wiederverwendet. Die Vorlagen werden nie verändert. Passt eine Vorlage schon, geht sie unverändert
hoch.

```
Instagram, Facebook   Reel 9:16, 1080×1920, max 90 s    aus der Hochformat-Vorlage
YouTube               Short 9:16, 1080×1920, max 180 s  aus der Hochformat-Vorlage
X                     16:9, 1920×1080, max 140 s        aus der Querformat-Vorlage
```

**Jede Fassung füllt das ganze Bild, es gibt keine Balken und keinen Rahmen.** Fehlt eine Vorlage,
wird die andere beschnitten: Von einem 16:9-Clip bleibt im Reel das mittlere Drittel, von einem
Hochkant-Clip bei X ebenso. Das geht, wenn die Action in der Mitte steht; sonst fehlt am Rand, was
zu sehen sein sollte. `compose.mjs` und `check.mjs` sagen es, wenn eine Vorlage fehlt. Mit
`--media clip.mp4` wird der Clip gemessen und als Hoch- oder Querformat eingeordnet.

Früher füllte eine unscharfe Kopie des Clips den Rest des Reels. In den Feeds sah das wie ein Rahmen
aus. Die alten `--reel.mp4` mit Rahmen werden nicht mehr verwendet (die neuen heißen
`--reel-v2.mp4`) und können gelöscht werden, ebenso die alten `--native.mp4` für X.

**Balken im Original werden erkannt und entfernt.** Die Archivclips stammen aus Browserfenstern und
bringen fast alle schwarze Ränder mit, beim Explosionsclip 184 px auf jeder Seite. Ohne das säßen
sie als harte schwarze Kanten im fertigen Bild. Erkannt wird an drei Zeitpunkten im Clip; es gewinnt
die *größte* gefundene Bildfläche, damit eine dunkle Szene nie zu eng schneiden kann.

`check.mjs` misst vorher, was die Umwandlung nicht reparieren kann:

```
unter 3 s     Instagram lehnt das Reel ab - nur eine längere Aufnahme hilft
über 90 s     die Reel-Fassung wird geschnitten, der Rest fällt weg
Vorlage fehlt die Fassung zeigt nur die Bildmitte der anderen - beide Formate aufnehmen
```

## 2. Lesen und freigeben

Vier Dateien, ein Eintrag pro Netzwerk:

```
data/captions.json    Instagram
data/fb_posts.json    Facebook
data/x_posts.json     X
data/yt_posts.json    YouTube - nur bei Clips
```

**YouTube bekommt nur Videos.** Ein Bild- oder Textbeitrag erzeugt dort gar keinen Eintrag, statt
einen, der nie rausgehen könnte. Der YouTube-Eintrag trägt statt einer Bildunterschrift `title`,
`description` und `tags`; der Titel wird aus dem ersten Satz gebaut und auf 70 Zeichen gekürzt —
mehr zeigt ein Telefon nicht. Steht `title-truncated` in den `flags`, wurde geschnitten und der
Titel ist es wert, von Hand geschrieben zu werden.

Beim neuen Eintrag `"status": "review"` auf `"ok"` setzen — oder auf `"skip"`, wenn er dort nicht
erscheinen soll. Texte darfst du frei ändern; setz dann `"edited": true`, damit ein späterer Lauf
deine Fassung nicht überschreibt.

Vor dem Ausliefern prüfen, ob alles beisammen ist:

```bash
node check.mjs
```

## 3. Ausliefern

```bash
node upload_media.mjs              # Bilder für Instagram (konvertiert, füllt auf)
node upload_media.mjs --source fb  # dieselben Dateien im Original für Facebook

node publish.mjs                   # Trockenlauf
node publish.mjs --live --limit 1  # Instagram
node publish_fb.mjs --live --limit 1
node publish_x.mjs --live --limit 1
node publish_youtube.mjs --live --limit 1
```

**Ohne `--live` passiert nichts.** Der Trockenlauf zeigt, was rausginge, bei X auch, was es
kostet, und bei YouTube, ob der Clip als Short oder als normales Video einsortiert wird.

YouTube braucht `upload_media.mjs` nicht: die Datei geht von der Platte hoch, nicht über die
GitHub-Release-URL, die Instagram und Facebook brauchen. Jeder Clip geht als Short hoch. Passt die
Hochformat-Vorlage schon, geht sie unverändert raus, denn YouTube kodiert ohnehin neu.

`--limit N` begrenzt, wie viele Beiträge ein Lauf absetzt. Ohne Angabe geht die ganze freigegebene
Warteschlange raus.

## Der Zeitplan

`scheduled/run.ps1` erledigt Schritt 3 allein — Token prüfen, dann je einen freigegebenen Beitrag
pro Netzwerk:

```powershell
powershell -ExecutionPolicy Bypass -File scheduled\run.ps1
powershell -ExecutionPolicy Bypass -File scheduled\run.ps1 -Limit 2
powershell -ExecutionPolicy Bypass -File scheduled\run.ps1 -PrepareOnly   # nichts veröffentlichen
```

Als wiederkehrende Aufgabe (Montag, Mittwoch, Freitag um 10 Uhr) — in `cmd`, nicht in PowerShell,
wegen des `^`:

```
schtasks /create /tn "razarion-social" /sc weekly /d MON,WED,FRI /st 10:00 ^
  /tr "powershell -NoProfile -ExecutionPolicy Bypass -File C:\dev\projects\razarion\code\razarion2\razarion-social\pipeline\scheduled\run.ps1"
```

```
schtasks /run /tn "razarion-social"       sofort einmal ausführen
schtasks /delete /tn "razarion-social" /f  wieder entfernen
```

Alles landet zusätzlich in `state/scheduled.log`.

## Was etwas kostet

```
Instagram, Facebook       kostenlos
X, Beitrag ohne Link      $0.015
X, Beitrag mit Link       $0.20
X lesen (nicht mehr aktiv) $0.005 pro Beitrag
YouTube                   kostenlos, aber 1600 von 10000 Kontingentpunkten pro Upload
```

**YouTube zahlt in Kontingent statt in Geld.** Sechs Uploads am Tag sind das Maximum, und der
Publisher warnt, wenn ein Lauf darüber hinausginge. Abfragen lässt sich der Rest nicht, also zählt
er nur mit.

**X-Beiträge tragen den Link nicht mehr.** Er kostete dort das Dreizehnfache, und X drückt
zusätzlich die Reichweite von Beiträgen mit Link — er kaufte also weniger Publikum zum höheren
Preis. Der Weg zur Seite ist auf X das Profil, wie auf Instagram auch. Bei drei Beiträgen pro Woche
sind das rund 31 Dollar im Jahr statt rund 2.

Instagram bekommt weiterhin „Link in bio.", Facebook den Link im Text — dort ist er klickbar und
kostet nichts.

## Wenn etwas klemmt

**„Nothing to do"** — kein Eintrag steht auf `ok`, oder alle sind schon veröffentlicht. Kein
Fehler.

**„marked ok but their media is not uploaded yet"** — `upload_media.mjs` fehlt noch.

**Instagram meldet Code 4 oder 9** — zu schnell gepostet. Code 4 legt sich nach einer Stunde,
Code 9 ist eine Account-Sperre und braucht Stunden bis einen Tag. Nicht dagegen anlaufen.

**Instagram-Token abgelaufen** — `node refresh_token.mjs`. Läuft im Zeitplan automatisch mit,
erneuert aber nur, wenn weniger als 14 Tage übrig sind. Ist er einmal abgelaufen, hilft nur ein
neuer Token aus dem Meta-Dashboard.

**X: „Something went wrong" beim Autorisieren** — die ausgegebene URL von Hand in dem Browser
öffnen, in dem du bei X angemeldet bist. Der automatisch geöffnete erwischt unter Umständen ein
Profil ohne Sitzung, und X meldet das als allgemeinen Fehler.

**Ein Beitrag ging raus, steht aber nicht im Zustand** — sollte nicht vorkommen, der Zustand wird
direkt nach dem Veröffentlichen geschrieben. Falls doch: den Eintrag in `state/posted*.json` von
Hand ergänzen, sonst wird er beim nächsten Lauf erneut gepostet.

## Wo was liegt

```
data/captions.json  fb_posts.json  x_posts.json
data/yt_posts.json                                 die Review-Dateien
data/own/                                          Bilder eigener Beiträge
data/scenes/                                       Studio-Renders je Einheit
data/cards/                                        gerenderte Textkarten
data/youtube/                                      Clips und Metadaten für Studio
state/posted*.json                                 was veröffentlicht wurde
                                                   (posted, posted_fb, posted_x, posted_yt)
state/generate.json                                wo die Einheiten-Rotation steht
state/scheduled.log                                Protokoll der geplanten Läufe
../.env                                            alle Zugangsdaten
```

`data/` und `state/` sind gitignored.
