package com.njust.csa.reg.repository.entities;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "applicant_info_view", schema = "test_reg_sys", catalog = "")
public class ApplicantInfoViewEntity {
    private int applicantNumber;
    private String value;
    private long id;
    private long structureId;
    private long tableId;
    private String type;

    @Basic
    @Column(name = "applicant_number", nullable = false)
    public int getApplicantNumber() {
        return applicantNumber;
    }

    public void setApplicantNumber(int applicantNumber) {
        this.applicantNumber = applicantNumber;
    }

    @Basic
    @Column(name = "value", nullable = false, length = 1000)
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Id
    @Column(name = "id", nullable = false)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "structure_id", nullable = false)
    public long getStructureId() {
        return structureId;
    }

    public void setStructureId(long structureId) {
        this.structureId = structureId;
    }

    @Basic
    @Column(name = "table_id", nullable = false)
    public long getTableId() {
        return tableId;
    }

    public void setTableId(long tableId) {
        this.tableId = tableId;
    }

    @Basic
    @Column(name = "type", nullable = false, length = 30)
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicantInfoViewEntity that = (ApplicantInfoViewEntity) o;
        return applicantNumber == that.applicantNumber &&
                id == that.id &&
                structureId == that.structureId &&
                tableId == that.tableId &&
                Objects.equals(value, that.value) &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(applicantNumber, value, id, structureId, tableId, type);
    }
}
