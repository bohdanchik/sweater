package com.example.sweater.controller;

import com.example.sweater.domain.User;
import com.example.sweater.mail.Sender;
import com.example.sweater.service.UserService;
import com.example.sweater.domain.Message;
import com.example.sweater.repos.MessageRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
public class MainController {

    @Autowired
    private MessageRepo messageRepo;

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String greeting() {
        return "greeting";
    }

    private static Sender tlsSender = new Sender("ADMIN EMAIL", "ENTER PASSWORD OF ADMIN EMAIL");

    @GetMapping("/main")
    public String main(Principal principal, Map<String, Object> model) {

        User user = (User) userService.loadUserByUsername(principal.getName());
        List<Message> messages = messageRepo.findByUserId(user.getId());
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getSendedBy() != null)
                messages.get(i).setText(messages.get(i).getText() + " ( shared by " + messages.get(i).getSendedBy() + " ) ");
        }


        model.put("messages", messages);
        return "main";
    }

    @PostMapping("add")
    public String add(Principal principal, String text) {

        User user = (User) userService.loadUserByUsername(principal.getName());


        Message message = new Message();
        message.setText(text);
        message.setUserId(user.getId());
        messageRepo.save(message);

        return "redirect:/main";

    }

    @PostMapping("edit")
    public String edit(Principal principal, @RequestParam int id, @RequestParam String newText, Map<String, Object> model) {

        Message message;
        messageRepo.findById(id).setText(newText);
        message = messageRepo.findById(id);
        messageRepo.save(message);

        model.put("messages", message);
        return "redirect:/main";
    }

    @PostMapping("delete")
    public String delete(Principal principal, @RequestParam int id, Map<String, Object> model) {

        messageRepo.deleteById(id);

        return "redirect:/main";
    }

    @PostMapping("sendToUser")
    public String sendToUser(Principal principal, @RequestParam int id, @RequestParam String nameOfUser, Map<String, Object> model) {

        Message message = new Message(messageRepo.findById(id).getText());

        User user = (User) userService.loadUserByUsername(nameOfUser);

        message.setUserId(user.getId());
        message.setSendedBy(principal.getName());
        messageRepo.save(message);


        return "redirect:/main";
    }

    @PostMapping("sendByEmail")
    public String sendByEmail(Principal principal, @RequestParam String id, @RequestParam String text, @RequestParam String email, Map<String, Object> model) {
        tlsSender.send(id, text, "admin email", email);

        return "redirect:/main";
    }


}
