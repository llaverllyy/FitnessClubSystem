1. Введение и цель работы
Во второй лабораторной работе архитектура системы управления фитнес-клубом,
сформированная в ЛР1, переводится на технический уровень. На этом этапе нужно
описать, как система будет работать с точки зрения данных, внешнего API,
межсервисного взаимодействия и выполнения основных бизнес-операций.

В рамках работы необходимо:
определить структуру баз данных каждого сервиса;
показать связь таблиц и полей со сценариями системы;
проектировать внешний GraphQL API;
определить синхронные вызовы через gRPC;
определить асинхронные события через Kafka;
писать последовательность выполнения основных сценариев;
составить поэтапный план разработки.

Результатом является техническая модель системы, которую можно использовать как
основу для реализации.

2. Исходная декомпозиция системы
В ЛР1 для системы управления фитнес-клубом были выделены следующие сервисы:
1) Identity Service
2) Training Management Service
3) Membership Service
4) Booking Service
5) Payment Service
6) Notification Service
7) Analytics Service

Общие правила проектирования:
у каждого сервиса собственная база данных;
прямые связи между БД разных сервисов не допускаются;
foreign key применяются только внутри одной БД;
идентификаторы сущностей из других сервисов хранятся как обычные поля;
таблицы и поля должны использоваться в реальных сценариях;
GraphQL используется как внешний API для клиента;
gRPC используется для синхронных внутренних вызовов;
Kafka используется для публикации событий.

3. Структура баз данных сервисов
3.1. Identity Service
Назначение сервиса: регистрация, аутентификация, хранение ролей и профилей
пользователей.

Таблица roles.
Поля:
id - UUID, primary key.
code - VARCHAR(32), not null, unique.
name - VARCHAR(64), not null.
description - VARCHAR(255), null.
Назначение таблицы: хранение ролей системы.

Таблица users.
Поля:
id - UUID, primary key.
email - VARCHAR(255), not null, unique.
password_hash - VARCHAR(255), not null.
role_id - UUID, not null, foreign key -> roles.id.
status - VARCHAR(20), not null.
created_at - TIMESTAMP, not null.
updated_at - TIMESTAMP, not null.
Назначение таблицы: хранение учётных записей.

Таблица clients.
Поля:
id - UUID, primary key.
user_id - UUID, not null, unique, foreign key -> users.id.
full_name - VARCHAR(255), not null.
phone - VARCHAR(20), not null.
birth_date - DATE, null.
membership_status - VARCHAR(20), not null.
created_at - TIMESTAMP, not null.
Назначение таблицы: хранение профилей клиентов.

Таблица trainers.
Поля:
id - UUID, primary key.
user_id - UUID, not null, unique, foreign key -> users.id.
full_name - VARCHAR(255), not null.
phone - VARCHAR(20), not null.
specialization - VARCHAR(255), not null.
employment_status - VARCHAR(20), not null.
created_at - TIMESTAMP, not null.
Назначение таблицы: хранение профилей тренеров.

Связи внутри сервиса:
roles 1:M users.
users 1:1 clients.
users 1:1 trainers.

Использование в сценариях:
roles и users используются при регистрации, входе и проверке прав.
clients используются при покупке абонемента, записи на занятие и уведомлениях.
trainers используются при создании занятий, расписания и фиксации посещаемости.

3.2. Training Management Service
Назначение сервиса: хранение категорий занятий, залов, самих занятий и расписания.

Таблица class_categories.
Поля:
id - UUID, primary key.
name - VARCHAR(100), not null, unique.
description - VARCHAR(255), null.
Назначение таблицы: хранение категорий занятий.

Таблица halls.
Поля:
id - UUID, primary key.
name - VARCHAR(100), not null, unique.
location - VARCHAR(255), not null.
capacity - INTEGER, not null.
status - VARCHAR(20), not null.
Назначение таблицы: хранение залов.

Таблица classes.
Поля:
id - UUID, primary key.
title - VARCHAR(150), not null.
description - TEXT, null.
duration_minutes - INTEGER, not null.
capacity - INTEGER, not null.
category_id - UUID, not null, foreign key -> class_categories.id.
trainer_id - UUID, not null.
status - VARCHAR(20), not null.
created_at - TIMESTAMP, not null.
updated_at - TIMESTAMP, not null.
Назначение таблицы: хранение описаний занятий.
Примечание: trainer_id относится к Identity Service, поэтому foreign key здесь не
используется.

