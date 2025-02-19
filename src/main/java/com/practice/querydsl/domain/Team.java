package com.practice.querydsl.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;


@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Team {

  @Id @GeneratedValue
  @Column(name = "team_id")
  private Long id;

  private String name;

  @OneToMany(mappedBy = "team")
  private List<Member> members = new ArrayList<>();

  public Team(String name) {
    this.name = name;
  }
}
