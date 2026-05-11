package br.gov.mg.tce.pagamentos;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PagamentosApplicationTests {

	@Test
	void contextLoads() {
		// Este teste verifica se o ApplicationContext do Spring
		// consegue carregar sem erros catastróficos.
	}

}
