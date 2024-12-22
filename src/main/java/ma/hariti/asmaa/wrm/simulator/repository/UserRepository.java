package ma.hariti.asmaa.wrm.simulator.repository;

import ma.hariti.asmaa.wrm.simulator.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
