# AntiF Browser - Bypass для Emergent.sh

## Что делает

Bypass v5.0 не даёт emergent.sh определить что ты уже использовал триал.

## Как работает

При заходе на emergent.sh скрипт автоматически:

1. **Удаляет все следы** - localStorage, sessionStorage, cookies, IndexedDB
2. **Блокирует FingerprintJS Pro** - подменяет visitorId на рандомный
3. **Блокирует PostHog** - аналитика не работает
4. **Блокирует tracking** - Facebook Pixel, TikTok, Cloudflare
5. **Генерирует новый Device-Id** - каждый раз новый
6. **Блокирует запись** - emergent.sh не может сохранить данные о тебе

## Сборка

```bash
./gradlew assembleDebug
```

APK будет в `app/build/outputs/apk/debug/app-debug.apk`

## Использование

1. Открой профиль в браузере
2. Зайди на emergent.sh
3. BYPASS включен по умолчанию
4. Для emergent.sh ты новый пользователь

## Меню браузера

- **BYPASS: ON/OFF** - вкл/выкл bypass
- **Reset modals** - сброс модалок
- **New Device-Id** - новый ID устройства
- **Check trial usage** - проверить следы триала
- **Reset trial data** - удалить следы триала

## Требования

- Android 7.0+
- Android Studio / JDK 17
