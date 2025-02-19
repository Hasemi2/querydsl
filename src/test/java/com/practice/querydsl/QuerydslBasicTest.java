package com.practice.querydsl;

import static com.practice.querydsl.domain.QMember.member;
import static org.assertj.core.api.Assertions.assertThat;

import com.practice.querydsl.domain.Member;
import com.practice.querydsl.domain.Team;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

  @Autowired
  EntityManager em;

  @BeforeEach
  public void before() {
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
   Member member = em.createQuery("select m from Member m where m.username = :username", Member.class)
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
}
