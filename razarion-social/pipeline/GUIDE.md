# Anleitung

Spickzettel für den Betrieb. Das ausführliche `README.md` erklärt, *warum* die Dinge so sind —
hier steht nur, was du eintippst.

Alle Befehle laufen in diesem Verzeichnis:

```
cd C:\dev\projects\razarion\code\razarion2\razarion-social\pipeline
```

## Der übliche Ablauf

```
1. Beitrag erzeugen      plan.mjs (macht der Zeitplan)   oder   produce / compose / generate
2. Lesen und freigeben   review.mjs   (oder status auf "ok" in den Review-Dateien)
3. Ausliefern            upload + publish (macht der Zeitplan)
```

Läuft der Zeitplan, bleibt dir nur Schritt 2: ab und zu `node review.mjs --open` und freigeben.

**Von Hand anstoßen: Doppelklick auf `Razarion-Social.cmd`.** Macht dasselbe wie der Zeitplan
(auffüllen, hochladen, je einen freigegebenen Beitrag pro Netzwerk veröffentlichen) und öffnet
danach die Review-Seite. Das Fenster offen lassen, solange die Seite gebraucht wird. Was du jetzt
freigibst, geht beim nächsten Doppelklick raus.

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

**Texte schreiben lassen** — `--write` gibt deinen `--text` an den Textschreiber (siehe unten), der
daraus einen englischen Text pro Netzwerk in einer Tonlage macht. Dein Text darf deutsch sein; er
sagt, *was* rüberkommen soll, nicht *wie*. `--dry-run` zeigt die Texte nur, ohne etwas zu kopieren
oder einzutragen:

```bash
node compose.mjs --portrait a.mp4 --landscape b.mp4 --text "19 Vipers landen auf dem Datacenter..." --write --dry-run
node compose.mjs --portrait a.mp4 --landscape b.mp4 --text "..." --write --tone behind-the-scenes
node compose.mjs --media clip.mp4 --text "..." --write --facts "Inszeniert auf dem Live-Server"
```

Jede Zahl im Ergebnis muss in `--text` oder `--facts` stehen. Kleine Ausschmückungen („in seconds“)
fängt die Prüfung nicht — dafür ist die Freigabe da.

`--link` und `--tags` sind optional. Ohne `--media` rendert der nächste Schritt eine Textkarte,
weil Instagram keine reinen Textbeiträge annimmt:

```bash
node render_cards.mjs
```

### Formate: Duell, Wochenbilanz, Devlog

```bash
node produce.mjs --list                              # alle Formate, wann jedes zuletzt lief
node produce.mjs --format duel                       # das fällige Einheiten-Paar
node produce.mjs --format duel --subject viper,badger
node produce.mjs --format week-in-numbers            # die letzten 7 Tage der Spielwelt
node produce.mjs --format devlog                     # erster Lauf: Entwurf, zweiter Lauf: Karte
node produce.mjs --format duel --dry-run             # Bild nach data/preview, sonst nichts
```

Ein Format ist ein Rezept: woher das Material kommt, was daraus wird, und woran es erkennt, dass
es ein Thema schon hatte. Die Formate liegen in `lib/formats/`. Wie `compose.mjs` landet alles auf
`review`.

- **battle** — ein Gefecht auf dem Live-Planeten, ohne dass jemand danebensitzt. Einheit
  (Viper, Badger), Bot-Basis und Kamerastil wechseln unabhängig voneinander: jedes ist das, was am
  längsten nicht dran war, damit zwei Clips hintereinander sich möglichst in allem unterscheiden.
  Kamerastile (`lib/director.mjs`): `overhead` (steil von oben, wie die September-Clips),
  `low-orbit` (tief hinter der Truppe, kreist mit), `push-in` (fährt von weit oben heran), `side`
  (flach, quer zum Angriff). Nur Bot-Basen mit mindestens 6 Items und 450 Abstand zu jedem
  Spieler. Filmt mit `record_director.mjs` hochkant und quer (vor der zweiten Aufnahme wird die
  Staging-Basis samt Überlebenden neu aufgestellt, danach immer entfernt). `record_director.mjs`
  zählt während der Aufnahme jede Sekunde, was auf beiden Seiten noch steht; **geschnitten wird
  von kurz vor dem ersten bis kurz nach dem letzten Abschuss** (8–18 s, `lib/cut.mjs`), weil eine
  bewegte Kamera auch über einer stillen Basis viel Bildbewegung erzeugt. Weniger als 3 Abschüsse:
  kein Beitrag, und die Basis kommt in `state/battle-attempts.json` ans Ende der Reihe. Eine
  Aufnahme mit eingebrochener Bildrate wird verworfen; ist nur eine Form brauchbar, bekommt X die
  Mitte der anderen. Zum Ausprobieren: `--style side`, `--unit badger`. Dauert
  ein paar Minuten und braucht die GPU dieses Rechners. **Auch `--dry-run` filmt** — nur ins
  Review und ins Inhaltsbuch schreibt er nicht. Die rohen 40-s-Aufnahmen bleiben als
  `data/own/<id>-battle-take-*.mp4` liegen (je 40–80 MB) und dürfen weg.
