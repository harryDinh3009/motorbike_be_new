package com.translateai.dto.system;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ResultDTO<T> {

    private T data;

    private int code = HttpStatus.OK.value();

    private String message = HttpStatus.OK.getReasonPhrase();

    public ResultDTO(T data) {
        this.data = data;
        code = HttpStatus.OK.value();
        message = HttpStatus.OK.getReasonPhrase();
    }

}