Таблица schedules.
Поля:
id - UUID, primary key.
class_id - UUID, not null, foreign key -> classes.id.
hall_id - UUID, not null, foreign key -> halls.id.
trainer_id - UUID, not null.
start_time - TIMESTAMP, not null.
end_time - TIMESTAMP, not null.
status - VARCHAR(20), not null.
available_slots - INTEGER, not null.
created_at - TIMESTAMP, not null.
updated_at - TIMESTAMP, not null.
Назначение таблицы: хранение конкретных элементов расписания.

Связи внутри сервиса:
class_categories 1:M classes.
classes 1:M schedules.
halls 1:M schedules.

Использование в сценариях:
class_categories и classes используются при создании занятий.
halls и schedules используются при формировании расписания, записи, отмене и
аналитике.

3.3. Membership Service
Назначение сервиса: хранение тарифов и абонементов клиентов.

Таблица subscription_plans.
Поля:
id - UUID, primary key.
name - VARCHAR(100), not null, unique.
duration_days - INTEGER, not null.
visit_limit - INTEGER, not null.
price - DECIMAL(10,2), not null.
is_active - BOOLEAN, not null.
Назначение таблицы: хранение тарифов.

Таблица subscriptions.
Поля:
id - UUID, primary key.
client_id - UUID, not null.
plan_id - UUID, not null, foreign key -> subscription_plans.id.
start_date - DATE, null.
end_date - DATE, null.
remaining_visits - INTEGER, not null.
status - VARCHAR(20), not null.
created_at - TIMESTAMP, not null.
activated_at - TIMESTAMP, null.
Назначение таблицы: хранение абонементов клиентов.

Связи внутри сервиса:
subscription_plans 1:M subscriptions.

Использование в сценариях:
subscription_plans используются при выборе тарифа.
subscriptions используются при покупке, активации, записи на занятие и списании
посещений.

3.4. Booking Service
Назначение сервиса: хранение записей клиентов на занятия и статусов посещения.

Таблица bookings.
Поля:
id - UUID, primary key.
client_id - UUID, not null.
schedule_id - UUID, not null.
subscription_id - UUID, not null.
status - VARCHAR(20), not null.
booked_at - TIMESTAMP, not null.
cancelled_at - TIMESTAMP, null.
attendance_marked_at - TIMESTAMP, null.
Назначение таблицы: хранение записи клиента, отмены записи и результата посещения.

Внутренних foreign key нет, так как client_id, schedule_id и subscription_id относятся к другим сервисам.

Использование в сценариях:
Таблица bookings используется в сценариях записи, отмены записи, фиксации посещаемости и отмены занятия клубом.

3.5. Payment Service
Назначение сервиса: хранение информации об оплатах.

Таблица payments.
Поля:
id - UUID, primary key.
client_id - UUID, not null.
subscription_id - UUID, not null.
amount - DECIMAL(10,2), not null.
currency - VARCHAR(3), not null.
payment_method - VARCHAR(30), not null.
status - VARCHAR(20), not null.
created_at - TIMESTAMP, not null.
paid_at - TIMESTAMP, null.
Назначение таблицы: хранение платежей по абонементам.

Использование в сценариях:
Таблица payments используется в сценариях покупки абонемента, подтверждения оплаты и финансовой аналитики.

3.6. Notification Service
Назначение сервиса: хранение уведомлений и статусов отправки.

Таблица notifications.
Поля:
id - UUID, primary key.
user_id - UUID, not null.
type - VARCHAR(50), not null.
channel - VARCHAR(20), not null.
title - VARCHAR(150), not null.
message - TEXT, not null.
status - VARCHAR(20), not null.
related_entity_type - VARCHAR(50), null.
related_entity_id - UUID, null.
created_at - TIMESTAMP, not null.
sent_at - TIMESTAMP, null.
Назначение таблицы: хранение уведомлений пользователям.

Использование в сценариях:
Таблица notifications используется при регистрации, записи на занятие, отмене записи, отмене занятия и успешной оплате.

3.7. Analytics Service
Назначение сервиса: хранение агрегированных данных для отчётов.

Таблица hall_load_stats.
Поля:
id - UUID, primary key.
hall_id - UUID, not null.
stat_date - DATE, not null.
total_slots - INTEGER, not null.
booked_slots - INTEGER, not null.
attended_count - INTEGER, not null.
Назначение таблицы: статистика загрузки залов.