- **duel** — zwei Kampfeinheiten mit ihren echten Werten vom Server und der Frage, wer gewinnt.
  Passen die Preise zueinander, kommt die Frage „3 Vipers oder 1 Badger?“ dazu. Die Antwort steht
  bewusst nicht drin — die kann ein Clip aus dem Studio liefern. Ein Paar kommt frühestens nach
  120 Tagen wieder.
- **week-in-numbers** — zerstörte Bot-Einheiten, geschleifte Bot-Basen, Level, Quests aus der
  Spielhistorie. Nur Zahlen, keine Namen. Eine Zahl unter 10 wird weggelassen, und mit weniger als
  drei Zahlen lässt das Format die Woche aus, statt eine leere Welt zu zeigen. Braucht den
  Endpunkt `/rest/editor/game-history-summary`, also einen Server-Deploy.
- **devlog** — die Commits der Woche, die das Spiel betreffen, landen in
  `data/drafts/devlog-<woche>.json`. Dort in `lines` 2–5 Sätze eintragen, die ein Spieler versteht,
  dann denselben Befehl noch einmal: jetzt entsteht die Karte. Der Planer versucht es bei jedem
  Lauf erneut, du musst also nur die Sätze eintragen.

### Der Planer

```bash
node plan.mjs               # Warteschlange bis zum Ziel auffüllen
node plan.mjs --dry-run     # nur sagen, was er machen würde
node plan.mjs --target 4    # vier offene Beiträge statt drei
```

Hält **drei Beiträge offen** — zur Prüfung oder freigegeben und noch nicht draußen, also eine
Woche Zeitplan. Fehlen welche, macht er höchstens zwei pro Lauf über `produce.mjs`: das Format,
das am längsten nicht dran war, zuerst, und jedes höchstens so oft, wie sein Abstand erlaubt
(Gefecht 1 Tag, Duell 2 Tage, Wochenbilanz und Devlog 6 Tage). Hat ein Format nichts (keine
Bot-Basis weit genug von den Spielern, alle Duell-Paare kürzlich gelaufen, stille Woche,
Devlog-Entwurf ohne Sätze) oder scheitert es, nimmt er das nächste.

Er gibt **nichts frei und veröffentlicht nichts** — alles landet auf `review`. Beim Gefecht greift
er allerdings in die Live-Welt ein, für die Dauer der Aufnahme.

### Der Textschreiber

`produce.mjs` lässt die Texte schreiben: einen pro Netzwerk (X, Instagram, Facebook, YouTube-Titel
und -Beschreibung), in einer **Tonlage**. Er ruft dafür `claude -p` auf, also Claude Code auf deinem
Abo — kein API-Key, keine zusätzliche Rechnung, es zählt nur gegen das Nutzungskontingent.

```bash
node produce.mjs --format duel                  # die Tonlage, die am längsten nicht dran war
node produce.mjs --format duel --tone question  # eine bestimmte
node produce.mjs --format duel --no-writer      # nur der Vorlagentext
```

| Tonlage | So klingt sie |
|---|---|
| `question` | Ich-Form, endet mit einer echten Frage an die Leser |
| `matter-of-fact` | dritte Person, ruhig, konkret |
| `behind-the-scenes` | Ich-Form, was ich gebaut oder bemerkt habe und warum es zählt |
| `punchy` | kurz, ein starker erster Satz |

Jedes Format erlaubt nur passende Tonlagen (Duell: `question`, `punchy`). Die Tonlage steht im
Inhaltsbuch — später lässt sich vergleichen, welche mehr Reichweite bringt.

**Was der Schreiber nicht darf, wird geprüft, nicht nur verlangt:** jede Zahl muss in den Fakten
stehen, kein Link, kein Hashtag, kein Versprechen („stay tuned“, „next post“), X höchstens 240
Zeichen. Eine abgelehnte Antwort geht einmal mit den Gründen zurück; scheitert auch die zweite, oder
ist `claude` nicht angemeldet, kommt der Vorlagentext hinein. Alles landet wie immer auf `review`.

### Das Inhaltsbuch

```bash
node ledger.mjs                     # was wann in welchem Format entstand, die letzten 8 Wochen
node ledger.mjs --rebuild           # Beiträge aus den Review-Dateien übernehmen (einmalig, harmlos)
node ledger.mjs --external data/clips/viper4-portrait.mp4 --date 2026-09-20 --note "Reel, von Hand"
```

`state/ledger.json` kennt jeden Beitrag mit Format, Thema und dem Hash seiner Medien.
`compose.mjs`, `generate.mjs` und `produce.mjs` tragen dort selbst ein. **Was von Hand
rausging, mit `--external` nachtragen** — dann lehnt `compose.mjs` denselben Clip ab, auch unter
anderem Namen (`--force`, wenn er wirklich noch einmal soll).

### Messen, was die Posts bringen

