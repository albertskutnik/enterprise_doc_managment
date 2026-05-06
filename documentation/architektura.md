## 1. Architektura systemu

System zostanie zaprojektowany jako aplikacja webowa do zarządzania dokumentami w firmie. Architektura musi wspierać wymagania opisane w analizie: logowanie użytkowników, dodawanie i pobieranie dokumentów, wyszukiwanie, udostępnianie zasobów, obsługę linków zewnętrznych, zapis historii operacji oraz przechowywanie plików lokalnie lub w magazynie zgodnym z S3.

Głównym założeniem jest zastosowanie **architektury heksagonalnej**.

### 1.1. Główne założenia

| Założenie | Znaczenie dla projektu |
|---|---|
| Architektura heksagonalna | oddziela logikę systemu od szczegółów technicznych |
| REST API | pozwala frontendowi komunikować się z backendem |
| PostgreSQL | przechowuje użytkowników, dokumenty, uprawnienia i historię operacji |
| Local/S3 storage | pozwala zapisywać pliki lokalnie lub w magazynie obiektowym |
| Konteneryzacja | ułatwia uruchamianie systemu lokalnie i na serwerze |
| Audyt | pozwala sprawdzić ważne operacje wykonane w systemie |

### 1.2. Podział na warstwy

| Warstwa | Odpowiedzialność |
|---|---|
| Frontend | interfejs użytkownika dostępny w przeglądarce |
| Adapter wejściowy REST | przyjmuje żądania HTTP i przekazuje je do aplikacji |
| Warstwa aplikacyjna | obsługuje przypadki użycia, takie jak dodanie dokumentu i wygenerowanie linku |
| Warstwa domenowa | zawiera najważniejsze reguły biznesowe systemu |
| Porty | opisują operacje wymagane przez aplikację, takie jak zapis pliku i odczyt użytkownika |
| Adaptery wyjściowe | łączą system z bazą danych, storage plików i mechanizmem audytu |


### 1.3. Moduły systemu

| Moduł | Odpowiedzialność | Powiązane wymagania |
|---|---|---|
| Identity | logowanie użytkowników i obsługa ról | FR-01, FR-12 |
| Documents | dodawanie, pobieranie, wyszukiwanie i opisywanie dokumentów | FR-02, FR-03, FR-04 |
| Folders | tworzenie folderów lub przestrzeni roboczych | FR-05 |
| Access Control | sprawdzanie i nadawanie uprawnień | FR-03, FR-06 |
| Sharing | generowanie linków współdzielonych | FR-07 |
| External Access | upload i download przez link dla partnera zewnętrznego | FR-08, FR-09 |
| Storage | zapis plików lokalnie lub w S3 | FR-10 |
| Audit | zapis historii ważnych operacji | FR-11 |


### 1.4. Architektura heksagonalna w projekcie

W architekturze heksagonalnej centrum systemu stanowi logika aplikacji. Zewnętrzne narzędzia, takie jak baza danych, REST API czy S3, są podłączone przez adaptery.

Podział elementów architektury:

| Element | Realizacja w projekcie |
|---|---|
| Rdzeń domenowy | dokument, folder, użytkownik, uprawnienie, link współdzielony |
| Przypadki użycia | dodanie dokumentu, pobranie dokumentu, udostępnienie folderu |
| Port wejściowy | interfejs przypadku użycia wywoływany przez kontroler REST |
| Port wyjściowy | repozytorium dokumentów, repozytorium użytkowników, port storage |
| Adapter wejściowy | kontrolery REST |
| Adapter wyjściowy | PostgreSQL, Local Storage, S3, mechanizm audytu |


### 1.5. Dane trwałe

W systemie należy rozdzielić metadane od fizycznej zawartości plików.

| Typ danych | Miejsce przechowywania |
|---|---|
| Użytkownicy i role | PostgreSQL |
| Dokumenty i metadane | PostgreSQL |
| Foldery i struktura dokumentów | PostgreSQL |
| Uprawnienia | PostgreSQL |
| Linki współdzielone | PostgreSQL |
| Historia operacji | PostgreSQL |
| Pliki binarne | dysk lokalny albo storage zgodny z S3 |

Główne encje systemu:

| Encja | Opis |
|---|---|
| User | konto użytkownika systemu |
| Role | rola użytkownika: administrator albo pracownik |
| Document | opis dokumentu i jego metadane |
| Folder | miejsce grupowania dokumentów |
| Permission | informacja o dostępie użytkownika do zasobu |
| ShareLink | link umożliwiający dostęp zewnętrzny |
| AuditLog | zapis ważnej operacji wykonanej w systemie |
| StoredFile | informacja techniczna o miejscu zapisu pliku |

