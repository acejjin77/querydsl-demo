package demo.querydsl.dto;

import lombok.Data;

@Data
public class MemberCond {

    private String memberName;
    private String teamName;
    private Integer ageGoe;
    private Integer ageLoe;
}
