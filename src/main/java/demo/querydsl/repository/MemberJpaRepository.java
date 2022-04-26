package demo.querydsl.repository;

import com.querydsl.core.BooleanBuilder;
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
}
