1. Цель лабораторной работы №4
Целью работы является расширение ранее созданной рабочей версии системы (ЛР3) поддержкой бизнес-операций, изменяющих состояние данных, а также расширение возможностей чтения данных.
На данном этапе требуется:
реализовать GraphQL Mutation для реальных бизнес-сценариев (не «технический CRUD»);
оставить GraphQL внешним слоем без бизнес-логики и без прямого доступа к БД;
реализовать во внутренних сервисах создание/изменение данных с проверками бизнес-правил;
расширить Query: фильтрация, сортировка, агрегация;
сохранить изоляцию сервисов;
межсервисное взаимодействие по-прежнему выполнять через заглушки (без реального gRPC);
корректно обрабатывать ошибки валидации и недопустимые действия пользователя.

2. Связь с предыдущими лабораторными
ЛР1 — архитектура, сервисы, GraphQL/gRPC/Kafka на уровне проектирования.
ЛР2 — таблицы, контракты, сценарии, формальные артефакты.
ЛР3 — первая рабочая версия: Query, чтение, demo-данные, stub-клиенты.
ЛР4 — Mutation + расширенные Query + бизнес-логика изменения состояния.

База ЛР3 сохранена:
модульный монолит Spring Boot 4.1.1 / Java 21;
разделение пакетов по сервисам;
schema БД на сервис;
Gateway только делегирует вызовы сервисному слою.

3. Ограничения и архитектурные решения ЛР4
3.1. Ограничения
реального gRPC нет;
Kafka не подключается;
GraphQL-резолверы не содержат бизнес-логики и не обращаются к Repository/БД;
мутации отражают бизнес-действия (запись на занятие, подтверждение оплаты, отмена записи и т.д.), а не универсальные create/update/delete таблиц;
сервисы не читают чужие репозитории напрямую.

3.2. Решение по слоям
Для изменяющих операций добавлен command-слой рядом с query-слоем:
*QueryService — чтение, фильтры, сортировка, агрегаты;
*CommandService — изменение состояния и бизнес-проверки.
Gateway:
QueryController — Query;
MutationController — Mutation.
Оба контроллера только принимают аргументы и вызывают сервисы.

3.3. Межсервисные stub-клиенты
Расширены/добавлены интерфейсы и stub-реализации:
TrainingClient / TrainingClientStub
  findScheduleById;
  requireAvailableSchedule;
  reserveSlot;
  releaseSlot;
MembershipClient / MembershipClientStub
  findSubscriptionById;
  requireValidSubscription;
  activateSubscription;
  registerVisit;
IdentityClient / IdentityClientStub
  findTrainerById;
  requireActiveTrainer;
BookingClient / BookingClientStub
  cancelBySchedule.
Stub имитируют будущие gRPC-вызовы, сохраняя границы сервисов.
Для разрыва циклических зависимостей Spring использован @Lazy.

4. Что реализовано в GraphQL API
4.1. Mutation
Реализованы бизнес-мутации:

registerUser — регистрация клиента/тренера;
login — вход и выдача demo-токена;
createClass — создание занятия (с проверкой тренера и категории);
updateClass — изменение параметров занятия;
createSchedule — постановка занятия в расписание;
changeScheduleStatus — смена статуса расписания (в т.ч. отмена клубом);
purchaseSubscription — оформление абонемента + создание платежа PENDING;
confirmPayment — подтверждение/отклонение оплаты и активация абонемента при COMPLETED;
bookSchedule — запись клиента на занятие;
cancelBooking — отмена записи клиентом;
markAttendance — отметка посещаемости ATTENDED/NO_SHOW.

4.2. Почему это бизнес-операции, а не CRUD
Пример bookSchedule:
проверяется доступность занятия и свободные места (Training);
проверяется валидность абонемента клиента (Membership);
запрещается повторная запись на тот же schedule;
резервируется место;
создаётся booking со статусом BOOKED;
формируется уведомление.
Это сценарий предметной области, а не «insert в таблицу bookings».

Аналогично confirmPayment:
платёж должен быть PENDING;
при COMPLETED вызывается ActivateSubscription (через MembershipClient stub);
при FAILED активации нет;
создаётся уведомление об результате оплаты.

4.3. Расширение Query
Добавлены/расширены операции чтения:

