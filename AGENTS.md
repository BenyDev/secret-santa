# Instrukcje projektu Secret Santa

Secret Santa służy do organizowania świątecznych losowań prezentów.

## Technologie i repozytorium

- `frontend/` — React, TypeScript, Vite, Tailwind CSS i Motion for React.
- `backend/` — Java 21, Spring Boot, Spring Data JPA, Spring Security, PostgreSQL, Flyway i Maven.
- `docs/` — dokumentacja projektu.
- `design/` — źródłowe zasoby graficzne. Gotowe zasoby aplikacji trafiają do `frontend/src/assets/`.

## Architektura

- Utrzymuj prostą, czytelną strukturę projektu. Frontend komunikuje się z backendem przez REST API.
- Rozwijaj aplikację małymi, testowalnymi funkcjonalnościami. Unikaj mikroserwisów i zbędnych zależności.
- Uzasadnij proponowaną zmianę architektury i uzyskaj zatwierdzenie przed jej wdrożeniem.
- W backendzie dbaj o poprawność relacji, transakcyjność i kontrolę uprawnień.

## Reguły biznesowe

- Organizator rejestruje konto przez e-mail i hasło. Może utworzyć wiele wydarzeń i zawsze uczestniczy w swoim wydarzeniu.
- Pozostali uczestnicy mogą brać udział bez konta. Można ich dodać ręcznie lub przez link ogólny. Każdy uczestnik może otrzymać unikalny link indywidualny.
- Organizator ustala limit kwoty prezentu i zamyka listę uczestników przed losowaniem. Po zamknięciu lista jest niezmienna.
- Każdy uczestnik kupuje prezent dokładnie jednej osobie i otrzymuje dokładnie jeden prezent. Losowanie siebie jest wykluczone.
- Wyniki losowania są prywatne. Listę życzeń widzą tylko jej właściciel i osoba, która go wylosowała. Rola organizatora nie daje automatycznego wglądu w cudze wyniki ani listy życzeń.
- Sugestie prezentowe są widoczne dla osób z dostępem do wydarzenia. Link ogólny pozwala dodawać je anonimowo.

Szczegółowe wymagania biznesowe utrzymuj w dokumentacji projektu. Unikaj powielania ich w kilku plikach.

## Zasady wizualne

- Stosuj malarski styl 2.5D inspirowany estetyką serialu Arcane. [Zatwierdzona choinka](design/landing/masters/christmas-tree-master.png) jest wzorcem dla kolejnych ilustracji oraz wspólnym źródłem dla obu motywów.
- Landing page ma być bajkowy i klimatyczny. Pozostałe ekrany projektuj prościej, z naciskiem na czytelność i subtelne efekty Liquid Glass.
- Projektuj od urządzeń mobilnych i dbaj o płynność animacji.
- Używaj zatwierdzonych grafik. Zastąpienie ich przypadkowymi ilustracjami lub emoji jest niedopuszczalne. Zmiany kierunku artystycznego wymagają zatwierdzenia.

## Współpraca

Właściciel projektu uczy się programowania z pomocą AI.

1. Przed większą zmianą przedstaw krótki plan.
2. Pracuj w zatwierdzonym zakresie i wdrażaj wyłącznie uzgodnione funkcjonalności.
3. Wyjaśniaj istotne decyzje techniczne prostym językiem.
4. Po zmianach podsumuj wynik i wskaż utworzone oraz zmodyfikowane pliki.
5. Uruchamiaj odpowiednie testy lub kontrole i podawaj wyniki. Zgłaszaj błędy, problemy i ograniczenia weryfikacji.
6. Commit i push do GitHub wykonuj wyłącznie na polecenie właściciela.
7. Zachowuj istniejące pliki i konfigurację poza zakresem zadania.

## Dokumentacja według zadania

Czytaj dokumenty istotne dla bieżącej zmiany.

- Wygląd, animacje i dostępność strony głównej opisuje [specyfikacja landing page](docs/UI_LANDING_PAGE.md).
- Warstwy, eksport i położenie plików opisuje [plan zasobów graficznych](docs/UI_ASSETS_PLAN.md).
- Kierunek artystyczny i prompty są w [dokumentacji ilustracji](design/landing/prompts.md).
