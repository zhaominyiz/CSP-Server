package com.njust.csa.reg.repository.entities;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@EntityListeners(AuditingEntityListener.class) //启动自动生成时间
@Table(name = "applicant_info", schema = "online_reg_sys", catalog = "")
public class ApplicantInfoEntity {
    private long id;
    private Timestamp gmtCreate;
    private Timestamp gmtModified;
    private long belongsToStructureId;
    private int applicantNumber;
    private String value;

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
    @CreatedDate
    @Column(name = "gmt_create", nullable = false)
    public Timestamp getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Timestamp gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    @Basic
    @LastModifiedDate
    @Column(name = "gmt_modified", nullable = false)
    public Timestamp getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Timestamp gmtModified) {
        this.gmtModified = gmtModified;
    }

    @Basic
    @Column(name = "belongs_to_structure_id", nullable = false)
    public long getBelongsToStructureId() {
        return belongsToStructureId;
    }

    public void setBelongsToStructureId(long belongsToStructureId) {
        this.belongsToStructureId = belongsToStructureId;
    }

    @Basic
    @Column(name = "applicant_number", nullable = false)
    public int getApplicantNumber() {
        return applicantNumber;
    }

    public void setApplicantNumber(int applicantNumber) {
        this.applicantNumber = applicantNumber;
    }

    @Basic
    @Column(name = "value", nullable = false, length = 1000, columnDefinition = "")
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        if(value == null){
            this.value = "";
        }
        else{
            this.value = value;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicantInfoEntity that = (ApplicantInfoEntity) o;
        return id == that.id &&
                belongsToStructureId == that.belongsToStructureId &&
                applicantNumber == that.applicantNumber &&
                Objects.equals(gmtCreate, that.gmtCreate) &&
                Objects.equals(gmtModified, that.gmtModified) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, gmtCreate, gmtModified, belongsToStructureId, applicantNumber, value);
    }
}
