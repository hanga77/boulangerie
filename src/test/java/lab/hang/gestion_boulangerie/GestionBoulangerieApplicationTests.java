package lab.hang.gestion_boulangerie;

import lab.hang.Gestion.boulangerie.GestionBoulangerieApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = GestionBoulangerieApplication.class)
@ActiveProfiles("test")
class GestionBoulangerieApplicationTests {

	@Test
	void contextLoads() {
	}

}
