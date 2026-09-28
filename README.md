# PozdroSprawdzanie

> Zaawansowany, asynchroniczny i w pełni zoptymalizowany system weryfikacji graczy (sprawdzarka) na serwery Minecraft.

---

### 🌟 Główne Funkcje

* **📦 Pełne Panele GUI**
Intuicyjne zarządzanie procesem kontroli (rozpoczęcie, oczyszczenie, bany za cheaty, przyznanie się, brak współpracy).

* **📜 Wielostronicowa Historia**
Zapis logów kontroli z asynchronicznym stronicowaniem w GUI.

* **⚡ Obsługa Baz Danych & HikariCP**
Błyskawiczny, asynchroniczny zapis do **MySQL / MariaDB** lub plików **YML**.

* **👁️ Izolacja Graczy**
Automatyczne ukrywanie innych graczy w świecie i na czacie dla moderatora oraz podejrzanego na czas kontroli.

* **💬 Dedykowany Czat Kontroli**
Odizolowany kanał komunikacji między sprawdzającym a graczem (blokada wiadomości globalnych).

* **🛡️ Ochrona przed Ucieczką (Quit-Ban)**
Automatyczne wykrywanie wyjścia z serwera podczas trwania kontroli i natychmiastowe nałożenie kary.

* **👑 Hierarchia Rang**
Zintegrowana z Vault/LuckPerms weryfikacja uprawnień (brak możliwości sprawdzania wyższej rangi).

* **🔌 Lekkie API**
Proste metody statyczne `$O(1)$` do integracji z pluginami od czatu, AFK czy teleportacji.

---

### 📋 Komendy i Uprawnienia

| Komenda | Opis | Uprawnienie |
| :--- | :--- | :--- |
| `/sprawdz <gracz>` | Otwiera panel weryfikacji gracza | `pozdrosprawdzanie.sprawdz` |
| `/sprawdzarka set-checker` | Ustawia punkt teleportacji sprawdzarki | `pozdrosprawdzanie.admin` |
| `/sprawdzarka set-spawn` | Ustawia punkt powrotny (spawn) | `pozdrosprawdzanie.admin` |
| `/sprawdzarka reload` | Przeładowuje pliki konfiguracyjne i GUI | `pozdrosprawdzanie.admin` |
| `/sprawdzarka teleport` | Teleportuje moderatora do strefy kontroli | `pozdrosprawdzanie.admin` |
| `/przyznajesie` | Dobrowolne przyznanie się gracza do cheatów | *Brak (dla sprawdzanego)* |

---

### 🔌 API dla Programistów

Plugin udostępnia lekkie, bezalokacyjne API do integracji w innych systemach serwera:

```java
import pl.pozdro320.api.PozdroSprawdzanieAPI;

// Sprawdzenie, czy gracz bierze udział w kontroli (np. w celu anulowania AFK / czatu)
if (PozdroSprawdzanieAPI.isInCheckSession(player)) {
    // Twoja logika...
}