Таблица class_popularity_stats.
Поля:
id - UUID, primary key.
class_id - UUID, not null.
stat_date - DATE, not null.
bookings_count - INTEGER, not null.
cancellations_count - INTEGER, not null.
attendance_count - INTEGER, not null.
Назначение таблицы: статистика популярности занятий.

Таблица client_activity_stats.
Поля:
id - UUID, primary key.
client_id - UUID, not null.
stat_month - DATE, not null.
bookings_count - INTEGER, not null.
attendance_count - INTEGER, not null.
cancellations_count - INTEGER, not null.
Назначение таблицы: активность клиентов по месяцам.

Таблица revenue_stats.
Поля:
id - UUID, primary key.
stat_date - DATE, not null.
payments_count - INTEGER, not null.
total_amount - DECIMAL(12,2), not null.
Назначение таблицы: финансовая аналитика.

Использование в сценариях:
Таблицы аналитики используются для административной отчётности и обновляются на основе событий Kafka.

4. Связь данных и сценариев
4.1. Сценарий регистрации пользователя
Используемые сервисы: Identity Service.
Читаются данные:
roles.
Создаются данные:
users;
clients или trainers.
Изменяются данные:
нет.

4.2. Сценарий входа пользователя
Используемые сервисы: Identity Service.
Читаются данные:
users;
roles;
clients или trainers.
Создаются данные:
нет.
Изменяются данные:
может обновляться users.updated_at.

4.3. Сценарий создания занятия
Используемые сервисы: Training Management Service, Identity Service.
Читаются данные:
class_categories;
данные тренера через gRPC.
Создаются данные:
classes.
Изменяются данные:
нет.

4.4. Сценарий формирования расписания
Используемые сервисы: Training Management Service, Identity Service.
Читаются данные:
classes;
halls;
данные тренера через gRPC.
Создаются данные:
schedules.
Изменяются данные:
нет.

4.5. Сценарий покупки абонемента
Используемые сервисы: Membership Service, Payment Service.
Читаются данные:
subscription_plans.
Создаются данные:
subscriptions;
payments.
Изменяются данные:
payments.status;
subscriptions.status;
subscriptions.start_date;
subscriptions.end_date;
subscriptions.remaining_visits.

4.6. Сценарий записи на занятие
Используемые сервисы: Booking Service, Training Management Service,
Membership Service.
Читаются данные:
schedules;
subscriptions.
Создаются данные:
bookings.
Изменяются данные:
schedules.available_slots.

4.7. Сценарий отмены записи
Используемые сервисы: Booking Service, Training Management Service.
Читаются данные:
bookings;
schedules.
Создаются данные:
нет.
Изменяются данные:
bookings.status;
bookings.cancelled_at;
schedules.available_slots.

4.8. Сценарий фиксации посещения
Используемые сервисы: Booking Service, Membership Service.
Читаются данные:
bookings;
subscriptions.
Создаются данные:
нет.
Изменяются данные:
bookings.status;
bookings.attendance_marked_at;
subscriptions.remaining_visits.

4.9. Сценарий отмены занятия клубом
Используемые сервисы: Training Management Service, Booking Service,
Notification Service.
Читаются данные:
schedules;
bookings.
Создаются данные:
notifications.
Изменяются данные:
schedules.status;
bookings.status.

4.10. Сценарий построения аналитики
Используемые сервисы: Analytics Service.
Читаются данные:
Kafka-события;
уже накопленные агрегаты.
Создаются данные:
hall_load_stats;
class_popularity_stats;
client_activity_stats;
revenue_stats.
Изменяются данные:
все перечисленные таблицы агрегатов.

5. GraphQL API
GraphQL используется как единая внешняя точка входа. Клиент работает только с GraphQL Gateway, а тот вызывает нужные внутренние сервисы и собирает единый ответ.

5.1. Query

type Query {
  me: UserProfile!
  schedule(filter: ScheduleFilterInput): [ScheduleItem!]!
  trainerSchedule(trainerId: ID!, from: String, to: String): [ScheduleItem!]!
  myBookings(status: BookingStatus): [BookingItem!]!
  mySubscriptions: [SubscriptionItem!]!
  myPayments: [PaymentItem!]!
  myNotifications(unreadOnly: Boolean): [NotificationItem!]!
  analytics(filter: AnalyticsFilterInput!): AnalyticsReport!
}

