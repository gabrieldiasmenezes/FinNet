package br.com.fiap.FinNet.controller;

import br.com.fiap.FinNet.components.AccountStatus;
import br.com.fiap.FinNet.model.Account;
import br.com.fiap.FinNet.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private AccountRepository accountRepository; // Injeção de dependência do repositório

    // Endpoint para pegar todas as contas
    @GetMapping
    public List<Account> index() {
        return accountRepository.findAll();
    }

    // Endpoint para pegar uma conta por ID
    @GetMapping("/{id}")
    public Account get(@PathVariable Long id) {
        log.info("Buscando conta " + id);
        return getAccount(id);
    }

    // Endpoint para pegar uma conta por CPF
    @GetMapping("/cpf/{cpf}")
    public Account get(@PathVariable String cpf) {
        log.info("Buscando conta " + cpf);
        return getAccountByCPF(cpf);
    }

    // Endpoint para criar uma nova conta
    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public ResponseEntity<Account> create(@Valid @RequestBody Account account) {
        log.info("Criando conta para " + account.getHoldName());
        account.setOpeningDate(LocalDate.now());
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);  // Salva a conta no banco de dados
        return ResponseEntity.status(201).body(account);
    }

    // Endpoint para fechar uma conta (tornar inativa)
    @PutMapping("/close/{id}")
    public ResponseEntity<Account> closeAccount(@PathVariable Long id) {
        log.info("Encerrando conta " + id);
        Account account = getAccount(id);
        account.setStatus(AccountStatus.INACTIVE);
        accountRepository.save(account);  // Atualiza a conta no banco de dados
        return ResponseEntity.ok(account);
    }

    // Endpoint para depositar dinheiro na conta
    @PutMapping("/deposit")
    public ResponseEntity<Account> deposit(@RequestParam Long id, @RequestParam double valueDeposit) {
        log.info("Depositando R$ " + valueDeposit + " na conta " + id);
        if (valueDeposit <= 0) {
            return ResponseEntity.badRequest().build();
        }
        Account account = getAccount(id);
        account.setInitialBalance(account.getInitialBalance() + valueDeposit);
        accountRepository.save(account);  // Atualiza a conta no banco de dados
        return ResponseEntity.ok(account);
    }

    // Endpoint para sacar dinheiro da conta
    @PutMapping("/withdraw")
    public ResponseEntity<Account> withdraw(@RequestParam Long id, @RequestParam double valueWithdraw) {
        log.info("Sacando R$ " + valueWithdraw + " da conta " + id);
        Account account = getAccount(id);
        if (valueWithdraw <= 0 || account.getInitialBalance() <= valueWithdraw) {
            return ResponseEntity.badRequest().build();
        }
        account.setInitialBalance(account.getInitialBalance() - valueWithdraw);
        accountRepository.save(account);  // Atualiza a conta no banco de dados
        return ResponseEntity.ok(account);
    }

    // Endpoint para realizar um PIX
    @PutMapping("/pix")
    public ResponseEntity<Account> pix(@RequestParam Long fromId, @RequestParam Long toId, @RequestParam double valuePix) {
        log.info("Transferindo R$ " + valuePix + " da conta " + fromId + " para a conta " + toId);
        Account fromAccount = getAccount(fromId);
        Account toAccount = getAccount(toId);
        if (valuePix <= 0 || fromAccount.getInitialBalance() <= valuePix) {
            return ResponseEntity.badRequest().build();
        }
        fromAccount.setInitialBalance(fromAccount.getInitialBalance() - valuePix);
        toAccount.setInitialBalance(toAccount.getInitialBalance() + valuePix);
        accountRepository.save(fromAccount);  // Atualiza a conta de origem no banco de dados
        accountRepository.save(toAccount);  // Atualiza a conta de destino no banco de dados
        return ResponseEntity.ok(toAccount);
    }

    // Método para buscar uma conta por ID
    private Account getAccount(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    // Método para buscar uma conta por CPF
    private Account getAccountByCPF(String cpf){
        return accountRepository.findByCpf(cpf)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
