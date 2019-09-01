package com.njust.csa.reg.repository.docker;

import com.njust.csa.reg.repository.entities.ApplicantInfoEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ApplicantInfoRepo extends CrudRepository<ApplicantInfoEntity, Long> {
    int countByBelongsToStructureId(long structureId);

    boolean existsByBelongsToStructureIdAndValue(long structureId, String value);

    Integer deleteAllByIdIn(List<Long> in);
}
