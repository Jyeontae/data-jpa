package study.datajpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
    List<MemberDto> findMemberDto();

    //반환타입
    List<Member> findListByUsername(String username); //컬렉션
    Member findMemberByUsername(String username); //단건
    Optional<Member> findOptionalByUsername(String username); //단건 Optional

    //페이징
    @Query(value = "select m from Member m left join m.team t",
            countQuery = "select count(m) from Member m")  //복잡할때는 이렇게 쓰면 count 쿼리는 조인을 안하고 실행함.
    Page<Member> findByAge(int age, Pageable pageable);

    //벌크연산
    @Modifying(clearAutomatically = true) //JPQL의 executeUpdate()와 같은 기능, clearAutomatically = true 자동 clear()기능
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age); //주의 : 바로 db에 update하고 영속성 컨텍스트에는 반영하지 않는다.

    //엔티티 그래프
    @Override
    @EntityGraph(attributePaths = {"team"}) // -> 페치 조인
    List<Member> findAll();

    @EntityGraph(attributePaths = {"team"})
    @Query("select m from Member m")
    List<Member> findMemberEntityGraph(); //위 메서드와 동일

    @EntityGraph(attributePaths = {"team"})
    List<Member> findEntityGraphByUsername(@Param("username") String username); //단건 조회 할 때도 기본적으로 team을 페치조인해서 가져옴

    //쿼리 힌트
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Member findReadOnlyByUsername(String username); //readOnly를 통해 스냅샷을 만들지않아 수정이 불가하게 만듦.

    //Lock
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Member> findLockByUsername(String username);
}
