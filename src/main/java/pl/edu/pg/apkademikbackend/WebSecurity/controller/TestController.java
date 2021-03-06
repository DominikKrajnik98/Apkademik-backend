package pl.edu.pg.apkademikbackend.WebSecurity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pg.apkademikbackend.user.repositry.UserRepository;

@RestController
@CrossOrigin(origins = "*")
public class TestController {

    @GetMapping("/greeting")
    public String welcomePage() {
        return "Welcome!";
    }

    @GetMapping("/server/test")
    public String returnOk(){
        return "Server is on!!!";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/server/test/admin")
    public String adminCheck(){
        return "Admin is logged!";
    }
}
