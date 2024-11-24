package com.mcode.llp.codeGen.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.Data;


@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Entity
@IdClass(PropertyId.class)
@Data
public class Property extends CommanProperty{

    private long id;
    @Id
    private String name;

    private String type;
    @Id
    private String entity;

    private boolean required;

}
