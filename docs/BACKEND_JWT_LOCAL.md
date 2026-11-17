# Lokalne logowanie JWT

## Klucze RSA

Uruchom generator z katalogu `backend`. Java 21 uruchamia plik źródłowy bez wcześniejszej kompilacji. Generator nie potrzebuje dodatkowych programów.

```powershell
cd C:\Projects\secret-santa\backend
java .\tools\GenerateJwtKeys.java
```

Generator tworzy parę RSA 3072-bit. Klucz prywatny zapisuje jako PEM PKCS#8, a publiczny jako PEM X.509 SubjectPublicKeyInfo. Istniejące pliki zachowuje. Jeśli brakuje jednego klucza, przerwie pracę i poprosi o przywrócenie pasującego pliku.

Katalog `.secrets/` jest ignorowany przez Git. Zachowaj klucz prywatny lokalnie i ogranicz dostęp do niego. Backend podpisuje token kluczem prywatnym i weryfikuje podpis publicznym. Wymiana pary powoduje odrzucenie tokenów podpisanych poprzednim kluczem.

## IntelliJ IDEA

Uruchom PostgreSQL poleceniem `docker compose up -d --wait` z katalogu `backend`, korzystając z dotychczasowego lokalnego `.env`.

W **Run → Edit Configurations** ustaw klasę główną `pl.secretsanta.SecretSantaApplication`, JDK 21 i katalog roboczy `C:\Projects\secret-santa\backend`. Profil `local` jest domyślny. Używa ustawień PostgreSQL z lokalnego `.env`, słucha pod `127.0.0.1:8080` i czyta klucze z `.secrets/`.

Jeśli trzymasz klucze poza projektem, ustaw w **Environment variables** wartości `JWT_PRIVATE_KEY_LOCATION` i `JWT_PUBLIC_KEY_LOCATION`. Przykładowe ścieżki to `file:///C:/Users/TWOJ_UZYTKOWNIK/jwt/jwt-private.pem` i `file:///C:/Users/TWOJ_UZYTKOWNIK/jwt/jwt-public.pem`. W konfiguracji zapisuj ścieżki do plików. Testy JUnit korzystają z osobnej pary tworzonej w pamięci kontekstu testowego.

## Postman

Zaimportuj oba pliki z `postman/`, wybierz **Secret Santa Local** i uruchom kolekcję w domyślnej kolejności. **Poprawna rejestracja** tworzy konto testowe, **Poprawne logowanie** ustawia lokalne `accessToken`, a **Mój profil z tokenem** wysyła go jako Bearer. Każda nowa rejestracja czyści wcześniejszy token. Po zakończeniu możesz usunąć lokalną wartość `accessToken` z aktywnego środowiska. Eksport w repozytorium zawiera puste pole.
