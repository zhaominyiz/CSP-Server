package com.njust.csa.reg.repository.docker;

import com.njust.csa.reg.repository.entities.ApplicantInfoViewEntity;
import com.njust.csa.reg.repository.entities.ScoreEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ScoreRepo extends CrudRepository<ScoreEntity, Long> {
    List<ScoreEntity> findAllByActidAndStuid(long actid,String stuid);
    List<ScoreEntity> findByStuid(String stuid);
}