Назначение Query:
me - получение профиля текущего пользователя;
schedule - просмотр доступного расписания;
trainerSchedule - просмотр расписания тренера;
myBookings - список и история записей клиента;
mySubscriptions - список абонементов клиента;
myPayments - история оплат;
myNotifications - уведомления пользователя;
analytics - агрегированная аналитика для администратора.

5.2. Mutation

type Mutation {
  registerUser(input: RegisterUserInput!): AuthPayload!
  login(input: LoginInput!): AuthPayload!
  createClass(input: CreateClassInput!): ClassItem!
  updateClass(input: UpdateClassInput!): ClassItem!
  createSchedule(input: CreateScheduleInput!): ScheduleItem!
  changeScheduleStatus(input: ChangeScheduleStatusInput!): ScheduleItem!
  purchaseSubscription(input: PurchaseSubscriptionInput!): SubscriptionPurchasePayload!
  confirmPayment(input: ConfirmPaymentInput!): PaymentItem!
  bookSchedule(input: BookScheduleInput!): BookingItem!
  cancelBooking(input: CancelBookingInput!): BookingItem!
  markAttendance(input: MarkAttendanceInput!): BookingItem!
}

Назначение Mutation:
registerUser и login используются для регистрации и входа;
createClass и updateClass используются для работы с занятиями;
createSchedule и changeScheduleStatus используются для работы с расписанием;
purchaseSubscription и confirmPayment используются при покупке абонемента;
bookSchedule и cancelBooking используются для записи и отмены записи;
markAttendance используется для фиксации посещаемости.

5.3. Основные типы данных

type UserProfile {
  id: ID!
  email: String!
  role: String!
  client: ClientProfile
  trainer: TrainerProfile
}

type ClientProfile {
  id: ID!
  fullName: String!
  phone: String!
  membershipStatus: String!
}

type TrainerProfile {
  id: ID!
  fullName: String!
  phone: String!
  specialization: String!
  employmentStatus: String!
}

type ClassItem {
  id: ID!
  title: String!
  description: String
  durationMinutes: Int!
  capacity: Int!
  categoryId: ID!
  trainerId: ID!
  status: String!
}

type ScheduleItem {
  id: ID!
  classId: ID!
  hallId: ID!
  trainerId: ID!
  startTime: String!
  endTime: String!
  status: String!
  availableSlots: Int!
}

type SubscriptionItem {
  id: ID!
  clientId: ID!
  planId: ID!
  startDate: String
  endDate: String
  remainingVisits: Int!
  status: String!
}

type PaymentItem {
  id: ID!
  clientId: ID!
  subscriptionId: ID!
  amount: Float!
  currency: String!
  paymentMethod: String!
  status: String!
  paidAt: String
}

type BookingItem {
  id: ID!
  clientId: ID!
  scheduleId: ID!
  subscriptionId: ID!
  status: String!
  bookedAt: String!
  cancelledAt: String
  attendanceMarkedAt: String
}

type NotificationItem {
  id: ID!
  type: String!
  channel: String!
  title: String!
  message: String!
  status: String!
  createdAt: String!
  sentAt: String
}

type AnalyticsReport {
  totalRevenue: Float!
  totalBookings: Int!
  totalAttendance: Int!
  hallLoad: [HallLoadItem!]!
  popularClasses: [PopularClassItem!]!
  activeClients: [ActiveClientItem!]!
}

5.4. Input-типы

input RegisterUserInput {
  email: String!
  password: String!
  role: String!
  fullName: String!
  phone: String!
  birthDate: String
  specialization: String
}

input LoginInput {
  email: String!
  password: String!
}

input CreateClassInput {
  title: String!
  description: String
  durationMinutes: Int!
  capacity: Int!
  categoryId: ID!
  trainerId: ID!
}

input UpdateClassInput {
  id: ID!
  title: String
  description: String
  durationMinutes: Int
  capacity: Int
  categoryId: ID
  trainerId: ID
  status: String
}

input CreateScheduleInput {
  classId: ID!
  hallId: ID!
  trainerId: ID!
  startTime: String!
  endTime: String!
}

input ChangeScheduleStatusInput {
  scheduleId: ID!
  status: String!
}

