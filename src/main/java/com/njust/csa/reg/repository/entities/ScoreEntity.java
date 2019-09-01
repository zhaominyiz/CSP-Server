package com.njust.csa.reg.repository.entities;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@EntityListeners(AuditingEntityListener.class) //启动自动生成时间
public class ScoreEntity {
    private long id;
    private String score;
    private String stuid;
    private long actid;

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }



    @Basic
    @Column(name = "actid", nullable = false)
    public long getActid() {
        return actid;
    }

    public void setActid(long actid) {
        this.actid = actid;
    }

    @Basic
    @Column(name = "score", nullable = false, length = 1000, columnDefinition = "")
    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        if(score == null){
            this.score = "";
        }
        else{
            this.score = score;
        }
    }

    @Basic
    @Column(name = "stuid", nullable = false, length = 1000, columnDefinition = "")
    public String getStuid() {
        return stuid;
    }

    public void setStuid(String stuid) {
        if(stuid == null){
            this.stuid = "";
        }
        else{
            this.stuid = stuid;
        }
    }
}
