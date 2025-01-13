package com.mcode.llp.codeGen.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
public class CommanProperty {
  // private long id;

    private Integer minimum;

    private Integer maximum;



}