Jeder Link auf razarion.com, den die Pipeline schreibt, trägt, woher er kommt: im Facebook-Text und in
der YouTube-Beschreibung automatisch, als `razarion.com/fb/<post-id>` bzw. `razarion.com/yt/<post-id>`.
Der Server leitet diese kurzen Pfade auf die Startseite weiter und hängt die UTM-Parameter an
(`ProfileLinkController`). X und Instagram haben keinen Link im Post — dort ist es der Profil-Link,
und den setzt du **einmal von Hand**:

```
Instagram (Bio)          razarion.com/ig
Facebook (Seite, Button) razarion.com/fb
X (Website im Profil)    razarion.com/x
YouTube (Kanal-Links)    razarion.com/yt
```

Kurz, weil ein Profil seinen Link anzeigt, und die lange Form mit `?utm_source=...` nach
„razarion.com/?utm_source=so…“ abgeschnitten wurde.

Im Backend, Tab *Daily*, zeigt **Own posts** diese Besucher. Wichtig ist das `social-`: Instagram und
Facebook hängen jedem Link ein `fbclid` an, auch dem Bio-Link, und bisher zählte deshalb jeder
Besucher von dort als Werbung. Werbelinks dürfen dieses Präfix nie tragen.

### Reichweite je Format und Tonlage

```bash
node metrics.mjs              # Zahlen holen, was noch nicht fertig ist, dann der Bericht
node metrics.mjs --report     # nur der Bericht aus dem Gespeicherten
node metrics.mjs --only x     # ein Netzwerk: x, ig oder fb
node metrics.mjs --refresh    # alles neu holen (kostet auf X)
```

Fragt X, Instagram und Facebook, wie weit jeder veröffentlichte Beitrag kam, legt es in
`state/metrics.json` ab und zeigt Mediane nach Format, Tonlage und Medium — **nur innerhalb eines
Netzwerks**, denn eine X-Impression und eine Instagram-Reichweite zählen Verschiedenes. Beiträge
unter 2 Tagen zählen nicht mit. Die Review-Seite zeigt die Zahlen bei jedem veröffentlichten
Beitrag, der Zeitplan holt sie am Ende jedes Laufs.

Ein Beitrag, der beim Abruf 7 Tage alt war, gilt als fertig und wird nicht mehr gefragt. Das hält
X billig: dort kostet jeder gelesene Beitrag $0.005, ein Lauf also ein paar Cent. YouTube fehlt,
solange die Uploads privat sind.

Auf Instagram und Facebook ist `mirrored` der Nachtrag vom August, in einem Schwung gepostet —
diese Reichweite ist nicht vergleichbar. Auf X sind es die echten, von Hand geschriebenen Posts.

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

### Auf der Review-Seite

```bash
node review.mjs            # dann http://127.0.0.1:4711 öffnen
node review.mjs --open     # öffnet den Browser gleich mit
node review.mjs --port 4800
```

Zeigt jeden Beitrag, der noch nicht überall draußen ist: das Medium (bei Clips jede Fassung, die
ein Netzwerk bekommt), die Vorgabe, Format und Tonlage aus dem Inhaltsbuch, und daneben den Text
pro Netzwerk mit Zeichenzähler. Texte direkt im Feld ändern, dann **Approve** oder
**Skip** — pro Netzwerk oder mit **Approve all** für den ganzen Beitrag. *All* oben
rechts zeigt auch die letzten veröffentlichten.

Die Seite schreibt genau das, was man sonst von Hand schreibt: `status` und bei geändertem Text
`"edited": true`, in dieselben vier Dateien. Was schon veröffentlicht ist, lässt sie nicht mehr
ändern, und was über der Grenze eines Netzwerks liegt (X 280, Instagram 2200, YouTube-Titel 100),
lässt sie nicht freigeben. Sie läuft nur auf diesem Rechner (127.0.0.1).

### Von Hand

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

`scheduled/run.ps1` erledigt Schritt 1 und 3 allein — Token prüfen, Warteschlange auffüllen
(`plan.mjs`), Medien der freigegebenen Beiträge hochladen, dann je einen freigegebenen Beitrag pro
Netzwerk veröffentlichen:

```powershell
powershell -ExecutionPolicy Bypass -File scheduled\run.ps1
powershell -ExecutionPolicy Bypass -File scheduled\run.ps1 -Limit 2
powershell -ExecutionPolicy Bypass -File scheduled\run.ps1 -PrepareOnly   # auffüllen, nichts veröffentlichen
powershell -ExecutionPolicy Bypass -File scheduled\run.ps1 -NoPlan        # nichts Neues erzeugen
```

Die Texte schreibt `claude -p` unter deinem Benutzer. Die Aufgabe muss deshalb als du laufen (der
Standard von `schtasks /create`), und Claude Code muss dort angemeldet sein — sonst kommt der
Vorlagentext hinein.

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
state/ledger.json                                  das Inhaltsbuch: jeder Beitrag, Format, Medien-Hash
data/drafts/                                       Devlog-Entwürfe zum Ausfüllen
data/preview/                                      Bilder aus --dry-run
state/scheduled.log                                Protokoll der geplanten Läufe
../.env                                            alle Zugangsdaten
```

`data/` und `state/` sind gitignored.
