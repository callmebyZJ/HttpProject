package com.zj.httpserver.controller;

import com.zj.httpserver.utils.Utils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@Controller
public class HttpController {

    @ResponseBody
    @GetMapping("/{number}")
    public Double CalSquareRoot(@PathVariable Integer number) {
        double sqrt = Math.sqrt(number * 1.0);
        Utils.simulateTimeConsumingOperation();
        return sqrt;
    }

}
