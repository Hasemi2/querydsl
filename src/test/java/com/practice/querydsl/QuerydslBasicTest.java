package com.practice.querydsl;

import static com.practice.querydsl.domain.QMember.member;
import static com.practice.querydsl.domain.QTeam.team;
import static org.assertj.core.api.Assertions.assertThat;

import com.practice.querydsl.domain.Member;
import com.practice.querydsl.domain.Team;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

  @Autowired
  EntityManager em;

  JPAQueryFactory queryFactory;

  @BeforeEach
  public void before() {
    queryFactory = new JPAQueryFactory(em);

    Team teamA = new Team("teamA");
    Team teamB = new Team("teamB");

    em.persist(teamA);
    em.persist(teamB);

    Member member1 = new Member("member1", 10, teamA);
    Member member2 = new Member("member2", 20, teamA);
    Member member3 = new Member("member3", 30, teamB);
    Member member4 = new Member("member4", 40, teamB);

    em.persist(member1);
    em.persist(member2);
    em.persist(member3);
    em.persist(member4);
  }

  @Test
  public void startJPQL() {
    Member member = em.createQuery("select m from Member m where m.username = :username",
            Member.class)
        .setParameter("username", "member1")
        .getSingleResult();

    assertThat(member.getUsername()).isEqualTo("member1");

  }

  @Test
  public void startQueryDSL() {
    JPAQueryFactory queryFactory = new JPAQueryFactory(em);

    Member member1 = queryFactory
        .select(member)
        .from(member)
        .where(member.username.eq("member1"))
        .fetchOne();

    assertThat(member1.getUsername()).isEqualTo("member1");
  }

  @Test
  public void search() {
    Member member1 = queryFactory
        .selectFrom(member)
        .where(member.username.eq("member1")
            .and(member.age.eq(10))
        ).fetchOne();

    assertThat(member1.getUsername()).isEqualTo("member1");
  }

  @Test
  public void searchAndParam() {
    Member member1 = queryFactory
        .selectFrom(member)
        .where(member.username.eq("member1")
            , (member.age.eq(10))
        ).fetchOne();

    assertThat(member1.getUsername()).isEqualTo("member1");
  }

  @Test
  public void resultFetch() {
    List<Member> member1 = queryFactory
        .selectFrom(member)
        .fetch();

    Member fetchFirst = queryFactory
        .selectFrom(member)
        .fetchFirst();

    QueryResults<Member> memberQueryResults = queryFactory
        .selectFrom(member)
        .fetchResults();

    memberQueryResults.getTotal();
    List<Member> content = memberQueryResults.getResults();

  }


  @Test
  public void sort() {
    em.persist(new Member(null, 100));
    em.persist(new Member("member5", 100));
    em.persist(new Member("member6", 100));

    List<Member> members = queryFactory
        .selectFrom(member)
        .where(member.age.eq(100))
        .orderBy(member.age.desc(), member.username.asc().nullsLast())
        .fetch();

    Member member5 = members.get(0);
    Member member6 = members.get(1);
    Member memberNull = members.get(2);

    assertThat(member5.getUsername()).isEqualTo("member5");
    assertThat(member6.getUsername()).isEqualTo("member6");
    assertThat(memberNull.getUsername()).isNull();

  }

  @Test
  public void paging1() {
    List<Member> members = queryFactory
        .selectFrom(member)
        .orderBy(member.username.desc())
        .offset(1)
        .limit(2)
        .fetch();

    assertThat(members.size()).isEqualTo(2);
  }

  @Test
  public void aggregation() {
    List<Tuple> result = queryFactory
        .select(member.count(),
            member.age.sum(),
            member.age.avg(),
            member.age.max(),
            member.age.min()
        )
        .from(member)
        .fetch();

    Tuple tuple = result.get(0);
    assertThat(tuple.get(member.count())).isEqualTo(4);
    assertThat(tuple.get(member.age.sum())).isEqualTo(100);

  }

  @Test
  public void groupBy() {
    List<Tuple> result = queryFactory
        .select(team.name, member.age.avg())
        .from(member)
        .join(member.team, team)
        .groupBy(team.name)
        .fetch();

    Tuple teamA = result.get(0);
    Tuple teamB = result.get(1);

    assertThat(teamA.get(team.name)).isEqualTo("teamA");
    assertThat(teamA.get(member.age.avg())).isEqualTo(15);
    assertThat(teamB.get(team.name)).isEqualTo("teamB");
    assertThat(teamB.get(member.age.avg())).isEqualTo(35);

  }
}
