package com.streetfoodgo.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Order entity.
 */
@Entity
@Table(
        name = "orders",
        indexes = {
                @Index(name = "idx_order_status", columnList = "status"),
                @Index(name = "idx_order_customer", columnList = "customer_id"),
                @Index(name = "idx_order_waiter", columnList = "waiter_id"),
                @Index(name = "idx_order_queued_at", columnList = "queued_at"),
        }
)
public final class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_order_customer"))
    private Person customer;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "waiter_id", nullable = false, foreignKey = @ForeignKey(name = "fk_order_waiter"))
    private Person waiter;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private OrderStatus status;

    @NotNull
    @NotBlank
    @Size(max = 255)
    @Column(name = "subject", length = 255)
    private String subject;

    @NotNull
    @NotBlank
    @Size(max = 1000)
    @Column(name = "customer_content", length = 1000)
    private String customerContent;

    // It can be null.
    @Size(max = 1000)
    @Column(name = "waiter_content", length = 1000)
    private String waiterContent;

    @CreationTimestamp
    @Column(name = "queued_at", nullable = false, updatable = false)
    private Instant queuedAt;

    @Column(name = "in_progress_at")
    private Instant inProgressAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    public Order() {
    }

    public Order(Long id,
                 Person customer,
                 Person waiter,
                 OrderStatus status,
                 String subject,
                 String customerContent,
                 String waiterContent,
                 Instant queuedAt,
                 Instant inProgressAt,
                 Instant completedAt) {
        this.id = id;
        this.customer = customer;
        this.waiter = waiter;
        this.status = status;
        this.subject = subject;
        this.customerContent = customerContent;
        this.waiterContent = waiterContent;
        this.queuedAt = queuedAt;
        this.inProgressAt = inProgressAt;
        this.completedAt = completedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Person getCustomer() {
        return customer;
    }

    public void setCustomer(Person customer) {
        this.customer = customer;
    }

    public Person getWaiter() {
        return waiter;
    }

    public void setWaiter(Person waiter) {
        this.waiter = waiter;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getCustomerContent() {
        return customerContent;
    }

    public void setCustomerContent(String customerContent) {
        this.customerContent = customerContent;
    }

    public String getWaiterContent() {
        return waiterContent;
    }

    public void setWaiterContent(String waiterContent) {
        this.waiterContent = waiterContent;
    }

    public Instant getQueuedAt() {
        return queuedAt;
    }

    public void setQueuedAt(Instant queuedAt) {
        this.queuedAt = queuedAt;
    }

    public Instant getInProgressAt() {
        return inProgressAt;
    }

    public void setInProgressAt(Instant inProgressAt) {
        this.inProgressAt = inProgressAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Ticket{");
        sb.append("id=").append(id);
        sb.append(", customer=").append(customer);
        sb.append(", waiter=").append(waiter);
        sb.append(", status=").append(status);
        sb.append(", subject='").append(subject).append('\'');
        sb.append(", customerContent='").append(customerContent).append('\'');
        sb.append(", waiterContent='").append(waiterContent).append('\'');
        sb.append(", queuedAt=").append(queuedAt);
        sb.append(", inProgressAt=").append(inProgressAt);
        sb.append(", completedAt=").append(completedAt);
        sb.append('}');
        return sb.toString();
    }
}
