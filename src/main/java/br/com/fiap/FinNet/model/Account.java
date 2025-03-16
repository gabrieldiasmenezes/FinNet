package br.com.fiap.FinNet.model;

import java.time.LocalDate;
import br.com.fiap.FinNet.components.AccountStatus;
import br.com.fiap.FinNet.components.AccountType;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class Account {
    private Long id;
    private String agency="1001";
    @NotBlank(message="Holder name is required")
    private String holdName;
    @NotBlank(message="cpf is required")
    private String cpf;
    @NotNull(message="Opening date is required")
    @PastOrPresent(message="Opening date cannot be in the future")
    private LocalDate openingDate=LocalDate.now();
    @NotNull(message="Initial balance is required")
    @PositiveOrZero(message="Initial balance cannot be negative")
    private Double initialBalance;
    @NotNull(message="status of account is required")
    private AccountStatus status=AccountStatus.ACTIVE;
    @NotNull(message="type of account is required")
    private AccountType type;
}
