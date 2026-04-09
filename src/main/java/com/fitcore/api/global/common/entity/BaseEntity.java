package com.fitcore.api.global.common.entity;

import lombok.Getter;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    // --- 등록 정보 ---
    @CreatedDate
    @Column(name = "insert_dtm", updatable = false, nullable = false)
    private LocalDateTime insertDtm;

    @Column(name = "insert_ip", updatable = false)
    private String insertIp;

    // --- 수정 정보 ---
    @LastModifiedDate
    @Column(name = "update_dtm", nullable = false)
    private LocalDateTime updateDtm;

    @Column(name = "update_ip")
    private String updateIp;

    // --- 삭제 정보 ---
    // [중요] delete_bool 필드는 @SoftDelete가 관리하므로 직접 선언하지 않습니다.

    @Column(name = "delete_dtm")
    private LocalDateTime deleteDtm;

    @Column(name = "delete_ip")
    private String deleteIp;

    // IP 설정을 위한 Setter
    public void setInsertIp(String ip) {
        this.insertIp = ip;
    }

    public void setUpdateIp(String ip) {
        this.updateIp = ip;
    }

    /**
     * 소프트 삭제 처리 시 부가 정보 기록 메서드
     */
    public void markAsDeleted(String ip) {
        this.deleteDtm = LocalDateTime.now();
        this.deleteIp = ip;
        // 실제 delete_bool값은 JPA 삭제 메서드(delete()) 호출 시
        // 하이버네이트 @SoftDelete에 의해 자동으로 true 처리됩니다.
    }
}
