package com.fms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

/**
 * 启动程序
 * 
 * @author ruoyi
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class FmsApplication
{
    public static void main(String[] args)
    {
        // System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(FmsApplication.class, args);
        System.out.println("  " +
                " __________________________________________________\n" +
                " /                                                  \\\n" +
                "|   _     ___   _   _   ___   ___   ___   _     _    |\n" +
                "|  | |   |_ _| | \\ | | |_ _| |_ _| |_ _| | |   | |  |\n" +
                "|  | |    | |  |  \\| |  | |   | |   | |  | |   | |  |\n" +
                "|  | |___ | |  | |\\  |  | |   | |   | |  | |___| |  |\n" +
                "|  |_____|___| |_| \\_| |___| |___| |___| |_____|    |\n" +
                "|                                                    |\n" +
                "|       IDENTITY MANAGEMENT INFORMATION SYSTEM       |\n" +
                "|                                                    |\n" +
                "|             (♥◠‿◠)ﾞ   启动成功                      |\n" +
                "\\__________________________________________________/");
    }
}
