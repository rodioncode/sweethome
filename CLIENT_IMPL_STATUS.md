# Статус реализации клиента — сверка с API Guide

> Дата: 2026-04-03
> Ветка: claude/gracious-bartik

Обозначения: ✅ Реализовано | ⚠️ Частично | ❌ Не реализовано

---

## 1. Аутентификация и токены

| Функциональность | Статус | Комментарий |
|---|---|---|
| POST /auth/register | ✅ | RegisterScreen + RegisterRequest |
| POST /auth/login | ✅ | AuthScreen + LoginRequest |
| POST /auth/guest | ✅ | AuthScreen → loginAsGuest() |
| POST /auth/refresh | ✅ | AuthRepository.refreshTokens() |
| POST /auth/logout | ✅ | AuthRepository.logout() + очистка всех кешей |
| POST /auth/link/email | ✅ | LinkEmailScreen + LinkEmailRequest |
| Хранение токенов (Android) | ✅ | EncryptedSharedPreferences (AES256-GCM) |
| Хранение токенов (iOS) | ✅ | iOS Keychain (SecItemAdd/SecItemCopyMatching) |
| Авто-рефреш при 401 | ✅ | HttpSend interceptor в apiClient с Mutex |
| Восстановление сессии при запуске | ✅ | checkStoredSession() |
| Очистка данных при смене пользователя | ✅ | tokenStorage + listsStorage + all repositories |
| UX: подсказка о привязке email для гостя | ❌ | Нет баннера/напоминания на главном экране |

---

## 2. Устройства и уведомления

| Функциональность | Статус | Комментарий |
|---|---|---|
| POST /auth/devices | ⚠️ | Метод в API есть (RegisterDeviceRequest), не вызывается |
| Регистрация push-токена при запуске | ❌ | FCM/APNs интеграции нет |
| GET /auth/notification-preferences | ❌ | Не реализовано |
| PUT /auth/notification-preferences | ❌ | Не реализовано |
| Экран настроек уведомлений | ❌ | Нет |

---

## 3. Группы

| Функциональность | Статус | Комментарий |
|---|---|---|
| GET /groups | ✅ | GroupsScreen + GroupsRepository |
| POST /groups | ✅ | CreateGroupDialog |
| POST /groups/{id}/invites | ✅ | GroupDetailScreen → createInvite |
| POST /invites/{token}/accept | ✅ | InviteScreen + deep link |
| POST /groups/{id}/transfer-ownership | ✅ | GroupDetailScreen → transferOwnership |
| DELETE /groups/{id} | ✅ | GroupDetailScreen → deleteGroup |
| DELETE /groups/{id}/members/{userId} | ✅ | Удаление участника + self-leave |
| Deep link: familytodo://invite/{token} | ✅ | iOS DeepLinkHandler, навигация через InviteDestination |
| Экран группы со списком участников | ✅ | GroupDetailScreen с ролями Owner/Member |
| Вкладка «Группы» в навигации | ✅ | MainScreen → GROUPS tab |

---

## 4. Списки

| Функциональность | Статус | Комментарий |
|---|---|---|
| GET /lists?scope=personal | ✅ | TodoListsScreen |
| GET /lists?scope=group&groupId=X | ✅ | GroupDetailScreen загружает списки группы |
| POST /lists | ✅ | CreateListRequest с type, title, scope |
| GET /lists/{id} (с items) | ✅ | TodoListDetailScreen |
| PATCH /lists/{id} (title, icon, archived) | ✅ | UpdateListRequest |
| DELETE /lists/{id} (soft archive) | ✅ | |
| Отображение иконки списка | ❌ | Поле `icon` в модели есть, в UI не отображается |
| Фильтрация архивных списков | ❌ | `archivedAt` в модели есть, UI не различает |
| Локальное кеширование (Room) | ✅ | TodoListEntity, TodoListDao |

---

## 5. Элементы списков (Items)

### Основное

| Функциональность | Статус | Комментарий |
|---|---|---|
| POST /lists/{id}/items | ✅ | CreateItemRequest |
| PATCH /items/{id} (toggle isDone) | ✅ | |
| PATCH /items/{id} (title, note, все поля) | ✅ | EditItemDialog |
| DELETE /items/{id} | ✅ | |
| Локальное кеширование (Room) | ✅ | TodoItemEntity, TodoItemDao |

### Поля модели

| Поле | В модели | В Entity | В UI | Комментарий |
|---|---|---|---|---|
| title | ✅ | ✅ | ✅ | |
| note | ✅ | ✅ | ✅ | Отображение + редактирование |
| isDone / doneAt | ✅ | ✅ | ✅ | |
| sortOrder | ✅ | ✅ | ❌ | Не используется для ручной сортировки |
| assignedTo | ✅ | ✅ | ✅ | В CreateItemRequest и UpdateItemRequest |
| dueAt | ✅ | ✅ | ✅ | Поле в диалоге создания general_todos |
| isFavorite | ✅ | ✅ | ✅ | Звёздочка в ItemRow |
| version | ✅ | ✅ | — | Используется для синхронизации |
| shopping.quantity / unit | ✅ | ✅ (JSON) | ✅ | Поля в диалоге создания shopping |
| shopping.category | ✅ | ✅ | ✅ | Выбор категории + создание новой |
| choreSchedule.intervalDays | ✅ | ✅ (JSON) | ✅ | Поле в диалоге создания home_chores |
| choreSchedule.category | ✅ | ✅ | ✅ | Выбор категории + создание новой |
| choreSchedule.lastDoneAt | ✅ | ✅ | ❌ | В модели есть, UI не показывает |
| Сброс assignedTo через `""` | ✅ | — | ✅ | Семантика пустой строки учтена в UpdateItemRequest |
| Сброс dueAt через `""` | ✅ | — | ✅ | То же |