### 1.6. Storage lokalny i S3

Wymaganie FR-10 zakłada zapis plików lokalnie lub w magazynie zgodnym z S3. Z tego powodu system będzie posiadał wspólny port storage `FileStoragePort`, który udostępnia operacje:

- zapis pliku,
- pobranie pliku,
- usunięcie pliku,
- sprawdzenie istnienia pliku.

Do tego portu zostaną przygotowane dwa adaptery:

| Adapter | Zastosowanie |
|---|---|
| LocalStorageAdapter | zapis plików na dysku lokalnym, dobry do prostego środowiska |
| S3StorageAdapter | zapis plików w Amazon S3, MinIO lub innym storage zgodnym z S3 |

Dzięki temu wybór storage nie wpływa na logikę biznesową. Aplikacja wywołuje port, a konfiguracja decyduje, który adapter zostanie użyty.

### 1.7. Bezpieczeństwo i kontrola dostępu

Bezpieczeństwo wynika z wymagań BR-02, NFR-04 i NFR-05. System powinien chronić dokumenty na kilku poziomach:

| Obszar | Rozwiązanie |
|---|---|
| Logowanie | użytkownik musi być uwierzytelniony przed dostępem do systemu |
| Role | administrator ma szersze uprawnienia niż zwykły użytkownik |
| Uprawnienia do zasobów | dostęp do dokumentów i folderów jest sprawdzany przed operacją |
| Linki współdzielone | każdy link ma token, zakres dostępu i czas ważności |
| Transmisja | komunikacja powinna odbywać się przez HTTPS |
| Audyt | ważne operacje są zapisywane w historii |

Najważniejsza zasada jest taka, że każda operacja na dokumencie musi przejść przez warstwę aplikacyjną. Nie powinno być możliwości pobrania pliku z pominięciem kontroli uprawnień.

### 1.8. Audyt

Audyt realizuje wymaganie FR-11 oraz NFR-09. System powinien zapisywać najważniejsze operacje, szczególnie te związane z bezpieczeństwem i dokumentami.

Zdarzenia audytowe zapisywane przez system:

- logowanie użytkownika,
- dodanie dokumentu,
- pobranie dokumentu,
- nadanie lub odebranie uprawnień,
- wygenerowanie linku współdzielonego,
- użycie linku zewnętrznego,
- nieudana próba dostępu.

Wpis audytowy powinien zawierać co najmniej: typ operacji, identyfikator użytkownika lub linku, czas zdarzenia, identyfikator zasobu i rezultat operacji.

### 1.9. Wdrożenie

System będzie możliwy do uruchomienia lokalnie, na serwerze lub w chmurze. Model wdrożenia opiera się na kontenerach.

| Element | Rola |
|---|---|
| Frontend | aplikacja uruchamiana w przeglądarce |
| Backend | REST API i logika systemu |
| PostgreSQL | baza danych dla metadanych |
| MinIO lub Amazon S3 | przechowywanie plików |
| Reverse proxy | obsługa ruchu HTTP/HTTPS |

W środowisku lokalnym zostanie użyty Docker Compose z backendem, frontendem, PostgreSQL i MinIO. W środowisku produkcyjnym pliki mogą być przechowywane w Amazon S3, ponieważ MinIO i Amazon S3 korzystają z tego samego modelu storage zgodnego z S3.

### 1.10. Użyte technologie

| Obszar | Technologia | Uzasadnienie |
|---|---|---|
| Backend | Java 21 + Spring Boot | stabilny stos do budowy REST API, logiki biznesowej i aplikacji modułowej |
| Frontend | React + TypeScript | budowa czytelnego interfejsu webowego z typowaniem po stronie frontendu |
| Baza danych | PostgreSQL | przechowywanie użytkowników, ról, dokumentów, uprawnień i audytu |
| Storage lokalny | system plików serwera | prosty zapis plików w środowisku lokalnym |
| Storage obiektowy | MinIO w środowisku lokalnym, Amazon S3 w środowisku produkcyjnym | przechowywanie plików w modelu zgodnym z S3 |
| Uwierzytelnianie | JWT | obsługa sesji użytkownika w komunikacji frontend-backend |
| Konteneryzacja | Docker + Docker Compose | uruchamianie backendu, frontendu, bazy danych i MinIO |
| Reverse proxy | Nginx | obsługa ruchu HTTP/HTTPS oraz przekazywanie żądań do aplikacji |
