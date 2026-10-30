1. Цель лабораторной работы №3
Целью работы является создание первой рабочей версии распределённой системы управления фитнес-клубом на основе архитектуры, спроектированной в ЛР1 и ЛР2.
На данном этапе требуется:
подготовить структуру проекта с разделением GraphQL API и внутренних сервисов;
подключить базы данных для сервисов;
реализовать слои приложения (данные / бизнес-логика / внешний API);
реализовать GraphQL Query (без Mutation);
реализовать базовые операции чтения и получение связанных сущностей;
обеспечить изоляцию сервисов и межсервисное взаимодействие через заглушки;
подготовить тестовые данные и проверить работоспособность системы.

2. Исходные ограничения и архитектурные решения
2.1. Ограничения задания
сервис работает только со своей областью ответственности и своими данными;
прямой доступ к таблицам, БД и репозиториям другого сервиса запрещён;
реальные gRPC и Kafka на этом этапе не реализуются;
межсервисные вызовы имитируются stub/mock-реализациями;
GraphQL является внешним слоем и отражает бизнес-сценарии, а не структуру таблиц;
в GraphQL-резолверах нет бизнес-логики и прямых запросов к БД.

2.2. Выбранный подход к реализации
Реализован модульный монолит на Spring Boot 4.1.1 (Java 21):
один исполняемый процесс для удобства запуска и проверки ЛР3;
логическое разделение на пакеты по сервисам архитектуры ЛР1/ЛР2;
данные сервисов изолированы через отдельные schema в H2;
межсервисные зависимости идут через интерфейсы-клиенты и stub-реализации.
Такой подход сохраняет границы сервисов и позволяет позже заменить stub на реальные gRPC/Kafka без перестройки бизнес-логики.

3. Структура проекта
Пакетная структура:

com.example.fitnessclubsystem
  gateway/graphql          — внешний GraphQL API
  identity                 — Identity Service
  training                 — Training Management Service
  membership               — Membership Service
  booking                  — Booking Service
  payment                  — Payment Service
  notification             — Notification Service
  analytics                — Analytics Service
  integration              — stub-клиенты межсервисного взаимодействия
  shared                   — общие исключения и контекст текущего пользователя
  config                   — загрузка демо-данных

Внутри каждого сервиса выдержаны слои:
domain — JPA-сущности;
persistence — Spring Data репозитории;
dto — объекты для передачи наружу (не сущности БД);
service — бизнес-логика чтения.

4. Технологический стек
Spring Boot 4.1.1;
Java 21;
spring-boot-starter-webmvc — HTTP-сервер;
spring-boot-starter-graphql — GraphQL API;
spring-boot-starter-data-jpa — доступ к данным;
H2 in-memory БД;
graphql-java-extended-scalars — скаляр DateTime;
GraphiQL — UI для проверки запросов;
JUnit + интеграционные тесты GraphQL.

Зависимости описаны в pom.xml.

5. Организация данных и изоляция сервисов
5.1. База данных
Используется одна in-memory H2 БД:
jdbc:h2:mem:fitness
при этом для каждого сервиса создана отдельная schema:
identity;
training;
membership;
booking;
payment;
notification;
analytics.
Создание schema включено через:
spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true
Таблицы создаются Hibernate (ddl-auto=update).

5.2. Источники истины по сервисам
Identity: roles, users, clients, trainers;
Training: class_categories, halls, classes, schedules;
Membership: subscription_plans, subscriptions;
Booking: bookings;
Payment: payments;
Notification: notifications;
Analytics: hall_load_stats, class_popularity_stats, client_activity_stats, revenue_stats.

5.3. Правило изоляции
репозиторий сервиса A не используется сервисом B;
если Booking нуждаются в данных расписания, вызывается TrainingClient (stub), а не ScheduleRepository;
foreign key между schema разных сервисов не создаются;
межсервисные идентификаторы хранятся как обычные UUID-поля.

6. Реализация внутренних сервисов
6.1. Identity Service
Сущности: RoleEntity, UserEntity, ClientEntity, TrainerEntity.
Сервис: IdentityQueryService.
Операции чтения:
getUserProfile(userId) — профиль + связанные client/trainer;
getClient(clientId);
getTrainer(trainerId).
Связанные данные внутри сервиса: User → Role, User → Client/Trainer.