---

## 6. Категории

| Функциональность | Статус | Комментарий |
|---|---|---|
| GET /categories?scope=shopping | ✅ | CategoriesRepository.loadCategories("shopping") |
| GET /categories?scope=chore | ✅ | CategoriesRepository.loadCategories("chore") |
| POST /categories | ✅ | Создание пользовательской категории из диалога |
| Выбор категории при создании/редактировании | ✅ | Dropdown в AddItemDialog |

---

## 7. Подсказки и рекомендации

| Функциональность | Статус | Комментарий |
|---|---|---|
| GET /suggestions/chore-templates | ✅ | SuggestionsRepository + секция в AddItemDialog |
| GET /suggestions/frequent-items | ✅ | SuggestionsRepository + секция в AddItemDialog |
| GET /suggestions/favorites | ⚠️ | API метод реализован, не вызывается из UI |
| Секция «Подсказки» при создании элемента | ✅ | Шаблоны и частые элементы в диалоге |

---

## 8. Синхронизация (оффлайн-режим)

| Функциональность | Статус | Комментарий |
|---|---|---|
| GET /sync?since={timestamp} | ✅ | SyncRepository.sync() |
| Локальная БД (Room) | ✅ | Есть для lists и items |
| Network-first с fallback на кеш | ✅ | При ошибке сети показываются кешированные данные |
| Поле `version` в items | ✅ | В модели, Entity и sync |
| Поле `deleted` / soft-delete обработка | ✅ | SyncItem.deleted → applySync |
| Sync при возврате из фона | ✅ | LifecycleEventObserver ON_RESUME |
| Очередь pending-операций | ❌ | Нет |
| Фоновый sync worker | ❌ | Нет |
| Sync при восстановлении сети | ❌ | Нет |
| Разрешение конфликтов (LWW по version) | ✅ | version-based в applySync |

---

## 9. Экраны

| Экран | Статус | Комментарий |
|---|---|---|
| Экран входа (Login) | ✅ | Email + пароль, кнопка гостевого входа |
| Экран регистрации | ✅ | displayName, email, пароль |
| Экран привязки email | ✅ | Конвертация гостя в аккаунт |
| Главный экран с вкладками | ✅ | MainScreen: «Мои списки» + «Группы» |
| Список моих списков | ✅ | Карточки с типом, FAB для создания |
| Список групп | ✅ | Карточки с ролью, кол-во участников |
| Детальный экран списка с элементами | ✅ | Чекбокс, удаление, добавление, редактирование |
| Детальный экран группы | ✅ | Участники, списки, инвайты, передача владения |
| Создание элемента (тип-зависимые поля) | ✅ | Shopping: qty/unit/category, Chores: interval/category, Todos: dueAt |
| Редактирование элемента | ✅ | EditItemDialog в TodoListDetailScreen |
| Экран обработки инвайта | ✅ | InviteScreen с deep link |
| Экран настроек / профиля | ❌ | Нет |
| Экран настроек уведомлений | ❌ | Нет |

---

## 10. Типы списков — специфичная логика

### Shopping
| Функциональность | Статус | Комментарий |
|---|---|---|
| Поля quantity, unit | ✅ | В модели и UI (AddItemDialog) |
| Категория товара | ✅ | Dropdown с API категориями |
| Избранные элементы | ✅ | isFavorite с звёздочкой |

### General Todos
| Функциональность | Статус | Комментарий |
|---|---|---|
| Дедлайн (dueAt) | ✅ | Поле в AddItemDialog |
| Подсветка просроченных | ❌ | Нет |
| Назначение участника (assignedTo) | ✅ | В модели и CreateItemRequest |

### Home Chores
| Функциональность | Статус | Комментарий |
|---|---|---|
| Расписание (intervalDays) | ✅ | Поле в AddItemDialog |
| Категория дела | ✅ | Dropdown с API категориями |
| Шаблоны (chore-templates) | ✅ | Секция подсказок из SuggestionsApi |
| lastDoneAt | ⚠️ | В модели есть, UI не показывает |
| Расчёт «осталось X дней» / «просрочено» | ❌ | Нет |
| Цветовое кодирование по статусу | ❌ | Нет |

---

## Итоговая сводка

| Блок | Готово | Частично | Не реализовано |
|---|---|---|---|
| Аутентификация | 10 | 0 | 1 |
| Уведомления | 0 | 1 | 4 |
| Группы | 10 | 0 | 0 |
| Списки (базовое) | 7 | 0 | 2 |
| Элементы (базовое) | 5 | 0 | 0 |
| Поля элементов | 13 | 0 | 1 |
| Категории | 4 | 0 | 0 |
| Подсказки | 3 | 1 | 0 |
| Синхронизация | 6 | 0 | 3 |
| Экраны | 11 | 0 | 2 |
| Типы: Shopping | 3 | 0 | 0 |
| Типы: Todos | 2 | 0 | 1 |
| Типы: Chores | 4 | 1 | 2 |
