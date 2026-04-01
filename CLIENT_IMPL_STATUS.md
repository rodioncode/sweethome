# Статус реализации клиента — сверка с API Guide

> Дата: 2026-04-01
> Ветка: claude/gifted-mclean

Обозначения: ✅ Реализовано | ⚠️ Частично | ❌ Не реализовано

---

## 1. Аутентификация и токены

| Функциональность | Статус | Комментарий |
|---|---|---|
| POST /auth/register | ✅ | RegisterScreen + RegisterRequest |
| POST /auth/login | ✅ | AuthScreen + LoginRequest |
| POST /auth/guest | ✅ | AuthScreen → loginAsGuest() |
| POST /auth/refresh | ✅ | AuthRepository.refreshTokens() |
| POST /auth/logout | ✅ | AuthRepository.logout() |
| POST /auth/link/email | ✅ | LinkEmailScreen + LinkEmailRequest |
| Хранение токенов (Android) | ✅ | EncryptedSharedPreferences (AES256) |
| Хранение токенов (iOS) | ⚠️ | NSUserDefaults — **не зашифровано**, нужен Keychain |
| Авто-рефреш при 401 | ❌ | При 401 просто падает с ошибкой, рефреш не вызывается |
| Восстановление сессии при запуске | ✅ | checkStoredSession() |
| UX: подсказка о привязке email для гостя | ❌ | Нет баннера/напоминания на главном экране |

---

## 2. Устройства и уведомления

| Функциональность | Статус | Комментарий |
|---|---|---|
| POST /auth/devices | ⚠️ | Метод в API есть (RegisterDeviceRequest), в приложение не встроен |
| Регистрация push-токена при запуске | ❌ | FCM/APNs интеграции нет |
| GET /auth/notification-preferences | ❌ | Не реализовано |
| PUT /auth/notification-preferences | ❌ | Не реализовано |
| Экран настроек уведомлений | ❌ | Нет |

---

## 3. Группы

| Функциональность | Статус | Комментарий |
|---|---|---|
| GET /groups | ❌ | Нет |
| POST /groups | ❌ | Нет |
| POST /groups/{id}/invites | ❌ | Нет |
| POST /invites/{token}/accept | ❌ | Нет |
| POST /groups/{id}/transfer-ownership | ❌ | Нет |
| DELETE /groups/{id} | ❌ | Нет |
| DELETE /groups/{id}/members/{userId} | ❌ | Нет |
| Deep link: familytodo://invite/{token} | ❌ | Нет |
| Экран группы со списком участников | ❌ | Нет |
| Экран «Группы» в навигации | ❌ | Нет |

> Модели данных частично готовы: `TodoList` имеет `ownerGroupId`, `scope`, `groupId` в `CreateListRequest`.

---

## 4. Списки

| Функциональность | Статус | Комментарий |
|---|---|---|
| GET /lists?scope=personal | ✅ | TodoListsScreen |
| GET /lists?scope=group&groupId=X | ❌ | Группы не реализованы |
| POST /lists | ✅ | CreateListRequest с type, title, scope |
| GET /lists/{id} (с items) | ✅ | TodoListDetailScreen |
| PATCH /lists/{id} (title, icon, archived) | ✅ | UpdateListRequest |
| DELETE /lists/{id} (soft archive) | ✅ | |
| Отображение иконки списка | ❌ | Поле `icon` в модели есть, в UI не отображается |
| Фильтрация архивных списков | ❌ | `archivedAt` в модели есть, UI не различает |
| Вкладки: «Мои» + по каждой группе | ❌ | Только личные списки |
| Локальное кеширование (Room) | ✅ | TodoListEntity, TodoListDao |

---

## 5. Элементы списков (Items)

### Основное

| Функциональность | Статус | Комментарий |
|---|---|---|
| POST /lists/{id}/items | ✅ | CreateItemRequest |
| PATCH /items/{id} (toggle isDone) | ✅ | |
| PATCH /items/{id} (title, note) | ✅ | |
| DELETE /items/{id} | ✅ | |
| Локальное кеширование (Room) | ✅ | TodoItemEntity, TodoItemDao |

### Поля модели

| Поле | В модели | В Entity | В UI | Комментарий |
|---|---|---|---|---|
| title | ✅ | ✅ | ✅ | |
| note | ✅ | ✅ | ✅ (отображение) | Ввод не реализован |
| isDone / doneAt | ✅ | ✅ | ✅ | |
| sortOrder | ✅ | ✅ | ❌ | Не используется при создании |
| assignedTo | ❌ | ❌ | ❌ | Отсутствует в моделях |
| dueAt | ❌ | ❌ | ❌ | Отсутствует в моделях |
| isFavorite | ❌ | ❌ | ❌ | Отсутствует в моделях |
| version | ❌ | ❌ | — | Нужен для оффлайн-синхронизации |
| shopping.quantity / unit | ✅ | ✅ (JSON) | ❌ | Данные хранятся, UI не реализован |
| shopping.category | ❌ | ❌ | ❌ | Отсутствует в ShoppingItemFields |
| choreSchedule.intervalDays / daysOfWeek | ✅ | ✅ (JSON) | ❌ | Данные хранятся, UI не реализован |
| choreSchedule.lastDoneAt | ❌ | ❌ | ❌ | Отсутствует в ChoreSchedule |
| choreSchedule.category | ❌ | ❌ | ❌ | Отсутствует в ChoreSchedule |
| Сброс assignedTo через `""` | ❌ | — | — | Специальная семантика пустой строки не учтена |
| Сброс dueAt через `""` | ❌ | — | — | То же |

