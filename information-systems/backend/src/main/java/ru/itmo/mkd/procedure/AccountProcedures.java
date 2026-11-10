package ru.itmo.mkd.procedure;

import org.springframework.stereotype.Repository;
import ru.itmo.mkd.domain.OwnershipType;

@Repository
public class AccountProcedures {

    private final ProcedureExecutor executor;

    public AccountProcedures(ProcedureExecutor executor) {
        this.executor = executor;
    }

    public Long registerResident(String login, String passwordHash, String fullName, String phone, String email,
                                 Long apartmentId, OwnershipType ownership) {
        return executor.queryForLong(
                "SELECT register_resident(?, ?, ?, ?, ?, ?, ?::ownership_type)",
                login, passwordHash, fullName, phone, email, apartmentId, ownership.name());
    }

    public void confirmResident(Long adminId, Long residentId, Long apartmentId) {
        executor.call("CALL confirm_resident(?, ?, ?)", adminId, residentId, apartmentId);
    }
}
