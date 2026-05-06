## 1. Bezpieczeństwo i audyt

### 1.1. Uwierzytelnianie użytkowników

Użytkownik będzie logował się do systemu za pomocą loginu i hasła. Po poprawnym logowaniu backend wygeneruje token JWT, który frontend będzie dołączał do kolejnych żądań.

| Element | Sposób realizacji |
|---|---|
| Logowanie | login i hasło |
| Obsługa sesji | token JWT |
| Backend | Spring Security |
| Hasła | zapis w bazie wyłącznie jako hash |
| Algorytm haseł | BCrypt |
| Baza użytkowników | PostgreSQL |

### 1.2. Autoryzacja i kontrola dostępu

Samo zalogowanie użytkownika nie oznacza dostępu do wszystkich dokumentów. Każda operacja na pliku musi zostać sprawdzona przez backend.

System będzie wykorzystywał dwa poziomy kontroli:

- role użytkowników, np. administrator i pracownik,
- uprawnienia do konkretnych dokumentów lub folderów.

| Operacja | Kontrola dostępu |
|---|---|
| Dodanie dokumentu | użytkownik musi być zalogowany |
| Pobranie dokumentu | użytkownik musi mieć dostęp do dokumentu |
| Udostępnienie dokumentu | użytkownik musi mieć prawo do danego zasobu |
| Zarządzanie użytkownikami | dostęp tylko dla administratora |
| Użycie linku zewnętrznego | system sprawdza token, zakres dostępu i czas ważności |

Kontrola dostępu będzie wykonywana w warstwie aplikacyjnej backendu.

### 1.3. Przechowywanie plików

W systemie zostanie rozdzielone przechowywanie metadanych od przechowywania właściwych plików. 

| Rodzaj danych | Miejsce przechowywania |
|---|---|
| Użytkownicy | PostgreSQL |
| Role i uprawnienia | PostgreSQL |
| Metadane dokumentów | PostgreSQL |
| Linki współdzielone | PostgreSQL |
| Logi audytowe | PostgreSQL |
| Pliki w środowisku lokalnym | lokalny system plików lub MinIO |
| Pliki w środowisku produkcyjnym | Amazon S3 |

Backend nie będzie zapisywał w bazie danych całych plików binarnych. W bazie zostaną zapisane tylko informacje potrzebne do identyfikacji dokumentu, takie jak nazwa, właściciel, typ, rozmiar, data dodania oraz klucz pliku w storage.

### 1.4. Zapis plików lokalnie i w S3

Za zapis plików będzie odpowiadał wspólny port. Dzięki temu logika aplikacji nie będzie zależeć od konkretnego sposobu przechowywania plików.

| Adapter | Zastosowanie |
|---|---|
| LocalStorageAdapter | zapis plików na dysku serwera |
| S3StorageAdapter | zapis plików w MinIO lub Amazon S3 |

Przy dodaniu dokumentu system wykona następujące kroki:

1. sprawdzi, czy użytkownik jest zalogowany,
2. sprawdzi uprawnienie do dodania dokumentu,
3. zweryfikuje nazwę, typ i rozmiar pliku,
4. zapisze plik w wybranym storage,
5. zapisze metadane dokumentu w PostgreSQL,
6. zapisze log audytowy o dodaniu dokumentu.

W przypadku S3 plik będzie przechowywany jako obiekt w bucketcie. System zapisze w bazie danych klucz obiektu, dzięki któremu będzie można później pobrać plik przez backend.

### 1.5. Ochrona plików

Pliki nie powinny być dostępne bezpośrednio z publicznego adresu. Pobieranie dokumentów będzie odbywać się przez backend, który najpierw sprawdzi uprawnienia użytkownika lub ważność linku współdzielonego.

| Mechanizm | Cel |
|---|---|
| Kontrola uprawnień przed pobraniem | ochrona przed dostępem do cudzych dokumentów |
| Niepubliczny bucket S3 | brak bezpośredniego dostępu do plików z internetu |
| Losowe klucze plików | utrudnienie odgadnięcia lokalizacji pliku |
| Limity rozmiaru pliku | ochrona przed przeciążeniem systemu |
| Walidacja typu pliku | ograniczenie ryzyka przesłania niepożądanych plików |
| HTTPS | ochrona danych przesyłanych między użytkownikiem i aplikacją |

Dla plików przechowywanych w S3 można wykorzystać mechanizmy szyfrowania po stronie storage. Dodatkowo system może przechowywać sumę kontrolną pliku, aby sprawdzić, czy plik nie został zmieniony poza aplikacją.

### 1.6. Linki współdzielone

Linki współdzielone będą umożliwiały dostęp do dokumentu lub folderu osobom spoza firmy.


| Typ linku | Możliwe działanie |
|---|---|
| Link do pobierania | partner zewnętrzny może pobrać wskazany plik |
| Link do przesyłania | partner zewnętrzny może przesłać plik do wskazanego folderu |


### 1.7. Logi audytowe

Logi audytowe będą zapisywane w PostgreSQL w osobnej tabeli. 

| Zdarzenie |
|---|
| Poprawne logowanie |
| Nieudane logowanie |
| Dodanie dokumentu |
| Pobranie dokumentu |
| Usunięcie dokumentu |
| Nadanie uprawnień |
| Odebranie uprawnień |
| Wygenerowanie linku |
| Użycie linku zewnętrznego |
| Nieudana próba dostępu |

### 1.8. Zakres danych w logu audytowym

Każdy wpis audytowy powinien zawierać zestaw informacji pozwalających odtworzyć przebieg zdarzenia.

| Pole | Znaczenie |
|---|---|
| Id | identyfikator wpisu audytowego |
| Typ zdarzenia | rodzaj operacji, np. dodanie dokumentu lub pobranie pliku |
| Id użytkownika | użytkownik wykonujący operację |
| Id zasobu | dokument, folder albo link, którego dotyczy operacja |
| Data i czas | moment wykonania operacji |
| Adres IP | adres, z którego wykonano żądanie |
| Rezultat | informacja, czy operacja zakończyła się powodzeniem |
| Opis błędu | powód odrzucenia operacji, jeśli wystąpił błąd |

W logach audytowych nie należy zapisywać haseł, tokenów JWT ani pełnej treści dokumentów. Logi mają opisywać operacje, ale nie mogą same tworzyć dodatkowego ryzyka bezpieczeństwa.

### 1.9. Wykorzystanie logów do audytu

Administrator będzie mógł wykorzystać logi do sprawdzenia historii działań w systemie.
Logi powinny być możliwe do filtrowania po użytkowniku, dokumencie, typie zdarzenia oraz zakresie dat. Dzięki temu administrator będzie mógł szybko znaleźć zdarzenia potrzebne do kontroli bezpieczeństwa.

### 1.10. Monitoring i obsługa błędów

Oprócz logów audytowych system będzie posiadał logi techniczne aplikacji. 

| Rodzaj logu | Zastosowanie |
|---|---|
| Log audytowy | kontrola operacji użytkowników |
| Log aplikacyjny | diagnozowanie błędów działania systemu |
| Log bezpieczeństwa | analiza prób nieuprawnionego dostępu |
| Log storage | sprawdzanie problemów z zapisem i odczytem plików |

### 1.11. Podsumowanie

Bezpieczeństwo systemu będzie oparte na logowaniu JWT, kontroli ról i uprawnień, ochronie plików oraz zapisie logów audytowych. Pliki będą przechowywane poza bazą danych, lokalnie lub w storage zgodnym z S3, natomiast PostgreSQL będzie przechowywał metadane, uprawnienia i historię operacji.
