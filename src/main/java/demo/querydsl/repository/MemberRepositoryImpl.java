package demo.querydsl.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import demo.querydsl.dto.MemberCond;
import demo.querydsl.dto.MemberTeamDto;
import demo.querydsl.dto.QMemberTeamDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import java.util.List;

import static demo.querydsl.entity.QMember.member;
import static demo.querydsl.entity.QTeam.team;

public class MemberRepositoryImpl implements MemberRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    public MemberRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public List<MemberTeamDto> search(MemberCond memberCond) {
        return queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.name.as("memberName"),
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .where(memberNameEq(memberCond.getMemberName()),
                        teamNameEq(memberCond.getTeamName()),
                        ageGoe(memberCond.getAgeGoe()),
                        ageLoe(memberCond.getAgeLoe())
                )
                .fetch();

    }

    @Override
    public Page<MemberTeamDto> searchPageSimple(MemberCond memberCond, Pageable pageable) {
        QueryResults<MemberTeamDto> results = queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.name.as("memberName"),
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .where(memberNameEq(memberCond.getMemberName()),
                        teamNameEq(memberCond.getTeamName()),
                        ageGoe(memberCond.getAgeGoe()),
                        ageLoe(memberCond.getAgeLoe())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<MemberTeamDto> content = results.getResults();
        long total = results.getTotal();

        return new PageImpl <>(content, pageable, total);
    }

    @Override
    public Page<MemberTeamDto> searchPageComplex(MemberCond memberCond, Pageable pageable) {
        return null;
    }

    private BooleanExpression memberNameEq(String memberName) {
        return StringUtils.hasText(memberName) ? member.name.eq(memberName) :null ;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return StringUtils.hasText(teamName) ? team.name.eq(teamName) : null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }

}
