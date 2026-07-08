package com.drawpin.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * JPA entity representing a Category in the DrawPin platform.
 *
 * <p>Categories form a hierarchical reference data structure optimized for reads.
 * They act as the foundational taxonomy for Pins, Search, and the Marketplace.
 *
 * <p><b>Table:</b> {@code categories}<br>
 * <b>Managed by Flyway:</b> {@code V5__create_categories.sql}
 *
 * <p><b>Important Architectural Notes:</b>
 * <ul>
 *   <li>Uses {@code @Getter}/{@code @Setter} instead of {@code @Data} to prevent infinite recursive {@code toString()} and {@code hashCode()}.</li>
 *   <li>Contains self-referencing {@code @ManyToOne} and {@code @OneToMany} relationships.</li>
 *   <li>All relationships use {@code FetchType.LAZY} to prevent N+1 queries.</li>
 *   <li>Does NOT use {@code @JsonIgnore}. JSON serialization cycles are prevented at the boundary via DTOs/MapStruct.</li>
 * </ul>
 */
@Entity
@Table(name = "categories")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "slug", nullable = false, unique = true, length = 100)
    private String slug;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "icon", length = 255)
    private String icon;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    /**
     * Self-referencing parent category.
     * Mapped as LAZY to ensure queries only fetch the parent when explicitly accessed.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    private Category parent;

    /**
     * Self-referencing child categories.
     * Mapped as LAZY. Used to construct hierarchical category trees.
     */
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<Category> children = new ArrayList<>();

    // --- Auditing ---

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // --- Identity-based equals & hashCode ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category category)) return false;
        return id != null && Objects.equals(id, category.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
