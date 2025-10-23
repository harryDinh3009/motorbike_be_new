package com.translateai.entity.system;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

import java.sql.Date;
import java.util.HashSet;
import java.util.Set;

@DynamicUpdate
@Entity
@Table(name="sys_resource")
@Data
public class ResourceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rsId;

    private String url;

    private String httpMethod;

    private String rsNm;

    private String rsType;

    private Date regDate;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "sys_resource_role", joinColumns = { @JoinColumn(name = "rsId") },
            inverseJoinColumns = { @JoinColumn(name = "rlId") })
    @ToString.Exclude
    private Set<RoleEntity> roleSet = new HashSet<>();

}
