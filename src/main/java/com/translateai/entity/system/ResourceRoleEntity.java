package com.translateai.entity.system;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;

@DynamicUpdate
@Entity
@Table(name="sys_resource_role")
@Data
public class ResourceRoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rrId;

    @ManyToOne
    @JoinColumn(name = "rsId")
    private ResourceEntity resource;

    @ManyToOne
    @JoinColumn(name = "rlId")
    private RoleEntity role;

}
