package com.fitcore.api.global.common.entity;

import lombok.Getter;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
public abstract class BaseDeleteEntity extends BaseTimeEntity {

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_ip")
    private String deletedIp;

    /**
     * 소프트 삭제 처리 시 부가 정보 기록
     */
    public void markAsDeleted(String ip) {
        this.deletedAt = LocalDateTime.now();
        this.deletedIp = ip;
    }
}
