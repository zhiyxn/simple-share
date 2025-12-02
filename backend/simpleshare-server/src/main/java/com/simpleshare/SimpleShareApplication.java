package com.simpleshare;

import com.simpleshare.config.SecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

/**
 * SimpleShare 启动程序
 *
 * @author SimpleShare
 */
@SpringBootApplication
@EnableConfigurationProperties({SecurityProperties.class})
@ComponentScan(basePackages = {"com.simpleshare"})
public class SimpleShareApplication {
    public static void main(String[] args) {
        // System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(SimpleShareApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  SimpleShare启动成功   ლ(´ڡ`ლ)ﾞ  \n" +
                " .-------.       ____     __        \n" +
                " |  _ _   \\      \\   \\   /  /    \n" +
                " | ( ' )  |       \\  _. /  '       \n" +
                " |(_ o _) /        _( )_ .'         \n" +
                " | (_,_).' __  ___(_ o _)'          \n" +
                " |  |\\ \\  |  ||   |(_,_)'         \n" +
                " |  | \\ `\'   /|   `-'  /           \n" +
                " |  |  \\    / \\      /           \n" +
                " ''-'   `'-'   `-..-'              ");
    }
}