package com.translateai.entity.common.codeMng;

import com.translateai.constant.classconstant.EntityProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

@DynamicUpdate
@Entity
@Table(name = "code_mng")
@Getter
@Setter
public class CodeMngEntity implements Serializable {

    @Id
    @Column(name = "cd_id")
    private String cdId;

    @Column(name = "up_cd_id")
    private String upCdId;

    @Column(name = "cd_category")
    private String cdCategory;

    @Column(name = "cd_nm")
    private String cdNm;

    @Column(name = "ord_no")
    private Long ordNo;

    @Column(name = "des", length = EntityProperties.LENGTH_DESCRIPTION)
    private String des;

    @Column(name = "sub_des", length = EntityProperties.LENGTH_DESCRIPTION)
    private String subDes;

}
