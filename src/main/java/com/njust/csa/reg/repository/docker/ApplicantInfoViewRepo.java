package com.njust.csa.reg.repository.docker;

import com.njust.csa.reg.repository.entities.ApplicantInfoViewEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ApplicantInfoViewRepo extends CrudRepository<ApplicantInfoViewEntity, Long> {
    List<ApplicantInfoViewEntity> findAllByTableId(long id);

    List<ApplicantInfoViewEntity> findAllByTableIdAndApplicantNumber(long tableId, int applicantNumber);
}
