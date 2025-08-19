package com.mathworks.bat.trupload.controller;

import java.util.Map;

import javax.naming.NamingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mathworks.bat.trupload.util.BrcReader;

@RestController
@RequestMapping("/brccache")
public class BrcController {
    @Autowired
    private BrcReader brcReader;

    @GetMapping("")
    public Map<String, Object> getCachedProperties() throws NamingException {
        return brcReader.getCachedProperties();
    }

    @DeleteMapping("")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void clearCache() {
        brcReader.flush();
        brcReader.getBrmLookup();
    }
}