---

## 6. Категории

| Функциональность | Статус | Комментарий |
|---|---|---|
| GET /categories?scope=shopping | ❌ | Нет |
| GET /categories?scope=chore | ❌ | Нет |
| POST /categories | ❌ | Нет |
| Выбор категории при создании/редактировании | ❌ | Нет |

---

## 7. Подсказки и рекомендации

| Функциональность | Статус | Комментарий |
|---|---|---|
| GET /suggestions/chore-templates | ❌ | Нет |
| GET /suggestions/frequent-items | ❌ | Нет |
| GET /suggestions/favorites | ❌ | Нет |
| Секция «Подсказки» при создании элемента | ❌ | Нет |

---

## 8. Синхронизация (оффлайн-режим)

| Функциональность | Статус | Комментарий |
|---|---|---|
| GET /sync?since={timestamp} | ❌ | Эндпоинт не реализован |
| Локальная БД (Room) | ✅ | Есть для lists и items |
| Network-first с fallback на кеш | ✅ | При ошибке сети показываются кешированные данные |
| Поле `version` в items | ❌ | Нет в модели и Entity |
| Поле `deleted` / soft-delete обработка | ❌ | Нет |
| Очередь pending-операций | ❌ | Нет |
| Фоновый sync worker | ❌ | Нет |
| Sync при возврате из фона | ❌ | Нет |
| Sync при восстановлении сети | ❌ | Нет |
| Разрешение конфликтов (LWW по version/updatedAt) | ❌ | Нет |

---

## 9. Экраны

| Экран | Статус | Комментарий |
|---|---|---|
| Экран входа (Login) | ✅ | Email + пароль, кнопка гостевого входа |
| Экран регистрации | ✅ | displayName, email, пароль |
| Экран привязки email | ✅ | Конвертация гостя в аккаунт |
| Список моих списков | ✅ | Карточки, FAB для создания |
| Детальный экран списка с элементами | ✅ | Чекбокс, удаление, добавление |
| Создание элемента (тип-зависимые поля) | ⚠️ | Только title; quantity/unit/dueAt/assignedTo — нет |
| Редактирование элемента | ❌ | Нет отдельного экрана/модала |
| Экран группы | ❌ | Нет |
| Вкладки: Мои / Группы / Настройки | ❌ | Нет Tab Bar |
| Экран настроек / профиля | ❌ | Нет |
| Экран настроек уведомлений | ❌ | Нет |
| Стартовый экран с проверкой токенов | ✅ | checkStoredSession() при запуске |

---

## 10. Типы списков — специфичная логика

### Shopping
| Функциональность | Статус | Комментарий |
|---|---|---|
| Поля quantity, unit | ⚠️ | В модели есть, в UI нет |
| Категория товара | ❌ | Нет в модели |
| Автоархивация через 24ч (серверная) | — | На сервере; клиент просто не получает их |
| Таймер «исчезнет через X часов» | ❌ | Нет |
| Избранные элементы | ❌ | isFavorite отсутствует |

### General Todos
| Функциональность | Статус | Комментарий |
|---|---|---|
| Дедлайн (dueAt) | ❌ | Нет в модели |
| Подсветка просроченных | ❌ | Нет |
| Назначение участника (assignedTo) | ❌ | Нет в модели |
| Экспорт в системный календарь | ❌ | Нет |

### Home Chores
| Функциональность | Статус | Комментарий |
|---|---|---|
| Расписание (intervalDays, daysOfWeek) | ⚠️ | В модели есть, в UI нет |
| startDate / endDate | ⚠️ | В модели есть, в UI нет |
| lastDoneAt | ❌ | Нет в модели |
| Расчёт «осталось X дней» / «просрочено» | ❌ | Нет |
| Цветовое кодирование по статусу | ❌ | Нет |
| Категория дела | ❌ | Нет в модели |
| Шаблоны (chore-templates) | ❌ | Нет |

---

## Итоговая сводка

| Блок | Готово | Частично | Не реализовано |
|---|---|---|---|
| Аутентификация | 7 | 2 | 2 |
| Уведомления | 0 | 1 | 4 |
| Группы | 0 | 0 | 9 |
| Списки (базовое) | 4 | 0 | 4 |
| Элементы (базовое) | 4 | 0 | 0 |
| Поля элементов | 3 | 2 | 10 |
| Категории | 0 | 0 | 3 |
| Подсказки | 0 | 0 | 4 |
| Синхронизация | 1 | 0 | 9 |
| Экраны | 6 | 1 | 8 |
| Типы: Shopping | 0 | 1 | 4 |
| Типы: Todos | 0 | 0 | 4 |
| Типы: Chores | 0 | 2 | 6 |
