package com.searchpath.controllers;

import com.searchpath.SingleRestHighLevelClient;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import org.elasticsearch.client.RestHighLevelClient;

@Controller("/hello")
public class HelloController {

    @Get (produces = MediaType.TEXT_PLAIN)
    public String index(){
        return "Hello World";
    }

}
