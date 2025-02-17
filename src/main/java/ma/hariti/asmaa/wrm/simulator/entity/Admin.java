package ma.hariti.asmaa.wrm.simulator.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ma.hariti.asmaa.wrm.simulator.entity.enums.Role;

@Entity
@Table(name = "admins")
@SuperBuilder
@NoArgsConstructor
public class Admin extends User {
    @Override
    public void setRole(Role role) {
        super.setRole(Role.ADMIN);
    }
}

