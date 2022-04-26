package demo.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MemberDto {

    private String name;
    private int age;

    @QueryProjection // Querydsl 에 의존되는게 단점
    public MemberDto(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
