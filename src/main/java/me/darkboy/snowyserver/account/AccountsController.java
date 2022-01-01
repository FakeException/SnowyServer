package me.darkboy.snowyserver.account;

import me.darkboy.snowyserver.entity.impl.SnowyAccount;
import me.darkboy.snowyserver.utils.PasswordUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping(path = "/accounts")
public class AccountsController {

    private final AccountRepository accountRepository;

    public AccountsController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @PostMapping(path = "/register")
    public @ResponseBody
    String registerUser(@RequestParam String name,
                        @RequestParam String email,
                        @RequestParam String password,
                        @RequestParam String picture,
                        HttpServletRequest request) {

        if (accountRepository.findByEmail(email) == null && accountRepository.findByName(name) == null) {

            if (PasswordUtils.isValidEmailAddress(email)) {
                if (password.length() >= 8 && password.length() <= 32) {
                    if (name.length() <= 16) {
                        SnowyAccount account = new SnowyAccount();
                        account.setName(name);
                        account.setEmail(email);

                        String generatedSalt = PasswordUtils.getSalt(30);
                        String generatedPassword = PasswordUtils.generateSecurePassword(password, generatedSalt);

                        String token = PasswordUtils.generateNewToken();

                        account.setSalt(generatedSalt);
                        account.setPassword(generatedPassword);
                        account.setToken(token);
                        account.setIp(request.getRemoteAddr());
                        account.setDisabled(false);

                        account.setProfilePictureUrl(Objects.requireNonNullElse(picture, "none"));

                        accountRepository.save(account);

                        return token;
                    } else {
                        return "Username problem";
                    }
                } else {
                    return "Password problem";
                }

            } else {
                return "Invalid email!";
            }
        } else {
            return "Account already exist!";
        }
    }

    @PostMapping(path = "/login")
    public @ResponseBody
    String login(@RequestParam String email,
                 @RequestParam String password) {

        if (accountRepository.findByEmail(email) != null) {

            SnowyAccount found = accountRepository.findByEmail(email);

            String decryptedPassword = PasswordUtils.generateSecurePassword(password, found.getSalt());

            if (decryptedPassword.equals(found.getPassword())) {
                return found.getToken();
            } else {
                return "Wrong password";
            }

        } else {
            return "Account doesn't exist!";
        }
    }

    @PostMapping(path = "/checkToken")
    public @ResponseBody
    String login(@RequestParam String token) {

        if (accountRepository.findByToken(token) != null) {

            SnowyAccount found = accountRepository.findByToken(token);

            return found.getToken();

        } else {
            return "Account doesn't exist!";
        }
    }

    @PostMapping(path = "/validToken")
    public @ResponseBody
    boolean token(@RequestParam String token) {
        return accountRepository.findByToken(token) != null;
    }

    @PostMapping(path = "/searchFor")
    public @ResponseBody
    List<String> searchFor(@RequestParam String name) {

        List<String> names = new ArrayList<>();
        for (SnowyAccount account : accountRepository.findSnowyAccountsByNameContaining(name)) {
            names.add(account.getName());
        }

        return names;
    }

    @GetMapping(path = "/fetchUsers")
    public @ResponseBody
    List<String> fetchUsers() {
        List<String> accounts = new ArrayList<>();

        for (SnowyAccount snowyAccount : accountRepository.findAll()) {
            accounts.add(snowyAccount.getName());
        }

        return accounts;
    }
}
