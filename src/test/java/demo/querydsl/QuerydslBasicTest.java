package demo.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import demo.querydsl.entity.Member;
import demo.querydsl.entity.QMember;
import demo.querydsl.entity.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.util.Calendar;
import java.util.List;

import static demo.querydsl.entity.QMember.member;
import static demo.querydsl.entity.QTeam.team;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager entityManager;

    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before() {
        queryFactory = new JPAQueryFactory(entityManager);
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
    }

    @Test
    public void startJPQL() {
        Member findMember = entityManager.createQuery("" +
                        "select m from Member m " +
                        "where m.name = :name", Member.class)
                .setParameter("name", "memberTest1")
                .getSingleResult();

        assertThat(findMember.getName()).isEqualTo("memberTest1");
    }

    @Test
    public void startQuerydsl() {
//        QMember member = QMember.member;
//        QMember member = new QMember("member");

        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.name.eq("memberTest1"))
                .fetchOne();

        assertThat(findMember.getName()).isEqualTo("memberTest1");
    }

    @Test
    public void search() {
        List<Member> memberList = queryFactory
                .selectFrom(member)
                .where(member.age.between(10, 40), member.name.like("%Test%"))
                .fetch();

        System.out.println("memberList = " + memberList);
        assertThat(memberList.size()).isEqualTo(4);
    }

    /**
     * and (,)
     */
    @Test
    public void searchAndParam() {
        List<Member> memberList = queryFactory
                .selectFrom(member)
                .where(member.name.eq("memberTest1"), member.age.eq(10))
                .fetch();

        System.out.println("memberList = " + memberList);
        assertThat(memberList.get(0).getName()).isEqualTo("memberTest1");
    }

    /**
     * ???????????? (fetch)
     */
    @Test
    public void resultFetch() {
        QueryResults<Member> memberList = queryFactory
                .selectFrom(member)
                .fetchResults(); // total ?????? + ????????? ->????????? .getCount() / ???????????? .getResult()
//                .fetchCount(); // total
//                .fetchFirst(); // ????????? ?????????

        System.out.println("memberList = " + memberList);
    }

    /**
     * ?????? (orderby)
     */

    @Test
    public void sort() {
        entityManager.persist(new Member(null, 40));
        entityManager.persist(new Member("memberTest5", 30));
        entityManager.persist(new Member("memberTest6", 30));
        List<Member> memberList = queryFactory
                .selectFrom(member)
                .orderBy(member.age.desc(), member.name.asc().nullsFirst())
                .fetch();

        System.out.println("memberList = " + memberList);

        assertThat(memberList.get(0).getName()).isNull();
    }

    /**
     * ?????????1 offset, limit
     */
    @Test
    public void paging1() {
        List<Member> memberList = queryFactory
                .selectFrom(member)
                .orderBy(member.age.desc())
                .offset(1)
                .limit(2)
                .fetch();

        assertThat(memberList.size()).isEqualTo(2);
    }

    /**
     * ?????????2 offset, limit
     */
    @Test
    public void paging2() {
        QueryResults<Member> queryResults = queryFactory
                .selectFrom(member)
                .where(member.name.contains("Test"))
                .orderBy(member.age.desc())
                .offset(1)
                .limit(2)
                .fetchResults();

        assertThat(queryResults.getTotal()).isEqualTo(4);
        assertThat(queryResults.getLimit()).isEqualTo(2);
        assertThat(queryResults.getOffset()).isEqualTo(1);
        assertThat(queryResults.getResults().size()).isEqualTo(2);
    }

    /**
     * ??????, ????????? tuple ?????? DTO??? ?????????
     */
    @Test
    public void aggregation() {
        List<Tuple> result = queryFactory
                .select(member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min())
                .from(member)
                .where(member.name.contains("Test"))
                .fetch();

        Tuple tuple = result.get(0);
        System.out.println("tuple = " + tuple);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }


    /**
     * groupby + having
     * ?????? ????????? ??? ?????? ?????? ??????
     */
    @Test
    public void group() throws Exception {
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .having(team.name.like("%Test%"))
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamTestA");
        assertThat(teamB.get(team.name)).isEqualTo("teamTestB");

        assertThat(teamA.get(member.age.avg())).isEqualTo(15);
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);

    }

    /**
     * join
     * ??? A??? ????????? ??????
    */
    @Test
    public void join() {
        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamTestA"))
                .fetch();

        assertThat(result)
                .extracting("name")
                .containsExactly("memberTest1", "memberTest2");

    }

    /**
     * Join ?????? ??????, ????????? teamA
     * JPQL : select m, t from member m left join m.team t on t.name = 'teamA'
     */

    @Test
    public void join_on_filtering() {
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .join(member.team, team)
                .where(team.name.eq("teamTestA"))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    /**
     * No relation ???????????? (?????????)
     * Join ?????? ??????, ??????????????? ???????????? ?????? ??????
     */

    @Test
    public void join_on_no_relation() {
        entityManager.persist(new Member("teamTestA", 30));
        entityManager.persist(new Member("teamTestB", 30));
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .join(team)
                .on(member.name.eq(team.name))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @PersistenceUnit
    EntityManagerFactory entityManagerFactory;

    /**
     * ????????????
     */

    @Test
    public void noFetchJoin() {
        entityManager.flush();
        entityManager.clear();

        Member findMember = queryFactory.
                selectFrom(member)
                .where(member.name.eq("memberTest1"))
                .fetchOne();

        System.out.println("findMember = " + findMember);

        boolean loaded = entityManagerFactory.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("?????? ?????? ?????????").isFalse();
    }

    @Test
    public void fetchJoin() {
        entityManager.flush();
        entityManager.clear();

        Member findMember = queryFactory.
                selectFrom(member)
                .join(member.team, team)
                .fetchJoin()
                .where(member.name.eq("memberTest1"))
                .fetchOne();

        System.out.println("findMember = " + findMember);

        boolean loaded = entityManagerFactory.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("?????? ?????? ?????????").isTrue();
    }

    /**
     * ????????????
     * ????????? ?????? ?????? ??????
     */
    @Test
    public void subQuery() {
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory.
                selectFrom(member)
                .where(member.age.goe(
                                JPAExpressions
                                        .select(memberSub.age.avg())
                                        .from(memberSub)
                                        .where(memberSub.age.goe(10))
                        ), member.name.contains("Test")
                )
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(30, 40);
    }

    /**
     * case?????? ??????
     * ????????? ?????? ????????? ???????????????????????? ??????????????????
     * DB??? ????????????
     */
    
    @Test
    public void basicCase() {
        List<String> result = queryFactory
                .select(member.age
                        .when(10).then("??????")
                        .when(20).then("?????????")
                        .otherwise("??????"))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("basic = " + s);
        }
    }

    @Test
    public void complexCase() {
        List<String> result = queryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(0, 20)).then("0~20")
                        .when(member.age.between(21, 30)).then("21~30")
                        .otherwise("??????")
                )
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("complex = " + s);
        }
    }
}
