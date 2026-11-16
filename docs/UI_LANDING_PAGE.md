# SECRET SANTA — Landing Page UI/UX Specification v1.0

- **Status:** Kierunek artystyczny i ilustracja choinki zatwierdzone
- **Technologie:** React, TypeScript, Vite, Tailwind CSS, Motion for React
- **Cel:** Stworzenie wyjątkowej, responsywnej strony głównej aplikacji Secret Santa.

## 1. Wizja artystyczna

Strona ma przedstawiać bajkową, zimową scenerię z centralnie umieszczoną choinką.

Styl:

- Malarska ilustracja 2.5D inspirowana estetyką serialu Arcane.
- Widoczne pociągnięcia pędzla, wyraziste stylizowane cieniowanie i geometrycznie zaakcentowane formy.
- Głębokie, stonowane kolory, subtelne złote refleksy i klimatyczne oświetlenie budujące głębię ilustracji.
- Elegancki, ciepły, magiczny charakter.
- Minimalna liczba elementów interfejsu.
- Interfejs inspirowany Apple Liquid Glass.
- Grafika stanowi główne tło, na które nakładane są przyciski i tekst.

Strona nie może wyglądać pstrokato ani jak przeładowana ilustracja dziecięca.

## 2. Główny element — choinka

Choinka jest centralnym elementem ekranu. Zatwierdzona ilustracja [christmas-tree-master.png](../design/landing/masters/christmas-tree-master.png) stanowi wzorzec dla wszystkich kolejnych ilustracji i źródło drzewa używanego w obu motywach.

Wymagania:

- Jedna i ta sama choinka w trybie jasnym i ciemnym.
- Identyczna geometria, położenie, ozdoby i prezenty.
- Na czubku złota gwiazda.
- Na gałęziach lampki świąteczne.
- Kilka subtelnych prezentów pod choinką.
- Miękko stylizowany śnieg i zimowy krajobraz.

**ZABRONIONE:**

- Podmienianie całej ilustracji choinki podczas zmiany motywu.
- Generowanie dwóch różnych drzew dla dnia i nocy.
- Przeskakiwanie lub resetowanie pozycji choinki przy przełączaniu.

Z zatwierdzonego rastra przygotuj wspólny zasób i niezależnie animowane warstwy. Poświaty lampek, gwiazdy i podłoża nakładaj osobno. Sposób przygotowania warstw opisuje [plan zasobów](UI_ASSETS_PLAN.md).

## 3. Tryb jasny — DAY

Stan początkowy:

- Jasne, spokojne zimowe niebo.
- Odcienie kremu, pastelowego błękitu i jasnej zieleni.
- Naturalne dzienne oświetlenie choinki.
- Lampki zgaszone.
- Złota gwiazda bez mocnego blasku.
- Delikatny ruch dwóch lub trzech wybranych końcówek gałązek.
- Subtelny wiatr, np. powoli przesuwające się chmurki.
- Brak opadającego śniegu.

Całość powinna być pogodna, spokojna i przestrzenna.

## 4. Tryb ciemny — NIGHT

Po aktywacji:

- Niebo przyjmuje głęboki granatowy odcień.
- Stopniowo pojawiają się gwiazdy.
- Choinka pozostaje w dokładnie tym samym miejscu.
- Lampki zapalają się ciepłym, złotym światłem.
- Złota gwiazda na czubku zaczyna delikatnie świecić.
- Rozpoczyna się powolny opad śniegu.
- Wokół choinki pojawia się subtelne ciepłe oświetlenie.
- Sceneria zachowuje bajkowy, spokojny charakter.

Nie stosować agresywnego migotania ani szybkich animacji.

## 5. Przejście DAY ↔ NIGHT — najwyższy priorytet

Zmiana motywu musi być jedną płynną transformacją tej samej sceny.

**Docelowy czas głównej transformacji: około 2,4 sekundy.**

Proponowana choreografia DAY → NIGHT:

| Czas      | Zdarzenie                                    |
| --------- | -------------------------------------------- |
| 0–0,4 s   | Niebo zaczyna stopniowo ciemnieć             |
| 0,3–1,5 s | Zmienia się oświetlenie krajobrazu i choinki |
| 0,7–1,7 s | Pojawiają się gwiazdy na niebie              |
| 1,0–2,0 s | Lampki choinki rozświetlają się              |
| 1,3–2,2 s | Gwiazda na czubku zyskuje delikatną poświatę |
| 1,6–2,4 s | Stopniowo pojawia się opad śniegu            |

Powrót NIGHT → DAY odwraca sekwencję.

Wymagania:

- Żadnego przeładowywania strony.
- Żadnych nagłych zmian grafiki.
- Choinka i ozdoby zachowują swoje położenie.
- Zmieniają się oświetlenie, barwy, widoczność efektów i atmosfera.
- Przerwanie animacji kolejnym kliknięciem nie może powodować przeskoków; stan powinien płynnie zmierzać do nowego celu.
- Wybór motywu powinien być zapamiętywany lokalnie.
- Przy pierwszej wizycie motyw może bazować na preferencji systemowej.

Czas animacji jest wartością projektową i może zostać dopracowany podczas testów wizualnych.

## 6. Kompozycja Landing Page

### Header

Lewy górny róg:

- Niewielkie logo i nazwa Secret Santa.

Prawy górny róg:

- Szklany przełącznik dzień/noc z ikonami słońca i księżyca.
- Szklany przycisk „Zaloguj się”.

Header jest nałożony na ilustrację i nie zajmuje oddzielnego masywnego paska.

### Hero

- Duża, wyśrodkowana choinka.
- Choinka pozostaje głównym punktem kompozycji.
- Minimalna liczba dodatkowych elementów krajobrazu.
- W dolnej części scenerii krótki slogan:

