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

## Ist-Zustand (2026-09-04, abends)

**Die Vorstufe ist vollständig durchlaufen, und ein Video liegt auf dem Kanal.**

```
YouTube Data API v3      aktiviert
OAuth-Client             angelegt, GOOGLE_CLIENT_ID/_SECRET in .env
Veröffentlichungsstatus  Produktion
Kanalbindung             Razarion (@razarion, UCgsooVWjIipqHt0xYyjc5og)
erster Upload            hDmrfUUK2IQ - über die API hoch, von Hand freigeschaltet
```

Der Weg dahin, weil zwei Sackgassen darin Zeit gekostet haben:

**Das Marken-Konto kann im Testmodus nicht autorisieren.** @Razarion hat keine eigene
E-Mail-Adresse und kann deshalb nicht als Testnutzer eingetragen werden; im Status *Test* darf nur
autorisieren, wer dort steht. Wählt man es trotzdem im Kontowähler, bricht Google wortlos ab („Ein
Problem ist aufgetreten"). Das persönliche Konto kam durch, besitzt aber selbst keinen Kanal —
`channels.list?mine=true` lieferte nichts, und ein Upload wäre in `youtubeSignupRequired` gelaufen.

**Der Produktionsmodus verlangt zwei Seiten.** Die Console nennt sie im Tooltip des ausgegrauten
Knopfes: „ein gültiger Anwendungsname, eine Support-E-Mail-Adresse sowie URLs für die Startseite
und die Datenschutzerklärung". `LegalController` liefert `/privacy` und `/terms`; sie mussten erst
auf PROD stehen, weil Google `https://razarion.com/privacy` prüft, nicht localhost.

Beides ist erledigt, und danach ging die Autorisierung mit **Razarion** auf Anhieb durch.

## Was noch fehlt: das Audit

Es ist der einzige verbliebene Punkt, und er entscheidet über genau eine Sache — ob ein Upload
öffentlich sein darf. Ohne bestandenes Audit setzt YouTube `privacyStatus: public` still auf
`private` zurück; freischalten geht dann nur von Hand in Studio.

Beantragt wird es über das *YouTube API Services – Audit and Quota Extension*-Formular, verlinkt in
der [Compliance-Dokumentation](https://developers.google.com/youtube/v3/guides/quota_and_compliance_audits).
Der Antragstext steht unten fertig. Rechne mit mehreren Wochen.

**Wenn es durch ist**, ist es eine Zeile: `DEFAULT_PRIVACY` in `pipeline/lib/youtube.mjs` auf
`'public'`. Bereits geschriebene Einträge behalten, was sie haben.

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

## Das Formular, Feld für Feld

Ausgefüllt am 2026-09-04, nicht abgeschickt. Das Formular selbst speichert nichts — wer es
schliesst, tippt neu. Deshalb steht hier jede Antwort, damit das Neutippen Abschreiben ist und
kein Nachdenken.

### 1 Art des Antrags
Compliance-Audit durchführen, um zusätzliches Kontingent anzufordern. Die zweite Option ist nur
für ein Re-Audit, zu dem Google auffordert. Dass wir gar kein höheres Kontingent wollen, steht in
Abschnitt 5 — das ist kein Widerspruch, sondern der übliche Weg zum Audit.

### 2 Organisation und Kontakt
```
Antrag als                als Einzelnutzer*in
Vollständiger Name        Beat Keller
Organisation (Pflichtfeld) Beat Keller (individual, filing on my own behalf)
Muttergesellschaft        leer
Website                   https://www.razarion.com
Land / Kanton             Schweiz / Zürich
Grösse und Art            Entwickler*in (unabhängig) / Alleininhaber*in
Kategorie                 Gaming, sonst Media & Entertainment
Kontakte                  alle drei identisch, beat.keller@btxtech.com
```
Kein Firmenname ohne Registereintrag — das Feld verlangt den Namen aus offiziellen Dokumenten,
und die Rechtsseiten nennen ebenfalls eine Privatperson.

### 3 Geschäftsmodell
```
Zielgruppe        nur "Interne Nutzer*innen" - gefragt sind die Nutzer des Clients,
                  nicht das Publikum der Videos
Monetarisierung   nur "Kostenloser Dienst"
Google-Kontakt    nein
Rechteinhaber-ID  leer (Content-Manager-System, nicht zutreffend)
Google Ads        leer
```
Der Freitext steht unten unter *Antragstext*.

### 4 API-Client
```
Clientname        Razarion Social Publisher
Name mit YouTube  nein
Zugriffs-URL      https://github.com/Razarion/razarion/tree/master/razarion-social
Datenschutz       https://www.razarion.com/privacy
Nutzungsbed.      https://www.razarion.com/terms
Öffentlich?       Nein - Einzelbetrieb, nicht verteilt
Demokonto         keins. Begründung im Feld "Besondere Hinweise", siehe unten
```
Ins Feld *Besondere Hinweise für den Zugriff*:

> There is no demo account. The API client is a command-line tool with no login of its own: it
> authorises against my own Google account over OAuth and uploads to my own channel. There is
> nothing for a third party to sign in to.
>
> The client can be reviewed in full instead. The source is public at
> https://github.com/Razarion/razarion/tree/master/razarion-social - the uploader is
> `src/platforms/youtube.mjs`, about 90 lines, and the publishing step that drives it is
> `pipeline/publish_youtube.mjs`. A video produced through exactly this path is public at
> https://www.youtube.com/watch?v=hDmrfUUK2IQ.

### 5 Anwendungsfälle
```
Projektnummern    1
Projektnummer     579831821740      (ID: neural-passkey-426618-j3)
Anwendungsfälle   Video-Upload und Kontoverwaltung
                  Internes Unternehmenstool
OAuth 2.0         Ja
Nutzungsvolumen   niedrigste Stufe - ~4800 Einheiten pro Woche
```

### Die Nachweise

Liegen als Bilder in `pipeline/data/audit/`, aufgenommen von der öffentlichen Seite nach dem
Deploy. Der Ordner ist gitignored und bleibt trotzdem liegen:

```
1-privacy-policy-google-section.jpg      Abschnitt 6 vollständig
2-homepage-youtube-and-privacy-link.jpg  YouTube-Symbol und Privacy-Link im selben Bild
3-terms-of-service.jpg                   Nutzungsbedingungen
4a-oauth-consent-screen.jpg              Zustimmungsbildschirm mit App-Namen
4b-oauth-scopes-and-revoke.jpg           beide Berechtigungen, samt Widerrufshinweis
```

Für den bedingten Nachweis fehlt noch ein Terminal-Screenshot von `check.mjs` und
`publish_youtube.mjs` — das ist die "Upload-Oberfläche" und das "Dashboard" eines Werkzeugs, das
keine grafische Oberfläche hat. Nur von Hand aufzunehmen.

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
