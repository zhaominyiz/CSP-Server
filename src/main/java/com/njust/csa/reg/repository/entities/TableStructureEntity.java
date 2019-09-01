package com.njust.csa.reg.repository.entities;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@EntityListeners(AuditingEntityListener.class) //启动自动生成时间
@Table(name = "table_structure", schema = "online_reg_sys", catalog = "")
public class TableStructureEntity {
    private long id;
    private Timestamp gmtCreate;
    private Timestamp gmtModified;
    private long tableId;
    private byte index;
    private String title;
    private String extension;
    private String type;
    private byte isRequired;
    private String defaultValue;
    private String tips;
    private String description;
    private String cases;
    private String range;
    private byte isUnique;
    private Long belongsTo;

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
    @Column(name = "table_id", nullable = false)
    public long getTableId() {
        return tableId;
    }

    public void setTableId(long tableId) {
        this.tableId = tableId;
    }

    @Basic
    @Column(name = "index_number", nullable = false)
    public byte getIndexNumber() {
        return index;
    }

    public void setIndexNumber(byte index) {
        this.index = index;
    }

    @Basic
    @Column(name = "title", nullable = false, length = 50)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Basic
    @Column(name = "extension", nullable = false, length = 30)
    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    @Basic
    @Column(name = "type", nullable = false, length = 30)
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Basic
    @Column(name = "is_required", nullable = false)
    public byte getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(byte isRequired) {
        this.isRequired = isRequired;
    }

    @Basic
    @Column(name = "default_value", nullable = false, length = 30)
    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Basic
    @Column(name = "tips", nullable = false, length = 30)
    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    @Basic
    @Column(name = "description", nullable = false, length = 70)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Basic
    @Column(name = "cases", nullable = false, length = 50)
    public String getCases() {
        return cases == null ? "" : cases;
    }

    public void setCases(String cases) {
        this.cases = cases;
    }

    @Basic
    @Column(name = "ranges", nullable = false, length = 50)
    public String getRanges() {
        return range == null ? "" : range;
    }

    public void setRanges(String range) {
        this.range = range;
    }

    @Basic
    @Column(name = "is_unique", nullable = false)
    public byte getIsUnique() {
        return isUnique;
    }

    public void setIsUnique(byte isUnique) {
        this.isUnique = isUnique;
    }

    @Basic
    @Column(name = "belongs_to", nullable = true)
    public Long getBelongsTo() {
        return belongsTo;
    }

    public void setBelongsTo(Long belongsTo) {
        this.belongsTo = belongsTo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableStructureEntity that = (TableStructureEntity) o;
        return id == that.id &&
                tableId == that.tableId &&
                index == that.index &&
                isRequired == that.isRequired &&
                isUnique == that.isUnique &&
                Objects.equals(gmtCreate, that.gmtCreate) &&
                Objects.equals(gmtModified, that.gmtModified) &&
                Objects.equals(title, that.title) &&
                Objects.equals(extension, that.extension) &&
                Objects.equals(type, that.type) &&
                Objects.equals(defaultValue, that.defaultValue) &&
                Objects.equals(tips, that.tips) &&
                Objects.equals(description, that.description) &&
                Objects.equals(cases, that.cases) &&
                Objects.equals(range, that.range) &&
                Objects.equals(belongsTo, that.belongsTo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, gmtCreate, gmtModified, tableId, index, title, extension, type, isRequired, defaultValue, tips, description, cases, range, isUnique, belongsTo);
    }
}
