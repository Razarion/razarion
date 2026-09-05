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
```

`--link` und `--tags` sind optional. Ohne `--media` rendert der nächste Schritt eine Textkarte,
weil Instagram keine reinen Textbeiträge annimmt:

```bash
node render_cards.mjs
```

### Clips

Ein Clip wird genauso übergeben wie ein Bild — `--media clip.mp4`. Um die Formate musst du dich
nicht kümmern: **jedes Netzwerk bekommt beim Ausliefern seine eigene Fassung**, abgeleitet neben der
Originaldatei als `<name>--<format>.mp4` und beim nächsten Lauf wiederverwendet. Die Originaldatei
wird nie verändert.

```
Instagram, Facebook   Reel 9:16, 1080×1920, max 90 s
X                     eigene Form, nur Codec und Obergrenze, max 140 s
YouTube               das Original, unverändert
```

Warum unterschiedlich: Instagram und Facebook zeigen Reels hochkant und quetschen alles andere in
einen Streifen. X hat dagegen kein festes Format, sondern nimmt alles von 1:3 bis 3:1 — dort einen
Querformat-Clip auf 16:9 zu polstern hieße, Balken an etwas zu kleben, das die Zeitleiste ohnehin
ganz gezeigt hätte.

**Balken im Original werden erkannt und entfernt.** Die Archivclips stammen aus Browserfenstern und
bringen fast alle schwarze Ränder mit — beim Explosionsclip 184 px auf jeder Seite. Ohne das säßen
sie mitten im fertigen Reel als harte schwarze Kanten. Erkannt wird über drei Zeitpunkte im Clip;
gewinnt die *größte* gefundene Bildfläche, damit eine dunkle Szene nie zu eng schneiden kann.

Beim Hochformat füllt eine unscharfe Kopie des Clips den Rest des Bildes, statt ihn schwarz zu
lassen — bei einem 2:1-Clip in 9:16 wären sonst 70 % der Fläche leer. Beschnitten wird nie: was zu
sehen ist, bleibt vollständig zu sehen.

`check.mjs` misst vorher, was die Umwandlung nicht reparieren kann:

```
unter 3 s     Instagram lehnt das Reel ab - nur eine längere Aufnahme hilft
über 90 s     die Reel-Fassung wird geschnitten, der Rest fällt weg
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
GitHub-Release-URL, die Instagram und Facebook brauchen. Der Master geht unverändert raus —
YouTube kodiert ohnehin neu, und anders als bei den Reel-Feeds gibt es kein Format zu treffen.

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