classes(filter, sortBy, sortDirection) — список занятий с фильтром и сортировкой;
schedule(..., sortBy, sortDirection) — сортировка расписания;
myBookings(..., sortBy, sortDirection) — сортировка своих записей;
bookings(filter, sortBy, sortDirection) — выборка записей по условиям;
subscriptionPlans(activeOnly) — тарифы;
bookingStats(filter) — агрегат по записям;
paymentStats(filter) — агрегат по платежам;
scheduleStats(filter) — агрегат по расписанию.

Типы агрегатов:
BookingStats: totalCount, bookedCount, cancelledCount, attendedCount, noShowCount;
PaymentStats: totalCount, completed/failed/pending counts, totalAmount, completedAmount;
ScheduleStats: totalCount, planned/completed/cancelled, totalAvailableSlots.

Фильтры:
ClassFilterInput — categoryId, trainerId, status, titleContains;
ScheduleFilterInput — trainerId, categoryId, даты, status;
BookingFilterInput — clientId, scheduleId, status;
PaymentStatsFilterInput — clientId, status.

Сортировка:
SortDirection: ASC/DESC;
ScheduleSortField: START_TIME, AVAILABLE_SLOTS, STATUS;
BookingSortField: BOOKED_AT, STATUS;
для classes — sortBy строкой (TITLE/CAPACITY/STATUS).

5. Доработка внутренних сервисов
5.1. Identity
IdentityCommandService:
register — создание user + client/trainer, проверка уникальности email, роли;
login — проверка пароля и статуса ACTIVE.
IdentityQueryService расширен:
getUserIdByClientId — для уведомлений.

5.2. Training
TrainingCommandService:
createClass / updateClass;
createSchedule;
changeStatus (при CANCELLED уведомляет Booking через BookingClient.cancelBySchedule);
requireAvailableForBooking;
reserveSlot / releaseSlot.
TrainingQueryService:
findClasses с фильтром/сортировкой;
findSchedules с сортировкой;
scheduleStats.

5.3. Membership
MembershipCommandService:
createPending;
requireValidForBooking (ACTIVE, не истёк, есть remaining_visits, принадлежит клиенту);
activate (из PENDING в ACTIVE, даты, лимит визитов; идемпотентно при повторной активации);
registerVisit (списание посещения).
MembershipQueryService:
findPlans.

5.4. Booking
BookingCommandService:
book;
cancel;
markAttendance;
cancelByClubForSchedule.
BookingQueryService:
find / findByClient с фильтром и сортировкой;
stats.
Связанные данные расписания по-прежнему через TrainingClient (без прямого доступа к training schema).

5.5. Payment
PaymentCommandService:
purchaseSubscription — создаёт PENDING subscription + PENDING payment;
confirmPayment — COMPLETED/FAILED, при COMPLETED ActivateSubscription через stub.
PaymentQueryService:
stats.

5.6. Notification
NotificationCommandService создаёт уведомления по событиям:
USER_REGISTERED;
BOOKING_CREATED / BOOKING_CANCELLED;
SCHEDULE_CANCELLED;
PAYMENT_COMPLETED / PAYMENT_FAILED.

5.7. Analytics
На ЛР4 analytics по-прежнему читает собственные агрегаты (как в ЛР3).
Обновление агрегатов «в реальном времени» через Kafka не делалось (по ограничению этапа).

6. Бизнес-правила и обработка ошибок
6.1. BusinessRuleException
Добавлен тип BusinessRuleException для нарушений правил.
GraphQlExceptionResolver:
NotFoundException → NOT_FOUND;
BusinessRuleException → BAD_REQUEST.

6.2. Примеры правил
нельзя забронировать занятие не в статусе PLANNED;
нельзя бронировать без свободных мест;
нельзя бронировать прошедшее занятие;
нельзя использовать чужой/неактивный/истёкший абонемент без визитов;
нельзя повторно записаться на тот же schedule;
отменить можно только BOOKED;
посещаемость ставится только для BOOKED и только ATTENDED/NO_SHOW;
confirmPayment только для PENDING;
activate только для PENDING (ACTIVE при повторе возвращается без ошибки);
createSchedule требует endTime > startTime;
тренер должен быть ACTIVE;
тариф должен быть active.

