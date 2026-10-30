Контракты системы:
docs/contracts/graphql/schema.graphql — GraphQL SDL;
docs/contracts/proto/identity.proto — gRPC Identity Service;
docs/contracts/proto/training.proto — gRPC Training Management Service;
docs/contracts/proto/membership.proto — gRPC Membership Service;
docs/contracts/events/envelope.schema.json — конверт Kafka-события;
docs/contracts/events/*.json — примеры payload.

Абонемент активируется через gRPC ActivateSubscription.
Событие PaymentCompleted обрабатывают Notification Service и Analytics Service.
После активации Membership Service публикует SubscriptionActivated.