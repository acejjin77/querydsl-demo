package demo.querydsl.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import demo.querydsl.dto.MemberCond;
import demo.querydsl.dto.MemberTeamDto;
import demo.querydsl.entity.Member;
import demo.querydsl.entity.Team;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;


@Transactional
@SpringBootTest
class MemberJpaRepositoryTest {

    @Autowired
    EntityManager entityManager;
    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Test
    public void basicTest() {
        Member member1 = new Member("member1", 10);
        memberJpaRepository.save(member1);

        Member findMember = memberJpaRepository.findById(member1.getId()).get();

        assertThat(findMember).isEqualTo(member1);

        List<Member> result = memberJpaRepository.findAll_Querydsl();
        assertThat(result).containsExactly(member1);

        List<Member> result2 = memberJpaRepository.findByName_Querydsl("member1");
        assertThat(result2).containsExactly(member1);
    }

    @Test
    public void searchTest() {

        Team teamA = new Team("teamTestA");
        Team teamB = new Team("teamTestB");
        entityManager.persist(teamA);
        entityManager.persist(teamB);

        Member member1 = new Member("memberTest1", 10, teamA);
        Member member2 = new Member("memberTest2", 20, teamA);
        Member member3 = new Member("memberTest3", 30, teamB);
        Member member4 = new Member("memberTest4", 40, teamB);
        entityManager.persist(member1);
        entityManager.persist(member2);
        entityManager.persist(member3);
        entityManager.persist(member4);

        MemberCond memberCond = new MemberCond();
        memberCond.setAgeGoe(35);
        memberCond.setAgeLoe(40);
        memberCond.setTeamName("teamTestB");

        List<MemberTeamDto> result = memberJpaRepository.searchByBuilder(memberCond);

        assertThat(result).extracting("memberName").containsExactly("memberTest4");
    }

}