package com.translateai.business.common.codeMng.service;


import com.translateai.dto.common.codeMng.CodeMngListDTO;
import com.translateai.dto.common.codeMng.CodeMngListReqDTO;
import com.translateai.dto.common.codeMng.CodeMngReqDTO;

import java.util.List;

public interface CodeMngService {

    List<CodeMngListDTO> getListCodeMngByUpCdId(CodeMngReqDTO codeMngResDTO);

    List<CodeMngListDTO> getListCodeMngByListUpCd(CodeMngListReqDTO codeMngResDTO);

}
