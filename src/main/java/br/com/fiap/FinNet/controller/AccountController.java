package br.com.fiap.FinNet.controller;

import org.springframework.web.server.ResponseStatusException;
import br.com.fiap.FinNet.components.AccountStatus;
import br.com.fiap.FinNet.model.Account;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
public class AccountController {
   private final Logger log=LoggerFactory.getLogger(getClass());
    private final List<Account> accounts= new ArrayList<>();
    private Long nextId=1L;
    //Pegando todas as contas
    @GetMapping
    public List<Account> index(){
        return accounts;
    }

    //Pegando as contas por id
    @GetMapping("/{id}")
    public Account get(@PathVariable Long id){
        log.info("Buscando conta "+id);
        return getAccount(id);
    }

    //Pegando as contas por cpf
    @GetMapping("/cpf/{cpf}")
    public Account get(@PathVariable String cpf){
        log.info("Buscando conta "+cpf);
        return getAccountByCPF(cpf);
    }

    //Adicionando uma conta
    @PostMapping
    @ResponseStatus(code=HttpStatus.CREATED)
    public ResponseEntity<Account> create(@Valid @RequestBody Account account){
        log.info("Criando conta para "+account.getHoldName());
        account.setId(nextId++);
        account.setOpeningDate(java.time.LocalDate.now());
        account.setStatus(AccountStatus.ACTIVE);
        accounts.add(account);
        return ResponseEntity.status(201).body(account);
    }
    //Deletando Conta
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void destroy(@PathVariable Long id) {
        log.info("Deletando conta " + id);
        Account account = getAccount(id);
        accounts.remove(account);

        // Se o ID da conta deletada for maior que o próximo ID, reatribui o próximo ID.
        if (account.getId() >= nextId) {
            nextId = account.getId() + 1;
        }
    }

    //Endpoint que permite o usuario colocar a conta ativa em inativa
    @PutMapping("/close/{id}")
    public ResponseEntity<Account> closeAccount(@PathVariable Long id) {
        log.info("Encerrando conta " + id);
        Account account = getAccount(id);
        account.setStatus(AccountStatus.INACTIVE);
        return ResponseEntity.ok(account);
    }
    //Deposito do dinheiro
    @PutMapping("/deposit")
    public ResponseEntity<Account> deposit(@RequestParam Long id, @RequestParam double valueDeposit) {
        log.info("Depositando R$ " + valueDeposit + " na conta " + id);
        if(valueDeposit<=0){
            return ResponseEntity.badRequest().build();
        }
        Account account = getAccount(id);
        account.setInitialBalance(account.getInitialBalance() + valueDeposit);
        return ResponseEntity.ok(account);
    }
    //Sacando da conta
    @PutMapping("/withdraw")
    public ResponseEntity<Account> withdraw(@RequestParam Long id, @RequestParam double valueWithdraw) {
        log.info("Sacando R$ " + valueWithdraw + " da conta " + id);
        Account account = getAccount(id);
        if(valueWithdraw<=0|| account.getInitialBalance()<=valueWithdraw){
            return ResponseEntity.badRequest().build();
        }
        account.setInitialBalance(account.getInitialBalance() - valueWithdraw);
        return ResponseEntity.ok(account);
    }
    
    //Pix da conta
    @PutMapping("/pix")
    public ResponseEntity<Account> pix(@RequestParam Long fromId,@RequestParam Long toId,@RequestParam double valuePix) {
        log.info("Transferindo R$ " + valuePix + " da conta " + fromId+" para a conta "+toId);
        Account fromAccount = getAccount(fromId);
        Account toAccount = getAccount(toId);
        if(valuePix<=0|| fromAccount.getInitialBalance()<=valuePix){
            return ResponseEntity.badRequest().build();
        }
        fromAccount.setInitialBalance(fromAccount.getInitialBalance() - valuePix);
        toAccount.setInitialBalance(toAccount.getInitialBalance() + valuePix);
        return ResponseEntity.ok(toAccount);
    }

    private Account getAccount(Long id){
        return accounts.stream().filter(a -> a.getId().equals(id)).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
    private Account getAccountByCPF(String cpf){
        return accounts.stream().filter(a -> a.getCpf().equals(cpf)).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    
}