6.2. Training Management Service
Сущности: ClassCategoryEntity, HallEntity, ClassEntity, ScheduleEntity.
Сервис: TrainingQueryService.
Операции:
getClassById;
getScheduleById;
findSchedules(filter);
findTrainerSchedule(trainerId, from, to).
Связанные данные: Schedule → Class → Category, Schedule → Hall.
В DTO возвращаются classItem и hallName.

6.3. Membership Service
Сущности: SubscriptionPlanEntity, SubscriptionEntity.
Сервис: MembershipQueryService.
Операции:
getById;
findByClientId.
Связанные данные: Subscription → Plan (planName).

6.4. Booking Service
Сущность: BookingEntity.
Сервис: BookingQueryService.
Операции:
getById;
findByClient(clientId, status).
Связанные данные из другого сервиса через TrainingClientStub:
scheduleStartTime;
classTitle.
Это пример корректного межсервисного чтения без прямого доступа к чужой БД.

6.5. Payment Service
Сущность: PaymentEntity.
Сервис: PaymentQueryService.
Операции:
getById;
findByClientId.

6.6. Notification Service
Сущность: NotificationEntity.
Сервис: NotificationQueryService.
Операции:
findByUser(userId, unreadOnly).

6.7. Analytics Service
Сущности агрегатов:
HallLoadStatEntity;
ClassPopularityStatEntity;
ClientActivityStatEntity;
RevenueStatEntity.
Сервис: AnalyticsQueryService.
Операция:
getReport(from, to) — агрегированный отчёт для администратора.
Analytics не читает транзакционные таблицы других сервисов, а работает со своими агрегатами.

7. Межсервисное взаимодействие через заглушки
Реализованы интерфейсы и stub-реализации:
TrainingClient / TrainingClientStub;
IdentityClient / IdentityClientStub;
MembershipClient / MembershipClientStub.
Назначение:
сохранить контракт будущего gRPC-вызова;
не допустить прямого использования чужих репозиториев;
оставить точку замены stub → реальный клиент в следующих лабораторных.
Пример: BookingQueryService обогащает запись данными расписания через TrainingClient.findScheduleById(...).

8. GraphQL API (только Query)
8.1. Роль Gateway
Внешний слой: gateway/graphql/QueryController.
Резолверы только:
принимают аргументы запроса;
вызывают сервисный слой;
возвращают DTO.
В резолверах нет:
обращений к Repository;
SQL/JPQL;
бизнес-проверок.

8.2. Реализованные Query
me — профиль текущего пользователя;
user(id) — профиль по ID;
classById(id) — занятие по ID;
schedule(filter) — список расписания с фильтром;
scheduleById(id) — элемент расписания по ID;
trainerSchedule(trainerId, from, to) — расписание тренера;
myBookings(status) — записи текущего клиента;
bookingById(id) — запись по ID;
mySubscriptions — абонементы текущего клиента;
subscriptionById(id) — абонемент по ID;
myPayments — платежи текущего клиента;
paymentById(id) — платёж по ID;
myNotifications(unreadOnly) — уведомления;
analytics(filter) — аналитический отчёт.

8.3. Связанные данные в GraphQL
UserProfile.client / UserProfile.trainer;
ScheduleItem.classItem / ScheduleItem.hallName;
BookingItem.classTitle / BookingItem.scheduleStartTime;
SubscriptionItem.planName.
Это соответствует требованию получения связанных сущностей и отражает бизнес-представление, а не «сырые» таблицы.

8.4. Текущий пользователь
Так как аутентификация в ЛР3 не реализуется полноценно, используется stub:
CurrentUserProvider;
идентификаторы задаются в application.properties:
app.demo.current-user-id;
app.demo.current-client-id.
Операции me / myBookings / mySubscriptions / myPayments / myNotifications работают от демо-клиента.

9. Обработка ошибок
Реализовано:
NotFoundException — отсутствие сущности;
GraphQlExceptionResolver — преобразование в GraphQL error с classification NOT_FOUND.
Пример ответа при запросе несуществующей записи:
message: "Booking not found: ...";
extensions.classification: NOT_FOUND.

