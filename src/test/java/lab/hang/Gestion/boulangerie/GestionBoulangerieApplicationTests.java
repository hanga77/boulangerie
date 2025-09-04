package lab.hang.Gestion.boulangerie;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.jpa.hibernate.ddl-auto=create-drop"
})
class GestionBoulangerieApplicationTests {

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	void contextLoads() {
		// Verify that the Spring application context loads successfully
		assertThat(applicationContext).isNotNull();
	}

	@Test
	void mainApplicationContextContainsExpectedBeans() {
		// Verify that essential beans are loaded
		assertThat(applicationContext.containsBean("gestionBoulangerieApplication")).isTrue();
	}

	@Test
	void applicationStartsSuccessfully() {
		// This test ensures the application can start without errors
		// The @SpringBootTest annotation already handles this, but we make it explicit
		assertThat(((ConfigurableApplicationContext) applicationContext).isActive()).isTrue();
	}
}