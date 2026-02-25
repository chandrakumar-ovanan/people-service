package com.plavonra.common.audit;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Data
public abstract class Auditable {

  @CreatedBy protected String createdBy;

  @CreatedDate protected Instant creationDate;

  @LastModifiedBy protected String lastModifiedBy;

  @LastModifiedDate protected Instant lastModifiedDate;
}
