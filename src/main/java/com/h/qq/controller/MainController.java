package com.h.qq.controller;

import com.h.qq.services.MainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author hhm
 * @date 2024/8/28
 * @description TODO
 */
@RestController
@RequestMapping("/main")
public class MainController {


    @Autowired
    private MainService mainService;


    @RequestMapping("/run")
    public String run(){

        //mainService.countSS();
        mainService.visitZone();

        return "ok";
    }
}
