package demo.querydsl.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import demo.querydsl.dto.MemberCond;
import demo.querydsl.dto.MemberTeamDto;
import demo.querydsl.dto.QMemberDto;
import demo.querydsl.dto.QMemberTeamDto;
import demo.querydsl.entity.Member;
import demo.querydsl.entity.QMember;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static demo.querydsl.entity.QMember.*;
import static demo.querydsl.entity.QMember.member;
import static demo.querydsl.entity.QTeam.team;

@Repository
public class MemberJpaRepository {

    private final EntityManager entityManager;
    private final JPAQueryFactory queryFactory;

    public MemberJpaRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    public void save(Member member) {
        entityManager.persist(member);
    }

    public Optional<Member> findById(Long id) {
        Member findmember = entityManager.find(Member.class, id);
        return Optional.ofNullable(findmember);
    }

    public List<Member> findAll() {
        return entityManager.createQuery("" +
                        "select m from Member m")
                .getResultList();
    }

    public List<Member> findAll_Querydsl() {
        return queryFactory
                .selectFrom(member)
                .fetch();
    }

    public List<Member> findByName(String name) {
        return entityManager.createQuery("" +
                "select m from Member m where m.name = :name")
                .setParameter("name", name)
                .getResultList();
    }

    public List<Member> findByName_Querydsl(String name) {
        return queryFactory
                .selectFrom(member)
                .where(member.name.eq(name))
                .fetch();
    }

    /**
     * 불린빌더 사용
     * @param memberCond
     * @return
     */
    public List<MemberTeamDto> searchByBuilder(MemberCond memberCond) {

        BooleanBuilder booleanBuilder = new BooleanBuilder();
        if (StringUtils.hasText(memberCond.getMemberName())) {
            booleanBuilder.and(member.name.eq(memberCond.getMemberName()));
        }
        if (StringUtils.hasText(memberCond.getTeamName())) {
            booleanBuilder.and(team.name.eq(memberCond.getTeamName()));
        }
        if (memberCond.getAgeGoe() != null) {
            booleanBuilder.and(member.age.goe(memberCond.getAgeGoe()));
        }
        if (memberCond.getAgeLoe() != null) {
            booleanBuilder.and(member.age.loe(memberCond.getAgeLoe()));
        }


        return queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.name.as("memberName"),
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .join(member.team, team)
                .where(booleanBuilder)
                .fetch();
    }

    /**
     * where 절에 메소드 삽입. 메소드 재사용이 가능해서 이게 더 좋다
     * @param memberCond
     * @return
     */
    public List<MemberTeamDto> searchByWhereParam(MemberCond memberCond) {
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
