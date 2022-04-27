package demo.querydsl.controller;

import demo.querydsl.dto.MemberCond;
import demo.querydsl.dto.MemberTeamDto;
import demo.querydsl.repository.MemberJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberJpaRepository memberJpaRepository;

    @GetMapping("/v1/members")
    public List<MemberTeamDto> searchMemberV1(MemberCond memberCond) {
        return memberJpaRepository.searchByWhereParam(memberCond);
    }
}
