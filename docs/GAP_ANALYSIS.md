# SweetHome — Gap Analysis: Design vs PRD

**Версия:** 1.0  
**Дата:** 2026-05-02  
**Источник дизайна:** `design_handoff_sweethome 4/` (23 экрана, HTML-прототип)  
**Источник требований:** `docs/PRD.md`  

Этот документ фиксирует что уже спроектировано и что нужно создать дизайнеру.

---

## Что уже готово в дизайне

Все 23 экрана в `design_handoff_sweethome 4/` — финальные, high-fidelity, light + dark theme:

| # | Экран | Файл |
|---|-------|------|
| 01 | Splash | `AuthScreens.jsx` |
| 02 | Login | `AuthScreens.jsx` |
| 03 | Register | `AuthScreens.jsx` |
| 04 | Dashboard (Home) | `OtherScreens.jsx` |
| 05 | Мои списки (Lists) | `ListScreens.jsx` |
| 06 | Детальный экран списка (ListDetail) | `ListScreens.jsx` |
| 07 | Создание списка (CreateList) | `ListScreens.jsx` |
| 08 | Шаблоны (Templates) | `OtherScreens.jsx` |
| 09 | Детальный шаблон (TemplateDetail) | `OtherScreens.jsx` |
| 10 | Уведомления (Notifications) | `OtherScreens.jsx` |
| 11 | Чат (Chat) | `OtherScreens.jsx` |
| 12 | Профиль (Profile) | `OtherScreens.jsx` |
| 13 | Семейный дом (FamilyHome) | `ExtraScreens.jsx` |
| 14 | Участники семьи (FamilyMembers) | `ExtraScreens.jsx` |
| 15 | Геймификация (Gamification) | `ExtraScreens.jsx` |
| 16 | Магазин семьи (FamilyShop) | `ExtraScreens.jsx` |
| 17 | Пространство группы (GroupSpace) | `ExtraScreens.jsx` |
| 18 | Вступление по коду (JoinByCode) | `ExtraScreens.jsx` |
| — | Дизайн-система (токены, цвета, типографика) | `README.md` |
| — | Компонентная библиотека (кнопки, поля, карточки) | `README.md` |

Дополнительно в `sweethome/` (расширенный набор):
- Календарь: полная спека + React-реализация (`CalendarScreens.jsx`, `CALENDAR_SPEC.md`)
- Tweaks panel (настройки пользователя)

---

## Что нужно спроектировать

### 🔴 Критично для MVP

#### G-01: Новый Dashboard (Главный экран)
**Текущее состояние:** Экран Dashboard есть, но показывает список пространств/списков.  
**Что нужно:**
- Контекст-пилюля `Работа / Личное` в шапке с автопереключением
- Секция «Сегодня»: задачи с dueDate = сегодня + просроченные
- Заглушка для Goals виджета (MMP)
- Quick Actions: FAB + shortcuts (Добавить задачу, Список покупок, Домашнее дело)
- Пустое состояние секции «Сегодня»

**Компоненты:** контекст-пилюля, task card компакт, quick action chips, empty state

---

#### G-02: Экран создания задачи с динамическими полями
**Текущее состояние:** Нет отдельного спроектированного экрана создания задачи.  
Во флоу ListDetail есть bottom sheet для добавления, но он минимальный.  
**Что нужно:**
- Полный bottom sheet / экран создания задачи
- Базовые поля (всегда): название, заметка, приоритет, дедлайн, исполнитель
- **Динамические секции:**
  - Расписание (только для `home_chores`, скрыть для одноразовой)
  - Детали товара — brand, image, url (только для `shopping`)
  - Награда в валюте (только для `family` / `mentoring` workspace)
  - media-поля (только для `media`)
- Кнопка «Использовать шаблон» — открывает G-03
- Состояния: создание / редактирование

**UX-паттерн:** коллапсирующиеся секции («Advanced», «Детали товара»), поля появляются плавно при выборе типа

---

#### G-03: Выбор шаблона inline (bottom sheet)
**Текущее состояние:** Экран шаблонов есть (Templates, частично mock), но нет inline-выбора при создании задачи. Клиент использует **legacy** `/v1/suggestions/chore-templates` (deprecated) и `/v1/templates` (deprecated union view).

**Бэкенд: Templates v2** (см. backend `docs/CLIENT_GUIDE.md` §9, на 2026-05-03 уже в `main`):
- Две сущности — **list-templates** (`/v1/templates/*`) и **task-templates** (`/v1/task-templates/*`).
- Видимость `private | pending | public` + state machine + админ-модерация.
- list-templates: `GET /public`, `GET /mine`, `GET /favorites`, `GET /{id}`, `POST`, `PATCH`, `DELETE`, `POST /{id}/use` (с `overrides`), `POST /{id}/request-publication`, `POST /{id}/withdraw-publication`, `PUT/DELETE /{id}/favorite`.
- task-templates: зеркальный набор + `POST /{id}/use` принимает `listId` и вставляет одну задачу в существующий список.
- `POST /v1/lists/{id}/save-as-template` — сохранить список как шаблон (типы `wishlist` запрещены).
- Items в шаблонах: `id, sortOrder, title, note, priority, reward, shoppingDetails, choreSchedule, mediaDetails`.

**Что нужно (G-03):**
- Bottom sheet поверх экрана создания задачи (внутри `ItemBottomSheet`)
- Разделы: «Шаблоны», «Часто добавляете», «Избранное»
  - **Шаблоны** = `/v1/task-templates/public?scope=<list_type>` ∪ `/v1/task-templates/mine?scope=<list_type>` (для совместимого скоупа)
  - **Часто добавляете** = `/v1/suggestions/frequent-items?listId=<id>` (как сейчас)
  - **Избранное** = `/v1/suggestions/favorites` (на клиенте есть API, в UI не интегрировано)