input PurchaseSubscriptionInput {
  clientId: ID!
  planId: ID!
  paymentMethod: String!
}

input ConfirmPaymentInput {
  paymentId: ID!
  status: String!
}

input BookScheduleInput {
  clientId: ID!
  scheduleId: ID!
  subscriptionId: ID!
}

input CancelBookingInput {
  bookingId: ID!
}

input MarkAttendanceInput {
  bookingId: ID!
  status: String!
}

input ScheduleFilterInput {
  trainerId: ID
  categoryId: ID
  startDate: String
  endDate: String
  status: String
}

input AnalyticsFilterInput {
  fromDate: String!
  toDate: String!
}

enum BookingStatus {
  BOOKED
  CANCELLED
  ATTENDED
  NO_SHOW
  CANCELLED_BY_CLUB
}

5.5. Связь GraphQL операций со сценариями
registerUser и login используются в сценарии регистрации и входа.
schedule и trainerSchedule используются для просмотра расписания.
createClass и updateClass используются в сценарии управления занятиями.
createSchedule и changeScheduleStatus используются при работе с расписанием.
purchaseSubscription и confirmPayment используются при оформлении абонемента.
bookSchedule и cancelBooking используются в сценариях записи и отмены записи.
markAttendance используется в сценарии фиксации посещаемости.
myBookings, mySubscriptions, myPayments и myNotifications используются в личном кабинете клиента.
analytics используется для получения административной аналитики.

6. Сценарии взаимодействия и контракты
6.1. gRPC-взаимодействия
gRPC используется тогда, когда без ответа другого сервиса операция не может быть завершена.

Identity Service:
GetTrainer.
Назначение: проверить существование тренера и получить его данные.
Вызывается сервисом Training Management Service.

GetUserContact.
Назначение: получить email и телефон пользователя.
Вызывается сервисом Notification Service.

Training Management Service:
GetScheduleAvailability.
Назначение: получить статус занятия, время и число свободных мест.
Вызывается сервисом Booking Service.

ReserveSlot.
Назначение: уменьшить число свободных мест после успешной записи.
Вызывается сервисом Booking Service.

ReleaseSlot.
Назначение: освободить место при отмене записи.
Вызывается сервисом Booking Service.

Membership Service:
CheckSubscription.
Назначение: проверить действительность абонемента.
Вызывается сервисом Booking Service.

ActivateSubscription.
Назначение: активировать абонемент после оплаты.
Вызывается сервисом Payment Service.

RegisterVisit.
Назначение: списать посещение после фактического присутствия.
Вызывается сервисом Booking Service.

6.2. Укрупнённые protobuf-контракты

syntax = "proto3";

service IdentityGrpcService {
  rpc GetTrainer(GetTrainerRequest) returns (GetTrainerResponse);
  rpc GetUserContact(GetUserContactRequest) returns (GetUserContactResponse);
}

message GetTrainerRequest {
  string trainer_id = 1;
}

message GetTrainerResponse {
  string trainer_id = 1;
  string full_name = 2;
  string employment_status = 3;
  bool exists = 4;
}

message GetUserContactRequest {
  string user_id = 1;
}

message GetUserContactResponse {
  string user_id = 1;
  string email = 2;
  string phone = 3;
  bool exists = 4;
}

service TrainingGrpcService {
  rpc GetScheduleAvailability(GetScheduleAvailabilityRequest) returns (GetScheduleAvailabilityResponse);
  rpc ReserveSlot(ReserveSlotRequest) returns (ReserveSlotResponse);
  rpc ReleaseSlot(ReleaseSlotRequest) returns (ReleaseSlotResponse);
}

message GetScheduleAvailabilityRequest {
  string schedule_id = 1;
}

message GetScheduleAvailabilityResponse {
  string schedule_id = 1;
  string status = 2;
  int32 available_slots = 3;
  string start_time = 4;
  string end_time = 5;
}

message ReserveSlotRequest {
  string schedule_id = 1;
}

message ReserveSlotResponse {
  string schedule_id = 1;
  int32 available_slots = 2;
  bool reserved = 3;
}

message ReleaseSlotRequest {
  string schedule_id = 1;
}

message ReleaseSlotResponse {
  string schedule_id = 1;
  int32 available_slots = 2;
  bool released = 3;
}

