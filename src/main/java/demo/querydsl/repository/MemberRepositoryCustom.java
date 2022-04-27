package demo.querydsl.repository;


import demo.querydsl.dto.MemberCond;
import demo.querydsl.dto.MemberTeamDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberRepositoryCustom {
    List<MemberTeamDto> search(MemberCond memberCond);
    Page<MemberTeamDto> searchPageSimple(MemberCond memberCond, Pageable pageable);
    Page<MemberTeamDto> searchPageComplex(MemberCond memberCond, Pageable pageable);
}