- Карточка шаблона: название, иконка по scope, краткое описание; бейдж «Моё/pending» для своих
- Тап на шаблон → pre-fill всех полей формы (title, note, priority, reward, shoppingDetails, choreSchedule, mediaDetails) → закрыть sheet
- Все поля остаются редактируемыми после выбора

**Расхождения с текущим кодом:**
- `SuggestionsModels.Template` не содержит `scope`, `userId`, `visibility`, `isFavorite` — нужны новые модели.
- `TemplateItem` содержит только `title` — отсутствуют `note/priority/reward/shoppingDetails/choreSchedule/mediaDetails` нужные для pre-fill.
- `UseTemplateRequest` не имеет `overrides`.
- Нет API/моделей для task-templates целиком, save-as-template, favorites.

---

#### G-04: Детали элемента в Shopping List
**Текущее состояние:** Строка в списке покупок: чекбокс + название + количество.  
**Что нужно:**
- Expanded состояние строки: отображает brand, thumbnail фото, ссылку
- Либо: bottom sheet при лонг-пресс с деталями товара
- Collapsed состояние: индикатор (иконка или chip «есть детали»), чтобы не засорять список
- Состояние редактирования деталей (добавить фото, бренд, ссылку)

**UX-паттерн (рекомендуется):** bottom sheet при лонг-пресс — список остаётся чистым

---

#### G-05: Home Space — Комнаты и фильтры
**Текущее состояние:** FamilyHome экран есть, но без разбивки по комнатам.  
**Что нужно:**
- TabRow по комнатам: Кухня / Спальня / Гостиная / Ванная / + Добавить
- Chip-фильтры под tabs: Приоритет | Исполнитель | Статус
- Фильтры работают как в Jira: multiple select, сбросить
- Экран/bottom sheet «Добавить комнату» (название + иконка)
- Пустое состояние вкладки

---

#### G-06: Quick-assign компонент
**Текущее состояние:** В групповых списках отображается UUID или ничего.  
**Что нужно:**
- Компонент `AssigneeChip` в строке задачи (в групповых списках):
  - `unassigned`: иконка «+» или «нет исполнителя»
  - Тап → мгновенно назначить текущего пользователя
  - `assigned_me`: аватар/инициалы текущего пользователя
  - `assigned_other`: аватар другого участника
  - Тап на `assigned_other` → bottom sheet выбора исполнителя

---

#### G-07: Удаление аккаунта (Danger Zone)
**Текущее состояние:** Нет.  
**Что нужно:**
- Секция «Danger Zone» в Настройках профиля
- Кнопка «Удалить аккаунт» (destructive, красная)
- Подтверждение: modal с предупреждением + ввод email/фразы
- Финальный success state: «Аккаунт удалён»

---

### 🟡 Важно для полноты продукта

#### G-08: Настройка рабочих часов
**Текущее состояние:** Нет.  
**Что нужно:**
- Секция в настройках профиля или workspace: «Рабочие часы»
- Time picker: от / до (например, 9:00 — 18:00)
- Дни недели (по умолчанию Пн–Пт)
- Preview: «Сейчас рабочее время / нерабочее время»

---

#### G-09: Настройка HomeCurrency (family workspace)
**Текущее состояние:** Экран Gamification есть (очки/достижения), но нет настройки валюты.  
**Что нужно:**
- Экран настроек в family workspace settings
- Поле: название валюты (текстовый ввод)
- Выбор иконки из набора (монета, звезда, сердечко, и т.д.)
- Preview: «Участники будут зарабатывать Монеты 🪙»
- Экран Reward Shop (список призов + кнопка добавить приз)

---

#### G-10: Флоу «Список из меню / рецепта»
**Текущее состояние:** Нет.  
**Что нужно:**
- Entry point: кнопка / FAB action «Создать из рецепта» в shopping lists
- Экран/bottom sheet: поле ввода текста рецепта или кнопка «выбрать шаблон рецепта»
- Результат: preview списка ингредиентов → пользователь редактирует → сохранить как список покупок

---

### 🔵 MMP / Будущее

#### G-11: Личные цели (Goals) — MMP
- Placeholder-виджет на Dashboard в MVP (надпись «Цели — скоро»)
- Полный флоу в MMP: создание цели, шаги, прогресс-бар, экран GoalDetail

#### G-12: Шаринг календаря — MMP
- Настройки уровня доступа к календарю
- Preview «что видит другой пользователь»

#### G-13: Telegram-бот привязка — MLP
- Экран привязки с генерацией кода

---

## Статус по компонентам

| Компонент | Готово | Нужно |
|-----------|--------|-------|
| Дизайн-токены (цвета, типографика, отступы) | ✅ | — |
| Кнопки | ✅ | — |
| Input fields | ✅ | — |
| Cards | ✅ | — |
| Chips | ✅ | — |
| Avatars | ✅ | — |
| Bottom Nav | ✅ | — |
| Контекст-пилюля (Work/Personal) | ❌ | G-01 |
| AssigneeChip | ❌ | G-06 |
| Task row (compact, с деталями) | ⚠️ Частично | G-01, G-04 |
| Dynamic form sections | ❌ | G-02 |
| Room tab + фильтры | ❌ | G-05 |
| Danger Zone pattern | ❌ | G-07 |
