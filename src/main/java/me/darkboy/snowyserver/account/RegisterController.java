package me.darkboy.snowyserver.account;

import me.darkboy.snowyserver.utils.PasswordUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(path = "/accounts")
public class RegisterController {

    private final AccountRepository accountRepository;

    public RegisterController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @PostMapping(path = "/register")
    public @ResponseBody
    String registerUser(@RequestParam String name,
                        @RequestParam String email,
                        @RequestParam String password,
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

                        account.setSalt(generatedSalt);
                        account.setPassword(generatedPassword);
                        account.setToken(PasswordUtils.generateNewToken());
                        account.setIp(request.getRemoteAddr());
                        account.setDisabled(false);
                        accountRepository.save(account);

                        return "User created!";
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
                return "Successfully login!";
            } else {
                return "Wrong password";
            }

        } else {
            return "Account doesn't exist!";
        }
    }
}