7. Ключевые сквозные сценарии
7.1. Покупка и активация абонемента
1) purchaseSubscription → Membership создаёт PENDING subscription;
2) Payment создаёт PENDING payment;
3) confirmPayment(COMPLETED) → Payment переводит платёж в COMPLETED;
4) через MembershipClient.activateSubscription абонемент становится ACTIVE;
5) Notification фиксирует успешную оплату.
Активация идёт только через command/stub ActivateSubscription, без дублирующей логики по «событию Kafka» (согласовано с замечаниями к ЛР1/ЛР2).

7.2. Запись на занятие
1) bookSchedule;
2) Training: проверка доступности + reserveSlot;
3) Membership: проверка абонемента;
4) Booking: создание BOOKED;
5) Notification: подтверждение записи.

7.3. Отмена занятия клубом
1) changeScheduleStatus(CANCELLED);
2) Training меняет статус schedule;
3) BookingClient.cancelBySchedule переводит связанные BOOKED в CANCELLED_BY_CLUB;
4) освобождаются места;
5) клиентам создаются уведомления.

7.4. Фиксация посещения
1) markAttendance(ATTENDED|NO_SHOW);
2) при ATTENDED Membership.registerVisit уменьшает remaining_visits.

8. Структура изменений в коде
Новые/ключевые элементы:
gateway/graphql/MutationController.java;
gateway/graphql/QueryController.java — расширен;
shared/exception/BusinessRuleException.java;
identity/service/IdentityCommandService.java;
training/service/TrainingCommandService.java;
membership/service/MembershipCommandService.java;
booking/service/BookingCommandService.java;
payment/service/PaymentCommandService.java;
notification/service/NotificationCommandService.java;
расширены integration/*Client и *ClientStub;
schema.graphqls — Mutation + filter/sort/aggregate;
тесты MutationControllerIT.

9. Тестирование и проверка
Добавлен MutationControllerIT:
сценарий purchase → confirm → book;
cancelBooking с собственными данными (без порчи seed);
createSchedule;
filter/sort/aggregations.
Обновлён QueryControllerIT под новые Query-аргументы и устойчивость к побочным эффектам других IT.
Результат: mvn test проходит успешно.

Ручная проверка через GraphiQL (/graphiql):
мутации покупки/записи/отмены;
query bookingStats / paymentStats / scheduleStats;
schedule с sortBy/sortDirection;
classes с filter.titleContains.

10. Проблемы, выявленные при реализации/проверке, и исправления
10.1. Циклические зависимости Spring
Проблема: TrainingCommandService ↔ BookingCommandService через stub-клиенты давали цикл бинов.
Исправление: @Lazy на отдельных зависимостях клиентов/сервисов.

10.2. Влияние Mutation IT на Query IT
Проблема: общий Spring-контекст и in-memory БД; cancelBooking по seed-записи ломал ожидания QueryControllerIT.
Исправление:
cancelBooking в IT создаёт собственные subscription/schedule/booking;
myBookings в Query IT фильтрует status: BOOKED;
проверка уведомлений не завязана строго на первый элемент списка.

10.3. Сохранение границ сервисов при сложной записи
Проблема: Booking нужна проверка мест и абонемента.
Исправление: только через TrainingClient и MembershipClient, без доступа к чужим Repository.

10.4. Ошибки пользователя
Проблема: недостаточно различать «не найдено» и «нарушено правило».
Исправление: NotFoundException и BusinessRuleException с разными GraphQL errorType.

11. Соответствие требованиям ЛР4
Mutation для бизнес-операций — выполнено.
Резолверы без бизнес-логики и без БД — выполнено.
Логика и проверки во внутренних сервисах — выполнено.
Фильтрация Query — выполнено.
Сортировка Query — выполнено.
Агрегации Query — выполнено.
Обработка ошибок валидации/бизнес-ограничений — выполнено.
Изоляция сервисов + stub вместо реального gRPC — выполнено.

12. Итог
В ЛР4 система переведена из «только чтение» в полноценный контур бизнес-операций:
клиент может регистрироваться/входить, покупать абонемент, оплачивать, записываться и отменять запись;
тренер/админ могут управлять занятиями, расписанием и посещаемостью;
админ/аналитические запросы поддерживают фильтрацию, сортировку и агрегаты;
границы микросервисной архитектуры сохранены за счёт command/query сервисов и stub-клиентов.
Реализация готова как база для следующих этапов (реальный gRPC и Kafka-события) без ломки внешнего GraphQL API.