# Реализация авторизации

Реализация авторизации в проекте sweethome на основе [auth-flows.md](../family-todo-backend/docs/auth-flows.md).

## Реализованные флоу

### 1. Регистрация (POST /auth/register)
- Email, пароль, displayName
- Экран `RegisterScreen`

### 2. Вход (POST /auth/login)
- Email и пароль
- Экран `AuthScreen` (форма входа)

### 3. Гостевой аккаунт (POST /auth/guest)
- Без тела запроса
- Кнопка «Продолжить как гость» на экране входа

### 4. Привязка гостевого аккаунта (POST /auth/link/email)
- Требует Bearer token
- Экран `LinkEmailScreen` для гостей

### 5. Обновление токенов (POST /auth/refresh)
- Метод `AuthRepository.refreshTokens()` для обновления access token

### 6. Выход (POST /auth/logout)
- Отзыв refresh token на сервере
- Очистка локального хранилища

## Структура

```
composeApp/src/
├── commonMain/kotlin/.../auth/
│   ├── AuthApi.kt          # Интерфейс API
│   ├── AuthModels.kt       # DTO и запросы
│   ├── AuthRepository.kt   # Репозиторий с refresh-логикой
│   ├── AuthScreen.kt       # Экран входа + гость
│   ├── AuthViewModel.kt
│   ├── ApiConfig.kt        # expect getApiBaseUrl()
│   ├── KtorAuthApi.kt      # Ktor-реализация
│   ├── LinkEmailScreen.kt  # Привязка email для гостей
│   ├── RegisterScreen.kt   # Регистрация
│   └── TokenStorage.kt     # expect/actual хранилище
├── androidMain/.../auth/
│   ├── ApiConfig.android.kt     # http://10.0.2.2:8080 (эмулятор)
│   └── TokenStorage.android.kt # EncryptedSharedPreferences
└── iosMain/.../auth/
    ├── ApiConfig.ios.kt        # http://localhost:8080
    └── TokenStorage.ios.kt     # NSUserDefaults
```

## Конфигурация

- **Android (эмулятор):** `http://10.0.2.2:8080`
- **iOS (симулятор):** `http://localhost:8080`
- **Реальное устройство:** измените `getApiBaseUrl()` в `ApiConfig.*.kt` на IP вашего компьютера в локальной сети

## Зависимости

- `androidx.security:security-crypto` — EncryptedSharedPreferences на Android
- `io.insert-koin:koin-android` — для androidContext в Koin
- `io.insert-koin:koin-compose` — для koinInject

## Запуск backend

Перед тестированием запустите API gateway и auth-service:

```bash
cd family-todo-backend/deploy
docker-compose up -d
```

API будет доступен на `http://localhost:8080`.
