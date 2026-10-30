package com.example.fitnessclubsystem.config;

import com.example.fitnessclubsystem.analytics.domain.ClassPopularityStatEntity;
import com.example.fitnessclubsystem.analytics.domain.ClientActivityStatEntity;
import com.example.fitnessclubsystem.analytics.domain.HallLoadStatEntity;
import com.example.fitnessclubsystem.analytics.domain.RevenueStatEntity;
import com.example.fitnessclubsystem.analytics.persistence.ClassPopularityStatRepository;
import com.example.fitnessclubsystem.analytics.persistence.ClientActivityStatRepository;
import com.example.fitnessclubsystem.analytics.persistence.HallLoadStatRepository;
import com.example.fitnessclubsystem.analytics.persistence.RevenueStatRepository;
import com.example.fitnessclubsystem.booking.domain.BookingEntity;
import com.example.fitnessclubsystem.booking.persistence.BookingRepository;
import com.example.fitnessclubsystem.identity.domain.ClientEntity;
import com.example.fitnessclubsystem.identity.domain.RoleEntity;
import com.example.fitnessclubsystem.identity.domain.TrainerEntity;
import com.example.fitnessclubsystem.identity.domain.UserEntity;
import com.example.fitnessclubsystem.identity.persistence.ClientRepository;
import com.example.fitnessclubsystem.identity.persistence.RoleRepository;
import com.example.fitnessclubsystem.identity.persistence.TrainerRepository;
import com.example.fitnessclubsystem.identity.persistence.UserRepository;
import com.example.fitnessclubsystem.membership.domain.SubscriptionEntity;
import com.example.fitnessclubsystem.membership.domain.SubscriptionPlanEntity;
import com.example.fitnessclubsystem.membership.persistence.SubscriptionPlanRepository;
import com.example.fitnessclubsystem.membership.persistence.SubscriptionRepository;
import com.example.fitnessclubsystem.notification.domain.NotificationEntity;
import com.example.fitnessclubsystem.notification.persistence.NotificationRepository;
import com.example.fitnessclubsystem.payment.domain.PaymentEntity;
import com.example.fitnessclubsystem.payment.persistence.PaymentRepository;
import com.example.fitnessclubsystem.shared.DemoIds;
import com.example.fitnessclubsystem.training.domain.ClassCategoryEntity;
import com.example.fitnessclubsystem.training.domain.ClassEntity;
import com.example.fitnessclubsystem.training.domain.HallEntity;
import com.example.fitnessclubsystem.training.domain.ScheduleEntity;
import com.example.fitnessclubsystem.training.persistence.ClassCategoryRepository;
import com.example.fitnessclubsystem.training.persistence.ClassRepository;
import com.example.fitnessclubsystem.training.persistence.HallRepository;
import com.example.fitnessclubsystem.training.persistence.ScheduleRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DemoDataLoader implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final TrainerRepository trainerRepository;
    private final ClassCategoryRepository classCategoryRepository;
    private final HallRepository hallRepository;
    private final ClassRepository classRepository;
    private final ScheduleRepository scheduleRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final NotificationRepository notificationRepository;
    private final HallLoadStatRepository hallLoadStatRepository;
    private final ClassPopularityStatRepository classPopularityStatRepository;
    private final ClientActivityStatRepository clientActivityStatRepository;
    private final RevenueStatRepository revenueStatRepository;

    public DemoDataLoader(
            RoleRepository roleRepository,
            UserRepository userRepository,
            ClientRepository clientRepository,
            TrainerRepository trainerRepository,
            ClassCategoryRepository classCategoryRepository,
            HallRepository hallRepository,
            ClassRepository classRepository,
            ScheduleRepository scheduleRepository,
            SubscriptionPlanRepository subscriptionPlanRepository,
            SubscriptionRepository subscriptionRepository,
            PaymentRepository paymentRepository,
            BookingRepository bookingRepository,
            NotificationRepository notificationRepository,
            HallLoadStatRepository hallLoadStatRepository,
            ClassPopularityStatRepository classPopularityStatRepository,
            ClientActivityStatRepository clientActivityStatRepository,
            RevenueStatRepository revenueStatRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.trainerRepository = trainerRepository;
        this.classCategoryRepository = classCategoryRepository;
        this.hallRepository = hallRepository;
        this.classRepository = classRepository;
        this.scheduleRepository = scheduleRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.notificationRepository = notificationRepository;
        this.hallLoadStatRepository = hallLoadStatRepository;
        this.classPopularityStatRepository = classPopularityStatRepository;
        this.clientActivityStatRepository = clientActivityStatRepository;
        this.revenueStatRepository = revenueStatRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            return;
        }

        Instant now = Instant.now();

        RoleEntity roleClient = role(DemoIds.ROLE_CLIENT, "CLIENT", "Клиент");
        RoleEntity roleTrainer = role(DemoIds.ROLE_TRAINER, "TRAINER", "Тренер");
        RoleEntity roleAdmin = role(DemoIds.ROLE_ADMIN, "ADMIN", "Администратор");
        roleRepository.save(roleClient);
        roleRepository.save(roleTrainer);
        roleRepository.save(roleAdmin);

        UserEntity clientUser = user(DemoIds.USER_CLIENT, "client@fitness.local", roleClient, now);
        UserEntity trainerUser = user(DemoIds.USER_TRAINER, "trainer@fitness.local", roleTrainer, now);
        UserEntity adminUser = user(DemoIds.USER_ADMIN, "admin@fitness.local", roleAdmin, now);
        userRepository.save(clientUser);
        userRepository.save(trainerUser);
        userRepository.save(adminUser);

        ClientEntity client = new ClientEntity();
        client.setId(DemoIds.CLIENT);
        client.setUser(clientUser);
        client.setFullName("Анна Клиентова");
        client.setPhone("+79001112233");
        client.setBirthDate(LocalDate.of(1998, 5, 12));
        client.setMembershipStatus("ACTIVE");
        client.setCreatedAt(now);
        clientRepository.save(client);

        TrainerEntity trainer = new TrainerEntity();
        trainer.setId(DemoIds.TRAINER);
        trainer.setUser(trainerUser);
        trainer.setFullName("Игорь Тренеров");
        trainer.setPhone("+79004445566");
        trainer.setSpecialization("Yoga");
        trainer.setEmploymentStatus("ACTIVE");
        trainer.setCreatedAt(now);
        trainerRepository.save(trainer);

        ClassCategoryEntity yoga = new ClassCategoryEntity();
        yoga.setId(DemoIds.CATEGORY_YOGA);
        yoga.setName("Yoga");
        yoga.setDescription("Йога и растяжка");
        classCategoryRepository.save(yoga);

        HallEntity hall = new HallEntity();
        hall.setId(DemoIds.HALL_A);
        hall.setName("Зал A");
        hall.setLocation("1 этаж");
        hall.setCapacity(20);
        hall.setStatus("ACTIVE");
        hallRepository.save(hall);

        ClassEntity classEntity = new ClassEntity();
        classEntity.setId(DemoIds.CLASS_YOGA);
        classEntity.setTitle("Morning Yoga");
        classEntity.setDescription("Утренняя йога для начинающих");
        classEntity.setDurationMinutes(60);
        classEntity.setCapacity(15);
        classEntity.setCategory(yoga);
        classEntity.setTrainerId(DemoIds.TRAINER);
        classEntity.setStatus("ACTIVE");
        classEntity.setCreatedAt(now);
        classEntity.setUpdatedAt(now);
        classRepository.save(classEntity);

        ScheduleEntity schedule1 = schedule(DemoIds.SCHEDULE_1, classEntity, hall, now.plus(1, ChronoUnit.DAYS), 12);
        ScheduleEntity schedule2 = schedule(DemoIds.SCHEDULE_2, classEntity, hall, now.plus(2, ChronoUnit.DAYS), 15);
        scheduleRepository.save(schedule1);
        scheduleRepository.save(schedule2);

        SubscriptionPlanEntity plan = new SubscriptionPlanEntity();
        plan.setId(DemoIds.PLAN_STANDARD);
        plan.setName("Standard 12");
        plan.setDurationDays(30);
        plan.setVisitLimit(12);
        plan.setPrice(new BigDecimal("4500.00"));
        plan.setActive(true);
        subscriptionPlanRepository.save(plan);

        SubscriptionEntity subscription = new SubscriptionEntity();
        subscription.setId(DemoIds.SUBSCRIPTION);
        subscription.setClientId(DemoIds.CLIENT);
        subscription.setPlan(plan);
        subscription.setStartDate(LocalDate.now());
        subscription.setEndDate(LocalDate.now().plusDays(30));
        subscription.setRemainingVisits(11);
        subscription.setStatus("ACTIVE");
        subscription.setCreatedAt(now);
        subscription.setActivatedAt(now);
        subscriptionRepository.save(subscription);

        PaymentEntity payment = new PaymentEntity();
        payment.setId(DemoIds.PAYMENT);
        payment.setClientId(DemoIds.CLIENT);
        payment.setSubscriptionId(DemoIds.SUBSCRIPTION);
        payment.setAmount(new BigDecimal("4500.00"));
        payment.setCurrency("RUB");
        payment.setPaymentMethod("CARD");
        payment.setStatus("COMPLETED");
        payment.setCreatedAt(now);
        payment.setPaidAt(now);
        paymentRepository.save(payment);

        BookingEntity booking = new BookingEntity();
        booking.setId(DemoIds.BOOKING);
        booking.setClientId(DemoIds.CLIENT);
        booking.setScheduleId(DemoIds.SCHEDULE_1);
        booking.setSubscriptionId(DemoIds.SUBSCRIPTION);
        booking.setStatus("BOOKED");
        booking.setBookedAt(now);
        bookingRepository.save(booking);

        NotificationEntity notification = new NotificationEntity();
        notification.setId(DemoIds.NOTIFICATION);
        notification.setUserId(DemoIds.USER_CLIENT);
        notification.setType("BOOKING_CREATED");
        notification.setChannel("IN_APP");
        notification.setTitle("Запись подтверждена");
        notification.setMessage("Вы записаны на Morning Yoga");
        notification.setStatus("SENT");
        notification.setRelatedEntityType("BOOKING");
        notification.setRelatedEntityId(DemoIds.BOOKING);
        notification.setCreatedAt(now);
        notification.setSentAt(now);
        notificationRepository.save(notification);

        HallLoadStatEntity hallLoad = new HallLoadStatEntity();
        hallLoad.setId(UUID.fromString("88888888-8888-8888-8888-888888888801"));
        hallLoad.setHallId(DemoIds.HALL_A);
        hallLoad.setStatDate(LocalDate.now());
        hallLoad.setTotalSlots(20);
        hallLoad.setBookedSlots(8);
        hallLoad.setAttendedCount(6);
        hallLoadStatRepository.save(hallLoad);

        ClassPopularityStatEntity popularity = new ClassPopularityStatEntity();
        popularity.setId(UUID.fromString("88888888-8888-8888-8888-888888888802"));
        popularity.setClassId(DemoIds.CLASS_YOGA);
        popularity.setStatDate(LocalDate.now());
        popularity.setBookingsCount(8);
        popularity.setCancellationsCount(1);
        popularity.setAttendanceCount(6);
        classPopularityStatRepository.save(popularity);

        ClientActivityStatEntity activity = new ClientActivityStatEntity();
        activity.setId(UUID.fromString("88888888-8888-8888-8888-888888888803"));
        activity.setClientId(DemoIds.CLIENT);
        activity.setStatMonth(LocalDate.now().withDayOfMonth(1));
        activity.setBookingsCount(3);
        activity.setAttendanceCount(2);
        activity.setCancellationsCount(1);
        clientActivityStatRepository.save(activity);

        RevenueStatEntity revenue = new RevenueStatEntity();
        revenue.setId(UUID.fromString("88888888-8888-8888-8888-888888888804"));
        revenue.setStatDate(LocalDate.now());
        revenue.setPaymentsCount(1);
        revenue.setTotalAmount(new BigDecimal("4500.00"));
        revenueStatRepository.save(revenue);
    }

    private RoleEntity role(UUID id, String code, String name) {
        RoleEntity entity = new RoleEntity();
        entity.setId(id);
        entity.setCode(code);
        entity.setName(name);
        entity.setDescription(name);
        return entity;
    }

    private UserEntity user(UUID id, String email, RoleEntity role, Instant now) {
        UserEntity entity = new UserEntity();
        entity.setId(id);
        entity.setEmail(email);
        entity.setPasswordHash("demo");
        entity.setRole(role);
        entity.setStatus("ACTIVE");
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        return entity;
    }

    private ScheduleEntity schedule(
            UUID id, ClassEntity classEntity, HallEntity hall, Instant start, int slots) {
        ScheduleEntity entity = new ScheduleEntity();
        entity.setId(id);
        entity.setClassEntity(classEntity);
        entity.setHall(hall);
        entity.setTrainerId(DemoIds.TRAINER);
        entity.setStartTime(start);
        entity.setEndTime(start.plus(1, ChronoUnit.HOURS));
        entity.setStatus("PLANNED");
        entity.setAvailableSlots(slots);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        return entity;
    }
}