10. Тестовые (демо) данные
Класс DemoDataLoader загружает данные при старте приложения.
DemoIds фиксирует стабильные UUID для воспроизводимых запросов.
Создаются:
роли CLIENT / TRAINER / ADMIN;
пользователи client@fitness.local, trainer@fitness.local, admin@fitness.local;
клиент и тренер;
категория Yoga, зал A, занятие Morning Yoga;
2 элемента расписания;
тариф Standard 12 и активный абонемент;
успешный платёж;
запись на занятие;
уведомление о записи;
агрегаты аналитики (выручка 4500, статистика загрузки и популярности).

11. Проблемы, обнаруженные при проверке, и исправления
11.1. Ошибка сборки Maven / красная подсветка в IDE
Проблема: в pom.xml у зависимости graphql-java-extended-scalars не была указана версия.
Следствие: Maven не мог разрешить зависимости, IDE подсвечивала проект ошибками.
Исправление: добавлена версия 24.0.

11.2. Конфликт порта 8080
Проблема: при запуске приложение падало с ошибкой Port 8080 was already in use.
Исправление при проверке: запуск на порту 8081.
Это проблема окружения, а не кода.

11.3. GraphQL endpoint возвращал 404
Проблема: после старта приложения POST /graphql давал 404.
Причина: Spring Boot GraphQL по умолчанию ищет схемы с расширениями .graphqls / .gqls, а файл был назван schema.graphql.
Из-за этого не создавались GraphQlSource и ExecutionGraphQlService, а значит не поднимался HTTP endpoint.
Исправление:
файл переименован в schema.graphqls;
в application.properties добавлено:
spring.graphql.schema.file-extensions=.graphqls,.gqls,.graphql.
После исправления в логе появляется:
GraphQL endpoint HTTP POST /graphql.

11.4. Совместимость со Spring Boot 4
Проблема: starter web в Boot 4 модульный; для servlet/GraphQL WebMvc использован spring-boot-starter-webmvc.
Также учтено, что DateTime scalar требует OffsetDateTime (DTO переведены с Instant на OffsetDateTime на границе API).

11.5. Интеграционные тесты
Добавлен QueryControllerIT:
проверка me;
schedule со связанным classItem;
classById;
myBookings с classTitle;
mySubscriptions / myPayments;
myNotifications / analytics;
ошибка not found для bookingById.
Все тесты проходят успешно.

12. Результаты проверки работоспособности
Выполнено:
mvn test — успешно;
запуск приложения — успешно;
проверка GraphQL-запросов через HTTP — успешно;
GraphiQL доступен.
Проверенные сценарии:
получение профиля текущего клиента;
получение списка расписания и связанных данных занятия/зала;
получение записи с обогащением названием занятия;
получение абонементов и платежей;
получение уведомлений;
получение аналитики;
обработка отсутствующей сущности.

Примеры точек входа:
GraphQL: POST /graphql
GraphiQL: /graphiql
H2 Console: /h2-console

13. Соответствие требованиям ЛР3
Разделение ответственности по сервисам — выполнено.
Нет прямого доступа к чужим данным — выполнено.
Межсервисные вызовы через stub — выполнено.
Базовая структура проекта и слои — выполнено.
Подключение БД (schema на сервис) — выполнено.
GraphQL schema + Query + input-типы — выполнено.
Получение по ID / списков / связанных данных — выполнено.
Резолверы без бизнес-логики и без прямого доступа к БД — выполнено.
Обработка ошибок отсутствия данных — выполнено.
Тестовые данные — выполнено.
Mutation на этом этапе намеренно не реализованы (по условиям ЛР3).

14. Итог
В ходе ЛР3 реализована первая рабочая версия системы Fitness Club:
логически разделены 7 сервисов и GraphQL Gateway;
обеспечена изоляция данных через schema и запрет чужих репозиториев;
реализованы операции чтения через GraphQL Query;
связанные данные формируются на уровне сервисов и stub-клиентов;
добавлены демо-данные и проверена работоспособность end-to-end.
Полученная реализация может служить основой для следующих лабораторных работ: Mutation, реальный gRPC и Kafka.
