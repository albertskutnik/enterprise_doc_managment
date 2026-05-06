## 1. Opis pracy inżynierskiej

Praca dotyczy stworzenia aplikacji webowej do zarządzania dokumentami w firmie. System ma pomagać w przechowywaniu plików, porządkowaniu ich oraz kontrolowaniu dostępu użytkowników. Ważne jest też to, aby projekt można było później rozwijać i uruchamiać w różnych środowiskach.

---

## 2. Wstęp teoretyczny

### 2.1. Systemy zarządzania dokumentami

System zarządzania dokumentami to aplikacja, która pozwala firmie przechowywać pliki w jednym miejscu i łatwiej nad nimi panować. Nie chodzi tylko o samo wrzucanie dokumentów, ale też o dostęp, współdzielenie, pobieranie i kontrolę nad tym, kto może wykonać daną operację.

W projektowanym systemie ważne będą przede wszystkim:

- dodawanie i pobieranie dokumentów,
- porządkowanie plików w folderach,
- udostępnianie dokumentów innym użytkownikom,
- przekazywanie plików przez bezpieczne linki,
- obsługa z poziomu przeglądarki.

### 2.2. Przechowywanie plików i S3

Pliki w takim systemie mogą być przechowywane lokalnie na serwerze albo w magazynie obiektowym zgodnym z S3. S3 to sposób przechowywania danych, w którym plik jest traktowany jako osobny obiekt. Każdy obiekt ma swój klucz, a obiekty są trzymane w tzw. bucketach.

Dzięki takiemu podejściu aplikacja nie musi znać dokładnej ścieżki pliku na dysku. Wystarczy, że zna bucket i klucz obiektu. Ułatwia to rozwijanie systemu, przenoszenie go między środowiskami i obsługę większej liczby plików.

W projekcie można przyjąć, że baza danych przechowuje informacje o dokumentach, a same pliki są zapisywane lokalnie albo w storage zgodnym z S3, na przykład MinIO lub Amazon S3.

### 2.3. Architektura heksagonalna

Architektura heksagonalna polega na oddzieleniu logiki aplikacji od szczegółów technicznych. Oznacza to, że główna część systemu nie powinna być mocno związana z konkretną bazą danych, frameworkiem ani sposobem przechowywania plików.

W tym podejściu można wyróżnić:

- logikę biznesową, czyli zasady działania systemu,
- porty, czyli opis operacji dostępnych w aplikacji,
- adaptery, czyli elementy łączące system z bazą danych, API, S3 lub interfejsem użytkownika.

Dla tego projektu jest to dobre rozwiązanie, ponieważ ułatwia testowanie, rozwój i ewentualną zmianę technologii w przyszłości. Na przykład adapter odpowiedzialny za zapis plików można wymienić z lokalnego dysku na S3 bez przepisywania całej logiki systemu.

### 2.4. Skalowalność systemu

System powinien być przygotowany tak, aby mógł działać najpierw dla małej liczby użytkowników, a później zostać rozbudowany. Skalowalność oznacza tutaj możliwość obsługi większej liczby dokumentów, użytkowników i operacji bez konieczności pisania aplikacji od początku.

Pomóc w tym mogą:

- podział aplikacji na moduły,
- przechowywanie plików w S3,
- osobna baza danych dla metadanych,
- konteneryzacja,
- architektura heksagonalna.

### 2.5. Zastosowanie systemu

Taki system może być używany w różnych firmach, na przykład w biurze rachunkowym, firmie logistycznej, software housie albo małej firmie usługowej. W każdym przypadku chodzi o łatwiejsze przechowywanie dokumentów i bezpieczną wymianę plików z pracownikami lub osobami z zewnątrz.

Przykładowe użycia:

- przechowywanie umów, faktur i ofert,
- wysyłanie dokumentów klientom,
- odbieranie plików przez link,
- kontrolowanie dostępu do folderów,
- sprawdzanie historii operacji.

### 2.6. Porównanie z gotowymi rozwiązaniami

Na rynku istnieją gotowe narzędzia, takie jak Google Drive, Dropbox czy SharePoint. Są wygodne, ale nie zawsze dają pełną kontrolę nad sposobem działania systemu, przechowywaniem danych i dopasowaniem do własnych zasad firmy.

Projektowane rozwiązanie ma być prostsze i bardziej dopasowane do założeń pracy. Jego główne cechy to własna logika dostępu, możliwość integracji z S3, architektura heksagonalna i większa kontrola nad wdrożeniem.

### 2.7. Wygoda użytkownika

System powinien być prosty w obsłudze, bo nawet dobrze zaprojektowana technicznie aplikacja nie będzie używana, jeśli będzie niewygodna. Użytkownik powinien szybko dodawać dokumenty, łatwo je znajdować, widzieć swoje uprawnienia i rozumieć komunikaty pojawiające się w aplikacji.

W projekcie ważne będą więc nie tylko kwestie techniczne, ale też czytelny interfejs i wygodna codzienna praca z dokumentami.
