package com.Agenda.IA.Controllers;

import com.Agenda.IA.Models.User;
import com.Agenda.IA.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/{id}")
    public Optional<User> getUser(@PathVariable("id")Long id){
        return userRepository.findById(id);
    }

    @GetMapping
    public Optional<User> getUserByEmail(@RequestParam String email){
        return userRepository.findByEmail(email);
    }



    @PostMapping
    public User saveUser(@RequestBody User user){
        return userRepository.save(user);
    }
}
