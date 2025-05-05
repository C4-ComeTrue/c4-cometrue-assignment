package org.c4marathon.assignment.adjustment.entity;

import java.util.ArrayList;
import java.util.List;

import org.c4marathon.assignment.util.common.AdjustmentStatus;
import org.c4marathon.assignment.util.entity.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Entity
@Table(name = "adjust")
public class Adjust extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "adjustTotalAmount", nullable = false)
    private Long adjustTotalAmount;

    @Column(name = "adjustmentStatus", nullable = false)
    private AdjustmentStatus adjustmentStatus;

    @OneToMany(mappedBy = "adjust", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<AdjustTarget> adjustTargetList = new ArrayList<>();

    @Builder
    public Adjust(Long id, Long adjustTotalAmount, AdjustmentStatus adjustmentStatus) {
        this.id = id;
        this.adjustTotalAmount = adjustTotalAmount;
        this.adjustmentStatus = adjustmentStatus;
    }
}
