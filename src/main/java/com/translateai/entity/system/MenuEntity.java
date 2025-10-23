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

import java.util.HashSet;
import java.util.Set;

@DynamicUpdate
@Entity
@Table(name = "menu")
@Data
public class MenuEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String siteType;

    private String name;

    private String route;

    private Long parentId;

    private String subUrl;

    private Integer displayOrder;

    private String deleteFlag;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "menu_role", joinColumns = {@JoinColumn(name = "menuId")}, inverseJoinColumns = {@JoinColumn (name = "roleId")})
    @ToString.Exclude
    private Set<RoleEntity> roleSet = new HashSet<>();

}
