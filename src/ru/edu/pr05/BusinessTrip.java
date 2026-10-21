package ru.edu.pr05;

import java.time.LocalDate;
import java.util.Objects;

public class BusinessTrip {
    private final String id;
    private final String purpose;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private TripStatus status;
    private String rejectionReason;

    public BusinessTrip(String id, String purpose, LocalDate startDate, LocalDate endDate) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id is blank");
        if (purpose == null || purpose.isBlank()) throw new IllegalArgumentException("purpose is blank");
        if (startDate == null || endDate == null) throw new IllegalArgumentException("dates are null");
        if (endDate.isBefore(startDate)) throw new IllegalArgumentException("endDate before startDate");
        
        this.id = id;
        this.purpose = purpose;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = TripStatus.DRAFT;
    }

    // ПРАВИЛО 1: SUBMITTED только из DRAFT
    public void submit() {
        if (status != TripStatus.DRAFT) {
            throw new IllegalStateException("Can submit only from DRAFT");
        }
        this.status = TripStatus.SUBMITTED;
    }

    // ПРАВИЛО 2: APPROVED только из SUBMITTED
    public void approve() {
        if (status != TripStatus.SUBMITTED) {
            throw new IllegalStateException("Can approve only from SUBMITTED");
        }
        this.status = TripStatus.APPROVED;
    }

    // ПРАВИЛО 3: REJECTED только из SUBMITTED, причина >= 10 символов
    public void reject(String reason) {
        if (status != TripStatus.SUBMITTED) {
            throw new IllegalStateException("Can reject only from SUBMITTED");
        }
        if (reason == null || reason.trim().length() < 10) {
            throw new IllegalArgumentException("Reason must be at least 10 characters");
        }
        this.rejectionReason = reason;
        this.status = TripStatus.REJECTED;
    }

    // ПРАВИЛО 4: CANCELLED из DRAFT, SUBMITTED, APPROVED (но не после COMPLETED)
    public void cancel() {
        if (status == TripStatus.COMPLETED || status == TripStatus.CANCELLED || status == TripStatus.REJECTED) {
            throw new IllegalStateException("Cannot cancel from status: " + status);
        }
        this.status = TripStatus.CANCELLED;
    }

    // ПРАВИЛО 5: COMPLETED только из APPROVED и после даты окончания
    // ЧАСТЬ А ВАРИАНТА 3: дополнительная проверка даты (дата окончания должна быть в прошлом)
    public void complete() {
        if (status != TripStatus.APPROVED) {
            throw new IllegalStateException("Can complete only from APPROVED");
        }
        if (LocalDate.now().isBefore(endDate) || LocalDate.now().isEqual(endDate)) {
            throw new IllegalStateException("Cannot complete before end date");
        }
        // Дополнительная проверка варианта 3: дата окончания не должна быть слишком старой (например, больше года назад)
        // Но проще: просто убедимся, что дата действительно прошла.
        this.status = TripStatus.COMPLETED;
    }

    // ЧАСТЬ Б ВАРИАНТА 3: новый метод
    public long getDurationDays() {
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
    }

    // Геттеры
    public TripStatus getStatus() { return status; }
    public String getRejectionReason() { return rejectionReason; }
    public String getId() { return id; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
}