service MembershipGrpcService {
  rpc CheckSubscription(CheckSubscriptionRequest) returns (CheckSubscriptionResponse);
  rpc ActivateSubscription(ActivateSubscriptionRequest) returns (ActivateSubscriptionResponse);
  rpc RegisterVisit(RegisterVisitRequest) returns (RegisterVisitResponse);
}

message CheckSubscriptionRequest {
  string subscription_id = 1;
  string client_id = 2;
}

message CheckSubscriptionResponse {
  string subscription_id = 1;
  string client_id = 2;
  string status = 3;
  int32 remaining_visits = 4;
  string valid_until = 5;
  bool is_valid = 6;
}

message ActivateSubscriptionRequest {
  string subscription_id = 1;
  string payment_id = 2;
}

message ActivateSubscriptionResponse {
  string subscription_id = 1;
  string status = 2;
  string start_date = 3;
  string end_date = 4;
  int32 remaining_visits = 5;
}

message RegisterVisitRequest {
  string subscription_id = 1;
}

message RegisterVisitResponse {
  string subscription_id = 1;
  int32 remaining_visits = 2;
  bool updated = 3;
}

6.3. Kafka-события

Топик user-events:
Событие UserRegistered.
Отправитель: Identity Service.
Получатели: Notification Service, Analytics Service.

Payload:
event_id - string.
user_id - string.
role - string.
email - string.
occurred_at - string.

Топик schedule-events:
События ScheduleCreated, ScheduleStatusChanged, ScheduleCancelled.
Отправитель: Training Management Service.
Получатели: Booking Service, Notification Service, Analytics Service.

Payload:
event_id - string.
schedule_id - string.
class_id - string.
trainer_id - string.
hall_id - string.
status - string.
start_time - string.
end_time - string.
occurred_at - string.

Топик subscription-events:
События SubscriptionCreated, SubscriptionActivated.
Отправитель: Membership Service.
Получатели: Notification Service, Analytics Service.

Payload:
event_id - string.
subscription_id - string.
client_id - string.
plan_id - string.
status - string.
start_date - string.
end_date - string.
occurred_at - string.

Топик booking-events:
События BookingCreated, BookingCancelled, AttendanceMarked.
Отправитель: Booking Service.
Получатели: Notification Service, Analytics Service.

Payload:
event_id - string.
booking_id - string.
client_id - string.
schedule_id - string.
subscription_id - string.
status - string.
occurred_at - string.

Топик payment-events:
События PaymentCompleted, PaymentFailed.
Отправитель: Payment Service.
Получатели: Membership Service, Notification Service, Analytics Service.

Payload:
event_id - string.
payment_id - string.
client_id - string.
subscription_id - string.
amount - decimal.
status - string.
payment_method - string.
occurred_at - string.

7. Последовательность выполнения бизнес-сценариев
7.1. Регистрация пользователя
1) Пользователь вызывает GraphQL mutation registerUser.
2) GraphQL Gateway передаёт запрос в Identity Service.
3) Identity Service читает таблицу roles.
4) В таблице users создаётся учётная запись.
5) В таблице clients или trainers создаётся профиль.
6) Пользователю возвращается результат регистрации и токен доступа.
7) Публикуется событие UserRegistered.
8) Notification Service создаёт приветственное уведомление.

7.2. Создание занятия и элемента расписания
1) Администратор или тренер вызывает createClass.
2) Gateway передаёт запрос в Training Management Service.
3) Сервис читает class_categories.
4) Через gRPC GetTrainer проверяется тренер.
5) В таблице classes создаётся занятие.
6) Затем вызывается createSchedule.
7) Сервис читает classes и halls.
8) В таблице schedules создаётся элемент расписания.
9) Публикуется событие ScheduleCreated.

7.3. Покупка абонемента
1) Клиент вызывает purchaseSubscription.
2) Gateway передаёт запрос в Membership Service.
3) Сервис читает subscription_plans.
4) В subscriptions создаётся запись со статусом PENDING.
5) Через Gateway создаётся платёж в Payment Service.
6) В payments создаётся запись со статусом PENDING.
7) После подтверждения оплаты вызывается confirmPayment.
8) Payment Service переводит платёж в статус COMPLETED.
9) Payment Service по gRPC вызывает ActivateSubscription.
10) Membership Service переводит абонемент в статус ACTIVE, задаёт даты действия и remaining_visits.
11) Публикуются события PaymentCompleted и SubscriptionActivated.
12) Notification Service создаёт уведомление об успешной оплате.

