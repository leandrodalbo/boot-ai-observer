package io.github.leandrodalbo.example;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class DemoController {

    private final List<byte[]> leakedMemory = new ArrayList<>();

    @GetMapping("/hello")
    public String hello() {
        return "Hello from AI Observer example!";
    }

    @GetMapping("/leak")
    public String leak() {
        leakedMemory.add(new byte[1024 * 1024]);
        return "Allocated 1MB. Total leaked: " + leakedMemory.size() + "MB";
    }

    @GetMapping("/slow")
    public String slow() throws InterruptedException {
        Thread.sleep(2000);
        return "Slow response after 2s";
    }

    @GetMapping("/error")
    public String error() {
        throw new RuntimeException("Simulated error for AI Observer demo");
    }
}