**Jedna osoba. Jeden prezent. Więcej magii.**

### Przyciski CTA

Dwa przyciski bezpośrednio na tle ilustracji:

1. **Zorganizuj losowanie**
2. **Dołącz do wydarzenia**

Przyciski powinny być niewielkie wizualnie, ale wygodne do kliknięcia.

Unikać dużego, mlecznego panelu zajmującego dolną część ekranu.

## 7. Styl Liquid Glass

Interfejs ma przypominać półprzezroczyste szkło unoszące się nad ilustracją.

Cechy:

- Lekko przezroczyste wypełnienie.
- Rozmycie tła (`backdrop-filter`).
- Cienka, jasna obwódka.
- Delikatne refleksy światła.
- Miękkie cienie i zaokrąglone narożniki.
- Naturalna zmiana wyglądu zależnie od trybu dzień/noc.

Główny przycisk:

- Szkło z subtelnym zielonym odcieniem.
- Wyraźny, kontrastowy napis.
- Lekka poświata przy interakcji.

Drugi przycisk:

- Bardziej neutralny i przezroczysty.
- Nadal dobrze czytelny na jasnym i ciemnym tle.

Nie próbować kopiować natywnego renderera Apple. Implementować podobny efekt z użyciem standardowych technologii webowych.

## 8. Interakcje

### Hover

- Delikatne rozświetlenie obramowania.
- Minimalna zmiana skali, około 1,02.
- Płynny czas reakcji, około 180–250 ms.

### Click

- Krótkie, subtelne wrażenie naciśnięcia.
- Brak przesadnego odbijania lub agresywnych efektów.

### Theme toggle

- Animowana zmiana stanu.
- Dostępny z klawiatury.
- Odpowiednia nazwa dla technologii asystujących.

## 9. Responsywność

Podejście mobile-first.

### Desktop

- Sceneria zajmuje całą dostępną przestrzeń ekranu.
- Choinka jest centralna i duża.
- Slogan i przyciski znajdują się w dolnej części sceny.
- CTA ustawione obok siebie.

### Tablet

- Proporcjonalnie zmniejszona choinka.
- Zachowanie centralnej kompozycji.
- Brak nachodzenia tekstu na ważne detale.

### Mobile

- Choinka pozostaje widoczna i centralna.
- Sceneria kadrowana bez deformowania choinki.
- CTA mogą być ułożone pionowo.
- Minimalna wysokość interaktywnych elementów około 48 px.
- Tekst, przełącznik i przyciski muszą być łatwe w obsłudze.
- Nie dopuszczać do poziomego przewijania.
- Uwzględnić małe ekrany i bezpieczne obszary urządzeń.

Testować co najmniej szerokości: 375, 768 i 1440 px.

## 10. Wydajność i dostępność

- Animacje muszą być płynne na typowym smartfonie.
- Unikać niepotrzebnych kosztownych efektów graficznych.
- Opad śniegu powinien być lekki i ograniczony liczbowo.
- Używać CSS i Motion for React tam, gdzie to uzasadnione.
- Uwzględnić `prefers-reduced-motion`.
- W trybie ograniczonego ruchu wyłączyć ciągłe ruchy oraz skrócić transformacje.
- Zapewnić czytelny kontrast tekstu na szklanych powierzchniach.
- Zapewnić widoczny focus klawiatury.
- Zachować funkcjonalność, gdy `backdrop-filter` nie jest dostępny.

## 11. Nawigacja

„Zorganizuj losowanie” → rejestracja / logowanie organizatora.

„Dołącz do wydarzenia” → ekran wejścia do wydarzenia.

„Zaloguj się” → ekran logowania organizatora.

Na etapie projektowania Landing Page można zastosować przygotowane trasy bez implementowania logiki backendowej.

## 12. Ograniczenia zakresu

Ta specyfikacja dotyczy wyłącznie publicznej strony głównej.

Nie implementować jeszcze:

- Autoryzacji.
- Tworzenia wydarzeń.
- Zarządzania uczestnikami.
- Algorytmu losowania.
- Komunikacji ze Spring Boot.
- Bazy danych.

Architektura projektu nadal zakłada React + TypeScript na frontendzie i Spring Boot na backendzie.

## 13. Kryteria akceptacji

Landing Page uznamy za gotowy, gdy:

- Choinka wygląda identycznie geometrycznie w obu motywach.
- Przejście dzień/noc jest ciągłe i płynne.
- Lampki i gwiazda stopniowo się rozświetlają.
- Opad śniegu pojawia się płynnie.
- Wszystkie przyciski wizualnie współgrają ze scenerią.
- Interfejs jest czytelny i wygodny na telefonie.
- Brak błędów w konsoli przeglądarki.
- Nie ma niekontrolowanych przeskoków układu.
- Animacje uwzględniają ograniczenie ruchu.
- Kod React jest podzielony na czytelne komponenty i możliwy do analizy przez początkującego programistę.

## 14. Zasady pracy agenta Codex

Przed implementacją:

1. Przeczytaj całą specyfikację.
2. Zaproponuj strukturę komponentów React.
3. Zaproponuj technikę budowy jednej choinki z niezależnymi warstwami.
4. Wyjaśnij, jak zapewnisz płynne przejście DAY/NIGHT.
5. Wskaż potrzebne zasoby graficzne i sposób ich przygotowania.
6. Nie zastępuj brakujących materiałów przypadkowymi grafikami ani emoji.
7. Nie rozpoczynaj implementacji bez zatwierdzenia planu.

Po zatwierdzeniu realizuj małe etapy, prezentuj zmiany w plikach i wyjaśniaj kod.
