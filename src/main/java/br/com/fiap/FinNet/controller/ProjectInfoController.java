package br.com.fiap.FinNet.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProjectInfoController {
     @GetMapping("/")
    public String ProjectInfo(){
        return "Project Name:Fin Net\nMembers:Gabriel Dias Menezes";
    }
    
}
