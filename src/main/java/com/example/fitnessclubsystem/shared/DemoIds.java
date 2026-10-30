package com.example.fitnessclubsystem.shared;

import java.util.UUID;

public final class DemoIds {

    public static final UUID ROLE_CLIENT = UUID.fromString("10000000-0000-0000-0000-000000000001");
    public static final UUID ROLE_TRAINER = UUID.fromString("10000000-0000-0000-0000-000000000002");
    public static final UUID ROLE_ADMIN = UUID.fromString("10000000-0000-0000-0000-000000000003");

    public static final UUID USER_CLIENT = UUID.fromString("11111111-1111-1111-1111-111111111101");
    public static final UUID USER_TRAINER = UUID.fromString("11111111-1111-1111-1111-111111111102");
    public static final UUID USER_ADMIN = UUID.fromString("11111111-1111-1111-1111-111111111103");

    public static final UUID CLIENT = UUID.fromString("22222222-2222-2222-2222-222222222201");
    public static final UUID TRAINER = UUID.fromString("22222222-2222-2222-2222-222222222202");

    public static final UUID CATEGORY_YOGA = UUID.fromString("33333333-3333-3333-3333-333333333301");
    public static final UUID HALL_A = UUID.fromString("33333333-3333-3333-3333-333333333302");
    public static final UUID CLASS_YOGA = UUID.fromString("33333333-3333-3333-3333-333333333303");
    public static final UUID SCHEDULE_1 = UUID.fromString("33333333-3333-3333-3333-333333333304");
    public static final UUID SCHEDULE_2 = UUID.fromString("33333333-3333-3333-3333-333333333305");

    public static final UUID PLAN_STANDARD = UUID.fromString("44444444-4444-4444-4444-444444444401");
    public static final UUID SUBSCRIPTION = UUID.fromString("44444444-4444-4444-4444-444444444402");

    public static final UUID PAYMENT = UUID.fromString("55555555-5555-5555-5555-555555555501");

    public static final UUID BOOKING = UUID.fromString("66666666-6666-6666-6666-666666666601");

    public static final UUID NOTIFICATION = UUID.fromString("77777777-7777-7777-7777-777777777701");

    private DemoIds() {
    }
}