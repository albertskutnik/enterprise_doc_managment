### 1. Wprowadzenie
Celem projektu jest stworzenie webowej aplikacji do zarządzania dokumentami, umożliwiającej ich bezpieczne składowanie, udostępnianie oraz elastyczne wdrażanie. System oparty na architekturze heksagonalnej ma rozwiązać problemy rozproszenia plików, braku kontroli dostępu i ułatwić współpracę zewnętrzną.

### 2. Metodologia wytwarzania
Projekt realizowany będzie iteracyjnie jako indywidualna praca inżynierska, łącząc prace analityczno-projektowe z programowaniem. Do śledzenia postępów, wersjonowania kodu i dokumentacji wykorzystane zostanie repozytorium GitHub.

### 4. Definicja architektury**
Przyjęto architekturę heksagonalną, co pozwoli uniezależnić logikę biznesową od warstwy infrastruktury. Główne technologie to Java/Spring Boot (backend), React (frontend), relacyjna baza PostgreSQL dla metadanych, magazyn S3 dla plików oraz konteneryzacja Docker.

### 5. Dane trwałe**
Model danych zakłada ścisły podział: metadane (informacje o użytkownikach, uprawnieniach i plikach) trafią do bazy PostgreSQL, a fizyczne pliki do magazynu S3. Każdy zapis dokumentu będzie wieloetapowy i obejmie walidację, transfer do bucketa, zapis w bazie oraz logowanie w systemie audytowym.

### 6. Specyfikacja analityczna i projektowa**
Komunikacja z aplikacją oprze się na REST API, a sama logika zostanie podzielona na moduły (m.in. tożsamość, zarządzanie, współdzielenie, audyt). Kluczowe reguły zakładają ścisłą autoryzację każdej operacji oraz udostępnianie plików podmiotom zewnętrznym wyłącznie przez kontrolowane, czasowe linki.

### 7. Projekt standardu interfejsu użytkownika**
Znajdą się tu założenia dotyczące wyglądu, nawigacji oraz użyteczności frontendowej części aplikacji. 

### 8. Specyfika testów**
Jakość systemu zostanie zweryfikowana przez testy jednostkowe, integracyjne, funkcjonalne, bezpieczeństwa oraz obciążeniowe. Zagwarantuje to poprawne działanie logiki biznesowej, stabilną komunikację z S3 i bazą oraz odporność na nieuprawniony dostęp.

### 9. Wirtualizacja i konteneryzacja**
Cały system (frontend, backend, baza, S3, reverse proxy) zostanie podzielony na niezależne kontenery Docker. Zapewni to łatwość lokalnego uruchamiania projektu oraz pełną powtarzalność środowiska produkcyjnego.

### 10. Bezpieczeństwo**

### 11-14. Podręczniki, Podsumowanie, Bibliografia**

### Jak uruchomic demo

1. Uruchom baze danych:

```bash
docker compose up -d
```

2. Uruchom backend:

```bash
cd backend
mvn spring-boot:run
```

3. Wgraj plik:

```bash
curl -F "file=@README.md" http://localhost:8080/api/documents
```

4. Wyswietl dokumenty:

```bash
curl http://localhost:8080/api/documents
```

5. Utworz link do pobrania. W miejsce DOCUMENT_ID wpisz id dokumentu z poprzedniej komendy:

```bash
curl -X POST http://localhost:8080/api/documents/DOCUMENT_ID/share-links \
  -H "Content-Type: application/json" \
  -d '{"expiresInHours":24}'
```

6. Pobierz plik przez link. Uzyj pola downloadUrl z poprzedniej odpowiedzi:

```bash
curl -O -J "DOWNLOAD_URL"
```
