# JobSystem

## Beschreibung
Das **JobSystem** ist ein Minecraft-Plugin, das eine GUI-basierte Jobverwaltung bietet. Spieler können mit dem Befehl `/jobs` eine grafische Benutzeroberfläche öffnen, um verschiedene Jobs auszuwählen oder ihre aktuellen Jobs zu verwalten. Seit Version **1.1** stehen zusätzliche Berufe zur Auswahl und das Menü umfasst nun zwei Reihen.

## Funktionen
- **GUI-basiertes Job-System**: Die Benutzeroberfläche wird mit `/jobs` geöffnet.
- **Verschiedene Jobs verfügbar**: Spieler können aus mehreren Berufen wählen, darunter seit Version 1.1 auch *Alchemist* und *Verzauberer*.
- **Fortschrittsspeicherung**: Fortschritte werden gespeichert und können abgerufen werden.
- **Belohnungen**: Spieler erhalten Belohnungen basierend auf ihrer Arbeit.
- **Konfigurierbar**: Anpassbare GUI, Belohnungen und Jobbedingungen.

## Installation
1. Lade das Plugin herunter und platziere es im `plugins`-Ordner deines Minecraft-Servers.
2. Starte den Server neu oder lade das Plugin mit `/reload`.
3. Konfiguriere das Plugin in der `config.yml`, falls gewünscht.

## Befehle
| Befehl       | Beschreibung                      |
|-------------|----------------------------------|
| `/jobs`      | Öffnet die GUI zur Jobauswahl  |
| `/jobs join <job>` | Einem bestimmten Job beitreten |
| `/jobs leave <job>` | Einen Job verlassen |
| `/jobs info <job>` | Informationen zu einem Job anzeigen |

## Berechtigungen
| Permission             | Beschreibung |
|----------------------|-------------|
| `jobs.use`          | Zugriff auf die GUI |
| `jobs.admin`        | Administrative Befehle nutzen |

## Konfiguration
Die Konfigurationsdatei befindet sich unter `plugins/JobSystem/config.yml` und ermöglicht individuelle Anpassungen der Jobs, Belohnungen und GUI-Einstellungen.

## Lizenz
Dieses Plugin wird unter der **MIT-Lizenz** bereitgestellt. Mehr Informationen findest du in der Datei `LICENSE`.

## Kontakt
Falls du Fragen oder Verbesserungsvorschläge hast, kannst du dich an den Entwickler wenden oder ein Issue im Repository erstellen.

