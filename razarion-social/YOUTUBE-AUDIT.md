# YouTube: Shorts automatisch hochladen

Ziel: die Clips aus der Pipeline landen als **öffentliche** Shorts auf
[@Razarion](https://www.youtube.com/@Razarion), ohne dass jemand sie in Studio einzeln freischaltet.

## Warum das nicht einfach geht

Seit Juli 2020 gilt: Videos, die über ein **nicht auditiertes** API-Projekt hochgeladen werden,
sind fest auf `private` gesetzt. Ein `privacyStatus: public` beim Upload wird stillschweigend
zurückgesetzt — es gibt keine Fehlermeldung, das Video ist einfach privat. Freischalten geht nur
von Hand in Studio.

Das Razarion-Cloud-Projekt stammt von Juni 2024, liegt also hinter dem Stichtag. Ohne bestandenes
Audit bringt der automatische Upload deshalb nichts, was das Hochziehen per Hand nicht auch bringt.

## Was schon da ist

```
src/platforms/youtube.mjs   Uploader, 90 Zeilen, wiederaufnehmbar, funktionsfähig
src/auth/google.mjs         OAuth über Loopback, Port 8723
                            Scopes: youtube.upload + youtube (zweiter nur fürs Thumbnail)
pipeline/build_yt_posts.mjs bereitet Titel, Beschreibung, Tags und Dateinamen auf
```

Der Uploader wird von `src/cli.mjs` aufgerufen, in der Pipeline gibt es dafür noch kein
`publish_youtube.mjs`.

**Das Format passt bereits.** Die Reel-Fassung für Instagram und Facebook ist 1080×1920, also
Seitenverhältnis 0,563 — YouTube sortiert alles mit Verhältnis ≤ 1,05 und ≤ 180 s als Short ein.
Es braucht keine eigene YouTube-Ableitung.

## Ist-Zustand (geprüft am 2026-09-02)

```
GCP-Projekt              neural-passkey-426618-j3  (Nummer 579831821740)
YouTube Data API v3      NICHT aktiviert
OAuth-Client             keiner - GOOGLE_CLIENT_ID in .env ist leer
gespeicherte Tokens      nur "x"
```

Es fehlt also nicht nur das Audit, sondern die komplette Vorstufe.

## Ist-Zustand (geprüft am 2026-09-04)

```
YouTube Data API v3      aktiviert
OAuth-Client             angelegt, GOOGLE_CLIENT_ID/_SECRET stehen in .env
Testnutzer               beat.keller@btxtech.com eingetragen
gespeicherte Tokens      "x" und "google" - Refresh-Token ist da
Veröffentlichungsstatus  Test
Kanalbindung             KEINE - channels.list?mine=true liefert 0 Kanäle
```

Die Kette funktioniert damit erwiesenermassen: Client, Scopes, Loopback und die
Token-Erneuerung laufen durch. Was fehlt, ist allein der Kanal.

**Warum das Token auf keinen Kanal zeigt.** @Razarion ist ein Marken-Konto und hat keine eigene
E-Mail-Adresse, kann also nicht als Testnutzer eingetragen werden. Im Status *Test* darf nur
autorisieren, wer dort steht — wählt man das Marken-Konto trotzdem im Kontowähler, bricht Google
wortlos ab („Ein Problem ist aufgetreten"). Autorisiert wurde deshalb das persönliche Konto, und
das besitzt selbst gar keinen YouTube-Kanal. Ein Upload liefe in `youtubeSignupRequired`.

Der Ausweg ist der Wechsel in den Produktionsmodus. Er verlangt laut Console „einen gültigen
Anwendungsname, eine Support-E-Mail-Adresse sowie URLs für die Startseite und die
Datenschutzerklärung". Die ersten beiden stehen; die Seiten gibt es seit dem 2026-09-04:
`LegalController` liefert `/privacy` und `/terms` aus `templates/privacy.ftl` bzw. `terms.ftl`,
verlinkt im Fuss der Landing Page. **Sie müssen erst auf PROD stehen** — Google prüft
`https://razarion.com/privacy`, nicht localhost.

Offen, in dieser Reihenfolge:

1. Deploy, damit die beiden URLs öffentlich erreichbar sind
2. In der Console: URLs ins Branding, `razarion.com` unter *Autorisierte Domains*, speichern
3. *Zielgruppe → App veröffentlichen*
4. `node src/cli.mjs auth google` erneut, diesmal **Razarion** wählen
5. `channels.list?mine=true` muss den Kanal zurückgeben, dann Test-Upload

Danach erst das Audit — ohne das bleibt jeder Upload `private`.

## Schritte

Die ersten drei kann nur Beat machen — sie laufen über sein Google-Konto und legen Zugangsdaten an.

### 1. YouTube Data API v3 aktivieren

```
gcloud services enable youtube.googleapis.com --project neural-passkey-426618-j3
```

Oder in der Console unter *APIs & Services → Library → YouTube Data API v3 → Enable*.

### 2. OAuth-Zustimmungsbildschirm einrichten

*APIs & Services → OAuth consent screen*. Benutzertyp **External**. Nötig sind unter anderem
App-Name, Support-E-Mail, Startseite, Datenschutzerklärung und Nutzungsbedingungen — das Audit
prüft genau diese Angaben, also sollten sie stimmen und erreichbar sein.

Solange die App im Status *Testing* steht, funktioniert der Upload nur für eingetragene
Testnutzer und Tokens laufen nach sieben Tagen ab. Für den Dauerbetrieb muss sie auf
*In production* stehen.

### 3. OAuth-Client anlegen

*APIs & Services → Credentials → Create credentials → OAuth client ID*, Typ **Desktop app**
(`google.mjs` nutzt einen Loopback-Redirect auf Port 8723, kein Web-Redirect).

Client-ID und Secret dann in `razarion-social/.env`:

```
GOOGLE_CLIENT_ID=…
GOOGLE_CLIENT_SECRET=…
```

### 4. Einmal autorisieren und einen Test-Upload machen

Danach steht das Refresh-Token in `.tokens.json`. Das Video landet auf `private` — das ist
erwartet und genau der Zustand, den das Audit aufheben soll.

Das ist kein verlorener Schritt: **das Audit will echte Nutzung sehen.** Ein Antrag für ein
Projekt, das die API noch nie aufgerufen hat, hat wenig zu prüfen.

### 5. Audit beantragen

Über das *YouTube API Services – Audit and Quota Extension*-Formular, verlinkt in der
[YouTube-API-Compliance-Dokumentation](https://developers.google.com/youtube/v3/guides/quota_and_compliance_audits).
Die Feldnamen ändern sich gelegentlich; der Inhalt unten passt trotzdem.

Rechne mit mehreren Wochen.

## Antragstext (Entwurf)

Zum Kopieren. Ehrlich gehalten: es ist ein Ein-Personen-Projekt, das seine eigenen Clips auf den
eigenen Kanal lädt — das ist ein einfacher Fall, und ihn kleiner darzustellen, als er ist, hilft
mehr als ihn aufzublasen.

> **What does your API Client do?**
>
> Razarion is an open-source, browser-based real-time strategy game
> (https://www.razarion.com, source at https://github.com/Razarion/razarion). The API client is a
> command-line publishing tool that I run myself. It uploads short gameplay clips, recorded from
> the game's own renderer, to the project's own YouTube channel (https://www.youtube.com/@Razarion),
> which I own.
>
> It is a single-operator tool. It is not distributed, has no other users, no user interface for
> third parties, and never touches any channel other than my own.
>
> **How does your API Client use YouTube API Services?**
>
> One endpoint: `videos.insert`, to upload a video with a title, description and tags, plus
> `thumbnails.set` for the still image. Nothing is read back, no other channel's data is accessed,
> and no YouTube data is stored beyond the returned video id, which is written to a local JSON file
> so the same clip is not uploaded twice.
>
> The same clips are published to X, Instagram and Facebook through the same tool. YouTube is one
> of four destinations, not the product.
>
> **Where does the content come from?**
>
> All of it is original material I produce: recordings of my own game, captured in its own scene
> editor. No third-party content, no YouTube content is downloaded, re-uploaded or repurposed.
>
> **Expected quota use**
>
> Roughly three uploads a week. At 1600 units per upload that is about 4,800 units a week, well
> inside the default 10,000-unit daily quota. I am not asking for a quota increase - only for the
> ability to publish my own videos publicly rather than having them forced to private.

## Was danach zu tun bleibt

Auch mit bestandenem Audit fehlt in der Pipeline noch:

- **`build_yt_posts.mjs` liest nur `data/posts.json`**, also das X-Archiv. Neu komponierte Clips
  aus dem Studio tauchen dort gar nicht auf und müssten ergänzt werden.
- **Ein `publish_youtube.mjs`** nach dem Muster der drei vorhandenen Publisher, mit demselben
  Review-Gatter und Zustandsschreiben nach `state/posted_yt.json`.

Beides ist überschaubar, lohnt aber erst, wenn das Audit durch ist.
