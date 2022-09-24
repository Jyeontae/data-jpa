package study.datajpa.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;


@SpringBootTest
@Transactional(readOnly = true)
@Rollback(value = false)
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    TeamRepository teamRepository;
    @PersistenceContext
    EntityManager em;

    @Test
    public void testMember() {
        Member member = new Member("memberA");
        Member saveMember = memberRepository.save(member);

        Member findMember = memberRepository.findById(saveMember.getId()).get();

        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member);
    }

    @Test
    public void findMemberDto() {
        Member member1 = new Member("AAA", 10);
        memberRepository.save(member1);

        Team team = new Team("teamA");
        member1.changeTeam(team);
        teamRepository.save(team);

        List<MemberDto> usernameList = memberRepository.findMemberDto();
        for (MemberDto memberDto : usernameList) {
            System.out.println("memberDto = " + memberDto);
        }

    }

    @Test
    public void returnType() {
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("BBB", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> aaa1 = memberRepository.findListByUsername("123141424");
        // 컬렉션 타입의 경우에는 없으면 null을 반환하는 것이 아니라 빈 컬렉션을 반환한다 size = 0, 단건 조회는 null을 반환한다.
        Member aaa2 = memberRepository.findMemberByUsername(member1.getUsername());
        Optional<Member> aaa3 = memberRepository.findOptionalByUsername(member1.getUsername());
        // Optional의 경우에는 Optional.empty반환함. -> data가 있을수 있고 없을 수도 있다면 Optional을 쓰는것이 좋다.


    }

    @Test
    @Transactional
    public void paging() {
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));
        Page<Member> page = memberRepository.findByAge(age, pageRequest);

        //엔티티 노출을 방지하기 위해 .mpa메소드로 바꿔주는 모습.
        Page<MemberDto> toMap = page.map(m -> new MemberDto(m.getId(), m.getUsername(), null));

        List<Member> content = page.getContent();
        long totalElements = page.getTotalElements();

        assertThat(content.size()).isEqualTo(3);
        assertThat(totalElements).isEqualTo(5);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();
    }


    @Test
    @Transactional
    public void bulkUpdate() {
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 20));
        memberRepository.save(new Member("member3", 30));
        memberRepository.save(new Member("member4", 40));
        memberRepository.save(new Member("member5", 10));

        int age = 20;
        int i = memberRepository.bulkAgePlus(age); //벌크연산은 db만 변경하고 영속성컨텍스트는 반영 안하므로 clear를 통해 영속성 컨텍스트를 없앤다.

        List<Member> result = memberRepository.findAll();
        for (Member member : result) {
            System.out.println("member = " + member);
        }
        assertThat(i).isEqualTo(3);
    }

    @Test
    @Transactional
    public void queryHint() {
        Member member1 = new Member("member1", 10);
        memberRepository.save(member1);
        em.flush();
        em.clear();

        Member findMember = memberRepository.findReadOnlyByUsername("member1");
        findMember.changeUsername("member2");

        em.flush();
    }

    @Test
    @Transactional
    public void lock() {
        Member member1 = new Member("member1", 10);
        memberRepository.save(member1);
        em.flush();
        em.clear();

        List<Member> member11 = memberRepository.findLockByUsername("member1");
    }

    @Test
    @Transactional
    public void custom() {
        Member member1 = new Member("member1", 10);
        memberRepository.save(member1);
        List<Member> resultMember = memberRepository.findMemberCustom();
        System.out.println("resultMember = " + resultMember.get(0));
    }
}