package demo.querydsl.repository;

import demo.querydsl.dto.MemberCond;
import demo.querydsl.dto.MemberTeamDto;
import demo.querydsl.entity.Member;
import demo.querydsl.entity.Team;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    EntityManager entityManager;

    @Autowired
    MemberRepository memberRepository;

    @Test
    public void basicTest() {
        Member member1 = new Member("member1", 10);
        memberRepository.save(member1);

        Member findMember = memberRepository.findById(member1.getId()).get();

        assertThat(findMember).isEqualTo(member1);

        List<Member> result = memberRepository.findAll();
        assertThat(result).containsExactly(member1);

        List<Member> result2 = memberRepository.findByName("member1");
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

        List<MemberTeamDto> result = memberRepository.search(memberCond);

        assertThat(result).extracting("memberName").containsExactly("memberTest4");
    }

    @Test
    public void searchPageSimple() {

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
        PageRequest pageRequest = PageRequest.of(0, 3);

        Page<MemberTeamDto> result = memberRepository.searchPageSimple(memberCond, pageRequest);

        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result.getContent()).extracting("memberName").containsExactly("memberTest1", "memberTest2", "memberTest3");

    }
}