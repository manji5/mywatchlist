package me.fatihenes.mywatchlist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MywatchlistApplication {

	public static void main(String[] args) {
		SpringApplication.run(MywatchlistApplication.class, args);
	}

}
