## 1. Analiza wymagań

### 1.1. Interesariusze

| Id | Interesariusz | Rola |
|---|---|---|
| ST-01 | Pracownik | dodaje, pobiera, wyszukuje i udostępnia dokumenty |
| ST-02 | Administrator | zarządza użytkownikami, rolami i konfiguracją systemu |
| ST-03 | Partner zewnętrzny | korzysta z dokumentów udostępnionych przez link |
| ST-04 | Dział IT | wdraża system i dba o jego działanie |

### 1.2. Wymagania biznesowe

| Id | Wymaganie |
|---|---|
| BR-01 | System ma usprawnić przechowywanie i obieg dokumentów w firmie |
| BR-02 | System ma zwiększyć bezpieczeństwo dostępu do dokumentów |
| BR-03 | System ma umożliwić kontrolowane udostępnianie plików |
| BR-04 | System ma pozwalać na współpracę z osobami spoza firmy |
| BR-05 | System ma być możliwy do uruchomienia lokalnie, na serwerze lub w chmurze |
| BR-06 | System ma być przygotowany do dalszej rozbudowy |

### 1.3. Wymagania użytkownika

| Id | Wymaganie |
|---|---|
| UR-01 | Użytkownik chce zalogować się do systemu |
| UR-02 | Użytkownik chce dodać dokument |
| UR-03 | Użytkownik chce pobrać dokument, do którego ma dostęp |
| UR-04 | Użytkownik chce wyszukać dokument |
| UR-05 | Użytkownik chce udostępnić dokument lub folder |
| UR-06 | Partner zewnętrzny chce pobrać plik przez link |
| UR-07 | Partner zewnętrzny chce przesłać plik przez link |
| UR-08 | Administrator chce zarządzać użytkownikami i rolami |
| UR-09 | Administrator chce sprawdzić historię operacji |
| UR-10 | Organizacja chce przechowywać pliki lokalnie lub w S3 |

### 1.4. Wymagania funkcjonalne

| Id | Wymaganie |
|---|---|
| FR-01 | System umożliwia logowanie użytkowników |
| FR-02 | System umożliwia dodawanie dokumentów z podstawowymi metadanymi |
| FR-03 | System umożliwia pobieranie dokumentów zgodnie z uprawnieniami |
| FR-04 | System umożliwia wyszukiwanie dokumentów |
| FR-05 | System umożliwia tworzenie folderów lub przestrzeni roboczych |
| FR-06 | System umożliwia nadawanie i odbieranie dostępu do dokumentów |
| FR-07 | System umożliwia generowanie linków współdzielonych |
| FR-08 | System umożliwia przesyłanie plików przez link |
| FR-09 | System umożliwia pobieranie plików przez link |
| FR-10 | System zapisuje pliki lokalnie lub w magazynie zgodnym z S3 |
| FR-11 | System zapisuje historię ważnych operacji |
| FR-12 | System umożliwia administratorowi zarządzanie użytkownikami i rolami |

### 1.5. Wymagania niefunkcjonalne

| Id | Wymaganie |
|---|---|
| NFR-01 | System ma być oparty na architekturze heksagonalnej |
| NFR-02 | System ma być możliwy do uruchomienia w kontenerach |
| NFR-03 | System ma być dostępny przez przeglądarkę internetową |
| NFR-04 | System ma chronić dane podczas przesyłania i przechowywania |
| NFR-05 | System ma kontrolować dostęp do dokumentów na podstawie uprawnień |
| NFR-06 | System ma umożliwiać testowanie logiki bez pełnej infrastruktury |
| NFR-07 | System ma być przygotowany do obsługi co najmniej 10 pracowników |
| NFR-08 | System ma pozwalać na dalszą rozbudowę |
| NFR-09 | System ma rejestrować operacje istotne dla bezpieczeństwa |
| NFR-10 | Interfejs systemu ma być prosty i czytelny dla użytkownika |

