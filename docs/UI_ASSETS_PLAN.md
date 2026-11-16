# Plan zasobów graficznych landing page

Obowiązuje malarski styl 2.5D inspirowany estetyką serialu Arcane. Widoczne pociągnięcia pędzla, wyraziste cieniowanie i geometrycznie zaakcentowane formy budują głębię. Paleta jest głęboka i stonowana, z subtelnymi złotymi refleksami i klimatycznym oświetleniem. Kompozycja pozostaje elegancka i oszczędna.

## Kolejność prac

1. **Choinka z prezentami.** [Zatwierdzony PNG](../design/landing/masters/christmas-tree-master.png) jest docelową ilustracją i wzorcem dla kolejnych grafik. Przygotuj warstwy pochodzące z tego pliku. Zachowaj sylwetkę, prezenty, ozdoby, żarówki i złotą gwiazdę.
2. **Zimowy krajobraz.** Dopasuj perspektywę, fakturę śniegu, cieniowanie i paletę do zatwierdzonej choinki. Szeroki kadr powinien mieć spokojny środek i zapas po bokach do kadrowania na telefonie. Zostaw przestrzeń pod drzewem na cień kontaktowy i ciepłe odbicie lampek oraz spokojne tło pod slogan i CTA.
3. **Chmury.** Jeśli będą potrzebne, przygotuj na końcu jedną lub dwie osobne chmury na przezroczystym tle. Dopasuj ich fakturę i kontrast do krajobrazu.

Instrukcje i prompty dla grafik są w [design/landing/prompts.md](../design/landing/prompts.md).

## Warstwy i wspólna geometria

- **Bazowy raster.** Przygotuj przezroczysty eksport zatwierdzonej choinki z prezentami. Fizyczne żarówki i złota gwiazda należą do rastra. W obu motywach używaj tego samego zasobu. Zatwierdzony PNG zachowaj jako źródło.
- **Dwie lub trzy końcówki gałęzi.** Wydziel je po naturalnych krawędziach, retuszując odsłaniane miejsca na warstwie roboczej. Zapisz punkty obrotu u nasady. Jeżeli na końcówce jest żarówka, zwiąż jej poświatę z ruchem tej końcówki.
- **Oświetlenie.** Użyj maski zgodnej z sylwetką do nocnego przyciemnienia. Dodaj osobną miękką plamę światła na śniegu. Cień kontaktowy zachowuje położenie w obu motywach.
- **Lampki i gwiazda na czubku.** Zapisz środki na zatwierdzonym obrazie i użyj nakładki SVG do animowania jasności poświat.
- **Atmosfera i interfejs.** Niebo, gwiazdy, opad śniegu i wiatr są oddzielnymi efektami. Niewielkie, półprzezroczyste przyciski Liquid Glass i slogan leżą nad ilustracją jako elementy interfejsu.

Ustal wspólny kadr i punkt zakotwiczenia przy podstawie pnia. Raster, maska, poświaty i wycinki gałęzi mają korzystać z tych samych współrzędnych. Jeśli wycinek ma własny kadr, zapisz jego przesunięcie i punkt obrotu względem wspólnego obszaru. Warianty rozdzielczości zachowują proporcje i marginesy. Zmiana motywu zachowuje na danym ekranie wariant pliku, skalę, położenie i bieżącą fazę wiatru.

## Pliki i kontrola jakości

- Pliki źródłowe przechowuj w `design/landing/masters/`.
- Docelowa ścieżka choinki w Vite to `frontend/src/assets/landing/tree/christmas-tree.webp`. Ewentualne warianty rozdzielczości i wycinki gałęzi umieść w tym samym katalogu.
- Krajobraz i opcjonalne chmury umieść w `frontend/src/assets/landing/landscape/`.
- Porównaj scenę dnia i nocy przy tej samej fazie wiatru. Sprawdź sylwetkę, położenie ozdób i prezentów oraz wyrównanie poświat. Na szerokościach 375, 768 i 1440 px sprawdź kadrowanie i czytelność kontrolek. Choreografię oraz kryteria dostępności określa [specyfikacja landing page](UI_LANDING_PAGE.md).
