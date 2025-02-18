package ma.hariti.asmaa.wrm.simulator.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ma.hariti.asmaa.wrm.simulator.entity.enums.Role;

@Entity
@DiscriminatorValue("CANDIDATE")
@SuperBuilder
@NoArgsConstructor
public class Candidate extends User {
    @Override
    public void setRole(Role role) {
        super.setRole(Role.CANDIDATE); // Fixed: This should be CANDIDATE, not ADMIN
    }
}