7.4. Запись клиента на занятие
1) Клиент вызывает bookSchedule.
2) Gateway передаёт запрос в Booking Service.
3) Booking Service вызывает GetScheduleAvailability.
4) Training Management Service возвращает статус занятия и available_slots.
5) Booking Service вызывает CheckSubscription.
6) Membership Service подтверждает валидность абонемента.
7) Booking Service вызывает ReserveSlot.
8) Training Management Service уменьшает available_slots.
9) В bookings создаётся запись со статусом BOOKED.
10) Публикуется событие BookingCreated.
11) Notification Service создаёт уведомление клиенту.
12) Analytics Service обновляет статистику.

7.5. Отмена записи
1) Клиент вызывает cancelBooking.
2) Gateway передаёт запрос в Booking Service.
3) Booking Service читает bookings.
4) Выполняется проверка статуса занятия и допустимости отмены.
5) Запись переводится в статус CANCELLED.
6) Заполняется cancelled_at.
7) Booking Service вызывает ReleaseSlot.
8) Training Management Service увеличивает available_slots.
9) Публикуется событие BookingCancelled.
10) Notification Service отправляет уведомление.

7.6. Фиксация посещения
1) Тренер вызывает markAttendance.
2) Gateway передаёт запрос в Booking Service.
3) Booking Service читает bookings.
4) В bookings устанавливается статус ATTENDED или NO_SHOW.
5) Заполняется attendance_marked_at.
6) Если клиент присутствовал, вызывается RegisterVisit.
7) Membership Service уменьшает remaining_visits.
8) Публикуется событие AttendanceMarked.
9) Analytics Service обновляет показатели посещаемости.

7.7. Отмена занятия клубом
1) Администратор вызывает changeScheduleStatus со статусом CANCELLED.
2) Gateway передаёт запрос в Training Management Service.
3) В schedules меняется статус.
4) Публикуется событие ScheduleCancelled.
5) Booking Service получает событие и переводит связанные записи в CANCELLED_BY_CLUB.
6) Notification Service создаёт уведомления записанным клиентам.
7) Analytics Service корректирует агрегаты.

7.8. Получение аналитики
1) Администратор вызывает query analytics.
2) Gateway обращается в Analytics Service.
3) Analytics Service читает hall_load_stats, class_popularity_stats, client_activity_stats и revenue_stats.
4) Формируется единый отчёт.
5) Отчёт возвращается через GraphQL Gateway.

8. План разработки системы
Этап 1. Identity Service и базовая аутентификация.
Реализуются таблицы roles, users, clients, trainers, а также сценарии регистрации, входа и получения профиля.

Этап 2. Training Management Service.
Реализуются таблицы class_categories, halls, classes, schedules и операции работы с занятиями и расписанием.

Этап 3. Membership Service и Payment Service.
Реализуются таблицы subscription_plans, subscriptions, payments и сценарии покупки, оплаты и активации абонемента.

Этап 4. Booking Service.
Реализуется таблица bookings и сценарии записи, отмены записи и фиксации посещаемости.

Этап 5. gRPC-контракты.
Реализуются синхронные вызовы между сервисами для проверки тренера, расписания, абонемента, резервирования места и списания посещения.

Этап 6. GraphQL Gateway.
Собирается единая внешняя схема API, объединяющая основные Query и Mutation.

Этап 7. Kafka и Notification Service.
Настраиваются топики, публикация событий и обработчики уведомлений.

Этап 8. Analytics Service.
Реализуются агрегаты и отчёты по доходности, загрузке, посещаемости и активности клиентов.

Этап 9. Интеграционное тестирование.
Проверяются сквозные сценарии регистрации, покупки абонемента, записи, отмены записи, фиксации посещаемости и получения аналитики.

Минимальный MVP должен включать:
1) Регистрацию и вход пользователя.
2) Создание занятий и расписания.
3) Покупку и активацию абонемента.
4) Запись и отмену записи.
5) Просмотр основных данных через GraphQL.

9. Итог
В лабораторной работе была выполнена техническая детализация системы управления фитнес-клубом. 
Для каждого сервиса определены таблицы, поля, типы и связи внутри базы.
Также показана связь структур данных со сценариями системы, спроектирован внешний GraphQL API, описаны gRPC-контракты и Kafka-события, а затем сформирован поэтапный план разработки.