### 1.6. Powiązanie wymagań

| Wymaganie biznesowe | Wymagania użytkownika | Wymagania funkcjonalne | Wymagania niefunkcjonalne |
|---|---|---|---|
| BR-01 | UR-02, UR-03, UR-04 | FR-02, FR-03, FR-04, FR-05 | NFR-03, NFR-10 |
| BR-02 | UR-01, UR-03, UR-09 | FR-01, FR-03, FR-06, FR-11 | NFR-04, NFR-05, NFR-09 |
| BR-03 | UR-05 | FR-06, FR-07 | NFR-04, NFR-05 |
| BR-04 | UR-06, UR-07 | FR-07, FR-08, FR-09 | NFR-04, NFR-09 |
| BR-05 | UR-10 | FR-10 | NFR-01, NFR-02, NFR-06 |
| BR-06 | UR-02, UR-08, UR-10 | FR-10, FR-12 | NFR-07, NFR-08 |

### 1.7. Przypadki użycia

| Id | Przypadek użycia | Aktor |
|---|---|---|
| UC-01 | Logowanie do systemu | Użytkownik |
| UC-02 | Dodanie dokumentu | Użytkownik |
| UC-03 | Pobranie dokumentu | Użytkownik |
| UC-04 | Wyszukanie dokumentu | Użytkownik |
| UC-05 | Udostępnienie dokumentu lub folderu | Użytkownik |
| UC-06 | Przesłanie pliku przez link | Partner zewnętrzny |
| UC-07 | Pobranie pliku przez link | Partner zewnętrzny |
| UC-08 | Zarządzanie użytkownikami i rolami | Administrator |
| UC-09 | Przegląd historii operacji | Administrator |

#### UC-02. Dodanie dokumentu

| Pole | Opis |
|---|---|
| Aktor | Użytkownik |
| Warunek wstępny | użytkownik jest zalogowany i ma prawo dodawania plików |
| Scenariusz | użytkownik wybiera plik, uzupełnia dane, system sprawdza uprawnienia, zapisuje plik i metadane |
| Rezultat | dokument jest dostępny w systemie |
| Wyjątki | brak uprawnień, zbyt duży plik, błąd zapisu |

#### UC-05. Udostępnienie dokumentu lub folderu

| Pole | Opis |
|---|---|
| Aktor | Użytkownik |
| Warunek wstępny | użytkownik ma prawo do danego zasobu |
| Scenariusz | użytkownik wybiera zasób, określa zakres dostępu i generuje link lub nadaje uprawnienia |
| Rezultat | dokument lub folder zostaje udostępniony |
| Wyjątki | brak uprawnień, błędny zakres dostępu, wygasły link |

#### UC-06. Przesłanie pliku przez link

| Pole | Opis |
|---|---|
| Aktor | Partner zewnętrzny |
| Warunek wstępny | partner ma ważny link z uprawnieniem do przesyłania plików |
| Scenariusz | partner otwiera link, wybiera plik, system sprawdza link i zapisuje plik |
| Rezultat | plik trafia do wskazanego folderu lub zasobu |
| Wyjątki | link wygasł, brak uprawnienia, zbyt duży plik |

### 1.8. Potwierdzenie wymagań

| Wymaganie | Sposób sprawdzenia |
|---|---|
| FR-01 | test logowania |
| FR-02 | test dodawania dokumentu |
| FR-03 | test pobierania dokumentu z kontrolą uprawnień |
| FR-07 | test generowania linku współdzielonego |
| FR-08 | test przesyłania pliku przez link |
| FR-10 | test zapisu pliku lokalnie lub w S3 |
| FR-11 | sprawdzenie wpisów w historii operacji |
| NFR-01 | sprawdzenie struktury projektu |
| NFR-04 | sprawdzenie sposobu ochrony danych |
| NFR-07 | test działania dla zakładanej liczby użytkowników |
