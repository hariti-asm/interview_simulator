package ma.hariti.asmaa.wrm.simulator.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ma.hariti.asmaa.wrm.simulator.entity.enums.Role;
@Entity
@Table(name = "candidates")
@SuperBuilder
@NoArgsConstructor
public class Candidate extends User {
    @Override
    public void setRole(Role role) {
        super.setRole(Role.ADMIN);
    }
}
